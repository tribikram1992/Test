from enum import Enum


NODE_EVENT_MAP = {
    "plan": ("planning", "Got it! Let me figure out the best way to answer this..."),
    "rewrite_queries": ("planning", "Breaking your question down into focused searches..."),
    "execute_agents": None,  # handled dynamically
    "synthesize": ("synthesizing", "Almost there! Putting it all together for you..."),
    "graceful_response": ("synthesizing", "Preparing response..."),
    # db agent nodes
    "retrieve_schema": ("retrieving", "Looking up relevant database schemas..."),
    "filter_chunks": ("retrieving", "Identifying the most relevant tables and columns..."),
    "generate_sql": ("generating", "Writing database queries..."),
    "validate_sql": ("generating", "Double-checking the queries for safety..."),
    "execute_sql": ("executing", "Fetching data from the database..."),
    "db_synthesize": ("synthesizing", "Making sense of the database results..."),
    # file agent nodes
    "retrieve_documents": ("retrieving", "Searching through documents..."),
    "file_synthesize": ("synthesizing", "Reading through the relevant document sections..."),
}

class AgentsEnum(str, Enum):
    DB_AGENT = "db"
    FILE_AGENT = "file"
    # Add more agents here as needed in the future (e.g. EXTERNAL_API_AGENT = "external_api" etc.)
