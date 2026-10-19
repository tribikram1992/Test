"""
LangGraph node functions for the Planner Agent graph.

    plan_node               → LLM Call 1, decides which agents to invoke
    rewrite_query_node      → LLM Call 2, decomposes mixed-intent or single intent query into focused
                              sub-queries per qualified agent — one query per distinct information need.
                              Fires for all paths (single and multi-agent).
                              Hard fails if LLM call fails; execute_agents_node falls back
                              to original query per qualified agent in that case.
    execute_agents_node     → fans out all sub-queries concurrently across all agent
                              subgraphs; collects results as lists
    synthesize_node         → LLM Call 3, synthesizes all evidence into final response
    graceful_response_node  → returns fixed message when no agent can answer
"""

import asyncio
import json
import time
from agents.planner import state
from core.process_chat_history import add_message, get_history
from guardrails.guard_service import BaxiGuard, GuardResponse
from typing import List
from agents.file.state import FileAgentState
from agents.planner.constants import AgentsEnum
from agents.planner.state import PlannerAgentState
from agents.planner.schemas import (
    PlannerDecision,
    FinalResponse,
    FinalResponseWithCost,
    EvidenceItem,
    QueryRewritePlan,
    QUERY_REWRITE_WITH_HISTORY,
)
from agents.planner.prompts import PLAN_PROMPT, REWRITE_PROMPT, REWRITE_PROMPT_WITH_HISTORY, SYNTHESIZE_PROMPT
from agents.db.graph import db_agent_graph
from agents.file.graph import file_agent_graph
from agents.db.state import DBAgentState
from llm.Extractor import Extractor
from llm.models import get_model_by_alias
import logging
from observability.trace import trace
from core.config import settings
from core.token_count import count_tokens_claude, count_tokens_pydantic, calculate_cost

log = logging.getLogger(__name__)


LLM_ALIAS = "claude-sonnet-4.6"  # Uses Claude Sonnet 4.6, which has strong structured output capabilities and is good at reasoning tasks, making it well-suited for both the planning and synthesis nodes in the Planner Agent graph.
llm = Extractor(llm_model=get_model_by_alias(model_alias=LLM_ALIAS))

GRACEFUL_RESPONSE = (
    "I appreciate your question! Unfortunately, I wasn't able to find relevant information "
    "in Baxter's databases or documents to answer this one. 🙂\n\n"
    "I'm best equipped to help with questions about Baxter's business data — such as "
    "attendance records, supply chain, inventory, contracts, vendor proposals or policy documents.\n\n"
    "Could you try rephrasing your question or ask something related to these areas? I'm happy to help!"
)

QUERY_PROCESSING_ERROR_RESPONSE = (
    "I'm sorry, something went wrong while processing your question. 🙁\n\n"
    "This could be a temporary issue — please try again in a moment. "
    "If the problem persists, try rephrasing your question or contact the Baxter GITC support team."
)

UNKNOWN_ERROR_RESPONSE = (
    "Oops! An unexpected error occurred on my end. 🙁\n\n"
    "Please try again in a moment. If this keeps happening, "
    "reach out to the Baxter GITC support team for assistance."
)

NO_EVIDENCE_FOUND = "No evidence was found from either agent to synthesize."

EMPTY_LLM_RESPONSE = "LLM returned empty response"

NO_AGENTS_RESPONDED = "No agents were able to provide a response."

NO_FINAL_STATE_FOUND = "Graph completed without producing a final state."

MAX_DISPLAY_ROWS = 20  # soft cap : max number of rows to display in the final response when DB evidence is included. This is to prevent overwhelming the user with too much data and to keep the evidence concise and relevant. The full results are still included in the evidence for transparency, but only a sample is shown in the answer with a note about truncation.


@trace
async def check_guardrails_policy(state: PlannerAgentState) -> PlannerAgentState:
    """
    Checks the query against the BaxiGuard service for content policy compliance.
    Returns a GuardResponse indicating whether the query is blocked and any relevant messages.
    """
    guard = BaxiGuard()

    guard_response: GuardResponse = await guard.check_prompt(
        query=state.query, app_name=settings.CHATBOT_NAME, requestor="test"
    )
    return state.model_copy(
        update={
            "is_blocked": guard_response.is_blocked,
            "guard_message": guard_response.message,
            "error": None
            if not guard_response.is_blocked
            else "The query doesn't comply with the content policy of Baxter \n Reason :"
            + guard_response.message,
        }
    )

async def rewrite_query_with_history(state: PlannerAgentState) -> PlannerAgentState:
    """
Use the chat history to rewrite the query for better context and clarity
    """
  
    chat_history_msgs = get_history()

    # If chat history is empty, just return the original state
    if not chat_history_msgs:
        return state
    

    try:

        chat_history = "\n".join([f"{msg['role']}: {msg['message']}" for msg in chat_history_msgs])

        # rewrite the query using the chat history for context
        prompt = REWRITE_PROMPT_WITH_HISTORY.format(
            bot_name=settings.CHATBOT_NAME, query=state.query, chat_history=chat_history)
        
        print(f"rewrite_query_with_history prompt: {prompt}")
        response: QUERY_REWRITE_WITH_HISTORY = await llm.extract_structured_response_from_query(
            messages=[{"role": "user", "content": prompt}],
            response_format=QUERY_REWRITE_WITH_HISTORY,
        )

        print(f"rewrite_query_with_history response: {response.rewritten_query}")
        input_tokens = count_tokens_claude(prompt)
        output_tokens = count_tokens_pydantic(response)

        # Update token breakdown
        token_breakdown = state.token_breakdown.copy()
        token_breakdown["query_rewrite_with_history"] = {
            "input_tokens": input_tokens,
            "output_tokens": output_tokens,
        }
        log.info(f"rewrite_query_with_history: rewritten_query={response.rewritten_query}")
        return state.model_copy(update={"query": response.rewritten_query})
    except Exception as e:
        log.error(f"rewrite_query_with_history failed: {e}")
        return state.model_copy(update={"error": f"rewrite_query_with_history failed: {e}"})



@trace
async def plan_node(state: PlannerAgentState) -> PlannerAgentState:
    """LLM Call 1 — decides which agents to invoke based on the user query."""
    try:
        prompt = PLAN_PROMPT.format(bot_name=settings.CHATBOT_NAME, query=state.query)
        decision: PlannerDecision = await llm.extract_structured_response_from_query(
            messages=[{"role": "user", "content": prompt}],
            response_format=PlannerDecision,
        )
        log.info(
            f"plan_node decision: db_agent={decision.invoke_db_agent} file_agent={decision.invoke_file_agent} | reason: {decision.reasoning}"
        )
        input_tokens = count_tokens_claude(prompt)
        output_tokens = count_tokens_pydantic(decision)

        # Update token breakdown
        token_breakdown = state.token_breakdown.copy()
        token_breakdown["plan"] = {
            "input_tokens": input_tokens,
            "output_tokens": output_tokens,
        }

        return state.model_copy(
            update={"plan": decision, "token_breakdown": token_breakdown}
        )
    except Exception as e:
        log.error(f"plan_node failed: {e}")
        return state.model_copy(update={"error": f"plan_node failed: {e}"})


@trace
async def rewrite_query_node(state: PlannerAgentState) -> PlannerAgentState:
    """
    LLM Call 2 — decomposes query into focused sub-queries per qualified agent.
    Fires for both single-agent and multi-agent paths.
    Hard fails if LLM call fails.
    """
    try:
        # build agents description string from plan decision
        agent_lines = []
        if state.plan.invoke_db_agent:
            agent_lines.append(
                f'- "{AgentsEnum.DB_AGENT.value}"   → Database Agent: handles structured data queries '
                "(SQL databases, records, numbers, aggregations, HR, supply chain, attendance, operations data)"
            )
        if state.plan.invoke_file_agent:
            agent_lines.append(
                f'- "{AgentsEnum.FILE_AGENT.value}" → File Agent: handles document search '
                "(contracts, MSAs, proposals, policies, procedures, unstructured text)"
            )
        # Form more agent descriptions here as needed in the future (e.g. external API agent etc.)
        agents_str = "\n".join(agent_lines)

        prompt = REWRITE_PROMPT.format(
            bot_name=settings.CHATBOT_NAME, query=state.query, agents=agents_str
        )
        rewrite_plan: QueryRewritePlan = (
            await llm.extract_structured_response_from_query(
                messages=[{"role": "user", "content": prompt}],
                response_format=QueryRewritePlan,
            )
        )

        log.info(
            f"rewrite_query_node: db_queries={rewrite_plan.db_queries} | "
            f"file_queries={rewrite_plan.file_queries}"
        )
        input_tokens = count_tokens_claude(prompt)
        output_tokens = count_tokens_pydantic(rewrite_plan)

        # Update token breakdown
        token_breakdown = state.token_breakdown.copy()
        token_breakdown["rewrite"] = {
            "input_tokens": input_tokens,
            "output_tokens": output_tokens,
        }
        return state.model_copy(
            update={"rewrite_plan": rewrite_plan, "token_breakdown": token_breakdown}
        )
    except Exception as e:
        log.error(f"rewrite_query_node failed: {e}")
        return state.model_copy(update={"error": f"rewrite_query_node failed: {e}"})


@trace
async def execute_agents_node(state: PlannerAgentState) -> PlannerAgentState:
    """
    Fans out agent execution parallely across all rewritten sub-queries from rewrite_plan,
    calling the correct agent subgraph per sub-query, all concurrently.
    Falls back to original query per qualified agent if rewrite_plan is None
    (e.g. rewrite_query_node failed or returned empty plan).
    """
    try:
        tasks = []
        task_meta = []  # "db" or "file" per task

        if state.rewrite_plan and (
            state.rewrite_plan.db_queries or state.rewrite_plan.file_queries
        ):
            # ── REWRITE PATH — use focused sub-queries ─────────────────────────
            for q in state.rewrite_plan.db_queries:
                tasks.append(db_agent_graph.ainvoke(DBAgentState(query=q)))
                task_meta.append(AgentsEnum.DB_AGENT.value)
                time.sleep(0.5)

            for q in state.rewrite_plan.file_queries:
                tasks.append(file_agent_graph.ainvoke(FileAgentState(query=q)))
                task_meta.append(AgentsEnum.FILE_AGENT.value)
                time.sleep(0.5)

        else:
            # ── FALLBACK PATH ──────────────────────────────────────────────────
            log.warning(
                "execute_agents_node: rewrite_plan is None or empty — "
                "falling back to original query per qualified agent."
            )
            state = state.model_copy(update={"error": None})

            if state.plan and state.plan.invoke_db_agent:
                tasks.append(db_agent_graph.ainvoke(DBAgentState(query=state.query)))
                task_meta.append(AgentsEnum.DB_AGENT.value)
                time.sleep(0.5)

            if state.plan and state.plan.invoke_file_agent:
                tasks.append(
                    file_agent_graph.ainvoke(FileAgentState(query=state.query))
                )
                task_meta.append(AgentsEnum.FILE_AGENT.value)

        if not tasks:
            return state

        results = await asyncio.gather(*tasks, return_exceptions=True)

        db_results = []
        file_results = []
        token_breakdown = state.token_breakdown.copy()

        for agent_type, result in zip(task_meta, results):
            if isinstance(result, Exception):
                log.error(f"{agent_type} agent sub-query failed: {result}")
                continue
            if agent_type == AgentsEnum.DB_AGENT.value:
                db_results.append(result)
            elif agent_type == AgentsEnum.FILE_AGENT.value:
                file_results.append(result)

        # Initialize agent keys
        if "db_agent" not in token_breakdown:
            token_breakdown["db_agent"] = {"input_tokens": 0, "output_tokens": 0}
        if "file_agent" not in token_breakdown:
            token_breakdown["file_agent"] = {"input_tokens": 0, "output_tokens": 0}

        # ✅ Handle both dict and state object
        for db_result in db_results:
            if isinstance(db_result, dict):
                input_tokens = db_result.get("input_tokens", 0) or 0
                output_tokens = db_result.get("output_tokens", 0) or 0
            else:
                input_tokens = db_result.input_tokens or 0
                output_tokens = db_result.output_tokens or 0

            token_breakdown["db_agent"]["input_tokens"] += input_tokens
            token_breakdown["db_agent"]["output_tokens"] += output_tokens

        for file_result in file_results:
            if isinstance(file_result, dict):
                input_tokens = file_result.get("input_tokens", 0) or 0
                output_tokens = file_result.get("output_tokens", 0) or 0
            else:
                input_tokens = file_result.input_tokens or 0
                output_tokens = file_result.output_tokens or 0

            token_breakdown["file_agent"]["input_tokens"] += input_tokens
            token_breakdown["file_agent"]["output_tokens"] += output_tokens

        return state.model_copy(
            update={
                "db_agent_results": db_results,
                "file_agent_results": file_results,
                "token_breakdown": token_breakdown,
            }
        )

    except Exception as e:
        log.error(f"execute_agents_node failed: {e}")
        return state.model_copy(update={"error": f"execute_agents_node failed: {e}"})


@trace
async def synthesize_node(state: PlannerAgentState) -> PlannerAgentState:
    """
    LLM Call 3 — lightweight synthesis pass that combines pre-summarized
    agent responses into a final coherent answer.
    Each agent has already synthesized its own evidence internally —
    this node just assembles the summaries and collects raw evidence
    for auditability.
    """
    try:
        evidence: List[EvidenceItem] = []
        agent_summaries = []

        # collect db evidence and summaries
        for db_result in state.db_agent_results:
            if db_result and db_result.agent_summary:
                agent_summaries.append(
                    {
                        "agent": "Database Agent",
                        "summary": db_result.agent_summary.summary,
                    }
                )
            # Evidence collection for DB agent results is only done if there are query results, since the agent summary already indicates if no data was found. This avoids cluttering the evidence with empty results.
            if db_result and db_result.query_results:
                for qr in db_result.query_results:
                    evidence.append(
                        EvidenceItem(
                            source_type="database",
                            source_name=qr.database_name,
                            platform=qr.platform,
                            sql_query=qr.sql_query,
                            results=json.dumps(qr.results, indent=2)
                            if qr.results
                            else None,
                        ).model_dump(exclude_none=True)
                    )

        # collect file evidence and summaries
        for file_result in state.file_agent_results:
            if file_result and file_result.agent_summary:
                agent_summaries.append(
                    {
                        "agent": "Document Agent",
                        "summary": file_result.agent_summary.summary,
                    }
                )
            # Evidence collection for File agent results is only done if there are query results, since the agent summary already indicates if no data was found. This avoids cluttering the evidence with empty results.
            if file_result and file_result.results:
                for point in file_result.results:
                    evidence.append(
                        EvidenceItem(
                            source_type="document",
                            source_name=point.get("source_name", "unknown"),
                            page_number=point.get("page_number"),
                            chunk_id=point.get("chunk_id"),
                            text=point.get("text"),
                            download_url=point.get("download_url"),
                        ).model_dump(exclude_none=True)
                    )
        # Add more agent summaries for other agent types here as needed in the future (e.g. external API agent etc.)

        if not agent_summaries:
            log.info("synthesize_node: no agent summaries to synthesize")
            error = state.error if state.error else NO_EVIDENCE_FOUND
            cost = calculate_cost(state.token_breakdown)

            # Add messages to chat history
            add_message(role='user', msg=state.query)
            add_message(role='bot',msg=GRACEFUL_RESPONSE)
            

            return state.model_copy(
                update={
                    "final_response": FinalResponseWithCost(
                        answer=GRACEFUL_RESPONSE,
                        confidence=0.0,
                        sources=[],
                        evidence=[],
                        errors=[error],
                        Cost=cost,
                    )
                }
            )

        summaries_text = json.dumps(agent_summaries, indent=2)
        prompt = SYNTHESIZE_PROMPT.format(
            query=state.query,
            agent_summaries=summaries_text,
            max_display_rows=MAX_DISPLAY_ROWS,
            bot_name=settings.CHATBOT_NAME,
        )

        response: FinalResponse = await llm.extract_structured_response_from_query(
            messages=[{"role": "user", "content": prompt}],
            response_format=FinalResponse,
        )

        if not response or not response.answer:
            log.info(
                "synthesize_node: LLM returned empty response, using graceful response"
            )
            error = state.error if state.error else EMPTY_LLM_RESPONSE
            cost = calculate_cost(state.token_breakdown)

            # Add messages to chat history
            add_message(role='user', msg=state.query)
            add_message(role='bot',msg=GRACEFUL_RESPONSE)


            return state.model_copy(
                update={
                    "final_response": FinalResponseWithCost(
                        answer=GRACEFUL_RESPONSE,
                        confidence=0.0,
                        sources=[],
                        evidence=[],
                        errors=[error],
                        Cost=cost,
                    )
                }
            )
        
        # Token calculation for synthesize_node
        input_tokens = count_tokens_claude(prompt)
        output_tokens = count_tokens_pydantic(response)
        token_breakdown = state.token_breakdown.copy()
        token_breakdown["synthesize"] = {
            "input_tokens": input_tokens if isinstance(input_tokens, int) else 0,
            "output_tokens": output_tokens if isinstance(output_tokens, int) else 0,
        }

        # attach raw evidence to final response for auditability
        response.evidence = [EvidenceItem(**e) for e in evidence]

        if state.error:
            if response.errors:
                response.errors.append(state.error)
            else:
                response.errors = [state.error]

        cost = calculate_cost(token_breakdown)

        # Add messages to chat history
        add_message(role='user', msg=state.query)
        add_message(role='bot',msg=response.answer)

        # now add cost to the final response with cost model
        final_response_with_cost = FinalResponseWithCost(
            answer=response.answer,
            confidence=response.confidence,
            sources=response.sources,
            evidence=response.evidence,
            errors=response.errors,
            Cost=cost,
        )

        return state.model_copy(
            update={"final_response": final_response_with_cost, "token_breakdown": token_breakdown}
        )

    except Exception as e:
        log.error(f"synthesize_node failed: {e}")
        return state.model_copy(update={"error": f"synthesize_node failed: {e}"})


async def graceful_response_node(state: PlannerAgentState) -> PlannerAgentState:
    """Returns a fixed graceful response when no agent can answer the query."""
    log.info(f"graceful_response_node triggered for query: '{state.query}'")
    errors = state.error if state.error else NO_AGENTS_RESPONDED
    cost = calculate_cost(state.token_breakdown)
    
    
    # Add messages to chat history
    add_message(role='user', msg=state.query)
    add_message(role='bot', msg=GRACEFUL_RESPONSE)
    return state.model_copy(
        update={
            "final_response": FinalResponseWithCost(
                answer=GRACEFUL_RESPONSE,
                confidence=0.0,
                sources=[],
                evidence=[],
                errors=[errors],
                Cost=cost,
            )
        }
    )


def should_invoke_agents(state: PlannerAgentState) -> str:
    """
    Conditional edge after plan_node.
    - neither agent qualified → graceful
    - any agent(s) qualified  → rewrite (always decompose, even for single agent)
    """
    if state.error:
        return "graceful"
    if not state.plan:
        return "graceful"
    if not state.plan.invoke_db_agent and not state.plan.invoke_file_agent:
        return "graceful"
    return "rewrite"  # always rewrite — single or multi agent


def should_start_planning(state: PlannerAgentState) -> bool:
    """
    Conditional edge after check_guardrails_policy.
    - blocked by guardrails → do not start planning
    - not blocked → start planning
    """
    if state.is_blocked:
        log.info(f"Query blocked by guardrails: {state.guard_message}")
        return False
    return True
