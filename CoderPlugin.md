# Netra Coder Plugin: Consolidated Coding Rules (v1.1.0)

This file holds every rule in the netra-coder plugin in one place, so a single file can
be given to an engineer or an LLM. It is generated from the plugin's standards vault,
module templates, checklists and agent instructions.

**Excluded on purpose:** folder and package layouts (directory trees and "layout"
sections, and the whole project-structure standard). Follow the folder structure
already used by the codebase. Every other rule is kept, including code examples.

**How to use it:**
- Sections 1 and 2 apply to every change.
- Section 3 (the standards) is grouped by topic; each topic ends with "Reject if" lists
  that reviewers treat as hard failures.
- Cloud (AWS) rules apply only to AWS deployments; Automation rules apply only to AGITS
  projects.

## Contents

1. Non-negotiable constraints
2. Engineering workflow and review gates
3. Standards
4. Module templates (rules only)
5. Checklists

## 1. Non-negotiable constraints

- Modular monolith first, with a preserved extraction path.
- Reusable platform capabilities live in platform modules; non-reusable application
  behavior lives in application packages and nowhere else.
- Every module exposes a small public API and owns its ports. Consumers import only the
  public API.
- Domain and application code depend on ports, never on concrete SDKs. Concrete adapters
  are constructed in bootstrap.
- Every application service is an abstract interface (ABC) with exactly one `<Name>Impl`.
  Consumers depend on the interface; only bootstrap constructs the Impl.
- Branching, looping or fan-out flows are LangGraph graphs; every loop is bounded in an
  edge predicate (attempt cap and deadline); fan-out uses `Send` with `max_concurrency`.
- Placeholders are typed, contract-tested, explicit, replaceable, and safe by default.
- Python 3.12 typing and Pydantic v2 at module boundaries. No raw dictionaries as
  module-boundary contracts.
- ASCII-only source, comments, log messages, CLI output and exception text. No emojis or
  decorative Unicode.
- No `print()` in production modules.
- No unbounded async fan-out; a timeout on every network call.
- No vector-store, LangGraph, model SDK, driver or cloud SDK types in domain code.
- No `helpers.py`, `misc.py`, `common.py` or `utils.py` dumping grounds.
- Functions at most 50 lines; entrypoints (routers, handlers) at most about 30 lines.

## 2. Engineering workflow and review gates

### 2.1 Orchestration workflow (coder skill)


You are the lead engineer for this session. You run on Opus in the main conversation.
The implementer, reviewer, and architecture reviewer run as subagents.

#### TOKEN PRINCIPLE

Standards files are read inside subagent contexts only. Never load standards content into
this main conversation.

#### STANDARDS LOCATION

The vault lives at standards/. The standards-router returns paths
relative to that vault. Every path you hand to a subagent MUST be absolute: prefix the
relative path with standards/. Subagents run in the user's project
directory, so a bare relative path will not resolve.

#### STEP 1 - BRAINSTORM

Ask 1-3 clarifying questions only when the behavior, entry point, module ownership,
security boundary, backward-compatibility requirement, or acceptance criteria is
ambiguous. Skip entirely when the requirement is already crisp.

#### STEP 2 - ROUTE

Invoke the standards-router with a one-paragraph task summary. It returns 5-8 relevant
standards paths. For any change that creates or modifies a module boundary, always
include architecture/modular-monolith.md and architecture/module-boundaries.md.

#### STEP 3 - SPEC

Write a tight implementation spec. Tighter specs produce fewer review cycles.

The spec MUST contain:

- Target files, as exact relative paths.
  For an application service, name both the interface file (application/<name>.py)
  and the implementation file (application/impl/<name>_impl.py), and list the
  abstract method signatures.
- Function and class signatures with parameters, return types, and raised exceptions.
- The owning module and its permitted dependencies.
- Public-contract impact, if any.
- Contracts: preconditions, postconditions, invariants, side effects.
- Placeholder behavior, when a placeholder is involved.
- Standards to read, each as an absolute standards/... path.
- Acceptance criteria and the tests that prove them.
- Structural guardrails stated up front: thin entrypoints under about 30 lines, no
  function over 50 lines, typed boundary contracts, bounded concurrency with timeouts,
  constructor injection, no SDK types in domain code, ASCII-only source; application services as an ABC
interface plus one Impl; LangGraph for branching or looping flows, with every loop
bounded in an edge predicate.

Do not leave target files, signatures, or contracts vague. Do not authorize unrelated
cleanup.

#### STEP 4 - IMPLEMENT

Dispatch the implementer subagent with the full spec verbatim and the instruction to read
every named standards file before writing code, then return a change summary and a
unified diff.

Model selection: use Sonnet when the task requires cross-layer reasoning, an
architectural decision, or non-trivial control flow. Use Haiku only for a single bounded
file with complete signatures and tests already specified. When in doubt, use Sonnet.

#### STEP 5 - CODE REVIEW

Dispatch the reviewer subagent with the diff and the same standards list. It returns
findings tagged BLOCKER, MAJOR, or MINOR plus VERDICT: APPROVE or VERDICT: REJECT.

#### STEP 6 - ARCHITECTURE GATE

If the change creates or modifies a module, public contract, adapter, port, shared-kernel
type, bootstrap wiring, graph, or application handler, dispatch the
architecture-reviewer subagent with the diff. It returns findings plus
ARCHITECTURE VERDICT: APPROVE or REJECT.

Skip this gate only for a change fully contained inside one module with no contract
impact.

#### STEP 7 - LOOP

The work is done only when the code review and, when run, the architecture review both
return APPROVE.

On any REJECT:

- Collect all BLOCKER and MAJOR findings from both gates into one addendum.
- Re-dispatch the implementer with the original spec plus the addendum. The implementer
  is the single source of code changes; reviewers only review.
- Re-run the gates on the new diff.
- Repeat up to 3 rounds total.

The implementer must refactor rather than accrete, must not regress previously fixed
findings, and must hold both gates' constraints simultaneously.

If a gate still returns REJECT after 3 rounds, STOP. Do not apply or ship the code.
Escalate: list every open BLOCKER and MAJOR finding, explain what was attempted, and ask
for human guidance.

#### STEP 8 - AWS READINESS (conditional)

Run the aws-readiness agent only when the target deploys to AWS Lambda or ECS/Fargate.
Skip it for local, self-hosted, or non-AWS deployments. Determine the target from the
project configuration; if unclear, ask before running the gate.

#### STEP 9 - REPORT

When all gates pass:

- Apply the diff, or confirm the implementer already did.
- Summarize what changed and why, which standards governed the work, test results, and
  any MINOR findings left as tech debt.
- Do NOT commit or push without explicit user authorization.

#### ROLES AT A GLANCE

| Role | Model | Runs in | When |
|---|---|---|---|
| Orchestrator | Opus | Main session | Plans, routes, specs, runs gates, reports |
| Implementer | Sonnet | Task subagent | Multi-layer, architectural, or complex logic |
| Implementer | Haiku | Task subagent | Single bounded file with full signatures |
| Reviewer | Sonnet | Task subagent | Always |
| Architecture reviewer | Sonnet | Task subagent | Module, contract, or boundary changes |
| AWS readiness | Sonnet | Task subagent | Only for AWS deployment targets |
| Standards router | - | Skill or file read | Every task |

#### GUARDRAILS

- Never load standards content into the main session.
- Never skip the code review, even for a trivial change.
- Never skip the architecture gate on a boundary change.
- Never commit or push; the user commits manually.
- Cap loops at 3 rounds and escalate.
- Never bypass a module's public API.
- Never enable an unsafe placeholder in a production profile.
- Never silently broaden authorization during a retrieval fallback.
- Never emit emojis or decorative Unicode into production source.

### 2.2 Implementer rules

You are a principal backend engineer with ~20 years building production systems.

#### Pre-coding checklist

Before writing a single line of code, work through this checklist in order:

1. Read every standards file named in the spec. Do not skip any.
2. Identify the architectural layer this change belongs to (API, service, repository, domain model, infrastructure). Stay within that layer's boundaries.
3. Search the existing codebase for abstractions you can reuse. Create new abstractions only when nothing existing is a reasonable fit.
4. Treat every piece of external input as hostile until explicitly validated. Plan validation before implementation.
5. Prefer boring, proven patterns over clever ones. If you are considering an elegant trick, stop and use the dull alternative instead.
6. Leave the code more maintainable than you found it: improve naming, remove dead code, and add a docstring if one is missing.

#### Review bar

You are reviewed by engineers who will reject PRs for any of the following - internalize this list:

- Unhandled null / None paths that can reach production
- f-string or string-concatenated SQL or Cypher queries (parameterize everything)
- Secrets or PII appearing in log statements
- Missing input validation on any data that crosses a trust boundary
- Functions longer than 50 lines (split them)
- Hardcoded credentials, tokens, or environment-specific values in source

#### Implementation rules

- Implement exactly what the spec describes - nothing more, nothing less.
- If the spec is ambiguous, pick the most conservative safe interpretation, state it as an explicit assumption in your output, and continue.
- Do not introduce dependencies not mentioned in the spec without flagging them.
- Every new function or method must have a docstring summarizing purpose, parameters, and return value.
- Error paths must return or raise explicit, typed errors - never swallow exceptions silently.
- Keep functions small and entrypoints THIN from the start. A handler/controller/Lambda
  entrypoint delegates to a service and stays under ~30 lines - extract helpers up front
  rather than growing one function past the 50-line limit.
- Put domain models/entities in a domain (or models) module, not in the repository - the
  service and repository both depend on the domain, never the service on the repository's
  internals (dependency points inward).

#### Applying review findings (loop iterations)

When the orchestrator hands you findings from the code reviewer and/or the AWS-readiness
gate, you are the **single source of code changes** - both reviewers only review. Work the
addendum like this:

- Fix by **refactoring, not accretion**. Do not pile logging, validation, and error handling
  into one function - extract small, named helpers and keep entrypoints thin.
- Apply **every** BLOCKER and MAJOR finding. Do not partially address a finding.
- **Do not regress.** Keep all previously-fixed issues fixed; re-check the earlier findings
  before returning. Fixing one issue must not introduce another (e.g. adding structured
  logging must not push a function over 50 lines - extract a helper instead).
- **Preserve the OTHER gate's satisfied constraints.** When you fix code-review findings, do
  not break an AWS-readiness rule you already satisfied (e.g. moving config reads must keep
  secrets in SSM/Secrets Manager and config externalized), and vice versa. Hold both gates'
  rules simultaneously on every edit.
- If you genuinely believe a finding is wrong, push back with explicit reasoning rather than
  silently ignoring it.

#### Output contract

After implementation is complete, return:

1. **Change summary** - bullet list of files changed and key decisions made (including any stated assumptions).
2. **Diff** - the full unified diff of all changes.

Do not add prose beyond the summary and diff.

##### Netra modular architecture addendum

When the spec touches a Netra module or any modular-monolith codebase:

- Read architecture/modular-monolith.md, architecture/module-boundaries.md, and
  architecture/dependency-injection.md before writing code.
- Import another module only through its public API. Never import an internal path.
- Put reusable capability in `modules/`. Put non-reusable application behavior in `apps/`.
  If a second application could reuse it with different configuration, it is not
  application-specific.
- Keep concrete SDK imports inside outbound adapters or framework infrastructure. Domain
  and application services must not import Qdrant, FastAPI, LangGraph, cloud SDKs,
  database drivers, or model-provider SDKs.
- Receive dependencies through the constructor. Never construct a client inside a service.
- Follow architecture/placeholders.md when a capability is not yet designed: implement the
  real port, return typed results and stable reason codes, and never fabricate success.
- Keep source code, comments, docstrings, log messages, CLI output, identifiers, and
  exception messages ASCII-only. No emojis, no decorative Unicode.
- Do not create `helpers.py`, `misc.py`, `common.py`, or `utils.py` dumping grounds.
- Chain exceptions with `raise NewError(...) from exc`.
- Bound every async fan-out and set a timeout on every network call.
- Create every application service as an ABC interface in application/<name>.py and
one implementation <Name>Impl(<Name>) in application/impl/<name>_impl.py. Consumers,
graph nodes and other services depend on the interface. Export both from public.py and
construct the Impl only in bootstrap. To add a feature, add the abstract method first,
then implement it.
- Build branching, looping or fan-out flows as LangGraph graphs (state.py, edges.py,
nodes grouped by concern, a builder). Bound every loop in an edge predicate with an
attempt cap and a deadline check; fan out with Send and max_concurrency.
- Run the architecture and contract tests before returning the diff.

### 2.3 Code reviewer rules

You are a staff backend engineer doing a rigorous code review.

#### Standards to load before reviewing

Read every standards file the implementer used (the spec passes these as absolute
`standards/...` paths), plus the master security checklist:

- `standards/security/owasp-checklist.md`

Do not skip any file. Your findings are only as good as your knowledge of the agreed standards.

#### Review checklist

Evaluate the diff against every item below. Flag each violation as a finding.

- **Layering boundaries** - does the change respect the architectural layer identified in the spec? No cross-layer leakage.
- **Null / None handling** - every value that can be null or None must be guarded before use.
- **Error handling and codes** - errors are typed and explicit; no bare `except` or swallowed exceptions; HTTP/gRPC status codes match the error semantics.
- **Input validation** - all data crossing a trust boundary is validated before use; rejection happens at the entry point, not deep inside business logic.
- **Injection safety** - SQL, Cypher, and shell commands use parameterized / structured APIs only; zero string concatenation or f-string query construction.
- **Secrets and logging hygiene** - no credentials, tokens, or PII appear in log statements, error messages, or exception text.
- **Naming** - names are unambiguous, follow the project convention, and do not require a comment to understand.
- **Function size** - flag functions over 50 lines; severity per the calibration (slightly over = MINOR, well over or hard-to-follow = MAJOR).
- **Test coverage** - new logic has corresponding tests; edge cases (empty, null, boundary values) are covered.
- **OWASP top-10 items** - cross-reference the owasp-checklist; call out any applicable risk not mitigated.

#### Severity definitions

Severity is about IMPACT, not about how many rules are touched. Calibrate honestly - do not
inflate polish into a blocker.

| Severity | Meaning |
|----------|---------|
| BLOCKER  | Security vulnerability, data-loss/corruption risk, or a crash/incorrect result in a reachable path. Must be fixed before merge. |
| MAJOR    | A real correctness, security, or significant maintainability problem that WILL cause bugs or operational pain - e.g. an unhandled null on a live path, an injection vector, a layering violation that couples modules and blocks change, a swallowed exception, missing tests for non-trivial branching logic. Must be fixed before merge. |
| MINOR    | Style, naming, purity preference, or a quality nit that does not threaten correctness/security and only marginally affects maintainability. Address as follow-up tech-debt; does NOT block. |

##### Calibration - do NOT rank these as MAJOR

- A function slightly over a threshold (e.g. 54 lines vs the 50 limit) -> **MINOR**. Reserve a
  size MAJOR for functions that are genuinely hard to follow (well over the limit, deeply nested).
- Reading config/env directly instead of injecting it, when it works correctly -> **MINOR**
  (a purity/testability preference), unless it actually breaks layering in a way that causes bugs.
- A type not narrowed after a guard, where the runtime behavior is already correct -> **MINOR**.
- Naming, formatting, docstring wording, ordering -> **MINOR**.

The goal is a loop that CONVERGES: once no genuine correctness/security/significant-maintainability
issue remains, APPROVE and list the polish items as MINOR tech-debt. Perfection is not the bar;
production-readiness is.

#### Output contract

List every finding in this format:

```
[SEVERITY] file:line - standard violated - description of the problem - required fix
```

After all findings (or "No findings." if the diff is clean), end with exactly one of:

```
VERDICT: APPROVE
```
```
VERDICT: REJECT
```

Issue REJECT if any BLOCKER or MAJOR finding exists. Issue APPROVE only when all findings are MINOR or there are none.

Do not add prose beyond the findings list and the verdict line.

##### Netra modular architecture addendum

For changes in a modular-monolith codebase, additionally verify:

- Cross-module imports target only the public API of the imported module.
- Domain and application services contain no concrete infrastructure SDK imports.
- Application packages do not import one another.
- The shared kernel contains no application-specific model or behavior.
- Placeholder adapters are typed, explicit, safe, and replaceable through bootstrap only;
  consumer code does not branch on placeholder mode.
- Retrieval fallback paths preserve mandatory authorization, publication, currency, and
  approval filters.
- Confidence is computed in code, never taken from model self-rating.
- Source code, comments, log messages, CLI output, identifiers, and exception messages
  contain no emojis or decorative Unicode.
- Architecture and contract tests cover every changed boundary.
- Every application service has an ABC interface and one Impl; no consumer imports or
type-hints an Impl outside public.py and bootstrap.
- LangGraph flows bound every loop in edges.py and fan out with Send plus
max_concurrency.

Severity guidance: a violation that couples modules, bypasses authorization, or leaks an
SDK type into domain code is MAJOR or BLOCKER by impact. File placement and naming
preferences that do not couple modules remain MINOR.

### 2.4 Architecture reviewer rules

You are a principal software architect reviewing a change to a modular-monolith backend.

#### Standards to load before reviewing

Read every architecture standard named in the task. For any module-boundary change,
always read these absolute paths:

- standards/architecture/modular-monolith.md
- standards/architecture/module-boundaries.md
- standards/architecture/shared-kernel.md
- standards/architecture/dependency-injection.md
- standards/architecture/architecture-tests.md
- standards/architecture/service-interface-implementation.md

Also read standards/architecture/placeholders.md when a
placeholder adapter is created or modified.

#### Review checks

- The correct platform or application module owns the change.
- A reusable capability was not buried inside an application package.
- Application packages do not import one another.
- Cross-module imports resolve to the target module's public API only.
- Domain and application code contain no concrete SDK imports.
- Bootstrap owns dependency construction and lifecycle.
- The shared kernel contains only stable cross-module primitives.
- Ports are narrow, caller-owned, typed, and do not mirror a vendor SDK.
- Placeholders are typed, explicit, safe, and replaceable through bootstrap only.
- Public-contract changes are identified, versioned, and contract-tested.
- Architecture tests cover the added or changed boundary.
- Code templates intended for the codebase comply with the ASCII-source standard.
- Application services follow service-interface-implementation.md: ABC interface, one
Impl, consumers depend on the interface, Impl constructed only in bootstrap.
- LangGraph graphs follow module-templates/langgraph-module.md: pure edges, bounded
loops, nodes grouped by concern, langgraph imported only in graph/ and impl/.

#### Severity

| Severity | Meaning |
|---|---|
| BLOCKER | A boundary violation that creates a security gap, or a change that makes the module unextractable and unusable by a second consumer. |
| MAJOR | Real coupling that will cause future regressions: private cross-module import, SDK leakage into domain, application-to-application import, missing architecture test for a new boundary, a consumer depending on a service Impl, an unbounded graph loop. |
| MINOR | Naming, file placement, or documentation issues that do not couple modules. |

Do not inflate a naming preference into a MAJOR. The goal is a converging loop.

#### Output contract

List every finding in this format:

[SEVERITY] file:line - rule violated - description of the problem - required fix

After all findings, or "No findings." if the change is clean, end with exactly one of:

ARCHITECTURE VERDICT: APPROVE
ARCHITECTURE VERDICT: REJECT

Issue REJECT if any BLOCKER or MAJOR finding exists. Do not edit any file; the
implementer applies fixes on the next loop iteration.

## 3. Standards

### 3.1 Architecture

#### Modular Monolith

*Source: standards/architecture/modular-monolith.md*

Build independently testable capability modules inside one deployable backend.
Preserve a future extraction path without paying the microservices tax early.

##### Ownership rules

- `modules/` owns reusable platform capabilities: orchestration, resource discovery,
  ingestion, retrieval, authorization, caching, answer generation, audit, guardrails.
- `apps/` owns only behavior that cannot be reused by another application.
- `shared/` holds stable cross-module primitives and contracts, nothing else.
- `infrastructure/` holds concrete SDK, persistence, and identity adapters.
- `bootstrap/` constructs the dependency graph and registers modules and routers.

##### The placement test

Before creating a file under `apps/`, answer: could a second application reuse this
with different configuration? If yes, it belongs in `modules/`. Document-format
extraction, chunking, embedding, vector search, reranking, deduplication, and evidence
assembly are reusable. HR applicability rules, JDE SQL policy, and contract clause
mapping are not.

##### Public API rule

Each substantial module defines `public.py`, `manifest.py`, and `README.md`. Consumers
import only from `public.py`. Importing another module's internal path is a violation.

##### Extraction readiness

A module is extraction-ready when it can be copied with its declared shared contracts,
given implementations for its outbound ports, and validated by its own contract tests.
No module may require another module's private implementation or repository.

##### Reject if

- A reusable platform capability is implemented inside an application package.
- An application package imports another application package.
- A module imports another module's internal file rather than its public API.
- Shared framework code contains application-specific models or policies.
- One in-process module calls another over the network without a deployment driver.
- A service is extracted without an independent scaling, cadence, or ownership reason.

#### Module Boundaries

*Source: standards/architecture/module-boundaries.md*

##### Dependency direction

- Inbound adapters call inbound ports or application use cases.
- Application services depend on domain types and outbound ports.
- Domain code depends only on domain code and the bounded shared kernel.
- Outbound adapters implement outbound ports and translate external types.
- Bootstrap selects and constructs concrete implementations.

##### Service rules

- Services coordinate use cases and apply policies.
- Services do not construct clients or read environment variables.
- Services do not import FastAPI, Qdrant, LangGraph, cloud SDKs, database drivers,
  or model-provider SDKs.
- Services receive dependencies through constructor injection.
- Every application service is an ABC interface with one Impl; consumers depend on
  the interface (architecture/service-interface-implementation.md).

##### Port rules

Use `Protocol` for ports unless runtime registration requires an abstract base class.
Ports are not services: application services use ABC interfaces, ports stay `Protocol`.
Ports are narrow and caller-owned. A port must not mirror a vendor SDK surface.

##### Public API rules

`public.py` re-exports only supported contracts and use cases. `__init__.py` stays thin.
Breaking a published contract requires versioning, consumer review, and contract tests.

##### Reject if

- Domain code imports adapters, infrastructure, or API modules.
- An API router or graph node contains business logic.
- A service locator is called inside business logic.
- A third-party exception crosses an adapter boundary unwrapped.
- A module exports private implementation classes through its public API. Service
  `<Name>Impl` classes are exported only so bootstrap can construct them; they are
  not private helpers.
- A port exists only to wrap one constructor with no second implementation or test double.
- An application service has no ABC interface, or a consumer depends on its Impl.

#### Service Interface and Implementation

*Source: standards/architecture/service-interface-implementation.md*

Every application service is split into an abstract interface and one implementation,
in the same way as a Java `XService` interface and its `XServiceImpl` class. Consumers
depend on the interface. Only bootstrap knows the implementation. A feature added to a
module is then a new abstract method plus its implementation, and no caller changes.

##### Scope

- Applies to every class in `modules/<module>/application/` that coordinates a use
  case, owns collaborators, or performs I/O through ports.
- Does not apply to value objects: Pydantic models, dataclasses, TypedDict, enums,
  exceptions.
- Does not apply to outbound ports. Ports stay `Protocol` in `ports/outbound.py`, and
  adapters implement them without an extra ABC (see module-boundaries.md).
- Graph node classes and pure policy functions are not services.

##### Interface rules

- The interface is `class <Name>(ABC)`.
- Every public method has `@abstractmethod`, a full type signature and a docstring
  with Args, Returns and Raises where relevant. The body is `...`.
- The interface file may hold only the contract types used in its signatures:
  settings models, request and result models, frozen dataclasses.
- No logic, constructor, collaborators, adapter imports or SDK imports.

##### Implementation rules

- The implementation is `class <Name>Impl(<Name>)` in
  `application/impl/<name>_impl.py`.
- It owns the constructor (constructor injection only), all logic and all private
  helpers.
- There is exactly one `Impl` per interface. A real second variant gets a descriptive
  name, for example `Cached<Name>Impl`, in its own file under `impl/`, and is selected
  in bootstrap.

##### Consumer rules

- Type hints, constructor parameters, graph-node dependencies and other services
  depend on `<Name>`, never on `<Name>Impl`.
- Only these may import from `application/impl/`:
  - the owning module's `public.py`;
  - other files inside the same `impl/` folder;
  - bootstrap and tests, through `public.py`.
- Bootstrap constructs `<Name>Impl` and injects it typed as `<Name>`.

##### Adding a feature to a module

1. Add the abstract method, with its docstring, to the interface.
2. Implement it in `<Name>Impl`. The architecture test fails until it exists.
3. Wire any new collaborator in bootstrap only.
4. Add a unit test that exercises the method through the interface type, using the
   `Impl` with fakes.

##### Relationship to YAGNI

This rule is a deliberate, approved exception to the general "no interface with a
single implementation" guidance in principles/dry-kiss-yagni.md and
principles/design-patterns.md. The interface is the module's stable contract, so it
always exists for application services, even with one implementation.

##### Example

```python
# application/guard_service.py
class GuardService(ABC):
    """Screens input and the complete output before display."""

    @abstractmethod
    async def screen(
        self, text: str, direction: GuardDirection, requestor: str, deadline: Deadline
    ) -> GuardVerdict:
        """Screen text, applying the failure policy.

        Raises:
            GuardrailUnavailableError: When screening cannot run and the policy is
                fail-closed.
        """
        ...

# application/impl/guard_service_impl.py
class GuardServiceImpl(GuardService):
    """Fail-open or fail-closed policy around the guard adapter."""

    def __init__(self, port: ContentGuardPort, is_fail_closed: bool, timeout_seconds: float) -> None:
        """Store collaborators."""
        ...
```

##### Enforcement

The architecture suite is a blocking CI gate. It must check that:

- every non-value class in `application/` (outside `impl/`) extends ABC, and every
  public method on it is abstract;
- every `impl/<name>_impl.py` defines `<Name>Impl(<Name>)` for each interface in its
  sibling `<name>.py`;
- every interface has an implementation file;
- no file outside the owning module's `public.py` and `impl/` imports
  `application.impl`.

##### Reject if

- An application service is a concrete class with no interface.
- A public interface method is not abstract, or carries logic.
- A consumer type hint, constructor parameter or graph-node dependency names an
  `Impl` class.
- A file outside the owning `public.py` or `impl/` imports `application.impl`.
- An interface has no implementation, or an `Impl` does not extend its interface.
- A port (`ports/outbound.py`) is converted into an ABC service to satisfy this rule.

#### Shared Kernel

*Source: standards/architecture/shared-kernel.md*

The shared kernel is small, stable, and changes less often than any feature module.

##### Allowed content

- Request context, correlation and trace identifiers.
- Stable identifier value objects and clock abstractions.
- Generic result, paging, and error-envelope contracts.
- Application, resource, evidence, and authorization reference contracts.
- Observability primitives independent of any business capability.

##### Prohibited content

- Document chunks, database table metadata, contract clauses, or application policies.
- Qdrant filters, ORM models, LangGraph state, provider responses, or any SDK type.
- Behavior that orchestrates an application-specific workflow.
- `Manager`, `Helper`, `Common`, `Misc`, or `Utils` classes and modules.

##### Promotion rule

A new shared type requires at least two current module consumers and an architecture
review. Two business models that merely look alike stay separate; shared shape is not
shared meaning.

##### Reject if

- Shared code imports a platform module or an application module.
- A shared abstraction accumulates per-application conditionals.
- Changing shared behavior can alter unrelated application runtime behavior.
- A new shared type has exactly one consumer.

#### Dependency Injection and Bootstrap

*Source: standards/architecture/dependency-injection.md*

##### Construction

- Constructor injection is mandatory for I/O clients, repositories, policies,
  strategies, clocks, identifier generators, and external services.
- Business code never resolves dependencies from a container or global registry.
- Bootstrap owns construction, lifecycle, router registration, and health registration.
- Bootstrap constructs service implementations (`<Name>Impl`) and injects them typed as
  their interfaces (`<Name>`). No other code constructs an Impl, except tests.
- Long-lived clients are created during application lifespan and closed on shutdown.

##### Adapter selection

Placeholder and production adapters are selected by typed configuration in bootstrap.
Business code must never branch on which adapter is active.

Prohibited:

```python
if settings.use_placeholder_cache:
    result = fake_lookup(request)
else:
    result = await cache_client.lookup(request)
```

Required:

```python
result = await self._cache.lookup(request)
```

##### Factories and registries

Use a registry or factory only when two runtime-selectable implementations exist or are
part of the approved current design. An unknown implementation name fails fast with a
message listing valid options. Never fall back silently to a default.

##### Testing

Every service must be constructible with fakes through its public constructor. Unit
tests must not patch module globals to replace a dependency.

##### Reject if

- A service constructs a Qdrant, HTTP, database, cache, model, or file client.
- A method calls a service locator or global accessor.
- Import-time code performs I/O or opens a network connection.
- A mutable global singleton carries request or domain state.
- Business code branches on placeholder versus production mode.

#### Layering

*Source: standards/architecture/layering.md*

Enforce strict layer boundaries so each layer has one reason to change.

##### HTTP / Router Layer

- Validate request shape and auth; delegate immediately to the service layer.
- Return HTTP responses; never contain business logic or query construction.
- Translate service-layer exceptions into HTTP status codes here, nowhere else.
- Permitted dependencies: service layer only.

##### Service Layer

- Own all business logic, invariants, and orchestration.
- No HTTP primitives (`Request`, `Response`, status codes) anywhere in this layer.
- No raw SQL or ORM queries; all data access goes through a repository interface.
- May call other service-layer objects or domain models; never calls the router layer.
- Transactions are coordinated here; the repository executes them.

##### Repository Layer

- Translate between domain objects and the storage layer; no logic beyond that.
- No business rules, no conditional branching on business state.
- Expose explicit query methods; do not leak ORM internals (raw sessions, cursors) out.
- One repository per aggregate root; never spans multiple aggregates in one method.

##### Dependency Direction

- Dependencies point inward: router -> service -> repository.
- Inner layers define interfaces; outer layers provide implementations.
- No layer may import from a layer outward (repositories must not import services).
- No layer skipping: routers must not call repositories directly.

##### Cross-Cutting Concerns

- Logging, metrics, and tracing belong at layer entry/exit points via middleware or decorators, not scattered in service logic.
- Configuration is injected; layers do not read environment variables directly.
- Domain events are raised in the service layer and dispatched after commit.

##### Reject if

- A router method contains conditional business logic or a DB query.
- A repository method applies business rules or calls a service.
- A service constructs or returns an HTTP response object.
- Any layer imports a class from a layer that is outward of it.
- Business logic is duplicated across multiple layers to satisfy a shortcut.
- A single repository method spans more than one aggregate root.

#### Production-Shaped Placeholders

*Source: standards/architecture/placeholders.md*

A placeholder lets a module with a pending design participate through a stable contract.
It is not an empty class, a mocked success, or a conditional inside its consumers.

##### Required behavior

- Implement the exact production port.
- Accept and return fully typed models.
- Return stable outcome and reason codes.
- Report adapter kind and version in diagnostics.
- Emit structured, sanitized logs.
- Pass the same contract tests the production adapter will pass.
- Be selectable only through bootstrap configuration.

##### Safety defaults

| Capability | Placeholder behavior |
|---|---|
| Authorization | Default deny outside explicit local fixtures |
| Cache | Typed miss with a reason code |
| Audit | Explicit non-durable receipt; prohibited in the production profile |
| Download | Unavailable, no public or unauthenticated URL |
| Groundedness | Not evaluated |
| Confidence | Null or explicitly provisional, never fabricated |
| Ticketing | Unavailable |

##### Example

```python
class PlaceholderCacheLookup(CacheLookupPort):
    """Returns a typed miss until the curated cache design is approved."""

    async def lookup(self, request: CacheLookupRequest) -> CacheLookupResult:
        return CacheLookupResult(
            hit=False,
            reason=CacheMissReason.PLACEHOLDER_ADAPTER,
            adapter="placeholder",
            adapter_version=1,
        )
```

##### Reject if

- Consumer code imports a placeholder class or checks a placeholder flag.
- A placeholder returns an untyped dictionary or `None` as a contract.
- A placeholder grants wildcard access or an implicit allow.
- A placeholder claims durable audit, real confidence, or a successful external action.
- A method raises `NotImplementedError` on a declared port method.
- A production startup profile accepts a prohibited placeholder adapter.

#### Resilience

*Source: standards/architecture/resilience.md*

Every outbound dependency is a failure domain; design every call to survive its failure.
Prefer fail-fast over hang - a quick error is always cheaper than a slow one for every caller in the chain.

##### Timeouts

- Set an explicit connect timeout AND read/response timeout on every outbound call: HTTP, gRPC, DB, cache, queue, internal RPC. Never rely on library or OS defaults.
- Connect timeout signals unreachable host; read timeout signals a slow/hung server - treat them differently in retry logic.
- Propagate caller deadlines (context, gRPC deadline, HTTP timeout) into every downstream call; never widen a deadline mid-chain. If the caller supplies none, enforce a hard service-level max.

##### Retries

- Retry ONLY on transient errors (network timeout, 429, 502-504) AND only when the operation is idempotent. Never retry 4xx client errors or business-logic failures.
- Use exponential backoff with FULL JITTER (randomize the entire sleep window, not just +/-offset); fixed delays are forbidden.
- Cap total attempts (<=3 default) and cap the absolute backoff ceiling (e.g., 30 s).
- Enforce a service-wide retry budget (token bucket or sliding-window counter) across all instances; when the budget is exhausted, fail immediately rather than retry - coordinated retry storms kill dependencies faster than the original fault. Full jitter also prevents thundering-herd after shared-dependency recovery.
- After all retries are exhausted, propagate the original error; swallowing it is forbidden.

##### Circuit Breakers

- Wrap every external service call in a circuit breaker; one breaker per logical dependency, never shared across dependencies.
- Define explicit thresholds for CLOSED->OPEN (failure rate or count in a rolling window) and for HALF-OPEN->CLOSED recovery (probe interval + required success count).
- In OPEN state, fail fast immediately - never attempt the call or incur a timeout. Log and alert every state transition.

##### Bulkheads

- Isolate thread pools, connection pools, or async semaphores per downstream dependency; saturation of one must not starve others.
- Set per-dependency concurrency limits; reject excess immediately rather than queuing without bound. Size pools for worst-case latency, not average.

##### Load Shedding & Backpressure

- Enforce a configurable concurrency/queue limit at service ingress; return 503 with Retry-After when exceeded.
- Propagate backpressure upstream via standard signals (429/503, gRPC RESOURCE_EXHAUSTED); never silently drop. Under extreme load, prioritize health-check and critical-path traffic over background work.

##### Graceful Degradation & Fallbacks

- Define and document the degraded behavior for every critical dependency before the feature ships.
- Return cached, stale, or default data where correctness tolerates it; never silently return null/empty when the real answer could not be fetched - callers must know response confidence.

##### Idempotency

- All state-mutating endpoints reachable via retries must accept client-supplied idempotency keys. Store the key + result atomically with the operation (no separate read-then-write). Expire keys after a documented TTL.
- "Exactly-once" delivery does not exist in distributed systems; assume at-least-once everywhere and design consumers to be idempotent.

##### Dead-Letter & Poison Messages

- Every async consumer routes unprocessable messages to a DLQ after a fixed retry cap. DLQs must be monitored and alerted; an accumulating DLQ is a production incident signal. Provide a replay path after root-cause resolution.

##### Health Checks

- Separate liveness (process alive, not deadlocked) from readiness (dependencies warm, able to serve). Readiness must check critical connection pools; a broken DB must pull the pod from rotation.
- Health-check endpoints must be exempt from auth and rate limiting, respond within 500 ms, and must never call an upstream service - doing so creates cascading readiness failures.

##### Chaos & Fault Injection

- Fault injection (latency, error, timeout) must be runnable in staging with no code change - via config or sidecar. Run circuit-breaker, bulkhead, and retry-budget scenarios in CI. Conduct periodic production game-day drills and document each dependency's verified blast radius in the runbook.

##### Reject if

- Any outbound call (HTTP, DB, cache, queue, gRPC) lacks an explicit connect or read timeout.
- A retry policy retries 4xx client errors, non-idempotent operations, or uses fixed sleep intervals without full jitter.
- No retry cap or service-wide retry budget exists, leaving coordinated retry storms / thundering-herd unmitigated.
- A circuit breaker is absent on any external service call, or state transitions are not logged and alerted.
- A single shared pool serves multiple unrelated downstream dependencies (missing bulkhead).
- The service has no ingress load-shedding limit and accepts unbounded concurrent load.
- A state-mutating endpoint reachable via retries has no idempotency key mechanism, or the implementation uses a non-atomic read-then-write check.
- Code or documentation claims "exactly-once" delivery or processing guarantees.
- Async consumers have no DLQ, or the DLQ has no alert configured.
- Liveness and readiness probes are combined, or a readiness probe calls an upstream service.
- Caller deadlines are not forwarded into downstream calls, allowing silent deadline extension mid-chain.
- Fault injection has never been run against the service's resilience mechanisms.

#### Architecture Tests

*Source: standards/architecture/architecture-tests.md*

Boundaries that are documented but not machine-enforced will erode. Every boundary rule
in this vault must have a corresponding automated check.

##### Required checks

- Domain packages cannot import API, adapters, infrastructure, or bootstrap.
- Application services cannot import concrete SDKs.
- Application packages cannot import one another.
- Cross-module imports resolve only to the target module's public API.
- The shared kernel cannot import a platform module or an application package.
- Infrastructure adapters implement a declared outbound port.
- Routers and graph nodes call use cases, not repositories or external clients.
- The package dependency graph is acyclic.
- Every non-value class in application/ extends ABC with only abstract public methods,
  and has exactly one `<Name>Impl(<Name>)` in application/impl/<name>_impl.py.
- No file outside the owning public.py or impl/ imports application.impl.
- langgraph is imported only in graph/ packages and application/impl/ files.
- graph/edges.py contains no async functions and no port, adapter, impl or
  infrastructure imports; graph nodes import no adapters or impl modules.
- Functions stay within the 50-line limit.

##### Enforcement

Use import-linter, a pytest import-graph suite, or an equivalent static analysis. The
architecture suite is a blocking CI gate. A new module must register its contracts in
the same pull request that creates it.

##### Reject if

- A boundary is documented in this vault but has no automated check.
- A newly created module is absent from the architecture test configuration.
- An allow-list exception is widened without a recorded architecture decision.
- The architecture suite is non-blocking or excluded from the merge gate.

##### Verifying the gate

When a new rule is added, prove it works: introduce one deliberate violation, confirm
the suite fails on it, then revert. A rule that has never failed has not been tested.

#### Testing

*Source: standards/architecture/testing.md*

Tests are the primary safety net for refactoring and the living specification of correct behavior; they must be fast, deterministic, and honest.

##### Test Pyramid

- Write the majority of tests as unit tests: fast, no I/O, no network, no filesystem.
- Write integration tests only at persistence boundaries, brokers, and external adapters.
- Write end-to-end tests sparingly; cover critical user journeys only, not every code path.
- An inverted pyramid - suite dominated by slow integration or e2e tests - is a systemic defect.

##### Structure (AAA)

- Enforce Arrange-Act-Assert strictly; sections visually distinct, no interleaving.
- One logical behavior per test; multiple unrelated assertions in one test mask failures.
- Name tests on behavior: "given_X_when_Y_then_Z" form is mandatory; never name after the method.
- Test observable behavior - return values, boundary side effects - never internal state.

##### Determinism

- Inject clocks, UUIDs, random sources, and feature flags; never read them from the environment.
- Freeze all time-dependent values; no uninjected `time.Now()` or `Date.now()` in logic under test.
- Tests produce the same result on every run regardless of order, parallelism, or machine.
- No real network calls in unit tests; integration tests target exactly one real external boundary.

##### Flaky-Test Policy

- Flaky tests are blocking defects on par with production bugs - not a nuisance to tolerate.
- Quarantine immediately; isolate from the required-pass suite and file a defect ticket.
- Fix or delete within one sprint; outliving a sprint is an escalation item.
- Never mark flaky as skipped or xfail permanently.

##### Dependency Isolation

- Override dependencies through interfaces or constructor injection - never monkey-patch internals.
- Distinguish doubles: stub returns canned data, mock verifies interactions, fake is a lightweight real implementation.
- Mock only at the boundary to an external system; use real implementations or fakes for internal collaborators.

##### Test Data

- Use builder or factory patterns; never duplicate raw structs across test files.
- Builders provide safe valid defaults; tests override only the field under examination.

##### Integration and Isolation

- Use ephemeral, containerized dependencies (Testcontainers or equivalent); no shared persistent instances.
- Each test owns its schema, namespace, or queue; no cross-test state leakage allowed.
- No test leaves mutable state - global variables, singletons, caches - that affects subsequent tests.
- Integration tests run in a separate CI step; they must not block local unit-test runs.

##### Contract Tests

- Verify the API contract between every pair of services with consumer-driven contract tests (Pact or equivalent).
- Contract tests run in CI on both sides independently; a broken contract fails the provider pipeline.
- Schema validation alone is not a contract test; behavior and status codes must be verified.

##### Coverage Philosophy

- Cover all branches in business logic; cover all error paths, edge cases, and boundary conditions in adapters.
- Do not gate on a line-coverage percentage; an untested critical path is a defect regardless of overall number.
- Prefer branch or mutation coverage as a meaningful signal over raw line coverage.
- Tests are first-class code: reviewed in PRs, refactored alongside production code, subject to the same quality bar.

##### Reject if

- A unit test opens a real network socket, database connection, or file outside a temp directory.
- A test relies on execution order or leaves mutable state that affects subsequent tests.
- Test names describe implementation details rather than expected behavior.
- Private or internal methods are accessed directly to set up or assert test state.
- Two services share an interface with no consumer-driven contract test validating it.
- A flaky test is marked skipped, ignored, or xfail without an active defect ticket and fix deadline.
- Test doubles are injected by overriding internals rather than through the public dependency interface.
- Ephemeral infrastructure is replaced by a shared persistent test database or broker.
- Test data is constructed via raw literal structs duplicated across more than two test files.
- Error paths, boundary conditions, or failure modes are absent from the test suite for a critical service.

#### Microservices

*Source: standards/architecture/microservices.md*

Define when decomposition earns its complexity cost, enforce safe service collaboration, and prevent distributed-system antipatterns.

##### Monolith First

- Start with a modular monolith; extract only when a concrete forcing function exists: independent scaling, distinct deployment cadence, or separate team ownership.
- A well-structured monolith beats chatty microservices; evaluate the distributed-systems tax (latency, partial failure, eventual consistency, observability) before any split.

##### Service Boundaries

- Boundaries follow business capabilities and bounded contexts (DDD), never technical layers.
- One bounded context per service; if two tables must update atomically together they belong in the same service.

##### Data Ownership

- Each service owns its data store exclusively; no other service touches that schema or tables directly; no cross-service FK at the DB level.
- Cross-service reads go through the owning service's API; prefer data duplication propagated via events or CDC over any shared table.
- Ban shared libraries that encode domain logic or data contracts - they couple services identically to a shared schema.

##### Sync vs Async Communication

- Use synchronous REST or gRPC when the caller needs an immediate result; use async messaging (Kafka, SQS, RabbitMQ) for decoupled workflows and fan-out.
- Chatty chains (A -> B -> C -> D per request) signal a boundary error - collapse or reroute; tolerate at most two hops.
- Every outbound call must have timeouts, retries with exponential backoff, and circuit-breaking.

##### Fallacies of Distributed Computing

- The network is unreliable, has non-zero latency, finite bandwidth, and a changing topology - design explicitly for each failure mode; never assume a call succeeded because no error was returned.
- Message brokers guarantee at-least-once delivery; exactly-once end-to-end is not achievable - consumers must be idempotent instead.

##### API Gateway and BFF

- Gateway handles cross-cutting concerns only: auth, rate-limiting, routing, SSL, correlation IDs - no business logic, no per-tenant branching.
- Introduce a BFF per client surface for aggregation or protocol translation; keep it thin.

##### Service Discovery and SAGAs

- Locate services via a registry or DNS; no hardcoded hostnames; treat addresses as ephemeral.
- Never use two-phase commit or cross-service row locking; implement multi-service state changes via SAGAs.
- Orchestration SAGAs (central orchestrator, easier to trace) vs choreography SAGAs (reactive events, more decoupled) - choose deliberately; every step must be idempotent with a compensating action on failure; design compensations before the forward path.

##### Eventual Consistency and Idempotency

- Accept eventual consistency; do not fight it with distributed locking; surface staleness in the API contract (ETag, Last-Modified, or explicit timestamps).
- Idempotency tokens are mandatory on all state-mutating retryable calls; async consumers must tolerate out-of-order and duplicate delivery.

##### Contract Versioning and Deployability

- Version APIs explicitly (URI or header - one scheme per org); all changes must be backward-compatible before shipping; use consumer-driven contract tests (Pact) to catch breaks.
- Deprecate with a published sunset date; maintain at least two API versions during migration; deploy each service independently via feature flags, not coordinated releases.

##### Observability and Ownership

- Structured logs with correlation/trace IDs and OpenTelemetry tracing are mandatory on every service-to-service boundary.
- Each service owns its alerting runbook and on-call rotation; expose /health (liveness) and /ready (readiness) endpoints reflecting actual dependency state.

##### Reject if

- Two services share a database table, schema, or direct cross-service FK.
- A synchronous call chain spans three or more services in one user-facing request.
- Distributed transaction logic uses two-phase commit or cross-service row locking.
- A service is extracted solely to separate technical layers rather than a bounded business capability.
- The gateway or BFF contains conditional business logic or per-tenant branching rules.
- A SAGA step is not idempotent or has no published compensating action.
- A shared library encodes domain logic or data-access patterns used by more than one service.
- A state-mutating cross-service endpoint lacks an idempotency token.
- Code or documentation claims exactly-once delivery as an end-to-end guarantee.
- Contract changes ship without consumer-driven contract tests or a versioned deprecation notice.
- A service has no distributed trace instrumentation or no published on-call runbook.

### 3.2 AI systems

#### Agentic Orchestration

*Source: standards/ai/orchestration.md*

##### Boundary

The planner understands intent, decomposes focused subqueries, selects authorized
application handlers, bounds fan-out, and synthesizes normalized evidence. It does not
implement application retrieval or execution logic.

##### Requirements

- The planner sees only handlers the caller is authorized to invoke.
- Planner output is typed and schema-validated before any execution.
- Subqueries carry stable identifiers and an application target.
- Fan-out has a configurable concurrency limit and an overall deadline.
- An application failure is isolated and represented as a typed outcome.
- The planner never fabricates evidence for an unavailable application.
- Prompts are versioned resources, not inline string literals.

##### Agent registration

Adding an application must be a registration, not a rewrite. A fixed boolean decision
schema that must be edited for every new agent is a design defect.

Prohibited:

```python
class PlannerDecision(BaseModel):
    invoke_db_agent: bool
    invoke_file_agent: bool
```

Required:

```python
class PlannerDecision(BaseModel):
    invocations: list[AgentInvocation]
    requires_clarification: bool = False
    clarification_question: str | None = None
```

##### LangGraph rules

Flows with branches, bounded loops or fan-out are LangGraph graphs. A hidden while loop
inside a service is not acceptable for such flows. Use the layout in
module-templates/langgraph-module.md.

- State models are typed and hold no SDK clients or open connections.
- Nodes are thin use-case adapters that delegate to application services.
- Edge conditions are pure, deterministic functions.
- Group nodes by concern; a single oversized `nodes.py` is rejected.
- Progress events may stream; unscreened answer tokens may not.
- The compiled graph is built once, without network I/O at import time, inside the
  service implementation constructor or a bootstrap graph factory.
- Every loop (corrective retrieval, SQL repair, model escalation) is bounded by a
  routing predicate in edges.py that checks an attempt cap and the remaining
  request deadline.
- Parallel fan-out uses Send. Results merge through an Annotated list reducer, and
  concurrency is bounded with the max_concurrency run configuration.
- A node converts typed failures into typed outcomes, so one fanned-out failure never
  fails its siblings. When the run order of parallel results matters, restore it
  explicitly before use.
- The langgraph package is imported only in graph/ packages and in
  application/impl/ service implementations.
- Nodes depend on service interfaces and ports, never on implementations or adapters.

##### Reject if

- The planner receives descriptors for unauthorized handlers.
- A node contains Qdrant, SQL, HTTP, or file-parsing implementation logic.
- Subquery fan-out is unbounded or has no deadline.
- Raw model output selects an execution branch without schema validation.
- Adding one agent requires editing the decision schema, routing, state, and fan-out.
- A loop in the flow has no attempt cap or no deadline check in an edge predicate.
- Fan-out is implemented with an unbounded gather instead of Send plus max_concurrency.
- langgraph is imported in domain code, ports, adapters or an application interface.

#### Resource Discovery

*Source: standards/ai/resource-discovery.md*

##### Universal model

Resource discovery answers one question: which authorized resources should this
application handler use? A resource may be a document version, a database table, a
contract, a module, a report, or an API operation.

This stage is application-agnostic. Only the resource type and the second-stage
operation differ.

##### Semantic descriptor

Every discoverable resource provides a stable identifier, application identifier,
resource type, display name, description, semantic profile, optional keywords, lifecycle
projection, authorization projection, filter attributes, and schema version.

Taxonomy is optional enrichment. It must never be a mandatory platform dependency, an
authorization input, or the sole hard gate on retrieval. A resource with a good semantic
profile and no taxonomy must remain fully discoverable.

##### Search sequence

1. Resolve the caller's authorization and mandatory filters.
2. Embed the focused subquery once and reuse the vector.
3. Search the resource metadata index with mandatory filters applied inside the search.
4. Rerank and select resources.
5. Return selected, complementary, ambiguous, and rejected candidates with diagnostics.

##### Selection rules

- Do not force a single winner. Return primary, complementary, and ambiguous sets.
- Do not collapse two resources merely because their content is similar.
- Ask one targeted clarifying question when close-scoring candidates represent mutually
  exclusive scopes.
- Use deterministic tie-breakers such as precedence rank and declared relationships.

##### Reject if

- Discovery can return unauthorized, superseded, draft, or wrong-application resources.
- Missing optional taxonomy blocks ingestion or discovery.
- A model decides authorization, currency, or approval status.
- Application-specific or SDK types appear in the common resource contracts.
- Mandatory filters are applied after the search instead of inside it.

#### Retrieval Core

*Source: standards/ai/retrieval.md*

The retrieval module is a reusable platform capability. It must not encode the knowledge
of any single application.

##### Responsibilities

Resource discovery, content retrieval through ports, grading, bounded corrective
retrieval, deduplication, diversity selection, reranking, context expansion,
retrieval-strength calculation, and evidence assembly.

##### Generic sequence

```text
focused subquery
-> resolve authorization and mandatory filters
-> embed once
-> resource metadata discovery
-> resource selection
-> resource-specific content retrieval
-> grade
-> bounded corrective retrieval when weak
-> deduplicate and diversify
-> rerank
-> expand context
-> compute retrieval strength
-> assemble evidence and diagnostics
```

##### Application specialization

| Application | Metadata resource | Second stage |
|---|---|---|
| Document assistant | Document version | Chunk search, parent and ancestor expansion |
| Database assistant | Environment, schema, table | Column context, then owned SQL generation |
| Contract assistant | Agreement | Clause and business-provision retrieval |
| Module assistant | Module or operation | Authorized adapter invocation |

##### Thresholds

Document-stage and content-stage limits are separate configuration values, never one
shared constant. All thresholds are initial values that must be calibrated against a
golden evaluation set before being treated as production facts.

##### Fallback safety

Fallback may broaden semantic constraints and rewrite the query. It must never remove
authorization, publication, currency, approval, classification, application, or
environment filters. Global fallback means global within the caller's authorized and
currently valid scope.

Every fallback level used must appear in the returned diagnostics. Silent fallback is
prohibited.

##### Deduplication

Exact deduplication groups by content hash and preserves every source reference. Near
duplicates are grouped, not deleted. Genuine conflicts and materially different
applicability are preserved for the answer stage.

##### Reject if

- A fallback path performs an unrestricted all-content search.
- Raw vector-store payload dictionaries cross a module boundary.
- A model-generated confidence value is presented as computed confidence.
- Deduplication discards source provenance.
- Corrective retrieval has no retry cap.
- Reranker unavailability fails the request instead of degrading with a recorded penalty.

#### Evidence, Citations, and Confidence

*Source: standards/ai/evidence.md*

##### Evidence contract

Evidence carries a stable evidence identifier, application identifier, resource
identifier, resource version identifier, source type, locator, the authorized excerpt or
result projection, provenance, and retrieval diagnostics. Resource-specific detail lives
in typed extensions, not in free-form dictionaries.

##### Citation resolution

The answer generator receives only authorized evidence and returns a typed answer plus
the evidence identifiers it used. Source labels, links, page or slide locators, executed
queries, and version information are assembled by deterministic code after the model
returns.

The model must never produce a citation string, a URL, or a version number.

##### Confidence

Confidence is computed from approved signals such as retrieval strength, groundedness,
source agreement, fallback level, and dependency availability. A placeholder returns
null or an explicitly provisional status.

Prohibited:

```python
class FinalResponse(BaseModel):
    answer: str
    confidence: float  # model self-rated
```

Required: the model returns the answer and the evidence it used; code computes and
attaches confidence afterward.

##### Structured answers

When rich output is required, the model returns a structured answer contract with
sections, tables, and emphasis intent. A deterministic renderer produces the final
presentation and any export. The model does not emit final document formatting.

Exports are generated from the stored structured answer and evidence snapshot. Never
re-invoke the model to produce a download, because the export would not match what the
user saw.

##### Reject if

- The model invents source labels, links, or version numbers.
- An answer claim cannot be traced to retrieved evidence.
- Raw sensitive rows or full document content are written to diagnostics or logs.
- Confidence exists with no documented calculation and calibration policy.
- A download is produced by a second model call rather than the stored answer.

#### Resource Ingestion

*Source: standards/ai/ingestion.md*

Ingestion is a full backend capability with API, services, workflow, ports, and
adapters. It is not a collection of scripts. The frontend must be able to validate,
preview, submit, monitor, retry, approve, and publish through the same services.

##### Framework responsibilities

Run lifecycle, idempotency, checkpoints, status, quarantine, preview, approval,
publication, progress events, and per-item results.

##### Strategy responsibilities

- Document strategy: validate metadata, extract layout, build hierarchy, chunk,
  reconcile coverage, embed metadata and content, publish both indexes.
- Database-schema strategy: discover environments, schemas, tables, columns, keys, and
  relationships, build semantic profiles, index metadata. Never ingest live rows.
- Application-catalog strategy: discover modules and operations and index their profiles.

##### Metadata precedence

Validated human metadata overrides authoritative source metadata, which overrides
automated enrichment, which overrides defaults. Automated classification must never
silently overwrite approved values. A disagreement is recorded as a diagnostic.

##### Optional enrichment

Missing taxonomy, keywords, or generated categories produce a warning and do not block
publication. Missing mandatory scoping, identity, or lifecycle metadata does block it.

##### Content fidelity

Hierarchical content ingestion must account for every source block. Boilerplate is
tagged, never silently deleted. Tables are preserved as units. A coverage reconciliation
must pass before publication; a failure quarantines the version.

##### Publication

Publication is idempotent and versioned. Partially indexed resources remain
non-retrievable. Identity is derived from stable version and structure, never from a
filename.

##### Missing values

Use typed null and empty collections. Never use empty strings, `"N/A"`, or `"None"` as
substitutes for absent data.

##### Reject if

- Frontend ingestion depends on standalone scripts rather than services.
- One bad record fails an entire batch with no per-item result.
- A filename is used as the immutable resource identity.
- A partially indexed resource becomes retrievable.
- Extracted content is deleted rather than tagged when classification is uncertain.
- Embedding is performed one item per network call where a batch API exists.

#### Vector Store Adapters and Payload Contracts

*Source: standards/ai/qdrant.md*

##### Boundary

Only vector-store adapters import the vector-store client library. Domain and
application services use owned ports with typed search requests, filters, candidates,
and results.

Prohibited outside the adapter:

```python
from qdrant_client.models import Filter, FieldCondition
```

##### Collections

Use separate metadata and content collections when two-stage retrieval is required.
Collection names, vector dimensions, distance metric, timeouts, and candidate limits are
typed configuration, never literals in business logic.

##### Payloads

A payload is a versioned retrieval projection, not the authoritative business record.
Keep stable join identifiers, mandatory filter fields, context and provenance fields,
and the canonical content the design requires.

Keep payloads flat. Use scalars and flat arrays. Nested objects filter poorly and are
not portable across stores.

Every payload carries a `schema_version`. Changing the payload shape requires a version
increment and a migration plan.

##### Filtering

Authorization, application, lifecycle, currency, approval, environment, resource type,
and selected-resource filters are applied inside the vector search, never after results
return. Frequently filtered fields have payload indexes created before bulk ingestion.

##### Embedding text versus stored text

The vector is generated from deliberately contextualized text. The payload stores the
original content used for display and citation. Reversing these is a defect.

##### Reject if

- Vector-store SDK models cross the adapter boundary.
- Mandatory access or lifecycle filters are applied after the search.
- A payload changes shape without a schema version and migration plan.
- A collection is created or altered from a request-handling path.
- Search, scroll, upsert, or delete operations have no timeout.
- Contextual embedding text is stored as the citation text.

### 3.3 Principles

#### SOLID

*Source: standards/principles/solid.md*

SOLID applied concretely to backend services - each principle has a hard boundary, not a guideline.

##### S - Single Responsibility
- One service class owns one domain capability. A `PaymentService` processes payments; it does not also send emails or write audit logs.
- Side effects (notifications, audit, metrics) are delegated to collaborators injected at construction time.
- If the class has more than one axis of change - two unrelated business reasons to modify it - split it.

##### O - Open/Closed
- Extend behavior by adding new implementations of an interface, not by modifying existing classes with new conditionals.
- A new feature requirement that causes you to edit existing, tested logic is a signal of missing abstraction.
- Feature flags that gate new code paths inside an existing method violate OCP; wrap the diverging path in a new implementation instead.

##### L - Liskov Substitution
- Every concrete implementation must fulfill the full contract of its interface without weakening preconditions or strengthening postconditions.
- Implementations that raise `NotImplementedError` on interface methods are rejected.
- Subtypes must not silently ignore method calls or return sentinel values not documented in the interface contract.

##### I - Interface Segregation
- Interfaces are defined by callers' needs, not by the implementor's convenience.
- Do not add methods to a shared interface because one implementation needs them; create a narrower interface.
- A class implementing an interface it only half-uses is a sign the interface is too broad.

##### D - Dependency Inversion
- All I/O-touching dependencies (database, cache, message broker, HTTP client) are injected as interfaces, not constructed inside the class.
- The service layer defines the interface; the infrastructure layer provides the implementation.
- Framework-level globals (ORM session objects, global HTTP clients) must not be imported directly inside domain classes.

##### Reject if
- A service class constructs its own database session, HTTP client, or queue publisher internally.
- An interface method is left as `raise NotImplementedError` in a concrete class that is actually used in production.
- Adding a new variant of behavior requires editing an existing switch/if-elif chain rather than adding a new implementation.
- A class has two unrelated public responsibilities identifiable by distinct business reasons to change.
- An implementation depends on a sibling concrete class rather than on a shared interface.

#### Modularization

*Source: standards/principles/modularization.md*

Modules define contracts, not implementations - enforce strict layering, acyclic dependencies, and single responsibility behind an explicit public API.

##### Canonical Layer Order
- The dependency arrow is one-directional: DTO/model -> interface/protocol -> service -> implementation -> repository.
- No layer may import from any layer below it in this stack; violations are build-time errors, not conventions.
- Repository interfaces live in the domain/service layer; all ORM/DB implementations live in infrastructure.
- DTOs cross layer boundaries; domain/ORM objects must not - map at the boundary before passing up or down.

##### Depend on Abstractions (DIP)
- Service constructors accept interfaces (Protocol/ABC/interface type), never concrete implementations.
- Inject every I/O-touching dependency: DB session, cache client, HTTP client, message queue producer/consumer.
- Use constructor injection (or equivalent DI framework); never use a service-locator, global registry, or mutable module-level singleton as a substitute for injection.
- Business logic contains zero imports from infrastructure packages (ORM, queue SDK, HTTP lib, cloud SDK).
- Concrete factories or DI containers wire implementations; the service layer never calls them directly.

##### Explicit Public API
- Declare the module's public surface explicitly: __all__, exported symbols only, or a dedicated api.py / index.go file.
- Everything not in the public API is private/unexported/_prefixed and treated as an implementation detail.
- Internal helpers must not be importable from outside the package.
- Cross-module communication goes through published service interfaces, never direct inter-package imports between feature internals.

##### Single Responsibility and Cohesion
- Each module owns exactly one clearly named responsibility; if you need "and" to describe it, split it.
- Every export in a module must relate to the same primary concept.
- Split a module when it exceeds ~300-400 lines, has multiple unrelated concepts, or requires testing one part without the other.
- No god-objects: a class that touches DB, sends emails, calls external APIs, and formats responses must be decomposed.
- No Util/Helper dumping grounds: shared logic goes into a purposefully named shared subpackage with its own interface.

##### Package by Feature
- Organise top-level packages by business capability (payments, notifications, inventory), not by layer (models, services, utils).
- Each feature package owns its own interface definitions, service logic, and repository contracts.
- Shared cross-feature primitives live in a bounded common package; that package must not import from any feature package.

##### Feature Modules over a Shared Kernel (compose primitives, don't share behavior)
- When integrating many similar external surfaces (connectors, provider integrations, per-source pipelines, multi-tenant feature handlers), give each its OWN module that fully owns its orchestration. Do not force several modules to inherit from one behavior base class or depend on one shared strategy that drives them.
- Rationale - shared behavior is coupling: a change made for one consumer can regress every other consumer of that shared class. And external sources evolve independently, so today's similarity between two modules is coincidental, not guaranteed. Prefer duplicating cheap orchestration glue over adopting the wrong shared abstraction. A shared abstraction that you must keep adding per-consumer `if`/branch special-cases to is already the wrong abstraction - split it.
- The shared layer is a kernel of DUMB, STABLE, consumer-agnostic primitives (e.g. an HTTP client with retry/backoff/circuit-breaking, a rate limiter, a transactional writer, a paginator, input validators) that modules COMPOSE by calling. It is never a smart base class that performs behavior on behalf of modules.
- Not all duplication is equal. Orchestration glue (which path, which key, which date window, which field to promote) MAY be duplicated per module - it keeps each module self-explanatory and independently evolvable. Security- and correctness-critical mechanics (retry/backoff, auth/token handling, injection/input validation, transactional commit, pagination-envelope parsing) MUST live exactly once in the shared kernel and be composed - never copy-pasted into each module, where a fix would inevitably be missed in one.
- An abstract base class or interface here defines only the uniform CONTRACT (e.g. `streams()`, `run()`) so an orchestrator can iterate modules polymorphically. It must not carry cross-module behavior beyond trivial template glue. Any reuse of kernel helpers is opt-in (a module may call a shared helper or inline its own equivalent), never mandated inheritance.
- The test: editing one feature module must not require reading, and must not risk regressing, any other feature module. If it does, behavior has leaked into a shared layer that should hold only the contract and dumb primitives.

##### Acyclic Dependencies
- The dependency graph between packages must be a DAG - no circular imports, enforced by linting or build tooling.
- If A imports B and B imports A, introduce a third module C holding the shared abstraction and have both depend on C.
- Treat a circular-import warning as a blocking error, not a style note.

##### Anti-Corruption Layer at External Boundaries
- Wrap every third-party API, legacy service, or external data source behind an ACL: an interface you own, adapters you control.
- The ACL adapter maps external types to internal domain types; domain code never references external library types directly.
- Replace the adapter without touching domain logic; that is the proof the boundary is correct.

##### Stable Interfaces, Changeable Internals
- Interfaces are versioned contracts; breaking changes include: adding required parameters, removing or renaming methods, and altering return types.
- Internal implementation may change freely as long as the interface is satisfied and tests pass.
- Extend interfaces via composition or new methods with defaults; never silently widen an existing signature.

##### Independent Testability
- Each layer must be exercisable with all dependencies replaced by test doubles (mocks/stubs/fakes).
- Test doubles must implement the same declared interface as the real dependency - monkey-patching internals is not an acceptable substitute.
- A unit test for a service never connects to a real database, network, cache, or filesystem.
- If spinning up external infrastructure is required to test a unit, the dependency boundary is wrong - fix the boundary.
- Repository implementations have their own integration tests, separate from service-layer unit tests.

##### Reject if
- A service class imports a concrete ORM model, DB session, or queue client directly instead of through an injected interface.
- Any layer imports from a layer below it in the canonical stack (e.g., domain imports from infrastructure).
- A unit test requires a live database, network call, filesystem write, or external service to pass.
- A test double does not implement the module's declared interface (monkey-patching private internals instead).
- Internal (private/unexported) module symbols are imported directly by another package.
- A module exports symbols covering two or more unrelated domain concepts without a deliberate split.
- A circular import exists anywhere in the dependency graph.
- Domain or ORM objects are passed raw across a module boundary without a DTO/mapping step.
- A god-object or Util/Helper module accumulates unrelated logic from multiple features.
- An external third-party type or SDK model leaks into domain logic without an ACL adapter.
- A mutable global singleton or service-locator is used in place of explicit constructor injection.
- A new module is added without declaring an explicit public API surface.
- Two or more feature/connector modules inherit shared behavior from a common base class or strategy such that a change made for one can alter another's runtime behavior, instead of each module composing shared primitives.
- A shared base class or strategy accumulates per-consumer `if`/branch special-cases to accommodate divergent module behavior (the wrong-abstraction smell).
- Security- or correctness-critical mechanics (retry/backoff, auth/token handling, injection/input validation, transactional commit) are copy-pasted into each feature module instead of living once in a shared kernel the modules compose.

#### DRY / KISS / YAGNI

*Source: standards/principles/dry-kiss-yagni.md*

Write the simplest correct solution today; extract only what repetition has proven necessary.

##### DRY - Don't Repeat Yourself
- Extract on the third repetition, not the second. Two similar implementations may be coincidental; three establish a pattern worth naming.
- The extracted abstraction must be named by the concept it represents, not by the callers that share it.
- DRY applies to logic and rules, not to code shape. Identical-looking code with different business meanings must NOT be merged.
- Shared utilities that span features belong in `shared/` with a domain name; do not create `utils.py` catch-alls.

##### KISS - Keep It Simple
- The simplest implementation that satisfies the current, specified requirements is the correct one.
- Complex solutions require justification in a comment or ADR; reviewer default is to prefer the simpler alternative.
- Avoid clever one-liners that compress logic at the cost of comprehension - optimize for the reader, not the writer.
- If a solution requires explanation beyond its naming, it is probably not simple enough.

##### YAGNI - You Aren't Gonna Need It
- Do not implement capability that no current ticket/requirement demands.
- Extension points (plugin hooks, abstract base classes, generic type parameters) are premature until there is a concrete second use case.
- Exception: application service interfaces required by
  architecture/service-interface-implementation.md are the module contract, not
  speculative extension points. They always exist, even with one implementation.
- Configuration knobs, feature flags, and strategy patterns introduced "for future flexibility" with one implementation are YAGNI violations.
- Speculative generality - designing for hypothetical scale, hypothetical multi-tenancy, or hypothetical integrations - is rejected.

##### Dead Code
- Delete unused code; do not comment it out. Version control is the history.
- Unreachable branches, unused imports, deprecated functions with no callers, and feature-flagged code that ships permanently disabled must be removed.
- Code guarded by a flag that has been fully rolled out must have the flag and its branch removed within one release cycle.

##### Reject if
- An abstraction is extracted after only two similar usages with no third confirmed callsite.
- An interface, base class, or extension hook is introduced with exactly one implementation and no immediate second use case (application service interfaces excepted, see architecture/service-interface-implementation.md).
- Commented-out code exists in the diff.
- A configuration parameter or strategy is added with only one possible value at the time of the PR.
- A function or class exists with no callers in the codebase.

#### Design Patterns

*Source: standards/principles/design-patterns.md*

Apply a pattern only when it eliminates a concrete, named problem you have right now - never to signal architecture maturity.

##### Pattern Selection
- Name the problem first; name the pattern second. If you cannot state the problem in one sentence, you do not need the pattern.
- YAGNI overrides GoF: do not abstract until you have two real, concrete callers in the current codebase - not hypothetical future ones.
- Prefer language idioms (context managers, closures, channels, decorators) over GoF ceremony when they solve the same problem with less indirection.
- Patterns must not obscure control flow; a new engineer must be able to trace a request path without IDE navigation.
- Document the chosen pattern and the reason in a module docstring or ADR; "we used Factory" without context is noise.

##### Repository
- Mandatory for all persistence-layer access inside domain or application services.
- Interface lives in the domain layer; the concrete ORM/SQL implementation lives in infrastructure.
- Expose domain-language query methods only - no raw query builders, no ORM session objects leaking across boundaries.
- One repository per aggregate root; never share a repository across two aggregates.
- Read models and projections may bypass Repository under CQRS; document the exception.

##### Strategy
- Use when the algorithm or backend is selected at runtime or by configuration, not by branching logic.
- All strategies must implement a shared interface; callers dispatch polymorphically - no isinstance or type-switch checks.
- Inject the chosen strategy; never let the consumer instantiate it.

##### Factory
- Use when construction is conditional on config, runtime input, or environment; not otherwise.
- If construction is a single constructor call with no branching, a factory is pure indirection - reject it.
- Abstract Factory is warranted only when an entire family of related objects must vary together.

##### Adapter / Anti-Corruption Layer
- Every external system (third-party API, vendor SDK, legacy service) is wrapped in an Adapter; the domain never imports the external package directly.
- The Adapter interface is defined by the domain's contract, not the vendor's API surface.
- One Adapter per external system; do not conflate multiple vendors in one class.
- Use an Anti-Corruption Layer (translation layer with its own model) when the external schema or semantics conflict with domain semantics.

##### Singleton
- Acceptable only for stateless shared clients: connection pools, SDK clients, config objects initialized once at startup.
- Must be injected through DI - never accessed via a global variable or module-level import side-effect inside business logic.
- Never use Singleton for objects that hold mutable domain state or are scoped per-request.

##### Dependency Injection
- Default wiring mechanism for all services, repositories, clients, and strategy instances.
- Inject via constructor; avoid property injection and service-locator lookups inside methods.
- DI containers are acceptable; magic auto-wiring that hides dependency graphs is not.
- Test doubles must be injectable without patching module globals.

##### Decorator / Middleware
- Use for cross-cutting concerns: logging, tracing, auth, rate-limiting, retry, caching.
- Decorators must be transparent to callers - same interface in, same interface out.
- Stack depth beyond 4 decorators on a single component signals the component has too many concerns.

##### Observer / Pub-Sub
- Use for decoupled event propagation where the producer must not know its consumers.
- Events must be immutable value objects; handlers must be idempotent.
- Async broker-based pub-sub requires explicit delivery-guarantee documentation; do not claim exactly-once delivery.
- Do not use Observer to avoid a direct dependency when a direct call is clearer.

##### Composition Over Inheritance
- Compose small, focused collaborators rather than extending base classes with shared behavior.
- Inheritance is reserved for true "is-a" relationships with stable contracts.
- Class hierarchies deeper than 2 levels (excluding the language root) require documented justification.
- Mixins are acceptable for narrow, stateless capability injection; mixins with state are a code smell.

##### Reject if
- A pattern is introduced with no articulable problem it solves in the current sprint.
- Singleton holds mutable domain state or is accessed via a global/module-level reference in business logic.
- A factory wraps construction that has no branching or conditional logic.
- Strategy implementations are selected via isinstance/type-switch instead of polymorphic dispatch.
- An Adapter or Repository leaks the external library's types into the domain layer.
- Class hierarchy depth exceeds 2 levels without explicit written justification.
- An interface has exactly one concrete implementation and no test-double usage (application service interfaces excepted, see architecture/service-interface-implementation.md).
- Observer handlers mutate shared state or are not idempotent.
- A decorator changes the interface of the wrapped object.
- Premature abstraction: an abstraction exists solely for a hypothetical future use-case with no current second caller.
- DI is bypassed via service-locator lookups or module-global singletons inside method bodies.
- Control flow through a pattern requires IDE navigation to trace a single request end-to-end.

#### Naming

*Source: standards/principles/naming.md*

Names are the primary documentation - every identifier must define its context and intent without a comment.

##### Casing Conventions
- Python: `snake_case` for variables/functions/modules, `PascalCase` for classes, `SCREAMING_SNAKE_CASE` for module-level constants.
- Go: `camelCase` for unexported, `PascalCase` for exported. Acronyms keep casing: `userID`, `HTTPClient`.
- Java/Kotlin/C#: `camelCase` for methods/variables, `PascalCase` for classes/interfaces, `UPPER_SNAKE_CASE` for constants.
- File names follow the dominant convention of the language (snake_case for Python, kebab-case for Go packages, PascalCase for Java/C# classes).

##### Precision & Specificity
- Names must encode both domain concept and role: `payment_retry_limit`, not `limit` or `n`.
- No single-letter variables except well-established loop counters (`i`, `j`, `k`) in tight numeric loops only.
- No abbreviations unless universally understood in the domain (`url`, `id`, `http`) - `usr`, `cfg`, `mgr` are rejected.
- Avoid noise words: `data`, `info`, `obj`, `temp`, `thing`, `item` add no information.

##### Booleans
- Boolean names must be predicates: `is_`, `has_`, `can_`, `should_`, `needs_` prefix required.
- Avoid negated booleans: `is_not_active` forces double-negation at call sites; use `is_active` and invert at the check.

##### Functions & Methods
- Name by verb + object: `fetch_user`, `validate_order_total`, `enqueue_notification`.
- Getters/accessors: use `get_` only if computation occurs; plain property access needs no prefix.
- Side-effecting functions must use strong verbs that signal mutation: `persist`, `delete`, `publish`, `update`.

##### Forbidden Grab-bags
- Classes/modules named `Manager`, `Util`, `Helper`, `Common`, `Misc`, `Utils` are rejected without exception.
- Such names signal missing decomposition; find the actual domain concept and name it.

##### Reject if
- Single-letter or unexplained abbreviation used outside a tight numeric loop.
- Boolean identifier missing `is_`/`has_`/`can_`/`should_`/`needs_` predicate prefix.
- Class or module named `Manager`, `Util`, `Helper`, `Common`, or `Misc`.
- Name requires an adjacent comment to convey its meaning.
- Casing violates the language convention defined above.

#### Readability

*Source: standards/principles/readability.md*

Code communicates intent to the next engineer - optimize for that, not for the author.

##### Function Length & Scope
- Hard cap: ~50 lines per function/method; if you need a scroll, split it.
- Each function does exactly one thing at one level of abstraction.
- Extract any block with a mental "step" into a named helper - name it by what it does, not how.
- Max nesting depth: 3 levels. Flatten with early returns and guard clauses.

##### Comments
- Comment WHY, never WHAT. The code already says what; the reader needs to know why.
- Business-rule rationale, legal constraints, non-obvious algorithm choices, and known gotchas warrant a comment.
- TODO/FIXME must include a ticket reference; bare TODOs are rejected.
- Never comment-out dead code; delete it. Version control is the history.

##### Naming Over Comments
- A well-named function/variable eliminates the need for its comment.
- Prefer positive boolean conditions and named predicates over inline expressions.
- If you feel compelled to add a clarifying inline comment, rename the symbol instead.

##### Abstraction Levels
- A function must operate at a single level of abstraction throughout its body.
- Do not mix high-level orchestration and low-level implementation in one function.
- Callers must not need to know implementation details to call a function correctly.

##### Complexity
- Cyclomatic complexity target: <=10 per function.
- Boolean expressions with more than 3 clauses must be extracted to a named predicate.
- Ternary chains (more than one nested ternary) are prohibited.

##### Reject if
- Any function exceeds ~50 lines excluding signature and blank lines.
- Nesting depth exceeds 3 levels (not counting class/function definition).
- Comment explains what the code does rather than why.
- Dead (commented-out) code exists in the diff.
- A function visibly operates at mixed abstraction levels.
- TODO/FIXME with no ticket reference.

#### Constants and Enums

*Source: standards/principles/constants-enums.md*

Eliminate magic numbers and strings; every named value lives in a typed, discoverable location.

##### No Magic Values

- Never embed numeric literals (timeouts, limits, status codes, retry counts) directly in logic; assign them to named constants.
- Never embed string literals used as identifiers, keys, or discriminators inline; use constants or enums.
- Domain-specific constants live in a `constants.py` module at the root of that domain package.

##### Cross-Domain Constants

- Constants shared across multiple domains live in `core/constants.py`; domain packages import from core, never from each other's constants modules.
- Do not duplicate a constant definition; a single authoritative source is required.

##### Enums for Closed Sets

- Use `StrEnum` (Python 3.11+) or `str, Enum` mixin for any closed set of string-valued discriminators (statuses, event types, permission names).
- Use `IntEnum` only when the integer value is part of a wire protocol; otherwise prefer `StrEnum`.
- Enum members must not be compared with raw string/integer literals; always compare against the enum type.
- Enums must not grow unbounded; if a "set" changes at runtime it is not a closed set and must not be an enum.

##### Error Messages and Codes

- All user-facing error messages and all error codes are constants; they are never constructed as f-strings at the raise site.
- Error codes are defined in `core/error_codes.py` or equivalent; no other module defines new codes.

##### Reject if

- A numeric or string literal appears inline in business logic, a condition, or a configuration value without a named constant.
- A new enum is added using plain `Enum` when `StrEnum` is available and the values are strings.
- A constant is defined in more than one module (duplication).
- An enum member is compared to a raw literal instead of the enum type.
- A user-facing error message is constructed as an inline f-string at the raise or return site.

#### Error Handling

*Source: standards/principles/error-handling.md*

Define a typed exception hierarchy, treat error codes as an immutable contract, and enforce single-boundary logging with zero internal leakage to clients.

##### Exception Hierarchy

- All application exceptions inherit from AppError; AppError carries: code (stable string), http_status (int), user_message, developer_detail, and a retryable flag.
- Domain sub-hierarchies (AuthError, ValidationError, IntegrationError, TimeoutError) extend AppError; never raise bare Exception or RuntimeError from application code.
- Third-party library exceptions are caught at the adapter layer and re-raised as typed AppError subclasses; they must not propagate into the service layer.
- Mark each subclass RETRYABLE (transient: network blip, DB connection loss, rate limit) or NON_RETRYABLE (permanent: validation, auth, not-found); retry policies rely on this flag.
- Timeout exceptions must resolve to a typed TimeoutError subclass with context (operation, duration, target); raw asyncio.TimeoutError or socket.timeout must never escape the adapter.

##### Error Codes as Contract

- Error code values are part of the public API; once published they are immutable and may only be deprecated with a sunset date, never renamed or deleted silently.
- Maintain a central error-code registry documenting each code, its meaning, retryability, and which endpoints emit it; new codes require a registry entry and changelog entry before merge.
- HTTP status derives from the exception type, never from ad-hoc integer literals scattered in route handlers.

##### Causal Chain Preservation

- Always chain exceptions: raise NewError(...) from original_exc - never discard the cause.
- Never catch-and-re-raise by constructing a new exception without passing the original as __cause__ or __context__.
- Wrapping a third-party error into a domain error must retain the upstream exception as cause, not just its message string.
- Preserve the original traceback across thread or async task boundaries by capturing exc_info at the point of origin.

##### Catch Discipline

- Every except block does exactly one of: handle completely, re-raise with wrapping, or log-and-re-raise.
- Never swallow: bare except: pass or except Exception: pass is always a reject.
- finally blocks must not suppress exceptions; return-in-finally is prohibited.
- Do not use exceptions for normal expected control flow (existence checks, pagination end, empty results); use sentinel values or typed result objects instead.

##### Fail Fast at Boundaries

- Validate all inputs (request params, config, env vars, message payloads) at the entry boundary before any domain logic executes.
- Fail loudly at startup if required config or dependencies are absent; don't defer config errors to runtime.
- Dependency health checks must raise typed errors, not return boolean flags that callers may silently ignore.

##### Partial Failures and Idempotency

- Batch or multi-step operations must distinguish partial-success from total failure; surface which items failed and why via structured error detail.
- Operations that mutate state must be idempotent or document the non-idempotency explicitly; partial-failure retries must not double-apply side effects.
- Compensating actions on failure must themselves be wrapped in error handling; a failed rollback must be logged as CRITICAL, not swallowed.

##### Centralized Error Handling

- A single boundary handler (middleware, error interceptor) maps domain errors to transport representation; route/handler code must not inline error formatting or status-code selection.
- Log once at the boundary with: error_code, retryable flag, correlation_id, upstream_cause type, and sanitized developer_detail; downstream layers propagate without re-logging.
- The boundary handler always strips developer_detail, stack traces, internal paths, SQL, and library names from the client response; only user_message and an opaque correlation ID may cross the boundary.
- Unhandled exceptions caught at the boundary are logged at CRITICAL and return a generic 500 with a stable opaque error code - never a raw traceback.
- RETRYABLE errors exceeding their retry budget escalate to ERROR; individual retry attempts log at WARN, not ERROR, to avoid alert fatigue.

##### Reject if

- A bare except: pass or silent exception swallow exists anywhere in the codebase.
- A third-party exception crosses the adapter boundary without being wrapped in an AppError subclass.
- An exception is re-raised without chaining (raise NewError(...) with no from clause when a cause exists).
- An error code string is changed, deleted, or introduced without a registry entry and changelog update.
- Stack traces, internal file paths, SQL fragments, or library names appear in the serialized API response body.
- An HTTP status is set via a raw integer literal outside the exception class definition.
- A route or handler formats its own error response instead of raising and deferring to the centralized handler.
- The same error is logged at more than one layer (double-logging).
- A timeout surface raises a generic OSError or asyncio.TimeoutError without wrapping in a typed TimeoutError subclass.
- A batch operation treats partial failure as total success and returns 200 with no failure detail.
- An exception is used as normal control flow for an expected, non-exceptional condition.
- A compensating/rollback action on failure silently swallows its own errors.

#### Null Handling

*Source: standards/principles/null-handling.md*

Services never use null/None as a control-flow signal; nullability is explicit in types, handled at every branch, and never allowed to propagate past the ingress boundary.

##### Absence Signaling

- Raise a typed domain exception (EntityNotFoundError, ResourceMissingError) when a lookup yields nothing; never return None.
- When Optional[T] or a Result/Either type is the correct return, annotate it explicitly; callers MUST handle both branches - exhaustive match or explicit guard. Ignoring the error branch of a Result is the same bug as ignoring None.
- Never rely on implicit None from functions that omit a return statement; async coroutines are especially prone to this - every code path must return explicitly.
- Distinguish three distinct states: absent (key never existed), null-value (explicitly set to null/None), and default (sentinel for unset); model each separately in the domain type.
- Never use None as an error sentinel; sentinel None collapses distinct failure modes into one, making upstream error handling ambiguous.

##### Boundary Validation

- Validate all external inputs (HTTP bodies, queue messages, gRPC payloads, DB rows) for None/missing fields at the ingress boundary before any inner-layer call.
- Use a schema validation library (Pydantic, marshmallow, protobuf validators) at every external boundary; scattered `if x is None` guards inside business logic are forbidden.
- Fail loudly with a typed validation error when a required field is absent; silent coercion of None to a default deep in a call chain is forbidden.
- Configuration and environment values must be validated at startup; a missing required env var must crash the process, not propagate None at runtime.
- Repository methods must return fully-hydrated, typed domain objects or raise; returning partially-hydrated ORM rows with nullable fields into service code is forbidden.

##### Type-Level Explicitness

- Annotate every nullable value with X | None (Python 3.10+) or the language-idiomatic nullable wrapper; absence of the annotation on a nullable field is a type error.
- Do not mark a field Optional if it is always present; overuse of Optional masks real design problems and weakens static analysis.
- Run a strict type checker (mypy --strict, pyright, or equivalent) in CI; null-safety violations must fail the build.
- Nullability must be reflected in the DB schema: a NOT NULL column maps to a non-optional field; a nullable column maps to X | None.

##### Chaining and Propagation

- Never chain attribute access or method calls on Optional-typed values without an explicit guard or early return prior to the chain.
- Long null-check ladders (more than two consecutive None guards on nested optionals) indicate a missing domain aggregate; introduce a typed aggregate instead.
- Avoid Optional parameters on internal service functions; prefer overloaded signatures, separate methods, or a discriminated union.
- Never use `def f(x=None)` to hide a branching default; if None means "use the global config" or "pick a default," that branch must be explicit at the call site.

##### Null-Object Pattern

- Apply the Null-Object pattern only when the caller legitimately treats "no entity" as a no-op; document the intent explicitly in the class docstring.
- Null-objects must implement the full collaborator interface; partial stubs that raise NotImplementedError on some methods are forbidden.
- Null-objects are for absent collaborators (optional dependencies, disabled features), not for absent data returned from queries.

##### Collections

- Return an empty collection, not None, when a query yields no results; callers never need to null-check a collection before iterating.
- Distinguish an empty result set from a missing aggregate: "no orders for user" is an empty list, "user not found" is an exception.

##### Database NULL Semantics

- Understand three-valued logic: NULL = NULL evaluates to UNKNOWN, not TRUE; comparisons against nullable columns must use IS NULL / IS NOT NULL, never =.
- Map nullable DB columns explicitly; ORM fields for nullable columns must carry the correct Optional type so the null case is visible to the type checker.
- Aggregate functions (SUM, AVG) return NULL on an empty set; handle with COALESCE or NULLIF in the query, not with None-checks after the fact in service code.

##### Object Initialization

- Construct objects fully or fail; factories and constructors must either produce a valid object or raise - partial construction followed by conditional post-assignment is forbidden.
- Mutable nullable fields set post-construction are a design smell; if deferred initialization is unavoidable, make the field an explicit Optional and enforce a guard before every use.

##### Reject if

- A service or repository method returns None to signal "not found" instead of raising a typed exception.
- An Optional or Result return type exists but the call site does not handle the None/error branch.
- Attribute chaining occurs on an Optional-typed value without a prior None check or early return.
- Inputs from external systems reach business logic without schema validation at the ingress boundary.
- None is used as an error sentinel, collapsing distinct failure modes into a single null return.
- A required configuration or environment value is allowed to be None at runtime rather than failing at startup.
- A nullable DB column maps to a non-optional field in the domain model, or three-valued DB NULL logic is ignored with equality comparisons.
- An Optional field annotation is used for a field that is always present, masking static analysis.
- A constructor leaves fields uninitialized (None) with post-construction assignment as the intended flow.
- A Null-Object implements only a subset of the collaborator interface, silently swallowing unimplemented calls.
- An empty-collection result and a missing-aggregate case are conflated by returning None for both.
- A function uses `def f(x=None)` to hide a branching default without making the branch explicit at the call site.

#### Logging

*Source: standards/principles/logging.md*

Emit structured, level-appropriate log records that are machine-parseable and safe to store; never use print().

##### Log Levels

- `DEBUG`: internal state useful only during development; disabled in production by default.
- `INFO`: normal operational events (service started, job completed, cache miss/hit counts).
- `WARNING`: recoverable anomalies or degraded operation that require investigation but do not fail the request.
- `ERROR`: unrecoverable failures within a request/job scope; always include the exception and stack trace.
- Never emit `ERROR` for expected business-logic conditions (e.g., not-found); those are `WARNING` or `INFO`.

##### Structured Logging

- All log records must be emitted in JSON format (or as structured key-value pairs) parseable by the log aggregator.
- Every record must include: `timestamp`, `level`, `logger`, `request_id`, `trace_id`, and `service_name`.
- Propagate `request_id` and `trace_id` via context variables (e.g., `contextvars.ContextVar`); never pass them as function arguments through every layer.
- Add domain-relevant fields (e.g., `user_id`, `entity_type`) as structured keys, not as interpolated strings in the message.

##### What Never to Log

- Secrets, API keys, tokens, passwords, private keys - in any field, at any level.
- PII: full names, email addresses, phone numbers, national ID numbers, payment card data.
- Full request/response bodies that may contain credentials or sensitive payloads.
- Internal file system paths, host names, or port numbers in records that may leave the network boundary.

##### Redaction & Sanitization

- Redact at emission, centrally: install a logging filter/formatter that scrubs secrets, tokens, credentials, and PII from every record - message string, exception text, and structured fields - before it is written. Do not rely on individual call sites remembering to scrub.
- Never log a raw exception that may carry a secret or PII. Log a typed error identity (`error_type` / `error_code`) plus a sanitized, length-bounded message; strip ARNs/account IDs, credential key=value pairs, emails, and ID patterns from that message.
- Code-level redaction is the PRIMARY control. Platform-level data protection (e.g. cloud log Data Protection policies - see `cloud/aws/observability-aws.md`) is a defense-in-depth BACKSTOP, not a replacement. Implement both; never omit code-level scrubbing on the assumption the platform will mask it.

##### Discipline

- Never use `print()` for operational output; it bypasses level filtering, formatting, and aggregation.
- Do not log the same error at multiple layers; log once at the point of final handling.
- Do not catch an exception solely to log it and then swallow it silently.

##### Reject if

- A `print()` call exists anywhere outside a CLI entry-point or a test fixture.
- A log record contains a password, token, secret, or PII field.
- `request_id` or `trace_id` is absent from any `ERROR` or `WARNING` record.
- An exception is caught, logged, and then silently swallowed.
- Log records are plain unstructured strings with no machine-parseable key-value structure.
- A raw exception object or message is logged without passing through a redaction step that can strip embedded secrets/PII (log `error_type`/`error_code` + sanitized message instead).
- Redaction depends solely on individual call sites remembering to scrub, with no central filter/formatter enforcing it.

#### Observability

*Source: standards/principles/observability.md*

Instrument every service so that logs, metrics, and traces are correlated, SLOs are owned, and any on-call engineer can diagnose a production incident without SSH access.

##### Three Pillars - Correlation Is Mandatory

- Every request must carry a W3C `traceparent` header from ingress to every downstream call.
- Extract or generate the trace/correlation ID at the service boundary; store it in request-scoped context (not function params).
- Every structured log record and every metric exemplar must embed `trace_id` and `span_id` so all three pillars are joinable.
- Emit observability signals - logs, metrics, spans - even on the error path; a failed request is the most important one to observe.

##### Metrics

- Apply the RED method to every service: request Rate, Error rate, Duration (p50/p95/p99 histograms, not averages).
- Apply the USE method to every resource: Utilisation, Saturation, Errors (CPU, memory, DB pool, queue depth, file descriptors).
- Emit business metrics (orders placed, payments processed, events enqueued) alongside technical metrics.
- Metrics must carry consistent labels: `service`, `version`, `environment`, `endpoint`, `status_code`.
- Use the OpenTelemetry SDK or canonical Prometheus client; never write ad-hoc exporters or push raw numbers via HTTP.
- Control label cardinality: never use unbounded values (entity IDs, user IDs, raw URLs) as label dimensions.
- Provide a per-service dashboard covering RED, USE, and business metrics; dashboards are owned by the team, not the platform.

##### Distributed Tracing

- Every inbound request must start or continue a trace; propagate `traceparent` (and `tracestate` if used) on all outbound HTTP, gRPC, and messaging calls.
- Instrument at boundaries: every outbound HTTP call, DB query, cache operation, and queue publish/consume must produce a child span.
- Spans must record operation name, entity IDs, and outcome; never record secrets, tokens, or PII in span attributes.
- Configure sampling explicitly per environment: production services under load must not default to 100% head-based sampling; use tail-based or adaptive sampling with a documented strategy stored in version-controlled config.

##### Structured Logs

- All log output must be JSON with consistent fields: `timestamp` (ISO-8601), `level`, `service`, `trace_id`, `span_id`, `message`, `error` (if present).
- Log at the correct level: ERROR for actionable failures, WARN for degraded operation, INFO for business events, DEBUG for development noise (off in prod).
- Never log secrets, tokens, passwords, PII, or full request/response bodies that contain sensitive data.
- Enrich log context at the middleware layer; downstream functions must not manually append trace IDs.

##### Health Endpoints

- Every service must expose `/health/live` (is the process alive?) and `/health/ready` (can it serve traffic?).
- Readiness must probe critical dependencies (primary DB, cache, required upstream); liveness must not - a failing dependency is not a reason to kill the pod.
- Both endpoints must be unauthenticated, idempotent, and respond within 500 ms; readiness failures must be distinguishable from liveness failures in dashboards.

##### SLIs, SLOs, and Alerting

- Every production-facing service must define at least one SLI (error ratio or latency percentile) with a documented SLO target and measurement window.
- Alert on symptoms (SLO burn rate, error budget consumption) not causes (CPU %, queue depth alone).
- Use multi-window, multi-burn-rate alerts (fast-burn >= 14x over 1 h, slow-burn >= 2x over 6 h) to avoid alert fatigue.
- Every alert must have a severity, an owner, and a runbook URL; alerts without runbooks must not be merged.
- Silence planned maintenance with explicit, time-bounded silences - do not disable alert rules.
- Tune or remove any paging alert that fires more than twice a week without a confirmed production impact.

##### Security and Audit Observability

- Emit an audit-log event for every security-relevant action: authentication, authorisation decisions, privilege escalation, data export, config change.
- Audit logs must be append-only, tamper-evident where possible, shipped to a separate sink, and must include: `actor`, `action`, `resource`, `outcome`, `timestamp`, `trace_id`.
- Never suppress or rate-limit audit log emission; it is a compliance boundary, not a debug stream.

##### Reject if

- A service ships with no `/health/ready` or `/health/live` endpoint.
- Outbound DB, cache, or HTTP calls have no tracing spans.
- `trace_id` is absent from any ERROR-level log record.
- Metric labels use unbounded cardinality dimensions (raw user IDs, entity GUIDs, unparameterised URLs).
- No SLO is defined or monitored for a production-facing service.
- Alerts fire only on resource thresholds (CPU, queue depth) with no SLO burn-rate alert present.
- Secrets, PII, tokens, or passwords appear in span attributes, metric label values, or log fields.
- Observability signals are suppressed or skipped on the error path.
- Health endpoints require authentication or liveness probes check external dependencies.
- An alert exists with no runbook URL in its annotations.
- Sampling strategy is hard-coded or undocumented; production defaults to 100% trace sampling.
- Business metrics are absent from a service that processes financially or operationally significant events.

#### Configuration

*Source: standards/principles/configuration.md*

Enforce strict separation of config from code; every service validates, types,
and centralises all configuration at startup before serving any traffic.

##### 12-Factor Compliance

- Values that vary per environment come from env vars or a secrets manager - never from committed files or literals.
- No environment-name conditionals (if-prod, if-staging) in application code; config values drive behavior.
- Committed files hold only non-secret, non-varying defaults (e.g. log format, safe flag defaults).
- Each service owns a distinct config namespace and performs its own independent startup validation.

##### Secrets Management

- Secrets (passwords, API keys, tokens, certificates) come exclusively from env vars or a secrets manager.
- Secrets are never committed in any form: no .env files, fixture files, CI variable dumps, or test helpers.
- .env files are local-dev only and must be in .gitignore; never committed.
- Secret fields must never carry defaults in the settings model - a default silently degrades to an insecure fallback.
- Business logic never touches the secret store directly; the config layer resolves and exposes values.

##### Typed Settings Object and Fail-Fast Validation

- Use a typed settings model (pydantic-settings BaseSettings or equivalent) as the single authoritative config source.
- Instantiate the settings object once at startup, before any network listener opens; lazy instantiation is forbidden.
- Missing required config raises a typed ConfigurationError at startup and halts the process immediately.
- Type-cast and range-validate every field: ports in [1, 65535], timeouts positive, URLs parseable, enums whitelisted.
- Boolean env vars are parsed by the settings model, never by ad-hoc string comparison at call sites.
- Optional fields carry explicit documented defaults; absence with no default is a required field, not an optional one.

##### Centralised Config Access

- Application code imports from the single settings instance; os.environ / os.getenv calls are forbidden elsewhere.
- The settings instance is injected (DI, function argument, framework context); no global mutable singleton.
- No config reads at import or module-load time; no side effects (network, I/O, DB) triggered during config load.

##### Immutability at Runtime

- Treat loaded config as immutable for the process lifetime; mutation after startup requires an explicit, logged reload hook.
- Config changes take effect via redeploy or a controlled reload signal - not via live os.environ mutation.
- Feature flag state that changes without a redeploy belongs in a flag store, not in env var sprawl.

##### Feature Flags

- Use a dedicated flag store (LaunchDarkly, Unleash, database-backed) for toggles that vary per user, tenant, or rollout.
- Env vars for feature flags are limited to deploy-time binary on/off switches only.
- Every flag documents owner, purpose, and sunset date; orphaned flags are dead code.

##### Documentation

- Every config variable is documented in the settings model: name, type, required/optional, default, one-line purpose.
- Documentation lives in the settings model, not a separate wiki that can drift.
- Deprecated vars are marked deprecated with a migration note, not silently ignored.

##### Reject if

- A secret or credential is committed to the repository in any file, including fixtures or CI configs.
- os.environ / os.getenv is called outside the designated settings module.
- A missing required config causes a KeyError, AttributeError, or None-dereference at runtime rather than a startup failure.
- A secret field carries a default value in the settings model.
- Environment name strings are used as conditionals in application logic.
- The settings object is instantiated lazily inside business logic rather than at startup.
- A field passes startup validation but fails at first use due to deferred parsing or an unchecked default.
- A feature-flag rollout is implemented as a proliferating set of env vars instead of a flag store.
- Prod credentials or endpoints appear in any dev, test, or seed configuration.
- A config variable has no documentation (name, type, purpose, default) in the settings model.

#### Concurrency

*Source: standards/principles/concurrency.md*

Write async code that never blocks the event loop; bound all parallel workloads; design every retried and concurrent operation for idempotency.

##### Async Discipline

- All I/O in an async service must use async-native clients; synchronous blocking calls in an async path are forbidden.
- Never call blocking functions (requests, psycopg2, synchronous file ops) from a coroutine.
- CPU-bound or unavoidably synchronous work must be offloaded to run_in_executor or a process pool; document the reason and add a TODO to replace it.
- Prefer asyncio.get_running_loop() over asyncio.get_event_loop() outside entry-point bootstrapping.
- No sleep-based synchronization; sleeping to wait for a state change is a race condition, not a synchronization primitive.

##### Bounded Concurrency

- All fan-out operations (gather, task groups, worker pools) must carry an explicit concurrency bound via a semaphore or bounded executor.
- Unbounded asyncio.gather over untrusted or variable-length input is forbidden; the bound must derive from capacity, not input cardinality.
- Background tasks spawned with create_task must be tracked and awaited or cancelled on shutdown; fire-and-forget for tasks that mutate state or produce I/O is forbidden.
- Worker pool sizes must be configurable at startup, not hardcoded.

##### Idempotency & Correctness

- Any operation that may be retried (queue consumers, webhooks, scheduled jobs) must be idempotent; duplicate execution must produce the same result without extra side-effects.
- Use idempotency keys for externally triggered operations; keys must be stored durably, not in memory.
- State transitions must use compare-and-set or conditional writes (optimistic locking) to prevent lost-update races.
- Atomic operations and compare-and-swap must be used for shared counters and flags; non-atomic read-modify-write sequences on shared state are forbidden.

##### Shared State & Locking

- Never share mutable state across coroutines or threads without explicit synchronization (lock, queue, atomic, or message-passing).
- Module-level mutable singletons written at runtime are forbidden; use dependency injection or context-local storage.
- Minimize lock scope: acquire only around the critical section, release before any I/O.
- Prefer immutable data structures and message-passing over shared mutable state wherever possible.
- Understand the distinction between races, deadlocks, and livelocks; document lock-ordering invariants wherever multiple locks are taken.

##### Client & Resource Thread-Safety

- Verify the thread-safety contract of every shared client (DB pools, HTTP sessions, SDK clients) before sharing across coroutines or threads.
- Connection pools must have explicit max-size limits; unbounded pool growth under load is forbidden.

##### Distributed Locks

- Use distributed locks only when coordination across processes cannot be achieved with idempotent writes or optimistic concurrency.
- Every distributed lock must carry a TTL to prevent indefinite holds on owner crash.
- Always use fencing tokens (monotonic version from the lock store) to guard writes protected by a distributed lock; assume the lock can expire under GC pause.

##### Cancellation, Timeouts & Backpressure

- All async operations must respect caller-supplied timeouts or cancellation tokens; swallowing CancelledError is forbidden.
- Propagate cancellation through the call stack; cancel child tasks when the parent is cancelled.
- Apply backpressure at ingestion boundaries (queue consumers, HTTP handlers, stream readers); unbounded in-memory queues fed by an external source are forbidden.
- Bound retry loops with exponential backoff and jitter; open-ended retry without a deadline or max-attempts cap is forbidden.

##### Graceful Shutdown

- On SIGTERM, stop accepting new work, drain in-flight requests and tasks within a bounded drain timeout, then exit.
- Tasks still running after the drain timeout must be cancelled and logged; resource cleanup must run in finally blocks or context manager exits.

##### Reject if

- A synchronous blocking call (sleep, requests.get, psycopg2 query) exists inside an async function or is called from the event loop thread.
- An asyncio.gather or similar fan-out has no semaphore or explicit concurrency cap.
- A retried consumer, webhook handler, or scheduled job is not idempotent.
- A background task is created with create_task with no reference kept and no cancellation on shutdown.
- Mutable module-level state is written from concurrent coroutines or threads without synchronization.
- A distributed lock is used without a TTL, or writes it guards are issued without a fencing token.
- CancelledError is caught and not re-raised, or cancellation is otherwise suppressed.
- A shared client is used across threads without verifying its thread-safety contract.
- Backpressure is absent at any external ingestion boundary, allowing unbounded in-memory queue growth.
- A retry loop has no deadline, max-attempts limit, or backoff.
- Graceful shutdown does not drain in-flight work before exit.
- Sleep is used as a substitute for a proper synchronization primitive (event, condition, queue).

#### Data Handling

*Source: standards/principles/data-handling.md*

Code-level robustness rules for any code that reads, transforms, or writes data records. These are coding rules, not pipeline design - they apply to any project, batch or online.

##### Idempotency of writes

- Make writes idempotent: upsert/merge keyed on a stable natural or business key - never insert-then-rescue-duplicate.
- Any operation that can be retried (queue consumer, scheduled job, API call) must be safe to run twice; dedupe by id or idempotency key.
- Assume at-least-once execution; never rely on a thing running exactly once.

##### Bounded memory & large data

- Never load an unbounded query result, file, or response fully into memory - stream/iterate (generators, server-side cursors, paged reads, chunked file I/O).
- Process in bounded batches; cap batch/page size; release each batch before fetching the next.
- Avoid building large intermediate lists/dicts; prefer lazy iterators over materialized collections.

##### Deterministic transforms

- Transformation/mapping functions are pure and deterministic: no clock, randomness, network call, or global mutable state read inside them.
- Inject time, UUIDs, and config as parameters so transforms are reproducible and unit-testable.
- Same input must always produce the same output, with no hidden side effects.

##### Types, precision, time, encoding

- Use Decimal / fixed-point for money and exact quantities - never binary float.
- Datetimes are timezone-aware and stored/compared in UTC; never use naive local time.
- Declare text encoding explicitly (UTF-8); never depend on the platform default encoding.
- Parse numbers and dates with explicit formats; fail loudly on ambiguous or unparseable input.

##### External record validation

- Validate and coerce every field of an external/inbound record at the boundary against an explicit schema before use.
- Handle missing, null, extra, and malformed fields explicitly - never assume a key exists.
- Reject or quarantine a bad record; one malformed record must not silently crash the whole batch or corrupt the target.

##### Resource lifecycle

- Acquire connections, files, cursors, and clients via context managers; release them on every path, including errors.
- Reuse expensive clients/connections - never open one per record.
- Bound concurrency and connection-pool size; close pools/handles on shutdown.

##### Bulk operations

- Use batch/bulk operations (executemany, COPY, bulk upsert, batch write APIs) - never write row-by-row in a loop.
- Cap batch size; handle partial-batch failures by reporting/retrying only the failed subset.

##### Partial failure & restartability

- Make multi-record operations restartable: commit progress in discrete units so a crash resumes without double-applying or losing data.
- A failure mid-batch must not leave the target half-written and inconsistent - use a transaction or an idempotent re-apply.

##### Reject if

- A write can create duplicates when retried (no idempotency/upsert key).
- Code loads an unbounded query, file, or response fully into memory.
- A transform reads the clock, randomness, or network internally.
- Money is stored as a float, or naive (non-UTC) datetimes are used.
- An external record's fields are accessed without validation or missing-key handling.
- Connections, files, or cursors are not closed on all paths (including errors).
- Records are written row-by-row where a bulk/batch operation applies.
- A mid-batch crash can double-apply records or leave the target inconsistent.
- Text is decoded/encoded without an explicit charset.

#### Dependency Management

*Source: standards/principles/dependency-mgmt.md*

Pin every dependency, minimise the supply-chain surface, isolate third-party code, and enforce compliance in CI - every rule below is a merge gate.

##### Version Pinning & Lockfiles

- Pin every direct dependency to an exact version; no bare ranges (`>=x`, `~=x`) in production manifests.
- Commit the lockfile (`poetry.lock`, pip-tools `.txt`, `package-lock.json`); CI must fail if it is stale relative to the manifest.
- Enable hash verification (`--require-hashes` for pip, built-in for Poetry/npm); builds without hash checks are not reproducible.
- Update dependencies in a dedicated PR, never as a side-effect of feature work.

##### Minimise the Dependency Surface

- Every new dependency requires a written PR justification: what it does, why stdlib is insufficient, and who maintains it.
- Prefer the standard library; add a package only when the gap is substantial.
- Audit the full transitive tree before merging - use `pipdeptree` / `npm ls --all`; packages that double transitive depth demand extra scrutiny.
- Remove unused packages before each release; run `pip-autoremove` / `depcheck` or equivalent.
- Prefer small, focused packages over monolithic SDK bundles when only a subset is needed.

##### CVE Auditing & Build Gates

- Run `pip-audit` / `safety` (Python) or `npm audit` / `trivy` on every CI run; CRITICAL findings fail the build with no exception (OWASP A06:2021).
- HIGH findings require a documented risk-acceptance comment and a remediation ticket before merge.
- Schedule a nightly audit job to catch CVEs disclosed after merge.
- Suppressions must be per-finding, time-bounded, and reviewed quarterly - never global.

##### Adapter / Anti-Corruption Layer

- Wrap all third-party clients, SDKs, and external calls behind a domain-owned interface (adapter/port).
- Application and domain code imports the adapter only - never the third-party module directly.
- Adapters own exception translation; third-party exceptions must not cross the adapter boundary.
- Swapping a library requires changes only inside the adapter; changes in service code indicate broken isolation.

##### Package Provenance & Integrity

- Install only from trusted registries (PyPI, npm public, or an internal mirror); never install from arbitrary URLs, GitHub refs, or local paths in production pipelines.
- Verify hashes at install time; halt the build if the published hash differs from the lockfile.
- Confirm canonical package names - typosquatting is a documented supply-chain vector.

##### Abandoned Packages & Dev/Prod Separation

- Packages with no release in 24+ months require explicit written justification; prefer packages with an active security policy and CVE response under 30 days; replace archived packages within one sprint.
- Declare test, lint, and build tools in a separate group (`[dev]`, `[test]`, extras); they must never appear in the production Docker layer, and CI prod stages install only the prod set.

##### SBOM & License Compliance

- Generate a CycloneDX or SPDX SBOM (including transitive deps) as a CI artifact on every release build.
- Run a license compliance check (`pip-licenses`, `license-checker`); GPL/AGPL/SSPL in a production dependency requires legal sign-off before merge.

##### Upgrade Strategy & Reproducible Builds

- Prefer frequent small bumps (patch -> minor) over deferred big-bang upgrades; automate via Dependabot or Renovate and merge/close PRs within two weeks.
- Pin base images and system packages in Dockerfiles (`apt-get install foo=1.2.3`); document the runtime version in `.tool-versions` or `pyproject.toml`.

##### Reject if

- A direct dependency is not pinned to an exact version in the committed lockfile.
- The lockfile is missing, uncommitted, or out of sync with the manifest.
- A CRITICAL CVE finding does not fail the CI build.
- A third-party client or SDK is called directly from service or domain code without an adapter layer.
- A new dependency has no written PR justification and a stdlib alternative exists.
- A package is installed from an arbitrary URL, GitHub ref, or unverified local path.
- Dev-only tools appear in the production dependency set or Docker runtime layer.
- A release build ships without a generated SBOM artifact.
- A GPL/AGPL/SSPL-licensed dependency is added without documented legal sign-off.
- Hash verification is disabled or suppressed in the install pipeline.
- A package with no release in 24+ months is added without explicit written justification.
- Audit suppressions are applied globally rather than per-finding with an expiry date.

#### Documentation

*Source: standards/principles/documentation.md*

Keep documentation authoritative, co-located with code, and limited to what a competent engineer cannot derive from reading the code alone.

##### Docstrings

- Every public function, method, and class must have a docstring stating its contract: what it does, parameters, return value, and which exceptions it raises.
- Docstrings describe the "what" and "why"; they do not restate the implementation line-by-line.
- Private helpers do not require docstrings unless the logic is non-obvious; use an inline comment for a single tricky line.
- Docstrings on async functions must note any side-effects, idempotency guarantees, and expected latency characteristics if relevant.

##### Module and Package READMEs

- Each top-level domain package must have a `README.md` describing its bounded context, primary responsibilities, and key public interfaces.
- READMEs live inside the package directory, not in the repository root.
- Operational runbooks for jobs, workers, or cron tasks belong in a `docs/runbooks/` directory and are updated in the same PR that changes the behavior.

##### ADRs (Architecture Decision Records)

- Every significant architectural decision (choice of queue system, data model design, caching strategy, async framework) must be recorded as an ADR in `docs/adr/`.
- ADRs are immutable once accepted; superseded ADRs are marked as such and linked to the replacement.
- An ADR must include: context, decision, consequences, and alternatives considered.

##### Error Code Reference

- Every error code a client may receive from a public API must be listed in `docs/error-codes.md` with: code string, HTTP status, cause, and resolution guidance.
- Error code documentation is updated in the same PR that introduces or changes the error.

##### Reject if

- A public function or class has no docstring.
- An ADR is missing for a new external dependency, data-store choice, or inter-service protocol.
- An error code is introduced without a corresponding entry in the error-code reference doc.
- Documentation lives in a separate repository or wiki page that is out of sync with the code version.
- A docstring restates the code line-by-line without adding contract or intent information.

#### Git Workflow

*Source: standards/principles/git-workflow.md*

Keep commits atomic and reviewable; enforce branch hygiene and secret hygiene through process and tooling.

##### Commits

- Each commit must represent exactly one logical change; do not mix refactors, features, and bug fixes in a single commit.
- Commit messages follow Conventional Commits: `<type>(<scope>): <imperative summary>` (e.g., `fix(auth): reject expired tokens before DB lookup`).
- The subject line is <=72 characters; the body explains "why", not "what".
- Do not amend or force-push commits that have been pushed to a shared branch.

##### Branches

- Branch names follow the pattern `<type>/<ticket-id>-<short-description>` (e.g., `feat/AUTH-42-refresh-token-rotation`).
- `main` and `release/*` branches are protected; direct pushes are forbidden.
- Delete merged branches promptly; stale branches older than 90 days must be cleaned up.
- Never commit directly to `main`; all changes enter via a reviewed PR.

##### Pull Requests

- PR scope must match a single ticket or a single coherent change; omnibus PRs spanning multiple concerns are forbidden.
- Every PR requires at least one approving review from an engineer who did not author the change before merge.
- PR descriptions must state: what changed, why, how to test, and any deployment considerations.
- Draft PRs must not be merged; remove draft status only when the PR is ready for final review.

##### Secrets and Safety

- Pre-commit hooks must include a secrets scanner (e.g., `gitleaks`, `detect-secrets`); commits that fail the scan are blocked.
- `.env` files, credential files, and key material must be in `.gitignore`; they must never be committed.
- If a secret is accidentally committed, treat it as compromised immediately: rotate before removing from history.

##### Reject if

- A commit mixes unrelated changes (feature + refactor, two unrelated bug fixes).
- A commit message does not follow Conventional Commits format.
- A PR is merged without at least one approving review.
- A secret, credential, or `.env` file is present in the commit diff.
- A direct push to `main` or a protected branch occurs outside an emergency procedure.

### 3.4 Python

#### Python Style

*Source: standards/languages/python/style.md*

Enforces Python-specific code style conventions for all backend code in this repository.

##### Formatting & Naming

- Follow PEP 8 strictly; line length 88 characters (Black default).
- Module and function names: `snake_case`. Class names: `PascalCase`. Constants: `UPPER_SNAKE_CASE`.
- All public function and method signatures must carry type hints; return type annotation is mandatory.
- Use `X | None` union syntax (Python 3.10+); never import or use `Optional[X]` from `typing`.
- Use `list[T]`, `dict[K, V]`, `tuple[T, ...]` builtins instead of `List`, `Dict`, `Tuple` from `typing`.

##### String Formatting

- Use f-strings for all string interpolation.
- Never use f-strings to construct SQL, shell commands, or any query string - use parameterised queries.
- Do not use `%`-formatting or `.format()` in new code.

##### Data Structures

- Use `dataclasses.dataclass` or Pydantic models for all structured data passed between layers; never raw `dict`.
- Mutable default arguments are forbidden; use `field(default_factory=...)` or `None` + guard.
- Do not use `TypedDict` where a dataclass or Pydantic model would serve - `TypedDict` is for external schema compatibility only.

##### Imports

- Group imports: stdlib, third-party, internal - separated by blank lines, sorted within each group.
- Do not use wildcard imports (`from x import *`) anywhere.

##### Reject if

- Any public function or method lacks a return type annotation.
- `Optional[X]` or `Union[X, None]` appears in new code instead of `X | None`.
- An f-string is used to build a SQL query, shell command, or URL with untrusted input.
- A mutable object (`[]`, `{}`, `set()`) is used as a function default argument.
- Raw `dict` is used as a function's primary data contract instead of a typed model.
- `%`-formatting or `.format()` is introduced in new code.

#### ASCII Source Code

*Source: standards/languages/python/ascii-source.md*

##### Rule

Production source code, identifiers, comments, docstrings, log messages, exception
messages, CLI output, configuration keys, and test names use plain ASCII characters.

Markdown documentation may quote a non-ASCII term when accuracy requires it, but any
code block intended to be copied into the codebase must be ASCII.

This rule governs source text only. User content and business data remain Unicode and
must be handled as UTF-8. Never strip, transliterate, or mangle user data to satisfy
this standard.

##### Prohibited in source

- Emojis and decorative symbols.
- Smart quotes and typographic dashes.
- Decorative arrows, check marks, and status glyphs.
- Non-ASCII whitespace characters.

Prohibited:

```python
logger.info("Document indexed \u2705")
```

Required:

```python
logger.info("Document indexed", extra={"document_id": document_id})
```

##### Enforcement

CI scans production source files and rejects non-ASCII bytes outside approved test data
or localization fixtures. Logging tests assert that operational message templates are
ASCII.

##### Reject if

- Production source contains an emoji or decorative Unicode character.
- A code template intended for copying contains smart punctuation.
- Unicode business data is altered merely to satisfy source style.

#### Pydantic

*Source: standards/languages/python/pydantic.md*

Governs Pydantic v2 usage for request validation, response shaping, settings, and all data contracts in backend services.

##### Model Segregation

- Define separate models per lifecycle role: `*CreateRequest`, `*UpdateRequest`, `*Response`. No single model serves both input and output.
- Response models are the only models passed to FastAPI `response_model`; never reuse request models as responses.
- Internal state, audit fields, and computed fields belong in domain/ORM objects, not in any response model.

##### Field Constraints

- Express all constraints declaratively via `Field`: `min_length`, `max_length`, `ge`, `le`, `gt`, `lt`, `pattern`, `default`.
- Reuse constraint sets via `Annotated` aliases (e.g., `PositiveInt`, `ShortStr`, `EmailStr`) - define once in a shared types module, import everywhere.
- Do not duplicate constraint logic across models; extract the `Annotated` type instead.

##### Validation

- Use `@field_validator` for single-field business rules; `@model_validator(mode='after')` for cross-field invariants.
- Never implement data-shape checks as `if` blocks inside route handlers or service functions - move them to validators.
- Validators must raise `ValueError` with a human-readable message; raising `HTTPException` inside a Pydantic validator is forbidden.
- Validate at construction time via `model_validate(data)` - do not defer validation or use raw constructor kwargs for untrusted external input.

##### Secrets & Sensitive Fields

- All passwords, tokens, API keys, and credentials must use `SecretStr`; plain values must never surface in `model_dump()` output.
- Response models must not include password hashes, internal secrets, session tokens, or audit-only identifiers; confirm exclusions in integration tests.

##### Configuration & ConfigDict

- Every model must declare `model_config = ConfigDict(...)` explicitly.
- `from_attributes=True` on any model hydrated from an ORM row; `extra='forbid'` on all request models (mass-assignment guard); `frozen=True` on value objects and domain events.
- Use `extra='ignore'` only on models consuming third-party payloads where the schema is not owned; document the reason inline.

##### ORM Integration

- Map ORM fields to API field names via `alias` or `serialization_alias`; set `ConfigDict(populate_by_name=True)` when both the alias and original field name must be accepted.
- Use `model_validate(orm_obj)` (not constructor kwargs) to hydrate a response model from an ORM instance.

##### Serialization

- Use `model_dump(mode='json')` when producing output destined for JSON serialization - this coerces datetime, UUID, and Decimal to JSON-safe types; plain `model_dump()` returns Python objects.
- Use `model_dump(exclude_unset=True)` in PATCH / partial-update handlers so only explicitly provided fields are written; omitting it overwrites existing data with `None`.

##### Settings

- All service configuration (env vars, secrets, feature flags) must be typed via a `pydantic-settings` `BaseSettings` subclass.
- Mark sensitive settings fields `SecretStr`; access the secret value only at the call site that needs it.
- Settings models must use `extra='forbid'` unless consuming a third-party config source.

##### v2 API Compliance

- Use `model_dump()` / `model_validate()` exclusively; `dict()`, `.json()`, `parse_obj()` are banned v1 methods. Do not mix `class Config` and `ConfigDict`.
- Do not use deprecated v1 decorators (`@validator`, `@root_validator`, `schema_extra`); use `@field_validator`, `@model_validator`, `json_schema_extra`.
- Avoid `arbitrary_types_allowed=True` unless integrating a genuinely non-serializable third-party type; add an inline comment explaining why.

##### Reject if

- A route handler or service function contains `if` checks that validate data shape instead of using field or model validators.
- A response model field exposes a password hash, secret token, session key, or internal-only identifier.
- `SecretStr` is absent on any field holding a credential, key, or secret value.
- `model_config` is missing or uses the deprecated v1 `class Config` style.
- The same Pydantic model is used as both request input and HTTP response output.
- Field constraints (`min_length`, `ge`, `pattern`, etc.) are enforced manually in application code instead of `Field(...)` or an `Annotated` type.
- A request model lacks `extra='forbid'`, leaving the endpoint open to mass-assignment.
- `dict()`, `.json()`, or `parse_obj()` (v1 methods) appear anywhere in the codebase.
- A `BaseSettings` subclass is absent; raw `os.environ` or `os.getenv` calls are used directly for typed config.
- `frozen=True` is absent on models that represent immutable value objects or domain events.
- A PATCH handler calls `model_dump()` without `exclude_unset=True`, risking silent field nullification.
- `arbitrary_types_allowed=True` is set without an inline comment justifying the non-serializable type.

#### Python Async

*Source: standards/languages/python/async.md*

Defines asyncio discipline rules to prevent blocking, resource leaks, cancellation bugs, and concurrency hazards in async backend services.

##### Async/Sync Boundaries

- Never call synchronous blocking I/O (file, network, subprocess, time.sleep) inside any `async def`, regardless of how fast it appears in testing.
- Offload blocking calls via `asyncio.get_running_loop().run_in_executor` or `anyio.to_thread.run_sync`; prefer anyio in library code.
- Never use synchronous ORM sessions (SQLAlchemy sync Session, Django ORM) in async routes, workers, or services - use async drivers exclusively.
- Do not mix sync and async code carelessly in the same call chain; the boundary must be explicit at every module edge.

##### HTTP Clients

- Use `httpx.AsyncClient` for all outbound HTTP in async code; `requests` is banned in async paths entirely.
- Instantiate `AsyncClient` as a long-lived singleton via DI/lifespan or as a context manager; never create one per request or per call.
- Set an explicit `timeout` on every `AsyncClient` instance; no unbounded outbound calls.

##### Database

- Use async-capable drivers and session factories: `asyncpg`, `aiosqlite`, `sqlalchemy.ext.asyncio`, `motor`.
- Never share a session or connection across concurrent coroutines; acquire a fresh session per unit-of-work via dependency injection.
- Always release connections back to the pool; acquire via async context manager only.

##### Timeouts

- Every external `await` (HTTP, DB query, cache, queue, RPC) must be wrapped with `asyncio.timeout` (3.11+) or `asyncio.wait_for`; no naked external awaits.
- Set timeouts at the call site in addition to any client-level default; layered timeouts are required for multi-hop pipelines.

##### Bounded Concurrency

- Never fan-out with `asyncio.gather` over a user-controlled or unbounded iterable without an `asyncio.Semaphore` or `anyio.CapacityLimiter`.
- Semaphore limits must be configurable via settings - no hardcoded magic numbers.
- Prefer `asyncio.TaskGroup` (3.11+) over bare `create_task` fan-out; TaskGroup propagates exceptions and cancels siblings automatically.

##### Cancellation

- `CancelledError` must be re-raised; never swallow it with `except Exception` or `except BaseException` without immediate re-raise.
- Do not suppress `CancelledError` inside `finally` blocks; cleanup there must be fast and non-blocking.

##### Graceful Shutdown

- On SIGTERM/SIGINT, cancel all pending tasks and `await` them; do not let the process exit with live tasks.
- Drain queues and flush buffers before cancelling consumers; shutdown ordering is contractual.
- Use lifespan context managers (FastAPI/Starlette lifespan or explicit hooks) - never `atexit`.

##### Resource Management

- All async resources (clients, connections, streams) must be closed via `async with` or explicit `await resource.aclose()`; never rely on GC.
- Coroutines yield at every `await`; any mutation of shared state visible across an `await` suspension point requires an `asyncio.Lock` - do not assume single-threaded execution makes shared state safe.
- Never use `threading.Lock` in async code; use `asyncio.Lock`, `asyncio.Queue`, or immutable structures.
- Never offload CPU-heavy work (ML inference, crypto, image processing) onto the event loop thread; use `run_in_executor` with a `ProcessPoolExecutor`.

##### Event Loop

- One event loop per process; never call `asyncio.run()` or `loop.run_until_complete()` inside library code or within an already-running loop.
- Do not store loop references in module globals; call `asyncio.get_running_loop()` when a handle is required.
- Never fire-and-forget with `create_task` without storing the Task reference and awaiting or cancelling it on shutdown.

##### Reject if

- Any `async def` contains a synchronous blocking call without thread-pool offload.
- `requests` or any sync HTTP client is imported or called in an async path.
- `asyncio.gather` fans out over an unbounded or user-controlled iterable without a semaphore or capacity limiter.
- `CancelledError` is caught and not re-raised.
- A sync ORM session or sync DB driver is used inside an async route, service, or worker.
- A new `httpx.AsyncClient` is instantiated per-request instead of reused.
- Any external `await` (HTTP, DB, cache, RPC) has no timeout guard.
- `create_task` is called fire-and-forget without storing and awaiting the Task on shutdown.
- Shared mutable state is accessed across coroutines without an `asyncio.Lock`.
- `asyncio.run()` or `loop.run_until_complete()` is called inside library code or from within a running event loop.
- Graceful shutdown omits cancelling and awaiting all pending tasks before process exit.

#### Packaging

*Source: standards/languages/python/packaging.md*

Governs project layout, dependency management, and import hygiene for Python backend packages.

##### Dependencies

- Pin all direct dependencies to a minimum version with an upper bound that excludes the next breaking major (`>=x.y,<x+1`).
- Lock transitive dependencies using a lock file (`uv.lock`, `poetry.lock`, or `pip-compile`-generated `requirements.lock`); commit the lock file.
- Separate dependency groups: `[project.dependencies]` for runtime, `[project.optional-dependencies]` for dev/test/lint; never install dev tools into production images.
- Do not add a dependency for a single utility function available in the stdlib.

##### Imports

- Use absolute imports everywhere within the package; relative imports (`from . import x`) are permitted only inside sub-packages where the relative target is unambiguous and stable.
- Never use `sys.path` manipulation at import time to resolve package paths; fix the install instead.
- `__init__.py` files must be kept thin: re-export only the public API surface; no business logic, no I/O, no heavy imports that trigger side effects.

##### Import-Time Side Effects

- Module-level code must not open network connections, read files, start threads, or perform I/O.
- Global singletons (DB engines, HTTP clients) must be initialised in explicit startup hooks (lifespan, `__main__`, or factory functions), not at module import time.
- Configuration must be read inside functions or dependency providers, not at the top-level of a module.

##### Reject if

- Importable package code exists outside `src/<package_name>/` (flat layout without `src/`).
- `setup.py` or `setup.cfg` is introduced in a new package instead of `pyproject.toml`.
- A direct dependency is unpinned (no version constraint at all).
- Module-level code performs I/O, opens a network connection, or starts a thread.
- `sys.path` is mutated at import time.
- `__init__.py` contains business logic or triggers heavy initialisation on import.

### 3.5 API

#### REST API Design

*Source: standards/api/rest-design.md*

##### Resource design

- Use plural nouns for collections and stable identifiers for individual resources.
- Keep routers thin: validate transport input, invoke one application use case, map the
  typed result. No business rules, no repository calls, no external clients.
- Never expose persistence, vector-store, or provider models in a public response.
- Use HTTP methods according to their semantics; make retryable mutations idempotent.
- Use a consistent URI major-version prefix.

##### Status and envelopes

- Success and error shapes follow `api/contracts.md`.
- Document every status and stable error code in the OpenAPI specification.
- Return 202 with a status resource for accepted asynchronous work.
- Return 409 for an in-flight idempotent duplicate and 422 for key reuse with a
  different payload.
- DELETE is idempotent and returns 200 or 204 when the resource is already gone.

##### Limits and security

- Validate authentication and request shape before invoking the use case.
- Authorization is enforced in the application service, not only in the router.
- Enforce request size, collection size, page size, cost, timeout, and concurrency limits.
- Never place secrets, tokens, or unrestricted personal data in paths or query strings.

##### Reject if

- A router contains business rules or calls a repository or external client directly.
- A retryable mutation has no idempotency design.
- A collection response has no pagination or explicit size bound.
- The OpenAPI specification omits a status or error code the implementation can return.
- An internal exception reaches the client unmapped.

#### API Contracts

*Source: standards/api/contracts.md*

Enforced shape, error, idempotency, and evolution rules for all backend API surfaces.

##### Error Envelope

- Every error response MUST be { error: { code, message, details?, trace_id } } - no exceptions.
- code is a stable snake_case constant; never a sentence, never repurposed or renamed once published.
- message is human-readable prose; clients MUST NOT branch on its text - it is not a contract.
- details carries structured field-level context (field, reason); omit the key entirely when absent.
- trace_id is the distributed trace root ID; always populate it, including on unhandled 500s.
- HTTP status and error.code MUST agree semantically; a 404 must never carry validation_failed.

##### Success Shape

- Single-resource: { data: object }. Collection: { data: array, meta: { ... } }. No naked arrays/objects.
- meta is mandatory on every collection even if empty; never omit it.
- Boolean fields are booleans only - never "true"/"false" strings or 0/1 integers.
- All identifiers are strings in responses regardless of storage type.

##### Pagination Metadata

- meta MUST include has_more (boolean) and exactly one paging token: cursor (preferred) or next_offset.
- Prefer cursor/keyset; offset pagination is unreliable on large mutating datasets (rows shift). Document the tradeoff if offset is chosen.
- total_count: include when cheap; if omitted, document that explicitly - never guess.
- Cursor values are opaque; page size default and hard cap must be documented.

##### Request & Correlation IDs

- Echo inbound X-Request-ID unchanged in every response header; generate a UUID v4 if absent.
- trace_id equals the distributed trace span root - not the request ID.
- Write correlation IDs to every log line; forward X-Request-ID and B3/traceparent downstream.

##### Idempotency

- POST, PUT, and PATCH endpoints that are retryable MUST accept an Idempotency-Key header.
- DELETE MUST return 200/204 (not 404) when the resource was already deleted.
- Store full response (status + body) keyed by Idempotency-Key for at least 24 hours.
- Duplicate key + same payload: return stored response verbatim. Different payload: 422 idempotency_conflict.
- In-flight race duplicate: return 409 request_in_progress - never a partial result.
- Document retention window and conflict behavior in every endpoint spec.

##### Types, Units & Formats

- Date-time: ISO 8601 UTC with Z suffix - never Unix timestamps in public contracts.
- Currency: integers in smallest denomination (cents/pence) - never floating-point.
- Durations: ISO 8601 (PT30S) or integer + explicit unit field - never ambiguous bare numbers.
- Enum fields: all valid values documented; adding values is additive-safe; removing is breaking.
- Every field MUST be explicitly marked nullable or required in the schema; ambiguity is a defect.

##### Content Types & Status Codes

- Set Content-Type: application/json on every JSON response - never rely on silent framework defaults.
- Require Content-Type: application/json on request bodies; reject wrong/missing types with 415.
- Accept negotiation failures return 406. Document every HTTP status code per endpoint in the spec.
- 204 carries no body; 200 always carries the defined envelope - never swap them situationally.

##### Backward-Compatible Evolution

- Field additions are additive and need no version bump; clients MUST tolerate unknown fields.
- Never remove, rename, or change the type/semantics of a field within a version.
- Deprecate fields with a documented sunset date and Deprecation response header before removal.
- New enum values are additive; clients MUST handle unknown enum values gracefully.
- Breaking changes require a new version prefix (/v2/); old version lives through stated sunset.

##### Reject if

- Error body is missing code, message, or trace_id, or deviates from the standard envelope.
- An error code has been renamed, repurposed, or deleted without introducing a new code value.
- A retryable POST/PUT/PATCH lacks Idempotency-Key support, or DELETE returns 404 on repeat.
- An in-flight duplicate Idempotency-Key returns a partial result instead of 409 request_in_progress.
- X-Request-ID is absent from any response header or omitted from log lines.
- A top-level response is a naked array or naked object outside the { data } wrapper.
- A date-time field uses Unix timestamps or a non-UTC ISO 8601 value in a public contract.
- Offset pagination is used on a large dataset without documenting the row-shift consistency tradeoff.
- A field is removed, renamed, or its type changed within an existing API version.
- Nullability of any schema field is unspecified or left ambiguous.
- An endpoint spec omits any HTTP status code or error code a client may receive.

#### OpenAPI Specification

*Source: standards/api/openapi.md*

Schema-first discipline, spec completeness, and code/spec synchronization requirements.

##### Schema-First Authorship

- Write the OpenAPI spec before implementing any endpoint; the spec is the source of truth, not documentation after the fact.
- All request bodies, path parameters, query parameters, and response bodies MUST have a schema entry.
- Use $ref to shared components/schemas; never inline duplicate schema definitions across endpoints.
- Enums, formats, and constraints (minLength, minimum, pattern) MUST be declared in the schema, not only in prose descriptions.
- Do not use additionalProperties: true on any schema in a public contract; lock the shape explicitly.

##### Error and Status Code Documentation

- Every endpoint MUST document all HTTP status codes it can return, including all 4xx and 5xx responses it emits.
- Each documented error status MUST reference the standard error envelope schema from components/schemas.
- Do not list only happy-path 200/201 responses and omit error codes; incomplete status documentation is a spec violation.
- Error codes (the machine-readable string constants) MUST be enumerated in the error schema or in the description.

##### Spec/Code Synchronization

- The OpenAPI spec file lives in the repository alongside the service code; it is versioned with the service.
- CI MUST lint the spec (e.g., spectral) and fail the build on any rule violation.
- CI MUST run a contract test or snapshot diff to detect drift between the live service and the spec.
- Any endpoint added, modified, or removed in code requires a matching spec update in the same PR.
- Generated client SDKs or server stubs derived from the spec MUST be regenerated and committed in the same PR as spec changes.

##### Request and Response Validation

- All incoming requests MUST be validated against the OpenAPI request schema at the framework or middleware layer.
- Reject non-conforming requests with 400 before they reach business logic.
- Response bodies MUST be validated against the OpenAPI response schema in test environments; log violations in production.
- Do not rely solely on manual testing to verify contract conformance; automated schema validation is required.

##### Spec Quality

- Every endpoint MUST have a summary and, for non-trivial operations, a description.
- Every path parameter and query parameter MUST have a description.
- Tag all endpoints; tags map to logical resource groupings, not to implementation modules.
- Mark deprecated endpoints and fields with deprecated: true and include x-sunset-date.
- The info.version field MUST match the deployed API major version.

##### Reject if

- An endpoint exists in the codebase with no corresponding entry in the OpenAPI spec.
- A documented endpoint omits any HTTP status code the implementation can actually return.
- The CI pipeline does not lint the spec and fail on violations.
- Request validation against the schema is absent or bypassed in the request handling path.
- Shared schemas are duplicated inline rather than defined once in components/schemas.

#### API Versioning

*Source: standards/api/versioning.md*

Rules for versioning strategy, SemVer contracts, deprecation lifecycle, and compatibility obligations.

##### Version Strategy

- Pick ONE strategy per API surface and never deviate: URI path (/v1, /v2) for all externally-facing APIs; Accept/Content-Type header versioning only when the URL surface is frozen and all consumers are internal.
- Never mix URI and header versioning within the same API boundary.
- Version the entire API surface under one prefix; do not version individual endpoints independently.
- The canonical major version MUST be the first path segment; /v2/... not /api/users/v2/....
- Document the current version in the OpenAPI spec root info.version using SemVer (MAJOR.MINOR.PATCH).

##### SemVer Contract Rules

- MAJOR: any breaking change - bump and publish a new route prefix.
- MINOR: additive, backward-compatible changes (new optional fields, new endpoints, new optional params) - no route bump required, update spec, log in changelog.
- PATCH: backward-compatible bug fixes and security hotfixes only - document explicitly; never silently alter behavior.
- A published contract is a legal commitment; no silent mutations regardless of apparent client tolerance.
- Additive-only changes MUST NOT bump MAJOR; overcautious versioning creates sprawl.

##### Backward Compatibility

- Adding optional response fields is non-breaking; adding required request fields is always breaking.
- Removing or renaming any endpoint, field, or path parameter is always breaking.
- Changing a field's type, format, optionality, or nullability is always breaking, even if current clients appear to tolerate it.
- Introducing a new enum value sent to clients is breaking unless the contract mandates unknown-value handling.
- Making an existing optional query param required is breaking.
- Never change the semantic meaning of an existing field - deprecate the old field, introduce a new one.
- Hotfixes to a published version are permitted only for security vulnerabilities; document the change in the changelog and notify consumers.

##### Deprecation Policy

- A deprecated version or field MUST remain fully functional for a minimum of 12 months after the deprecation notice is issued.
- Every response from a deprecated endpoint MUST carry both Deprecation (RFC 9745) and Sunset (RFC 8594) headers.
- The Sunset header value MUST be an RFC 7231 HTTP-date; do not retire before that date under any circumstances.
- Flag deprecated fields in the OpenAPI spec with deprecated: true plus an x-sunset-date vendor extension.
- Publish deprecation notices in the API changelog before setting Sunset headers in production; notify all known consumer teams directly.
- The migration guide MUST be live and publicly accessible before the Sunset header is set in any environment; a link is mandatory in the deprecation changelog entry.
- Never sunset a version without at least one stable successor being generally available and documented.

##### Version Lifecycle & Transition

- Run old and new major versions concurrently for the full deprecation window; a hard cutover with no overlap is prohibited.
- Publish the supported-version window (current and supported major versions with their sunset dates) in the developer docs and keep it current; consumers must be able to plan migrations without requesting this information.
- Retire versions on a published schedule to prevent version sprawl; default supported-version window is two concurrent majors.
- Beyond two concurrent majors requires explicit architectural sign-off and a documented sunset plan for the oldest.
- Use feature flags for gradual rollout of new behavior within an existing major version; the flag MUST be removed before GA.
- Feature-flagged behavior that would be breaking if universally enabled constitutes a breaking change - treat it as MAJOR.

##### Contract Integrity & Tooling

- Run a backward-compatibility diff tool (oasdiff, openapi-diff, or equivalent) in CI against the previous published spec on every PR.
- Any breaking change detected by CI MUST block merge until the change is promoted to a new major version.
- Consumer-driven contract tests (Pact or equivalent) MUST pass in CI for every consumer registered against the current version.
- Adding a new consumer contract test requires no PR process; removing or relaxing one requires tech-lead approval.
- Maintain a versioned changelog (CHANGELOG.md or equivalent) with entries for every MAJOR, MINOR, and security PATCH release.
- The changelog entry MUST record: version, date, breaking/non-breaking classification, and link to migration guide if applicable.

##### Reject if

- A breaking change is merged without bumping the major version and opening a new route prefix.
- Sunset or Deprecation headers are absent on any response from a deprecated endpoint or field.
- CI does not enforce a backward-compatibility spec diff against the previous published version.
- A version is retired before its documented Sunset date.
- Individual endpoints carry version identifiers that differ from the API-wide major version.
- More than two concurrent major versions exist without documented sign-off and a sunset plan for the oldest.
- A migration guide is not live and publicly linked at the time Sunset headers are set in production.
- A feature flag that gates breaking behavior is shipped to production without being classified as a MAJOR change.
- The OpenAPI info.version field does not reflect the current SemVer of the deployed contract.
- Consumer-driven contract tests are absent or not run in CI for any registered consumer.
- The supported-version window (active majors and their sunset dates) is not published in developer documentation.

### 3.6 Data

#### Database Access Standards

*Source: standards/data/database.md*

Governs all database interaction: query construction, indexing, transactions, migrations, pooling, and operational patterns.

##### Query Construction

- Use parameterized queries or ORM-bound parameters exclusively - never build SQL or Cypher via f-strings, string concatenation, or format templates.
- Select only the columns the call site actually consumes; never SELECT * in application code.
- Qualify column names with table/alias when joining two or more tables.
- Set a query timeout on every externally-triggered query; never let an unbounded query starve the pool.
- Run EXPLAIN/ANALYZE on every new query in a migration or feature branch before merge; attach output to the PR.

##### Indexing

- Index every column or composite set that appears in a WHERE, JOIN ON, or ORDER BY hot path backed by real query plans.
- Avoid over-indexing: each index adds write-amplification; justify every index against actual access patterns, not hypothetical ones.
- Partial and covering indexes are preferred over wide composite indexes when selectivity allows.
- Drop unused indexes; they impose write overhead and mislead future reviewers.
- Never add an index to a large table without a concurrent/online build to avoid table locks.

##### N+1 Prevention

- Identify access patterns at design time; batch or eager-load related records intentionally.
- A loop that issues a query per iteration is a hard rejection - rewrite as a single batched query or join.
- Use dataloader-style batching in graph-layer or resolver code.
- Treat ORM lazy-loading as opt-in only; disable it by default on all relationships and enable explicitly only on paths that never traverse collections.

##### Transactions

- Wrap every multi-step write in a single transaction with an explicitly stated isolation level.
- Choose deliberately: read-committed for most OLTP; repeatable-read to prevent phantom reads on aggregates; serializable only when correctness demands it and you accept throughput cost.
- Keep transactions short-lived; never hold them open across network calls, external HTTP, or file I/O.
- Roll back explicitly on every error path; never swallow exceptions inside a transaction block.
- Flush before commit: in a multi-step write, flush/execute all statements so the database surfaces constraint, type, foreign-key, and serialization errors BEFORE you commit; commit only when the flush reports zero errors. On any error, roll back the entire transaction and raise a typed error - never commit a partial or half-applied batch. (ORM sessions: call `session.flush()` to force errors, then `session.commit()`; raw drivers: execute inside an explicit transaction so errors raise before `COMMIT` and the context rolls back.)
- Prefer optimistic locking (version/etag column check on UPDATE) for low-contention rows; use pessimistic locking (SELECT FOR UPDATE) only when contention is high and retries are unacceptable.

##### Connection Pooling

- Configure pool min-size, max-size, max-overflow, connection timeout, and recycle/max-age explicitly; never rely on driver defaults.
- Acquire connections from the pool; never open a raw connection per-request.
- Release connections in finally blocks or equivalent deterministic resource-manager constructs.
- Set pool_pre_ping or equivalent keep-alive to detect stale connections before use.
- Monitor pool wait time and saturation; alert before exhaustion, not after.

##### Migrations

- Every schema change lives in a numbered, sequentially ordered migration file - no ad-hoc DDL against any environment.
- Each migration must be reversible: provide a down path unless destructive irreversibility is explicitly documented and approved.
- Apply the expand-contract pattern for zero-downtime changes: expand (add nullable column or column with server-side default so old code still runs), deploy app, backfill, then contract (drop old structure) in a separate later migration.
- Never drop a column, table, or constraint in the same migration that the application code still references; destructive DDL requires a documented multi-step safe rollout plan.
- No migration reaches staging or production without code review identical to application code review.
- Never apply schema changes outside the migration system - no manual ALTER TABLE in prod, no ORM autogenerate without review.

##### Pagination

- Use keyset (cursor) pagination for large tables; never use OFFSET-based pagination beyond the first few thousand rows.
- Document the sort key and uniqueness guarantee for every keyset cursor.

##### Data Modeling

- Store all timestamps in UTC; reject any timestamp column without explicit timezone or UTC enforcement.
- Define soft-delete policy explicitly at the model layer: hard-delete with audit log, or soft-delete with deleted_at and filtered views - ad-hoc per-table mixing is rejected.
- Avoid storing derived or computed data unless derivation cost is measured and justified; prefer computing at read time or via a materialized view with an explicit refresh policy.

##### Reject if

- Any query is built by concatenating or interpolating user-supplied or runtime-computed strings into SQL or Cypher.
- A loop body issues a per-row database query (N+1 pattern).
- A multi-table write lacks explicit transaction boundaries with a stated isolation level.
- A multi-step write commits without first flushing to surface errors, so a mid-batch failure can leave a partial or half-applied batch persisted.
- A schema change is applied outside a versioned, reviewed migration.
- Pool configuration is absent or left to driver defaults.
- SELECT * appears in non-temporary, non-exploratory application code.
- A new index is added without EXPLAIN output confirming it is used by the target query plan.
- A large-table column drop or rename ships without an expand-contract rollout plan, or a new column is added non-nullable without a server-side default.
- A query has no timeout and can run unbounded under adversarial or degraded input.
- Timestamps are stored without UTC enforcement or an explicit timezone column constraint.
- Soft-delete behavior is inconsistent across models in the same service with no documented policy.
- OFFSET-based pagination is used on a table that can grow beyond a few thousand rows.

#### Caching Standards

*Source: standards/data/caching.md*

Governs cache pattern selection, key design, TTL discipline, invalidation correctness, stampede protection, security, and observability for backend services.

##### Pattern Selection

- Cache-aside (lazy population) is the default: read from cache, miss -> fetch source, populate, return.
- Write-through is permitted for latency-critical reads where stale-on-miss is unacceptable; document the justification inline.
- Write-behind (async flush) requires a durable queue, explicit failure handling, and acknowledged data-loss risk if the queue fails before flush; justify in the design doc.
- Stale-while-revalidate requires a background refresh job and a defined max-stale bound; do not leave max-stale unbounded.
- Choose one consistency model per data type; mixed models on the same key are bugs waiting to surface.
- Cache only idempotent read paths; never cache the result of a mutating operation.
- Cache at the right layer - application-level for business objects, CDN-level for static assets, DB query cache for hot aggregates; do not cache at every layer indiscriminately.
- The cache is never the system of record; the backing store is always authoritative.

##### TTLs

- Every cached entry carries an explicit TTL - no indefinite or zero TTL unless the entry is managed by tested event-driven invalidation.
- Apply jitter (+/-10-25 % of base TTL) to all TTLs on high-cardinality key sets to spread expiry load.
- Set TTLs proportional to data volatility and acceptable staleness; record the reasoning alongside the constant.
- Default-to-short when staleness impact is unclear; defaulting long shifts the burden to invalidation correctness.
- Negative-cache not-found results with a short TTL (seconds to low minutes) to prevent repeated source hammering on missing entities.

##### Invalidation

- Invalidate or update the cache entry synchronously - or via a reliable, idempotent async task - on every write path that mutates cached data.
- A missing invalidation on a write path is a correctness bug, not a performance concern; treat it as such in review.
- When multiple services can mutate the same data, assign invalidation ownership to one authoritative service; others signal via domain events, never mutate the cache directly.
- Invalidation events must be idempotent; duplicate delivery must not corrupt cache state.

##### Key Design

- Namespace all keys: service:version:entity:id - collisions across services or deploys are bugs.
- Include a schema-version segment; bump the version when the cached value structure changes - this is the migration strategy, not a cache flush.
- Keep keys deterministic and reproducible; never embed timestamps, request IDs, or random values as the primary discriminator.
- Bound total key-space cardinality for any key pattern; unbounded key spaces exhaust memory and destroy hit rates.

##### Stampede and Thundering-Herd Protection

- Use single-flight / mutex-based recomputation for any key with high expected concurrency on miss; only one goroutine/thread fetches, others wait.
- Alternatively, use probabilistic early expiry (XFetch or equivalent) to proactively refresh before expiry under load.
- Cold-start thundering herd (deploy, cache flush, restart) must be handled explicitly - warm the cache before cutting over traffic, or use a staggered rollout with read-through fallback.
- Background refresh jobs must have circuit breakers; a failing refresh must not cascade into source overload.

##### Security

- Never store plaintext PII, credentials, session secrets, or access tokens in a shared cache without field-level or transport encryption.
- Apply at-rest encryption or use a dedicated secrets store for any sensitive value; document the encryption mechanism.
- Scope cache ACLs so that one tenant cannot read another tenant's entries; key namespacing alone is insufficient - enforce at the cache layer.
- Do not deserialize cached data using pickle, Java native serialization, or any format that executes code on load; use JSON, MessagePack, or Protobuf.

##### Size, Eviction, and Observability

- Every cache instance must have a bounded max-memory limit and an explicit eviction policy (LRU, LFU, or TTL-based); unbounded caches are OOM incidents.
- Expose hit rate, miss rate, eviction rate, and latency as metrics; alert when hit rate drops below the established baseline for a cache tier.
- A hit rate below 50 % on a steady-state cache signals the caching strategy is wrong, not a tuning problem.
- Monitor key expiry clustering (TTL jitter failure) and eviction spikes (size bound too small) as separate alert conditions.

##### Reject if

- Any cache entry lacks an explicit TTL and has no tested, monitored event-driven invalidation path.
- Sensitive data (PII, credentials, tokens) is stored in a shared cache without documented encryption.
- Cache keys are not namespaced and schema-versioned.
- A write path mutates source data without invalidating or updating the corresponding cache entry.
- High-concurrency keys have no single-flight or mutex protection against simultaneous miss recomputation.
- Cold-start thundering herd is unaddressed - no cache warm-up, staggered rollout, or read-through fallback documented.
- Negative results (not-found) are not cached, causing repeated source queries for missing entities under load.
- Cache deserialization uses pickle, Java native serialization, or any execution-capable format on untrusted data.
- The cache has no max-memory bound or eviction policy configured.
- Hit rate, miss rate, and eviction rate are not instrumented and not alertable.
- The consistency model for a cached data type is undocumented or mixed across callers.

#### Messaging & Event-Driven Standards

*Source: standards/data/messaging.md*

Governs producer/consumer contracts, delivery guarantees, failure handling, ordering, and schema evolution for all message-based integrations.

##### Delivery Assumptions

- At-least-once delivery is the default; design every consumer to tolerate duplicates without exception.
- "Exactly-once" is a myth - achieve effectively-once via idempotent consumers, not broker magic.
- Document the delivery guarantee (at-least-once / at-most-once) for every topic/queue at its definition site.

##### Idempotent Consumers

- Every consumer deduplicates on message id (or explicit idempotency id) before mutating state; the same id processed twice must produce the same observable outcome as once.
- Persist processed ids in durable storage (DB row, Redis key) scoped per consumer group - in-memory sets do not survive restarts or horizontal scale-out.
- Dedupe-store TTL must exceed the broker's maximum redelivery / retention window by at least 2x.
- Run the idempotency check inside the same transaction or atomic operation as the state mutation wherever possible.
- Safe replay is a first-class requirement: replay a DLQ batch in staging and assert no double-effects.

##### Transactional Outbox

- Any publish triggered by a DB write uses the transactional outbox: write the event row in the same DB transaction as the business mutation, relay asynchronously.
- Publishing directly to the broker inside a DB transaction is prohibited - broker publish is not atomic with the DB commit.
- Monitor relay lag; alert when lag exceeds the topic SLO. Purge relayed rows on a schedule to prevent unbounded table growth.

##### Dead-Letter Queues, Retry & Poison Messages

- Every consumer has an explicit DLQ; never rely on broker defaults for failure routing.
- Configure a finite max-retry count with exponential back-off and jitter; unbounded retries are prohibited.
- After max retries, route to the DLQ automatically - never discard. Alert on DLQ depth > 0 within the SLO response window.
- DLQ contents are observable (metrics, alerts) with a documented remediation runbook; the redrive policy is tested for idempotency survival.
- A message failing deserialization or schema validation on first attempt is a poison message: route it to the DLQ immediately without retry, log raw payload and error; never block the consumer on a single poison message.

##### Ordering Guarantees

- Declare ordering requirements explicitly per topic/queue; default assumption is unordered.
- For per-key ordering, route all messages sharing a logical key to the same partition or shard - partition assignment must be deterministic from the key.
- Consumers on unordered queues must not rely on arrival order for correctness; prefer idempotency over ordering constraints.

##### Ack / Offset Discipline

- Ack (or commit offset) only after successful processing and durable side-effects are committed.
- Never pre-ack before processing completes; pre-acking silently converts at-least-once to at-most-once.
- On failure, nack or leave the offset uncommitted so the broker redelivers.

##### Prefetch, Concurrency & Backpressure

- Set an explicit prefetch/max-in-flight limit per consumer instance; unbounded prefetch causes memory pressure and retry storms.
- Apply backpressure upstream when the consumer is saturated - do not silently drop or infinite-buffer messages.
- Tune concurrency to downstream resource capacity (DB connections, rate-limited APIs).

##### Schema Versioning & Evolution

- All schemas are versioned; include a schema version field in every message envelope.
- Changes must be backward-compatible (new optional fields, no removed required fields); consumers must ignore unknown fields (forward-compatible).
- Breaking changes require a new versioned message type - in-place modification of a live schema is prohibited.
- Enforce compatibility via a schema registry or CI contract test before any producer ships.

##### Large Payloads & Claim-Check

- Messages exceeding the broker payload limit use the claim-check pattern: store in object storage, publish only the reference.
- The reference must include all metadata needed to fetch the object (bucket, key, TTL); the consumer fetches then deletes or TTL-expires the payload - do not leave orphaned objects.

##### Correlation & Causation IDs

- Every message carries a correlation-id (trace root) and causation-id (immediate parent message or request id).
- Propagate these ids from the triggering request or parent message - never generate new ids mid-chain; consumers log both on every processing attempt.

##### Producer / Consumer Decoupling

- Producers must not encode consumer-specific logic, routing hints, or consumer topology into the message.
- Producers must not subscribe to consumer ACK or reply topics - callback coupling re-introduces synchronous coupling via the broker.
- Topic/queue contracts are owned by the producing domain; consumer count and identities are invisible to the producer.

##### Reject if

- A consumer has no deduplication mechanism and processes duplicate messages non-idempotently.
- A message is published directly to the broker inside a DB transaction (outbox pattern violated).
- A consumer queue has no DLQ configured or the DLQ is not monitored and alerted.
- Retry count is unbounded or back-off is absent, risking retry storms.
- A poison message blocks the consumer rather than being isolated to the DLQ immediately.
- Ordering correctness is assumed on a topic not explicitly configured for ordered delivery.
- Schema changes break existing consumer deserialization without a versioned migration path.
- Offset/ack is committed before processing and side-effects are durably written.
- Dedupe-store TTL is shorter than the broker's maximum redelivery window.
- Large payloads are embedded in the message body instead of using the claim-check pattern.
- Correlation or causation ids are absent, making cross-service trace reconstruction impossible.
- A producer encodes consumer-specific routing, topology, or subscribes to consumer ACK topics.

### 3.7 Security

#### OWASP Pre-Merge Security Checklist

*Source: standards/security/owasp-checklist.md*

THE master pre-merge gate - reviewer runs this on every PR touching auth, data access, API surface, config, serialization, or dependencies.

##### OWASP Top 10 (2021)

###### A01 - Broken Access Control
- Every resource operation re-validates the authenticated principal owns or is permitted to access the specific object (BOLA/IDOR); never trust client-supplied ID alone.
- Enforce deny-by-default; missing permission check == block, not pass-through.
- Horizontal and vertical privilege escalation paths reviewed; see auth-authz.md.

###### A02 - Cryptographic Failures
- Passwords hashed with bcrypt/argon2/scrypt; MD5/SHA-1/DES/RC4/plain-text storage rejected unconditionally.
- Data in transit uses TLS 1.2 minimum (prefer TLS 1.3); no plaintext fallback or downgrade path; see headers-transport.md.
- Encryption keys managed outside the database; no hardcoded key material; see secrets.md.

###### A03 - Injection
- All DB queries use parameterized statements or ORM binding - zero string concatenation with user input; see injection.md.
- OS commands, LDAP queries, and XML/XPath calls reject or escape user-controlled values.
- Server-side template rendering never receives raw user input.

###### A04 - Insecure Design
- Threat model updated or confirmed current for new data flows or trust boundaries.
- Sensitive flows (password reset, MFA bypass, privilege change) have documented abuse-case mitigations.
- Business logic limits (transfer caps, enumeration guards) enforced server-side, not just UI; see rate-limiting.md.

###### A05 - Security Misconfiguration
- DEBUG mode and verbose stack traces disabled in production; see secrets.md.
- Default credentials, sample routes, and framework example endpoints removed.
- Security headers present and correct; directory listing disabled; server banners suppressed; see headers-transport.md.

###### A06 - Vulnerable & Outdated Components
- pip audit / npm audit / trivy (or equivalent) run; no critical/high findings unmitigated.
- Dependency versions pinned; no unpinned `latest` ranges in production manifests.
- Transitive dependencies reviewed when a direct dependency is changed.

###### A07 - Identification & Authentication Failures
- Session tokens cryptographically random, >=128 bits, rotated on privilege change, invalidated on logout.
- MFA enforced on admin and privileged flows; brute-force protection (rate limit + lockout) on all credential endpoints.
- Credential-stuffing mitigations in place (CAPTCHA, anomaly alerting); see rate-limiting.md.

###### A08 - Software & Data Integrity Failures
- Deserialization of untrusted data uses type-safe formats (JSON schema validated) or integrity-checked blobs; see ssrf-deserialization.md.
- CI/CD pipeline steps verified (signed artifacts, pinned action SHAs by hash not tag); no arbitrary code execution from PR-triggered workflows.
- Supply-chain integrity confirmed: direct dependencies pinned to verified hashes; dependency-confusion attack surface assessed for private package namespaces.

###### A09 - Security Logging & Monitoring Failures
- Auth failures, access-control denials, and input-validation failures logged with timestamp, principal, resource, and IP.
- No sensitive values (passwords, tokens, PII) in any log field; see secrets.md.
- Alerting exists for anomalous error rates, 429 spikes, and repeated auth failures.

###### A10 - SSRF
- All outbound requests with user-influenced URLs validated against an allow-list and post-DNS IP check; HTTP redirects not blindly followed (re-validate destination after each redirect); see ssrf-deserialization.md.
- Metadata IP ranges (169.254.169.254, fd00::/8, etc.) and RFC-1918 space blocked at the network call layer before socket open.

##### OWASP API Security Top 10 (2023)

- API1 BOLA - Object-level authorization re-checked on every handler; resource IDs use opaque tokens not sequential integers (OWASP API1).
- API2 Broken Auth - JWT/token validation (sig, exp, iss, aud) enforced on every protected route; bearer tokens never logged (OWASP API2).
- API3 BOPLA - Response serializers filter to only the fields the caller is authorized to receive; no wildcard include; see auth-authz.md (OWASP API3).
- API4 Unrestricted Resource Consumption - Pagination caps, payload size limits, and query depth/complexity limits enforced; see rate-limiting.md (OWASP API4).
- API5 BFLA - Function/endpoint-level roles verified independently of object ownership; admin-only routes protected by role middleware (OWASP API5).
- API6 Unrestricted Access to Sensitive Business Flows - High-value flows (bulk export, mass account creation, financial transactions) rate-limited and anomaly-monitored (OWASP API6).
- API7 SSRF - Same controls as A10 above; also applied to webhook registration endpoints (OWASP API7).
- API8 Misconfiguration - CORS origin allow-list restricted; no wildcard origin on credentialed requests; unused HTTP methods (TRACE, OPTIONS if not needed) explicitly disabled per route (OWASP API8).
- API9 Improper Inventory - New endpoints registered in API schema/catalog; deprecated endpoints removed or versioned, not silently abandoned (OWASP API9).
- API10 Unsafe Consumption - Third-party API responses treated as untrusted; schema-validated before use; timeouts and circuit breakers applied (OWASP API10).

##### References

- OWASP Top 10 (2021): https://owasp.org/www-project-top-ten/
- OWASP API Security Top 10 (2023): https://owasp.org/API-Security/editions/2023/en/0x00-header/
- OWASP Cheat Sheet Series: https://cheatsheetseries.owasp.org/
- OWASP Developer Guide: https://devguide.owasp.org/

##### Reject if

- This checklist was skipped entirely on a PR touching auth, data access, API surface, serialization, or secrets.
- Any checklist item is left unchecked without a documented exception and an approving security review.
- Any item is marked N/A without written justification in the PR description.
- Vulnerable or unpinned dependencies are merged without a documented mitigation or accepted-risk record.
- A new endpoint is added without updating the API schema/catalog (API9).
- Deserialization of untrusted input lacks schema validation or integrity verification (A08).
- Encryption uses a deprecated algorithm (MD5, SHA-1, DES, RC4) anywhere in the diff.
- Secret, credential, or PII appears in a log statement, error response, URL parameter, or committed file.
- An object-level access check is absent or delegated solely to the client (A01/API1).
- SSRF protections are missing on any endpoint that initiates outbound HTTP with user-supplied input, including redirect chains (A10/API7).

#### Authentication Standard

*Source: standards/security/auth.md*

Enforce cryptographically sound credential storage, token lifecycle controls, session hygiene, and enumeration-safe flows on every auth path. (OWASP A07, API2)

##### Password Storage

- Hash with argon2id (preferred), bcrypt (min cost 12), or scrypt; never MD5, SHA-1, SHA-256, any unsalted hash, or reversible encoding.
- Never store plaintext passwords, reversibly encrypted passwords, or password hints.
- Use the library's built-in per-user salt generation; never supply a static, global, or user-derived salt.
- Minimum password length 12 characters; impose no arbitrary composition rules (uppercase theater).
- Screen new and reset passwords against a breached-credentials list (e.g., HaveIBeenPwned) using the k-anonymity prefix API; reject matches with a generic policy message.

##### Password Reset & Account Recovery

- Reset tokens must be single-use, server-side, and expire within 15-60 minutes.
- Invalidate all existing reset tokens on successful password change.
- Never embed reset tokens in URL query parameters exposed to Referer headers; use POST-body or fragment delivery.
- Return identical responses for both found and not-found accounts (OWASP A07 enumeration prevention).
- Require re-authentication or a fresh MFA challenge before allowing email/phone changes.

##### Brute-Force & Credential-Stuffing Defense

- Rate-limit authentication endpoints per IP and per account independently (OWASP API4).
- Apply exponential back-off after 5 consecutive failures; enforce a hard lockout after 10 within a configurable window.
- Log and alert on anomalous patterns: high failure rate, distributed IPs against one account, unusual geolocation delta.
- Require CAPTCHA or proof-of-work challenges when per-IP failure threshold is breached.
- Never reveal which field (username vs. password) failed; return a single generic error with uniform HTTP status.

##### Account Enumeration Prevention

- All auth-related endpoints (login, register, reset, MFA) must return identical HTTP status codes and response bodies for success and failure paths.
- Normalize response timing using constant-sleep padding or a dummy KDF call on non-existent accounts.
- Never expose user existence through distinct error messages, redirects, or HTTP codes.

##### Multi-Factor Authentication

- Require TOTP or WebAuthn for admin, privileged, and high-risk operations; enforce at the service layer, not the API gateway or UI alone.
- Validate TOTP with a +/-1 step window maximum; mark each accepted code as used and reject replays within the same validity window (TOTP one-time-use).
- WebAuthn credentials must bind to origin and rpId; reject ceremonies where the origin or rpId does not match the server.
- Backup codes must be single-use, stored as hashed values, and revoked in bulk on MFA device re-enrollment.
- Never allow MFA bypass via support override without a break-glass audit trail.

##### JWT Hygiene

- Reject any token where alg is none, or where alg is not in a server-managed allow-list (OWASP API2).
- Prevent algorithm-confusion attacks: if the key is RS256/ES256, the verifier must never fall back to HS256.
- Validate iss, aud, exp, and nbf on every request; treat missing or mismatched claims as invalid.
- Never decode the payload before verifying the signature.
- Validate kid claims against a known key set; reject tokens with a kid absent from the JWKS endpoint or resolving to a user-controlled value (kid injection).
- Rotate signing keys via JWKS; support key-ID-based rollover without downtime.
- Access tokens: TTL <= 15 minutes for sensitive operations, <= 1 hour maximum.
- Implement refresh-token rotation: each use issues a new refresh token and immediately invalidates the prior one; store refresh tokens server-side (hashed); detect reuse as a compromise signal and revoke the entire family.

##### Session Management

- Rotate the session ID immediately after login and after any privilege elevation or role change (OWASP A07).
- Enforce both an idle timeout (<= 30 minutes inactivity) and an absolute session lifetime (<= 8 hours).
- Invalidate the session server-side on logout; a client-side cookie deletion alone is insufficient.
- Maintain a server-side token store (blocklist or allowlist); check on every authenticated request.
- Revoke all sessions on password change, MFA device change, or confirmed account compromise.

##### Secure Cookies

- Set HttpOnly, Secure, and SameSite=Strict (or Lax with documented cross-site justification) on all session and auth cookies.
- Prefer the __Host- prefix (enforces Secure, Path=/, no Domain attribute) to prevent subdomain override; use __Secure- at minimum.
- Persistent auth cookies must have an explicit Max-Age or Expires; session-only cookies (no expiry, closed on browser close) are acceptable when absolute server-side timeout is enforced.
- Never store JWTs or sensitive tokens in localStorage or sessionStorage for browser clients; HttpOnly cookies are mandatory.

##### Credential Transmission Safety

- Never transmit credentials, tokens, or secrets as URL path segments or query parameters (OWASP A02).
- All auth endpoints must be TLS-only; HSTS with a min-age of 1 year must be configured at the infrastructure level.
- Use constant-time comparison (e.g., hmac.compare, crypto.timingSafeEqual) for all credential, token, and HMAC equality checks; never use == or language-level string equality.
- Never log credentials, tokens, or secrets in any application, access, or audit log.

##### Reject if

- Password is stored as plaintext, MD5, SHA-1, SHA-256, or any reversible encoding.
- New passwords are not screened against a breached-credentials list.
- JWT accepted without signature verification, alg=none permitted, or algorithm-confusion path exists.
- Access tokens have a lifetime exceeding 1 hour without documented and reviewed justification.
- Session ID is not rotated on login or privilege change.
- Session cookie is missing HttpOnly, Secure, or SameSite attributes, or lacks the __Host-/__Secure- prefix.
- Reset token is multi-use, lacks expiry, or is delivered in a URL query parameter.
- Auth endpoints return distinct responses (body, status, or timing) for found vs. not-found accounts.
- Credential or token equality check uses non-constant-time comparison.
- MFA is enforced only at the UI or gateway layer, not at the service/API handler level.
- TOTP codes are not marked used, allowing replay within the same validity window.
- kid, iss, aud, or nbf JWT claims are not validated server-side on every request.

#### Authorization

*Source: standards/security/authorization.md*

Enforce deny-by-default access control at every layer - object, function, field, and tenant - on every request, using server-side identity only. (OWASP A01, API1, API3, API5)

##### Deny by Default

- All access is denied unless an explicit policy grants it; absence of a matching rule is a deny, never a pass.
- New endpoints, actions, and resource types require documented authz rules before merging; ship no unguarded routes.
- Apply authz at the service layer on every request - gateway/middleware checks are defense-in-depth, not the primary control.
- Enforce authz on internal and service-to-service calls; authenticate service identities via mTLS or short-lived signed tokens.

##### Object-Level Access Control - BOLA / IDOR (OWASP API1)

- On every read, write, or delete, verify the authenticated caller owns or has explicit permission for the specific resource ID in the request before executing the operation.
- Fetch the resource from the datastore, then evaluate ownership or policy against server-resolved identity; do not trust caller-supplied owner fields.
- Never use sequential or predictable IDs as the sole access barrier; an ownership check is mandatory regardless of ID opacity.
- Prefer opaque, unguessable identifiers (UUIDs v4, ULID) to reduce enumeration surface - but never treat obscurity as a substitute for an ownership check.
- Re-verify ownership after any ID resolution, redirection, aliasing, or state-change step that may alter resource context.

##### Function/Endpoint-Level Access Control - BFLA (OWASP API5)

- Every endpoint must declare its required permission or role explicitly; default is no access.
- Administrative, privileged, and internal routes must be gated by a function-level permission distinct from standard user roles; a valid session is not sufficient.
- Enforce function-level checks at the service layer, not solely at the router; a service called via message queue or background job must re-evaluate the originating principal's permissions.
- Guard state-mutating operations (POST/PUT/PATCH/DELETE) with stricter permission requirements than reads; treat unauthenticated mutation as an immediate reject.

##### Property/Field-Level Access Control - BOPLA / Mass-Assignment (OWASP API3)

- Allowlist every field returned in a response to the caller's permitted view; do not serialize full ORM objects and rely on client suppression.
- Reject any write request that includes fields the caller's role may not set; do not silently drop unexpected fields - fail with 400 and log the attempt.
- Never bind request payloads directly to data models; use explicit DTO/schema mapping with strict input validation per role.
- Sensitive attributes (password hashes, PII beyond scope, internal flags, financial data) must be excluded from responses unless the caller holds explicit read permission for that field.

##### Multi-Tenant Isolation

- Scope every datastore query with a tenant predicate derived from the server-resolved tenant identity; never accept tenant ID from a client-supplied header or body field.
- Enforce tenant scoping at the repository/query layer, not only in application logic, so no bypass path exists via direct DB calls or batch jobs.
- Cross-tenant operations (support, analytics, migration) require an elevated, separately issued credential; log every cross-tenant access with full context.
- Validate that paginated and search results cannot leak records from other tenants through cursor manipulation or wildcard queries.

##### Centralized Policy - RBAC / ABAC / ReBAC

- Define all roles, permissions, and relationships in a central policy store (OPA, Casbin, Cedar, or equivalent); do not hardcode role strings or permission checks in business logic.
- Use ABAC or ReBAC when decisions depend on resource attributes or relationship graphs; do not approximate with coarse role checks.
- Never resolve permissions by loading all records and post-filtering in application code; push policy predicates into the datastore query.
- Ensure policy changes are versioned, reviewed, and auditable; treat policy as code with the same review gates.

##### Privilege Escalation Guards

- Horizontal escalation: confirm the caller's tenant/org context matches the resource's tenant before any operation - a valid token for tenant A must never access tenant B's resources.
- Vertical escalation: a principal may not elevate its own role, add itself to privileged groups, or modify its own permission attributes; these operations require a separate privileged principal.
- After any credential refresh, token exchange, or session elevation, re-evaluate authz from scratch; never serve a cached pre-elevation decision.
- Validate that bulk or batch endpoints apply per-item authz checks and do not short-circuit to a single top-level check.

##### Trust Boundaries

- Never trust client-supplied role claims, group memberships, owner IDs, tenant IDs, or `is_admin` flags; resolve every identity attribute server-side from the authoritative identity store.
- Strip and reject any request field that attempts to set authorization attributes (`role`, `permissions`, `owner_id`, `tenant_id`, `is_admin`, etc.); treat their presence in a write payload as a 400 error.
- Downstream services receiving forwarded requests must re-validate the caller's identity and permissions; do not inherit trust from an upstream service's assertion alone.

##### Audit Logging

- Log every authz decision that results in a denial: caller identity, resource type, resource ID, action, and timestamp; feed into SIEM.
- Log privileged operations (admin actions, cross-tenant access, role grants) at INFO level regardless of outcome; include all context needed for forensic reconstruction.
- Alert on anomalous authz failure rates per caller or IP (brute-force BOLA enumeration); treat spike as an incident signal.
- Do not log sensitive payload content in authz logs; log identifiers and metadata only.

##### Reject if

- An endpoint performs a resource operation without a verified per-object ownership or permission check on the specific resource ID (OWASP API1).
- Access is granted when no matching policy is found - i.e., deny-by-default is not implemented.
- Role, ownership, tenant ID, or any authz attribute is read from a client-controlled request field rather than the server-side identity store.
- Authz logic exists only at the gateway or middleware layer and is absent from the service or repository layer.
- A response serializes fields the caller's role is not permitted to read (over-fetch / BOPLA).
- A write endpoint binds the request body directly to a data model without field-level allowlisting (mass-assignment).
- Multi-tenant queries lack a server-resolved tenant predicate at the datastore layer.
- A new endpoint or action is merged without an explicit, documented authz policy covering it.
- Privileged or admin routes are gated only by authentication (valid session) rather than a specific elevated permission (OWASP API5).
- Authz denial events are not logged with caller identity, resource, and action.
- A bulk or batch endpoint applies a single top-level authz check rather than per-item checks.
- An authz decision made before a credential refresh, token exchange, or session state change is reused without re-evaluation.

#### Input Validation

*Source: standards/security/input-validation.md*

Validate, canonicalize, and constrain ALL input at every trust boundary before any processing, routing, or persistence.

##### Boundary Enforcement

- Validate every external input at the entry point: HTTP body, query params, path params, headers, cookies, message-queue payloads, webhook events, gRPC messages, and inter-service calls crossing a trust boundary.
- Use POSITIVE allow-lists (type, format, length, range, enum) - deny anything not explicitly permitted (OWASP A03).
- Reject requests with unknown or extra fields at deserialization; schema strict mode is mandatory, not optional.
- Validate Content-Type header against the actual parsed body format; reject mismatches with 415 before touching the body.
- Reject oversized payloads at the ingress layer (reverse proxy or framework middleware) before deserialization begins - never read a full body then discard it.
- Never trust client-supplied Content-Length, record counts, array lengths, or page-size hints; derive sizes server-side after parsing.

##### Canonicalization

- CANONICALIZE before validating, never after - validate-then-canonicalize ordering is exploitable via double-encoding and Unicode normalization attacks.
- Normalize Unicode to NFC before any string comparison, storage, or regex match.
- Resolve percent-encoding and collapse path-traversal sequences (../, ..\) before path validation.
- Reject inputs that cannot be cleanly canonicalized: malformed Unicode sequences, invalid percent-encoding, null bytes, overlong UTF-8 encodings.

##### Schema and Type Constraints

- Enforce strict types - integer, UUID, boolean, ISO-8601 datetime, enum - never coerce or cast ambiguous input implicitly.
- Apply maximum AND minimum length limits to every string field; reject oversized inputs with 400, never silently truncate.
- Apply inclusive min/max bounds to every numeric field; out-of-range values are rejected, not clamped silently.
- Validate all enumerated fields against a server-side allow-list; reject any value not in the set.
- Guard all integer arithmetic for overflow before computation - use type-safe checked arithmetic or explicit range guards (OWASP A03).

##### Regex Safety

- All server-side regexes that process user input MUST be reviewed for catastrophic backtracking (ReDoS); ban nested quantifiers on overlapping character classes.
- Enforce an explicit input-length cap BEFORE applying any regex; never run a regex over unbounded input.
- Prefer finite-state-machine parsers over regexes for structured formats (email, URL, date).
- Set regex engine timeouts where the runtime supports them; treat a timeout as a rejection, not an error.

##### Mass Assignment Defense

- Declare an explicit allow-list of writable fields per operation; create and update allow-lists may differ - enforce both separately.
- Never bind a raw deserialized object or map directly to a domain model, ORM entity, or event schema.
- Treat every field NOT in the allow-list as if it does not exist; log its presence at WARN, never surface it downstream.
- Audit all spread-object, `**kwargs`, `MapStruct`-style, and reflection-based binding patterns at every release.

##### Nested and Array Payloads

- Deep-validate every element of nested objects and arrays; validation applied only at the root level is insufficient.
- Enforce maximum array/collection element counts server-side; reject payloads exceeding limits before iterating.
- Enforce maximum nesting depth for recursive or deeply-nested schemas; reject beyond the limit to prevent stack-overflow amplification.
- Validate each element against the same type/format/length rules as a top-level scalar.

##### File Upload Validation

- Verify file content via magic-byte inspection; do NOT rely on the Content-Type header or file extension alone (OWASP A04).
- Enforce a server-side size cap per file and per request before reading bytes into memory.
- Store uploaded files outside the webroot; never derive the storage path from any user-supplied value.
- Generate server-side filenames (UUID or cryptographic hash); discard the client-supplied filename entirely after logging it for audit.
- Validate filename characters against a strict allow-list if the original name must be preserved for display; reject names containing null bytes or percent-encoded equivalents.
- Block path-traversal sequences and reject double-extension patterns (e.g., file.php.jpg) and null-byte injection (file.php%00.jpg) in any user-supplied filename before any filesystem operation.
- Scan or quarantine uploaded files before making them accessible to any other service or user.

##### Reject if

- Any input field reaches business logic, a query layer, or an event bus without schema validation.
- Unknown or extra request fields are silently accepted, ignored, or forwarded rather than rejected with 400.
- Canonicalization occurs after validation rather than before it.
- A string field has no enforced maximum length limit.
- A numeric field has no enforced min/max bounds or integer-overflow guard.
- A regex over user input has no input-length cap or has not been reviewed for catastrophic backtracking.
- A raw deserialized body or map is bound directly to a domain model without an explicit field allow-list.
- File uploads are accepted based on extension or Content-Type header without magic-byte verification.
- Uploaded files are stored inside the webroot or with a user-supplied filename or path component.
- Oversized payloads are fully read into memory before rejection rather than blocked at ingress.
- Nested arrays or objects are validated only at root depth, leaving child elements unvalidated.
- Client-supplied array lengths, counts, or sizes are trusted without server-side verification.

#### Injection Prevention (OWASP A03)

*Source: standards/security/injection.md*

Enforce hard separation of code and data at every query, command, and rendering boundary across all backends.

##### Query Construction - SQL / NoSQL / Cypher / LDAP / XPath

- Use parameterized/prepared statements for all SQL; never interpolate user input via `+`, `%`, f-string, or `.format()`.
- Use ORM parameter binding (`filter(col=value)`, named placeholders) as the only approved SQL generation path.
- Treat `.raw()`, `.extra()`, and `text()` (Django/SQLAlchemy equivalents) as review-gated; every call requires an explicit security-review comment referencing the ticket.
- For NoSQL (MongoDB, Redis, DynamoDB): use typed query builders and operator allow-lists; never deserialize user input directly into a query object (OWASP A03).
- For Cypher (Neo4j): use `$param` syntax exclusively; never build Cypher strings with f-strings or `.format()`.
- For LDAP: use parameterized APIs or the provider's official escape function before embedding any value in filter strings.
- For XPath: use parameterized XPath (XQuery variables) or a vetted library-level escaper; never concatenate user values into XPath expressions.
- Dynamic `ORDER BY` columns and `IN`-clause values must come from a server-side allow-list; user input selects an index into that list, never the SQL token directly.
- Stored-procedure dynamic SQL must pass the same parameterization review gate as ORM raw queries; dynamic `EXEC`/`sp_executesql` with unbound inputs is a block.

##### Second-Order / Stored Injection

- Treat data read back from persistent storage (DB, cache, queue, file) with the same distrust as direct user input when reused in a query or command context (OWASP A03).
- Document the trust boundary in code: a comment must declare when stored data is considered safe and why.
- Re-validate and re-parameterize data at every query boundary, not only at ingestion.

##### OS Command Injection

- Never construct shell command strings from user-controlled input; pass arguments as arrays/lists to subprocess, child_process, or the platform equivalent.
- `shell=True` (Python subprocess), `exec` with shell interpolation (Node.js), and POSIX `popen(string)` are banned when any argument component derives from a request.
- Prefer library/SDK APIs over shelling out; if shell execution is unavoidable, validate and allow-list every argument component before construction.
- Reject any PR that uses `os.system`, `Runtime.exec(String)`, or `shell: true` with concatenated input.

##### Template Injection / SSTI

- Never pass user-supplied content as the template source string to any engine (Jinja2, Pebble, Freemarker, Velocity, Twig, etc.).
- User data may only populate template *variables*; it must never control template *logic or structure*.
- Sandbox-mode alone is not a sufficient control; separation of template source from user data is mandatory.

##### XML External Entities (XXE)

- Disable DTD processing and external entity resolution on all XML parsers at construction time (OWASP A03/A05 overlap).
- Set `FEATURE_EXTERNAL_GENERAL_ENTITIES` and `FEATURE_EXTERNAL_PARAMETER_ENTITIES` to false for Java SAX/DOM; use `defusedxml` for Python; set `libxml2_disable_entity_loader` for PHP.
- Do not accept XML from untrusted sources unless the parser has entity expansion disabled and a maximum entity depth/size limit configured.

##### Log Injection / CRLF

- Strip or encode CR (`\r`), LF (`\n`), and null bytes from all user-controlled values before they appear in log output.
- Use a structured logger that accepts key-value arguments; never concatenate request data into log format strings.
- Validate `Referer`, `User-Agent`, and forwarded headers before logging them.

##### Header Injection

- Reject or strip CR/LF characters from any value written into HTTP response headers.
- Never reflect raw user input (path segments, query params, form fields) directly into `Location`, `Set-Cookie`, or custom headers.
- Use framework-provided header-setting APIs that perform value validation; avoid raw header string assembly.

##### Input Validation (Allow-List First)

- Apply allow-list validation - explicit type, format, length, and range checks - before input reaches any query, command, or rendering layer (OWASP A03).
- Deny-lists and stripping are secondary hardening only; they do not replace allow-list validation.
- Validate at the service boundary (controller/handler), not only inside repository/DAO layers.

##### Output Encoding (Secondary Defense)

- Apply context-appropriate encoding (HTML entity, XML, JSON serializer, URL percent-encoding) when input is reflected in structured output.
- Use established encoding libraries; never hand-roll encoders for security-sensitive contexts.
- Output encoding is defense-in-depth; it does not substitute for parameterization or allow-list validation.

##### Driver / ORM Dependency Management

- Pin ORM and DB driver versions in the lockfile; unpin is blocked without explicit justification.
- Run a CVE audit (`pip-audit`, `npm audit`, Dependabot) against all ORM/driver packages before merging version bumps.
- Treat any injection-related CVE in a pinned dependency as a P1 blocker; patch before the next release cut.

##### Reject if

- Any query string is assembled via `+`, `%`, f-string, `.format()`, or string template with user-supplied data.
- An ORM raw-query escape hatch (`.raw()`, `.extra()`, `text()`, `RawSQL(...)`) is used without a security-review comment and ticket reference.
- `shell=True`, `os.system`, `Runtime.exec(String)`, or equivalent shell-interpolation API is called with input derived from a request.
- LDAP, Cypher, XPath, or NoSQL query objects are hydrated directly from deserialized user payloads without field-level type and value validation.
- Data read from storage (DB row, cache entry, message body) is reused in a query or command context without re-parameterization and a documented trust-boundary comment.
- User input is passed as the template source string to any server-side templating engine.
- An XML parser is used without DTD and external entity resolution explicitly disabled at construction time.
- CR or LF characters from user input appear unencoded in log output or HTTP response headers.
- A dynamic `ORDER BY` column name or `IN`-clause token is derived from user input without mapping through a server-side allow-list.
- An ORM/driver dependency has an open injection-related CVE and the PR bumps or retains that version without a documented mitigation.
- Input reaches a query, command, or rendering layer before allow-list type and format validation is applied at the service boundary.

#### Secrets Management

*Source: standards/security/secrets.md*

Secrets never touch source control, logs, URLs, images, or user-visible output - ever (OWASP A02).

##### Storage & Injection

- Source secrets exclusively from a secret manager (HashiCorp Vault, AWS Secrets Manager, GCP Secret Manager, Azure Key Vault) or runtime-injected environment variables; no other mechanism is permitted.
- Never hard-code secrets in source files, config files, migration scripts, seed files, test fixtures, Dockerfiles, or image layers - including base images.
- `.env` files are gitignored; `.env.example` holds only placeholder values; no real credentials at any path tracked by git.
- Infrastructure-as-code templates reference secret manager ARNs or paths - never literal values.
- Secrets must not be baked into container images, mobile binaries, client-side bundles, or build artifacts of any kind.
- Never store secrets in browser localStorage, sessionStorage, or non-HttpOnly/non-Secure cookies; tokens placed in the browser must use HttpOnly Secure cookies scoped to the minimum path (OWASP A02).
- Encrypt secrets at rest inside the secret manager; verify the backing KMS key is service-managed or customer-managed per your data classification policy.

##### Rotation & Lifecycle

- Prefer short-lived, dynamic credentials (database dynamic secrets, IAM role assumption, short-TTL tokens) over static long-lived secrets wherever the secret manager supports it.
- Set a maximum secret age; for static credentials the hard limit is 90 days - automate rotation before that deadline.
- Rotate immediately upon any suspected or confirmed exposure; treat ambiguous cases as confirmed compromises (OWASP A07).
- A committed secret is a compromised secret - rotate it immediately; do not treat a git history rewrite as remediation.
- Each environment (dev, staging, prod) holds distinct, independently rotatable credentials; no value may appear in more than one environment.
- Decommission secrets and revoke service accounts as part of the same PR that removes the consuming code.
- Audit access to each secret; set automatic expiry/TTL everywhere the secret manager supports it; enable secret-manager access logging and alert on anomalous read patterns.

##### Least Privilege

- Each microservice holds credentials scoped only to the exact operations it performs; no shared root, admin, or superuser credentials between services (OWASP API1).
- Database: one role per service, granted only the required SQL verbs on required objects - no `GRANT ALL`.
- Cloud IAM: no wildcard resource (`*`) policies in staging or production; scope to specific resource ARNs/IDs.
- Restrict read access to each secret to the specific service identity (IAM role, service account, Vault policy path) that requires it; no human access to production secrets without break-glass audit trail.
- Separate secret-manager policies per service; deny cross-service secret reads at the policy level.

##### Secrets in Transit & Exposure Vectors

- Never include secrets in log lines, structured log fields, error messages, exception backtraces, stack traces, or API response bodies (OWASP A09).
- Never pass secrets as URL query parameters, path segments, or HTTP headers that are routinely logged by proxies, load balancers, or WAFs.
- Never pass secrets as command-line arguments; they are visible in process listings (`ps aux`, `/proc/<pid>/cmdline`) and shell history - use files, sockets, or environment variables instead.
- Never place secrets in CI/CD log output, build artefact metadata, pipeline environment echo steps, or deployment annotations.
- Redact secrets from diagnostic output, health-check endpoints, `/metrics` endpoints, and debug dumps before they leave the process boundary.
- Never return secrets or credential fragments in client responses, GraphQL introspection payloads, or server-sent events.

##### Secret Scanning & CI Enforcement

- Require gitleaks or trufflehog (or equivalent) in every CI pipeline; fail the build on any detected secret pattern.
- Enforce pre-commit hooks (gitleaks pre-commit, detect-secrets) in the repository; document bypass exemption process.
- Run secret scanning on pull-request diff and on full repo history in periodic scheduled scans.
- Maintain a baseline of known false-positives in a tracked allowlist file; review additions via code review.
- Alert on-call within 15 minutes of any scanner finding that reaches a protected branch.

##### Reject if

- Any secret, token, key, password, or credential literal appears in any committed file, including test code, example configs, or commented-out code.
- `.env` is absent from `.gitignore`, or any tracked file contains a real secret value.
- Credentials are shared across environments (identical value in dev and prod).
- A secret appears in a log entry, error response, URL, CI output, command-line argument, or client-visible payload.
- A service role or IAM policy grants permissions broader than its documented operational requirements, or uses wildcard resource scope in production.
- A committed or leaked secret was "remediated" solely by rewriting git history without rotating the secret.
- Any service uses a static long-lived credential where the secret manager offers a dynamic/short-lived alternative.
- Secret rotation is manual, undocumented, or lacks a defined maximum age.
- A secret is readable by more than one service identity or by any human identity without a recorded break-glass event.
- Container images, build artefacts, or client bundles contain embedded secrets or credentials at any layer.
- CI secret scanning is disabled, bypassed, or not enforced as a required status check on protected branches.
- Production secrets are accessible in development or test environments.

#### Sensitive Data Exposure

*Source: standards/security/data-exposure.md*

Enforce strict data classification, allow-list serialization, envelope encryption, and zero-PII logging across all backend services (OWASP A02, API3).

##### Data Classification

- Tag every model and schema field with a classification tier: PUBLIC, INTERNAL, CONFIDENTIAL (PII/PHI/PCI), or SECRET.
- Treat any field containing name, email, phone, DOB, SSN, IP address, government ID, or biometric as PII by default.
- PHI fields (diagnoses, prescriptions, health history) are CONFIDENTIAL and additionally subject to HIPAA controls.
- PCI data (full PANs, CVVs, track data) must never be stored; tokenize at the point of entry via a PCI-certified vault.
- Secrets (API keys, private keys, credentials) must never appear in any database column, config file, or environment beyond a secrets manager.

##### Response Field Allow-Lists (OWASP API3)

- Every serializer, DTO, and projection must declare an explicit ALLOW-LIST of output fields; never return a full ORM model dump.
- No over-fetching: fetch only required columns from the database; do not SELECT * and filter after.
- Field visibility must be gated by the caller's authorization context - a lower-privilege caller receives a structurally different response, not a render-time filter.
- Strip internal fields (_id variants, audit columns, state machines, foreign keys) unless the caller has explicit, documented need.
- Never expose password hashes, raw secrets, internal tokens, stack traces, or database row metadata in any response.

##### Encryption in Transit (OWASP A02)

- Enforce TLS 1.2+ on every connection: external APIs, internal service mesh, database drivers, cache clients, message brokers.
- Reject plaintext HTTP for any endpoint handling authentication, session tokens, or classified data; redirect HTTP to HTTPS and set HSTS with max-age=31536000; includeSubDomains on all domains.
- Validate server certificates on all outbound calls; disable cert verification only in documented, temporary, non-production contexts.
- mTLS is required for service-to-service calls in zero-trust network segments.

##### Encryption at Rest (OWASP A02)

- Encrypt CONFIDENTIAL and SECRET columns with field-level encryption (AES-256-GCM minimum) before writing to the database.
- Use envelope encryption: a data encryption key (DEK) per record or per tenant, wrapped by a key encryption key (KEK) stored in KMS.
- Never implement or vendor hand-rolled cryptographic primitives; use audited libraries (libsodium, AWS Encryption SDK, Google Tink).
- Rotate KEKs on a defined schedule (maximum 12 months) and immediately on any indication of compromise; support online re-encryption.
- Store encryption keys exclusively in a dedicated KMS (AWS KMS, GCP Cloud KMS, HashiCorp Vault); never co-locate keys with the data they protect.
- Encrypt all database backups and object-store snapshots with the same or stricter key management policy.
- Use cryptographically strong RNG (CSPRNG) for all token generation, nonces, and salts; reject Math.random(), rand(), or equivalent.
- Hash passwords exclusively with bcrypt, argon2id, or scrypt; never use MD5, SHA-1, or unsalted SHA-2 for password storage.

##### Tokenization, Masking, and Truncation

- Return last-4 only for PANs, SSNs, and phone numbers in all read-path responses; never return full values unless the business flow provably requires it and the caller is explicitly authorized.
- Apply masking or redaction in serializers, not in application logic, to avoid accidentally returning unmasked values on new code paths.
- Replace full PII identifiers with opaque tokens in cross-service calls; dereference tokens only at trust boundaries with authorization checks.

##### PII Exclusion Zones

- PII must never appear in: log lines, error messages, exception stack traces, distributed trace attributes, analytics events, audit records, or metric label values.
- PII must never appear in: URLs, query strings, fragment identifiers, or path parameters - use POST bodies or encrypted tokens.
- PII must never be used as a cache key, session key, or message queue routing key.
- Log the opaque user ID or a hashed correlation token, never the email address, phone number, or full name.
- Scrub structured logging pipelines with a deny-list processor to catch accidental PII leakage before export.
- Do not pass PII to third-party analytics scripts, pixels, or CDN-hosted tags; review all client-side integrations for data leakage.

##### Client-Side and Cache Security

- Responses containing CONFIDENTIAL data must set Cache-Control: no-store; absence of this header on a sensitive endpoint is a defect.
- Never rely on Cache-Control: private as a substitute for no-store for CONFIDENTIAL data.
- CDN and reverse-proxy layers must never cache responses containing Set-Cookie or Authorization echoes.
- Session and auth cookies must carry Secure, HttpOnly, and SameSite=Strict (or Lax) attributes; omitting any attribute is a defect.
- Sensitive tokens and PII must never be stored in localStorage or sessionStorage; use short-lived, HttpOnly cookies or in-memory state.

##### Data Retention and Erasure (GDPR / CCPA)

- Every data store must have a documented retention policy per classification tier; enforce TTLs programmatically, not procedurally.
- Implement a verifiable erasure path for GDPR/CCPA right-to-erasure requests; erasure must cascade to derived stores, replicas, and caches.
- Anonymize rather than delete only when re-identification is provably impossible; document the method and residual risk.
- Truncate soft-deleted PII after the retention window; a deleted flag is not erasure.
- Securely overwrite or cryptographically shred ephemeral sensitive data (temp files, in-memory buffers) before release.

##### Mass Assignment Guard

- Define an explicit allow-list of writable fields on every create/update endpoint; reject undeclared fields at deserialization time (OWASP A03).
- Never bind raw request bodies directly to ORM models; map through a validated input schema before persistence.

##### Reject if

- A serializer returns a full ORM model or document without an explicit field allow-list.
- A password hash, raw secret, API key, or internal token appears in any API response or log output.
- Sensitive fields at rest are unencrypted or encrypted with a key co-located in the same repository or config file.
- TLS is not enforced, HSTS lacks max-age=31536000; includeSubDomains, or certificate validation is disabled on any endpoint handling PII.
- A full PAN, SSN, or PHI field is returned in a response where last-4 or a token is sufficient.
- PII appears in a URL, query string, log line, trace attribute, cache key, or is passed to a third-party analytics tag or pixel.
- A response containing CONFIDENTIAL data lacks Cache-Control: no-store, or a session cookie is missing Secure, HttpOnly, or SameSite.
- Encryption uses a hand-rolled algorithm, MD5/SHA-1 for any purpose, or passwords are hashed without bcrypt/argon2id/scrypt.
- Sensitive tokens or PII are stored in localStorage or sessionStorage instead of HttpOnly cookies or in-memory state.
- A create/update endpoint accepts and persists fields without an explicit writable-field allow-list.
- KMS key rotation exceeds 12 months or there is no documented re-encryption procedure.
- A GDPR/CCPA erasure request has no verifiable deletion path through all downstream stores and caches.

#### Headers & Transport Security

*Source: standards/security/headers-transport.md*

Enforce TLS and HTTP security headers at every service boundary - internal, external, and dev traffic alike (OWASP A05).

##### TLS & Cipher Suites

- TLS 1.2 minimum everywhere; explicitly disable TLS 1.0, 1.1, SSLv3.
- Permitted cipher suites: ECDHE+AESGCM and CHACHA20-POLY1305 only; ban RC4, 3DES, NULL, EXPORT, anon suites.
- Enforce server-side cipher-order preference; do not defer to client preference.
- Verify server certificates on every outbound TLS connection; never disable verification in any environment.
- Rotate certificates before expiry; automate via ACME or equivalent; alert at 30-day threshold.

##### HSTS & HTTP Redirect

- Serve `Strict-Transport-Security: max-age=63072000; includeSubDomains; preload` on all HTTPS responses.
- Redirect HTTP -> HTTPS with 301 at the edge; never serve content over plain HTTP in any environment.
- Submit domains to the HSTS preload list for customer-facing services.
- Strip HSTS header from HTTP responses to prevent poisoning of non-TLS contexts.

##### Internal / Service-to-Service TLS

- All inter-service calls must use TLS; plain-text gRPC or HTTP between pods is forbidden.
- Enforce mTLS for service-to-service authentication in zero-trust environments (e.g., via service mesh or explicit cert pinning).
- Do not share the edge wildcard certificate with internal services; use dedicated per-service certs.

##### Security Response Headers

- `X-Content-Type-Options: nosniff` on every response without exception.
- `X-Frame-Options: DENY` on all API and HTML responses; relax to `SAMEORIGIN` only with documented approval.
- Prefer `Content-Security-Policy: frame-ancestors 'none'` over X-Frame-Options when the response is HTML.
- `Content-Security-Policy: default-src 'none'` baseline for pure API responses (no body HTML).
- For any HTML-bearing response: CSP must include a nonce or hash for any script; `unsafe-inline` and `unsafe-eval` are forbidden.
- `Referrer-Policy: no-referrer` for APIs; `strict-origin-when-cross-origin` minimum for HTML.
- `Permissions-Policy`: explicitly disable every feature not required (camera, microphone, geolocation, payment, usb, etc.).
- `Cross-Origin-Opener-Policy: same-origin` on all responses.
- `Cross-Origin-Embedder-Policy: require-corp` on all HTML responses by default; omit only for responses explicitly documented as cross-origin-isolation-exempt.
- `Cross-Origin-Resource-Policy: same-site` (or `same-origin`) on all API responses to prevent no-cors cross-origin reads.

##### Banner & Information Suppression

- Remove `Server` and `X-Powered-By` headers from every response; emit no version strings in any header or body.
- Suppress stack traces, framework version strings, internal file paths, and query text from all error responses.
- Return generic error objects in production; log details server-side only with correlation ID.
- Disable directory listing on every static-file or object-storage path.
- Suppress verbose TCP/application-layer banners (SMTP, FTP, SSH, database connectors) where configurable.

##### CORS

- Maintain a static origin allow-list per service; validate the incoming `Origin` header against it exactly.
- Never reflect an arbitrary request `Origin` back; never derive the allow-list from request input at runtime.
- `Access-Control-Allow-Origin: *` is permitted only on endpoints that are genuinely public and require no authentication whatsoever; any endpoint handling tokens, sessions, PII, or authorization data must use an explicit allow-list.
- `Access-Control-Allow-Origin: *` with `Access-Control-Allow-Credentials: true` is unconditionally forbidden (OWASP API7).
- Restrict `Access-Control-Allow-Methods` and `Access-Control-Allow-Headers` to the minimum documented set.
- Validate preflight OPTIONS requests; reject or 403 requests that do not match the defined CORS policy.

##### Cache & Cookie Controls

- `Cache-Control: no-store` on every response that contains credentials, tokens, PII, or authorization data; also set `Pragma: no-cache` for HTTP/1.0 backward compatibility on those responses.
- Cookies must carry `Secure`, `HttpOnly`, `SameSite=Strict` (or `Lax` with documented justification).
- Use the `__Host-` prefix for session and auth cookies to lock them to the exact host and path `/`.
- Never set `SameSite=None` without `Secure`; never omit `SameSite` on new cookies.

##### Mixed Content

- APIs that return URLs must return HTTPS URLs; never embed or redirect to HTTP resources.
- Reject or normalize any client-supplied redirect URL that resolves to an HTTP scheme.

##### Reject if

- Any production path accepts or serves over plain HTTP without an immediate 301 redirect.
- HSTS header is absent or `max-age` is less than one year (31536000).
- `Access-Control-Allow-Origin` reflects the raw request `Origin` outside a validated static allow-list.
- `Access-Control-Allow-Origin: *` coexists with `Access-Control-Allow-Credentials: true`.
- `X-Content-Type-Options: nosniff` is missing from any response.
- `Server` or `X-Powered-By` exposes a version string or framework identity.
- Stack traces, internal paths, or DB query text appear in any error response body.
- An inter-service call uses plain HTTP or unverified TLS.
- A session or auth cookie lacks `Secure`, `HttpOnly`, or `SameSite`.
- CSP on an HTML response contains `unsafe-inline` or `unsafe-eval` without a documented, approved exception.
- `Cache-Control: no-store` is absent on any response carrying credentials, tokens, PII, or authorization data.
- Directory listing is reachable on any file-serving path in any environment.

#### Rate Limiting & Resource Consumption

*Source: standards/security/rate-limiting.md*

Enforce consumption limits in shared infrastructure to prevent abuse, denial-of-service, and amplification attacks against every reachable API surface (OWASP API4).

##### Store & Algorithm

- Rate-limit counters MUST live in a shared store (Redis, gateway-level store, or managed rate-limit service) - never in per-process memory.
- Per-process counters reset on deploy, split across replicas, and fail open; they are not acceptable under any circumstance.
- Implement sliding-window or token-bucket algorithms; reject fixed-window designs - they permit burst attacks at window boundaries.
- The shared store connection MUST be monitored; treat store unavailability as a fail-closed condition on security-critical paths (see Fail-Closed below).

##### Dimensions & Limits

- Apply independent limits on ALL three dimensions: authenticated user identity, source IP, and specific endpoint.
- User-identity limits prevent account-level abuse; IP limits prevent anonymous abuse; endpoint limits prevent targeted attacks.
- IP MUST be resolved from the verified network-layer source or a trusted, configured proxy header (e.g. a pinned X-Forwarded-For depth); never trust arbitrary client-supplied IP headers - attackers spoof them to bypass IP limits.
- Authentication endpoints (login, token exchange, MFA verify): max 5 attempts / 15 min per IP AND per account (OWASP A07).
- Password-reset and OTP delivery endpoints: max 3 requests / 30 min per account; add lockout after repeated failures.
- Expensive endpoints (report generation, bulk export, search with aggregation): separate, stricter budget limits per user.
- Endpoints that trigger outbound side-effects (email, webhook, third-party API): limit calls to prevent cost amplification.
- Unauthenticated read paths are not exempt - apply broad IP-based limits to block enumeration and scraping (OWASP API3).

##### Response Contract

- Return 429 Too Many Requests on limit breach; never silently drop or queue requests without response.
- Every 429 MUST include Retry-After (seconds until reset) and the standard RateLimit-Limit, RateLimit-Remaining, RateLimit-Reset headers.
- Do not leak internal counter details (exact Redis key names, algorithm internals) in response headers or bodies.

##### Fail-Closed

- If the shared rate-limit store is unreachable, security-critical paths (auth, password-reset, privilege escalation) MUST reject all requests - do not fall back to in-memory or bypass limiting.
- Non-critical read paths MAY degrade to a conservative static limit when the store is unavailable; document the fallback value.
- Log every fail-closed event as a high-severity alert; do not silently absorb store failures.

##### Resource Consumption Caps

- Enforce max request body size at the ingress/gateway layer before application code executes; the limit MUST be defined in infrastructure config, not application code alone.
- All paginated endpoints MUST enforce a hard max page size; clamp or reject requests exceeding it - never stream unbounded result sets.
- Batch and array inputs MUST have a documented max element count; reject over-limit payloads with 400, not 500.
- GraphQL endpoints MUST enforce query depth limits, field count limits, and a query complexity budget; reject over-budget queries before execution (OWASP API4).
- All database queries and outbound HTTP calls MUST carry explicit timeouts; propagate cancellation rather than waiting indefinitely.
- Enforce max concurrent in-flight requests per user to prevent connection exhaustion.
- File upload endpoints MUST enforce max file size AND allowed MIME types; validate both at the gateway and in application code.
- File processing pipelines (image resize, document parse, zip extraction) MUST enforce max output size and processing time; abort and reject, do not queue indefinitely.

##### Cost-Based Limiting

- Assign a cost weight to expensive operations (LLM inference, heavy queries, exports); deduct from a per-user cost bucket, not just a request count.
- Enforce separate daily/hourly cost ceilings for expensive resource tiers; return 429 with an explanatory error code when the ceiling is hit.

##### Anti-Automation

- Apply CAPTCHA or proof-of-work challenges on abuse-prone unauthenticated flows (account registration, public contact forms, password reset initiation) after a threshold breach.
- Detect and block credential-stuffing patterns: high request volume hitting many distinct accounts from a narrow IP range - treat as an incident, not just a 429.
- Use constant-time comparison for credential verification; do not vary response time based on credential validity - timing differences enable account enumeration.

##### Amplification Protection

- Batch or fan-out APIs that issue N downstream calls per request MUST cap N explicitly; document and enforce the cap in API contract.
- Webhook and outbound-trigger endpoints MUST verify the caller controls the target URL (SSRF risk) and rate-limit trigger frequency per destination (OWASP A10).
- Recursive or deeply nested query structures must be bounded before execution, not after.

##### Monitoring

- Emit structured metrics and log events for every rate-limit hit including dimension (user/IP/endpoint) and algorithm state.
- Alert on sustained 429 rates above baseline on auth endpoints - treat as a credential-stuffing or brute-force signal.
- Dashboard cost-bucket utilisation for expensive operation tiers; alert at 80% of ceiling.

##### Reject if

- Rate-limit state is stored in per-process memory (dict, local cache, application variable) rather than a shared store.
- Any authentication, MFA, or password-reset endpoint has no rate limit configured (OWASP A07).
- 429 responses omit Retry-After or RateLimit-* headers.
- Security-critical paths fall back to unlimited or in-memory limiting when the shared store is unavailable (fail-open).
- Client IP is resolved from an arbitrary untrusted header (X-Forwarded-For, X-Real-IP) without a pinned trusted-proxy configuration - spoofable to bypass IP limits.
- Paginated endpoints have no enforced hard max page size - unbounded queries are possible.
- GraphQL or dynamic query endpoints lack depth, field count, or complexity limits.
- Batch/array inputs accept no upper bound on element count.
- File upload or processing endpoints enforce no size cap or processing timeout.
- Expensive operations are rate-limited only by request count, not by resource cost/weight.
- An outbound fan-out or webhook endpoint applies no cap on the number of downstream calls per user request.
- No alerting exists for sustained 429 spikes on authentication paths.

#### SSRF & Insecure Deserialization Security Standard

*Source: standards/security/ssrf-deserialization.md*

Enforce strict egress control and safe deserialization on every service boundary to eliminate SSRF (OWASP A10) and Insecure Deserialization (OWASP A08) attack surfaces.

##### SSRF - URL & Host Validation

- Treat every user-supplied URL, hostname, IP, or path segment as hostile; validate before any network call is issued.
- Maintain an explicit EGRESS ALLOW-LIST of approved hostnames/CIDRs; default-deny everything else - no regex-based exemptions.
- Block the following address spaces by resolving to IP first, then checking - string comparison is insufficient and bypassable:
  loopback 127.0.0.0/8 and ::1, RFC1918 ranges 10/8 / 172.16/12 / 192.168/16, link-local 169.254.0.0/16 (includes cloud metadata 169.254.169.254), all-zeros 0.0.0.0/8, IPv6 link-local fe80::/10, IPv6 ULA fc00::/7.
- Resolve and pin the IP at request construction time; re-validate the pinned IP on every redirect hop to prevent DNS-rebinding attacks.
- Disable redirect following entirely when not required; where redirects are necessary, limit to 2 hops maximum and re-run the full IP blocklist check on each hop's resolved target before following.
- Never forward user-supplied Host headers or redirect targets to backend services without independent validation.

##### SSRF - Protocol Allow-List

- Permit only http:// and https:// schemes; explicitly reject file://, gopher://, ftp://, dict://, sftp://, netdoc://, and all custom/internal-protocol schemes at URL-parse time - before DNS resolution.
- Apply scheme allow-list enforcement in code, not solely in network policy; defence-in-depth requires both.

##### SSRF - Cloud & Webhook Hardening

- Enforce IMDSv2 token-required mode on all EC2-equivalent instances; IMDSv1 MUST be disabled - treat any service running IMDSv1 as a blocking defect.
- Proxy and webhook workers must run in a dedicated network segment with no route to the instance metadata service or private subnets (OWASP A10).
- Webhook and callback URLs submitted by tenants are subject to the same EGRESS ALLOW-LIST and post-DNS IP check as any other outbound URL.
- Re-validate webhook targets on every invocation, not only at registration time; stored URLs can be poisoned post-registration via DNS rebinding.
- Log every outbound request host; alert on first-seen destinations and on any target that falls outside the allow-list.

##### Deserialization - Forbidden Formats (OWASP A08)

- NEVER deserialize untrusted data with: Python pickle / pickletools, PyYAML yaml.load with default or UnsafeLoader, Java native ObjectInputStream, Ruby Marshal, PHP unserialize, .NET BinaryFormatter, NetDataContractSerializer, or SoapFormatter.
- "Internal" data that traverses any network boundary or is stored in a client-accessible location is untrusted - no exceptions.
- Dependency-supplied serialization helpers inherit this rule; audit and pin versions before introducing any serialization library.

##### Deserialization - Safe Parsing Patterns

- Use data-only formats - JSON, Protobuf, Avro, or MessagePack - coupled with a strict schema validator before objects are consumed by business logic.
- Replace yaml.load(untrusted) with yaml.safe_load() unconditionally; if custom YAML types are required, use an explicit allow-listed constructor, never the full Loader.
- Enforce a type allow-list on deserialized payloads: reject any object whose concrete type is not on the approved list before instantiation.
- Any serialized token or state blob that round-trips through the client (cookies, JWTs, opaque query params) MUST be integrity-protected with HMAC or an asymmetric signature; reject unsigned or tampered blobs.
- Validate schema constraints after deserialization and before business logic; do not trust the deserializer's implicit type coercion.

##### Deserialization - XML / XXE (OWASP A03)

- Disable external entity (XXE) processing and DTD loading on every XML parser at construction time in application bootstrap - not per-call-site.
- Never enable FEATURE_EXTERNAL_GENERAL_ENTITIES, FEATURE_EXTERNAL_PARAMETER_ENTITIES, or equivalent flags on untrusted inputs.
- XPath and XSLT processors are code-execution surfaces; apply the same entity/DTD restrictions.

##### Reject if

- Any outbound HTTP call uses a user-supplied URL without resolving the hostname to an IP and checking against the IP blocklist.
- Internal address ranges (169.254.0.0/16, RFC1918, loopback 127.0.0.0/8, 0.0.0.0, IPv6 ::1, fe80::/10, fc00::/7) are blocked only by hostname string prefix, not post-DNS IP validation.
- Redirects are followed without re-running the full IP blocklist check on each hop's resolved target, or more than 2 redirect hops are permitted.
- IMDSv1 is enabled on any cloud instance running a service in this platform.
- pickle, yaml.load (non-safe loader), Java ObjectInputStream, Ruby Marshal, PHP unserialize, or any .NET unsafe deserializer (BinaryFormatter, NetDataContractSerializer, SoapFormatter) appears on any code path processing external or client-supplied data.
- Serialized data that round-trips through the client carries no HMAC or asymmetric signature to detect tampering.
- XML parsing executes without explicitly disabling DTD loading and external entity resolution at the parser constructor level.
- file://, gopher://, ftp://, or dict:// schemes are reachable through any URL-fetching code path.
- Webhook or callback URLs are validated only at registration time and not re-validated on every invocation.
- A proxy or webhook worker has a network route to the cloud metadata endpoint or to private subnets.
- Redirects are not disabled by default; services that have no redirect requirement follow redirects without explicit opt-in configuration.

#### API Inventory & Unsafe Consumption

*Source: standards/security/api-inventory.md*

Maintain a complete, current inventory of every API surface; treat all third-party API responses as untrusted input. (OWASP API9, API10)

##### Inventory Requirements

- Keep a single canonical, version-controlled inventory: path, method, version, auth scheme, data-sensitivity classification, owner, and lifecycle status.
- Include every internal/service-to-service endpoint; "internal-only" does not exempt an API from inventory or security controls (OWASP API9).
- Auto-generate inventory from code via OpenAPI annotations or route introspection; manual-only inventories are flagged as a process risk in the next audit.
- Audit inventory against live gateway traffic on every release; any endpoint found in traffic but absent from inventory is treated as a security incident.
- Tag every endpoint with its highest data-sensitivity tier (PII, PCI, PHI, internal, public) and the auth controls required; untagged endpoints are blocked at review.
- Shadow endpoints - reachable routes not in the inventory - are treated as vulnerabilities, not tech debt.
- Zombie endpoints - registered in the router but undocumented and unmaintained - must be deleted, not disabled.

##### Versioning & Deprecation Lifecycle

- Version all public and partner APIs with a path prefix (`/v1/`, `/v2/`) or an explicit version header; never mutate a contract in-place.
- Announce deprecation with a written sunset date, published in changelog and in `Deprecation` / `Sunset` response headers on every deprecated route.
- Enforce hard retirement on or before the published sunset date; expired versions must be removed from the router, not left serving silently.
- Block the deployment of a new version if the prior version's sunset has passed and the route still appears in the router.
- Versioning policy must cover all environments; do not keep v1 alive in staging as a convenience after prod retirement.

##### Environment Separation

- Production must not expose: debug endpoints, profiling routes, heap-dump handlers, raw query consoles, spring-boot actuator paths, Swagger/OpenAPI UI, GraphQL playground, or any /test/ or /internal/ prefix unless guarded by network policy AND strong authentication (OWASP API9).
- Use separate API keys, credentials, and base URLs per environment; prod secrets must never be reused in staging or dev.
- Feature-flag removal of debug endpoints gated strictly on `ENV=production`; the flag must be evaluated at startup, not at request time.
- Admin and diagnostic endpoints must be documented in inventory, restricted to a non-public network segment, and require auth equivalent to production business endpoints.

##### Single Managed Entry Point

- All external-facing API traffic must flow through a managed gateway or service mesh entry point; direct-to-service bypass routes are prohibited.
- The gateway owns: authentication, rate limiting, TLS termination, request size enforcement, and audit logging.
- Undocumented gateway bypass rules (IP allow-lists that skip auth) must appear in the inventory and have a documented justification and owner.
- Apply per-consumer (API key or client-ID level) rate limiting at the gateway on every endpoint tagged high-cost or high-sensitivity; shared pool-only limits do not satisfy this requirement.

##### OpenAPI Spec Hygiene

- The OpenAPI spec is authoritative and code-generated; hand-edited specs require a drift-detection gate in CI.
- Every endpoint in the spec must declare: authentication requirements, all possible response schemas, and data-sensitivity tags.
- Drift between the committed spec and live router behavior fails the build.

##### Unsafe Consumption of Third-Party APIs (OWASP API10)

- Treat all data received from third-party or integrated APIs as untrusted: validate schemas, sanitize values, and enforce size limits before any downstream use.
- Enforce TLS with certificate validation on every outbound call to a third-party API; self-signed or expired certs must fail hard.
- Set explicit connect, read, and total timeouts on every outbound API client; no unbounded blocking calls.
- Enforce response size limits on every outbound client; oversized responses must be rejected, not buffered fully then discarded.
- Never blindly follow HTTP redirects from third-party APIs; limit redirect hops to zero or validate destination URLs against an allow-list before following.
- Maintain a strict allow-list of third-party hostnames the service may call; outbound calls to unlisted hosts fail at the HTTP client layer.
- Log and alert on third-party API error spikes, schema changes, and latency anomalies as operational risk signals.
- Assess and document the risk tier of each third-party API dependency: data sensitivity, blast radius if compromised, SLA, and compliance scope.
- Secrets (API keys, OAuth tokens) for third-party APIs must be stored in a secrets manager, rotated on a defined schedule, and never committed to source.
- Do not pass third-party API responses directly into SQL, shell commands, or eval-like functions; treat them as untrusted user input (OWASP A03).

##### Reject if

- A route is reachable in production but absent from the API inventory.
- A deprecated version has passed its published sunset date and is still serving requests.
- Any Swagger UI, GraphQL playground, actuator, or debug endpoint is reachable from a public network in production.
- An endpoint carries PII, PCI, or PHI data with no data-sensitivity tag in the inventory.
- An outbound third-party API client has no explicit timeout or response size limit configured.
- Third-party API responses are used in downstream logic without schema validation or sanitization.
- An outbound HTTP client follows redirects without destination validation or allow-list enforcement.
- A third-party hostname is called that does not appear on the service's approved outbound allow-list.
- The committed OpenAPI spec is not code-generated and no drift-detection gate exists in CI.
- An admin or diagnostic endpoint is reachable without authentication equivalent to production business endpoints.
- A new API version is shipped by mutating an existing versioned contract rather than introducing a new version path.
- Per-consumer rate limiting is absent on any endpoint tagged high-cost or high-sensitivity.

### 3.8 Patterns and examples

#### Factory Pattern

*Source: standards/patterns/factory.md*

Use the factory pattern when the concrete type to instantiate is determined at
runtime (from config, a request parameter, or an environment variable) and
adding a new variant must not require editing existing dispatch code. It is the
canonical implementation of the Open/Closed Principle for object creation.

##### When to apply

Apply the factory pattern when **all three** of the following are true:

1. Two or more concrete implementations of the same interface exist **or are
   realistically planned** (one concrete class with no second on the horizon is
   not a factory use-case - see YAGNI).
2. The choice of implementation is **data-driven** at runtime (a config value,
   an enum field on a request, a tenant identifier, an environment variable).
3. Adding a new variant should require **only adding a new file** - never
   editing the factory itself or any existing switch / if-elif chain.

Typical signals: multiple integrations for the same capability (payment
providers, HR connectors, notification channels, storage backends), feature
toggles between two fully-different implementations, multi-tenant systems where
each tenant uses a different provider.

##### Python implementation

```python
# ---------------------------------------------------------------------------
# products/protocol.py  - the contract; concrete classes implement this
# ---------------------------------------------------------------------------
from __future__ import annotations
from typing import Protocol

class ConnectorRunnerProtocol(Protocol):
    """What callers see - no concrete imports past this boundary."""

    async def run_bulk(self, config: object) -> None: ...

    async def run_delta(self, config: object) -> None: ...

# ---------------------------------------------------------------------------
# products/keka_runner.py  - one concrete implementation
# ---------------------------------------------------------------------------

class KekaConnectorRunner:
    """Delegates to the existing keka_connector bulk / delta runners."""

    async def run_bulk(self, config: object) -> None:
        from keka_connector.bulk.runner import run_bulk
        await run_bulk(config)  # type: ignore[arg-type]

    async def run_delta(self, config: object) -> None:
        from keka_connector.delta.runner import run_delta
        await run_delta(config)  # type: ignore[arg-type]

# ---------------------------------------------------------------------------
# products/bamboohr_runner.py  - a second implementation (future example)
# ---------------------------------------------------------------------------

class BambooHRConnectorRunner:

    async def run_bulk(self, config: object) -> None:
        from bamboohr_connector.bulk.runner import run_bulk
        await run_bulk(config)  # type: ignore[arg-type]

    async def run_delta(self, config: object) -> None:
        from bamboohr_connector.delta.runner import run_delta
        await run_delta(config)  # type: ignore[arg-type]

# ---------------------------------------------------------------------------
# factory.py  - registry + factory; never import concrete classes at module
#               scope here - keep imports lazy (inside create()) when the
#               concrete packages are optional / heavyweight.
# ---------------------------------------------------------------------------
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from products.protocol import ConnectorRunnerProtocol

class ConnectorFactory:
    """Registry-based factory.  Adding a new connector = one register() call.
    The factory itself is never edited when a new variant is added (OCP).
    """

    _registry: dict[str, type] = {}

    @classmethod
    def register(cls, name: str, runner_cls: type) -> None:
        """Register a concrete runner class under a lookup name.

        Call this once per implementation, typically at the bottom of each
        concrete module so the registration is co-located with the class:

            ConnectorFactory.register("keka", KekaConnectorRunner)
        """
        if name in cls._registry:
            raise ValueError(
                f"Connector '{name}' is already registered. "
                "Each connector name must be unique."
            )
        cls._registry[name] = runner_cls

    @classmethod
    def create(cls, name: str) -> "ConnectorRunnerProtocol":
        """Instantiate and return the connector runner for *name*.

        Args:
            name: Connector identifier (e.g. ``"keka"``, ``"bamboohr"``).
                  Typically read from a config value or CLI argument.

        Returns:
            A new instance of the registered runner.

        Raises:
            ValueError: If *name* is not in the registry.  Fail fast - an
                        unknown connector name is always a configuration error.
        """
        runner_cls = cls._registry.get(name)
        if runner_cls is None:
            available = sorted(cls._registry)
            raise ValueError(
                f"Unknown connector '{name}'. "
                f"Registered connectors: {available}"
            )
        return runner_cls()

    @classmethod
    def available(cls) -> list[str]:
        """Return sorted list of registered connector names (for CLI help text)."""
        return sorted(cls._registry)

# ---------------------------------------------------------------------------
# Register concrete implementations - one line per connector.
# Each product module is responsible for calling register() on import.
# ---------------------------------------------------------------------------
ConnectorFactory.register("keka",     KekaConnectorRunner)
ConnectorFactory.register("bamboohr", BambooHRConnectorRunner)

# ---------------------------------------------------------------------------
# main.py / entry point - factory usage
# ---------------------------------------------------------------------------
import argparse

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--mode",      required=True, choices=["bulk", "delta"])
    parser.add_argument("--connector", required=True,
                        choices=ConnectorFactory.available(),
                        help="Which HR connector to run")
    args = parser.parse_args()

    runner = ConnectorFactory.create(args.connector)

    import asyncio
    if args.mode == "bulk":
        asyncio.run(runner.run_bulk(config))
    else:
        asyncio.run(runner.run_delta(config))
```

##### Registration styles

**Explicit (preferred for small, known sets)** - call `register()` at the
bottom of each concrete module:

```python
# keka_runner.py
class KekaConnectorRunner: ...

ConnectorFactory.register("keka", KekaConnectorRunner)
```

**Decorator (preferred for plugin-style extensibility)** - lets third-party
packages register implementations without editing the factory file:

```python
def connector(name: str):
    """Class decorator that registers a runner with the factory."""
    def _decorator(cls: type) -> type:
        ConnectorFactory.register(name, cls)
        return cls
    return _decorator

@connector("keka")
class KekaConnectorRunner: ...
```

Use the decorator style when implementations live in separate packages that are
discovered at install time (entry points / plugin architecture). Use explicit
`register()` calls when all implementations are in the same repository.

##### Invariants the reviewer must verify

- The factory's `create()` method **never contains an if-elif chain** that
  names concrete classes. All dispatch goes through `_registry`.
- Concrete classes are **never imported at factory module scope** unless they
  are always present (avoids import-time side effects for optional packages).
- `create()` **raises immediately** on an unknown name with a message that
  lists valid options - no silent fallback to a default.
- The abstract contract is a `Protocol` (or ABC), not a concrete base class.
  Concrete classes do not inherit from the factory.
- Each concrete class satisfies the full `Protocol` contract - no unimplemented
  methods, no methods that raise `NotImplementedError`.

##### Reject if

- The factory method contains `if name == "keka": ... elif name == "bamboohr": ...`
  instead of a registry lookup.
- A new variant was added by modifying an existing concrete class rather than
  creating a new one.
- The concrete type is instantiated directly at the call site instead of going
  through the factory (bypasses the abstraction).
- The factory is applied where only a single implementation exists with no
  credible second implementation planned (over-engineering - YAGNI).
- The abstract contract is defined as a concrete base class with `pass` or
  `return None` method bodies instead of a `Protocol`.
- A concrete class raises `NotImplementedError` for any method required by the
  Protocol - this violates LSP (see `standards/principles/solid.md`).

#### Exception Hierarchy - Python / FastAPI

*Source: standards/examples/exception-hierarchy-py.md*

Defines a typed exception tree rooted at `AppError`. Every domain error carries a
machine-readable `code`, a human-readable `message`, and the HTTP status that the
global handler maps to. Keeps all error-shape logic in one place; routers simply
`raise NotFoundError(...)`.

```python
# exceptions.py
from __future__ import annotations

import logging
import traceback
from typing import Any

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Base
# ---------------------------------------------------------------------------

class AppError(Exception):
    """Root of the application exception hierarchy.

    All subclasses must pass a stable snake_case `code` (used by API clients
    for programmatic handling) and an HTTP status code.  Never expose internal
    stack traces to clients; log them server-side here.
    """

    def __init__(
        self,
        message: str,
        *,
        code: str = "internal_error",
        http_status: int = 500,
        details: dict[str, Any] | None = None,
    ) -> None:
        super().__init__(message)
        self.message = message
        self.code = code
        self.http_status = http_status
        self.details = details or {}

    def to_dict(self) -> dict[str, Any]:
        """Shape returned to the client - never include internal state."""
        payload: dict[str, Any] = {"code": self.code, "message": self.message}
        if self.details:
            payload["details"] = self.details
        return payload

# ---------------------------------------------------------------------------
# Domain subclasses
# ---------------------------------------------------------------------------

class NotFoundError(AppError):
    """Resource does not exist or is not visible to the caller."""

    def __init__(self, message: str = "Resource not found", **kw: Any) -> None:
        super().__init__(message, code="not_found", http_status=404, **kw)

class ValidationError(AppError):
    """Input failed business-rule validation (beyond schema checks)."""

    def __init__(self, message: str, *, field: str | None = None, **kw: Any) -> None:
        details = {"field": field} if field else {}
        super().__init__(message, code="validation_error", http_status=422, details=details, **kw)

class UnauthorizedError(AppError):
    """Caller is not authenticated or token is invalid/expired."""

    def __init__(self, message: str = "Authentication required", **kw: Any) -> None:
        super().__init__(message, code="unauthorized", http_status=401, **kw)

class ForbiddenError(AppError):
    """Caller is authenticated but lacks permission for this resource (IDOR guard)."""

    def __init__(self, message: str = "Access denied", **kw: Any) -> None:
        super().__init__(message, code="forbidden", http_status=403, **kw)

class ConflictError(AppError):
    """State conflict - e.g. duplicate unique key or optimistic-lock violation."""

    def __init__(self, message: str, **kw: Any) -> None:
        super().__init__(message, code="conflict", http_status=409, **kw)

# ---------------------------------------------------------------------------
# FastAPI global exception handler
# ---------------------------------------------------------------------------
# Register in main.py:
#
#   from fastapi import FastAPI
#   from exceptions import AppError, app_error_handler, unhandled_error_handler
#
#   app = FastAPI()
#   app.add_exception_handler(AppError, app_error_handler)
#   app.add_exception_handler(Exception, unhandled_error_handler)

from fastapi import Request
from fastapi.responses import JSONResponse

async def app_error_handler(request: Request, exc: AppError) -> JSONResponse:
    """Convert any AppError subclass into the standard error envelope.

    Logs full context (request id, path, user, traceback) server-side so that
    clients only receive the sanitised shape.
    """
    # Structured log - attach to your observability pipeline (Datadog, etc.)
    logger.error(
        "handled_error",
        extra={
            "error_code": exc.code,
            "http_status": exc.http_status,
            "path": request.url.path,
            "method": request.method,
            # Include traceback so on-call engineers can see the call site.
            "traceback": traceback.format_exc(),
        },
    )
    return JSONResponse(
        status_code=exc.http_status,
        content={"error": exc.to_dict()},
    )

async def unhandled_error_handler(request: Request, exc: Exception) -> JSONResponse:
    """Catch-all for unexpected exceptions - never leak stack traces to clients."""
    logger.exception(
        "unhandled_error",
        extra={"path": request.url.path, "method": request.method},
        exc_info=exc,
    )
    return JSONResponse(
        status_code=500,
        content={"error": {"code": "internal_error", "message": "An unexpected error occurred."}},
    )
```

#### Repository Pattern - Python

*Source: standards/examples/repository-py.md*

The repository isolates all SQL from the service layer. A `Protocol` defines the
contract so services depend on the abstraction (easy to mock in tests). Transaction
ownership lives in the service, not the repository. Batch-fetch helpers eliminate N+1
queries when loading related objects.

```python
# ---------------------------------------------------------------------------
# models.py  (SQLAlchemy mapped dataclasses - plain Python, no magic)
# ---------------------------------------------------------------------------
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime

@dataclass
class Article:
    id: int
    author_id: int
    title: str
    body: str
    created_at: datetime
    published: bool

@dataclass
class Author:
    id: int
    name: str
    email: str

# ---------------------------------------------------------------------------
# repositories/protocols.py  - the contract
# ---------------------------------------------------------------------------
from typing import Protocol, Sequence

class ArticleRepositoryProtocol(Protocol):
    """What the service layer sees - no SQLAlchemy leaks past this line."""

    def get_by_id(self, article_id: int) -> Article | None: ...

    def list_published(
        self, *, limit: int, offset: int
    ) -> tuple[list[Article], int]: ...

    def get_authors_for_articles(
        self, article_ids: Sequence[int]
    ) -> dict[int, Author]: ...

    def create(self, author_id: int, title: str, body: str) -> Article: ...

    def save(self, article: Article) -> Article: ...

# ---------------------------------------------------------------------------
# repositories/article_repository.py  - concrete SQLAlchemy implementation
# ---------------------------------------------------------------------------
from sqlalchemy import func, select, text
from sqlalchemy.orm import Session  # Session injected; unit-of-work owned outside

class ArticleRepository:
    """Concrete repository backed by SQLAlchemy ORM.

    The `Session` is injected so the caller (service or FastAPI dependency)
    controls the transaction boundary.  This class never calls
    session.commit() - that is the service's job.
    """

    def __init__(self, session: Session) -> None:
        self._session = session

    # ------------------------------------------------------------------
    # Single-item fetch
    # ------------------------------------------------------------------

    def get_by_id(self, article_id: int) -> Article | None:
        # Use ORM mapped class (ArticleORM) but return plain dataclass.
        row = self._session.get(ArticleORM, article_id)
        return _to_article(row) if row else None

    # ------------------------------------------------------------------
    # Paginated list - returns (items, total_count) to avoid double query
    # ------------------------------------------------------------------

    def list_published(
        self, *, limit: int, offset: int
    ) -> tuple[list[Article], int]:
        base = select(ArticleORM).where(ArticleORM.published.is_(True))

        # COUNT sub-query reuses the same WHERE clause - single round-trip
        # would require a window function; two queries is clearer here.
        total: int = self._session.scalar(
            select(func.count()).select_from(base.subquery())
        ) or 0

        rows = self._session.scalars(
            base.order_by(ArticleORM.created_at.desc()).limit(limit).offset(offset)
        ).all()

        return [_to_article(r) for r in rows], total

    # ------------------------------------------------------------------
    # Batch fetch - eliminates N+1 when rendering article lists with authors
    # ------------------------------------------------------------------

    def get_authors_for_articles(
        self, article_ids: Sequence[int]
    ) -> dict[int, Author]:
        """Return {article_id: Author} using a single JOIN query.

        The caller passes all article IDs at once; the repository issues
        exactly one SQL statement regardless of list length.
        """
        if not article_ids:
            return {}

        stmt = (
            select(ArticleORM.id, AuthorORM)
            .join(AuthorORM, AuthorORM.id == ArticleORM.author_id)
            .where(ArticleORM.id.in_(article_ids))
        )
        return {
            article_id: _to_author(author_row)
            for article_id, author_row in self._session.execute(stmt)
        }

    # ------------------------------------------------------------------
    # Writes - always parameterized; never string-format user input into SQL
    # ------------------------------------------------------------------

    def create(self, author_id: int, title: str, body: str) -> Article:
        row = ArticleORM(author_id=author_id, title=title, body=body, published=False)
        self._session.add(row)
        self._session.flush()  # populate row.id without committing
        return _to_article(row)

    def save(self, article: Article) -> Article:
        row = self._session.get(ArticleORM, article.id)
        if row is None:
            raise ValueError(f"Article {article.id} not found for update")
        row.title = article.title
        row.body = article.body
        row.published = article.published
        self._session.flush()
        return _to_article(row)

# ---------------------------------------------------------------------------
# services/article_service.py  - depends on the Protocol, not the concrete class
# ---------------------------------------------------------------------------

class ArticleService:
    """Business logic layer.

    Accepts the Protocol so unit tests can inject a stub/mock without a
    real database.  The session (and therefore transaction) is owned here.
    """

    def __init__(
        self,
        repo: ArticleRepositoryProtocol,
        session: Session,  # same session injected into the repo
    ) -> None:
        self._repo = repo
        self._session = session

    def publish_article(self, article_id: int, requesting_user_id: int) -> Article:
        """Fetch, authorise, mutate, persist - all inside one transaction."""
        article = self._repo.get_by_id(article_id)
        if article is None:
            from exceptions import NotFoundError
            raise NotFoundError(f"Article {article_id} not found")

        if article.author_id != requesting_user_id:
            from exceptions import ForbiddenError
            raise ForbiddenError("Only the author may publish their article")

        article.published = True
        saved = self._repo.save(article)

        # commit is here - the service owns the unit of work
        self._session.commit()
        return saved

    def list_feed(
        self, *, page: int, page_size: int
    ) -> tuple[list[dict], int]:
        """Return articles enriched with author data - no N+1 query."""
        articles, total = self._repo.list_published(
            limit=page_size, offset=(page - 1) * page_size
        )
        author_map = self._repo.get_authors_for_articles([a.id for a in articles])

        enriched = [
            {
                "id": a.id,
                "title": a.title,
                "author_name": author_map[a.id].name if a.id in author_map else None,
                "created_at": a.created_at.isoformat(),
            }
            for a in articles
        ]
        return enriched, total

# ---------------------------------------------------------------------------
# Private helpers - keep ORM mapping out of business logic
# ---------------------------------------------------------------------------

def _to_article(row: "ArticleORM") -> Article:  # noqa: F821
    return Article(
        id=row.id,
        author_id=row.author_id,
        title=row.title,
        body=row.body,
        created_at=row.created_at,
        published=row.published,
    )

def _to_author(row: "AuthorORM") -> Author:  # noqa: F821
    return Author(id=row.id, name=row.name, email=row.email)
```

#### FastAPI Endpoint - Production Patterns

*Source: standards/examples/fastapi-endpoint-py.md*

Demonstrates the canonical three-layer slice: thin router, service with business
logic, repository for data access. Covers Pydantic request/response split, `Depends()`
for DB session and authenticated user, object-level authorisation (IDOR guard), and
cursor-style pagination on a list endpoint.

```python
# ---------------------------------------------------------------------------
# schemas/article.py  - request and response models are SEPARATE classes
# ---------------------------------------------------------------------------
# Splitting request from response prevents accidentally returning write-only
# fields (e.g. internal IDs, raw passwords) and makes evolution easier.

from __future__ import annotations

from datetime import datetime
from typing import Generic, TypeVar

from pydantic import BaseModel, Field, field_validator

T = TypeVar("T")

class ArticleCreateRequest(BaseModel):
    """Only the fields a client is allowed to supply on creation."""

    title: str = Field(..., min_length=3, max_length=255)
    body: str = Field(..., min_length=1)

    @field_validator("title")
    @classmethod
    def title_not_blank(cls, v: str) -> str:
        if not v.strip():
            raise ValueError("title must not be blank")
        return v.strip()

class ArticleUpdateRequest(BaseModel):
    """Partial update - all fields optional (PATCH semantics)."""

    title: str | None = Field(default=None, min_length=3, max_length=255)
    body: str | None = None
    published: bool | None = None

class ArticleResponse(BaseModel):
    """What the client receives - never includes internal fields."""

    id: int
    title: str
    author_id: int
    created_at: datetime
    published: bool

    model_config = {"from_attributes": True}  # allow ORM objects as input

class PagedResponse(BaseModel, Generic[T]):
    """Reusable pagination envelope."""

    items: list[T]
    total: int
    page: int
    page_size: int
    pages: int

    @classmethod
    def build(cls, items: list[T], total: int, page: int, page_size: int) -> "PagedResponse[T]":
        import math
        return cls(
            items=items,
            total=total,
            page=page,
            page_size=page_size,
            pages=math.ceil(total / page_size) if page_size else 0,
        )

# ---------------------------------------------------------------------------
# dependencies.py  - reusable FastAPI Depends() factories
# ---------------------------------------------------------------------------

from functools import lru_cache
from typing import Annotated, Generator

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from database import SessionLocal  # your sessionmaker

def get_db() -> Generator[Session, None, None]:
    """Yield a DB session and guarantee it is closed after the request."""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# ------------------------------------------------------------------
# Auth dependency - validates the bearer token and returns the user.
# Raises 401 (UnauthorizedError) if missing/invalid; never 403 here.
# ------------------------------------------------------------------

_bearer = HTTPBearer(auto_error=False)

def get_current_user(
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(_bearer)],
    db: Annotated[Session, Depends(get_db)],
) -> "User":
    from exceptions import UnauthorizedError
    from services.auth_service import AuthService

    if credentials is None:
        raise UnauthorizedError("No bearer token provided")

    user = AuthService(db).resolve_token(credentials.credentials)
    if user is None:
        raise UnauthorizedError("Token is invalid or expired")
    return user

# Convenience type alias used in endpoint signatures.
CurrentUser = Annotated["User", Depends(get_current_user)]
DBSession = Annotated[Session, Depends(get_db)]

# ---------------------------------------------------------------------------
# routers/articles.py  - the router is intentionally thin
# ---------------------------------------------------------------------------
# Business logic lives in ArticleService; SQL lives in ArticleRepository.
# The router's only jobs: deserialise input, call the service, serialise output.

from fastapi import APIRouter, Query
from sqlalchemy.orm import Session

from exceptions import ForbiddenError, NotFoundError
from repositories.article_repository import ArticleRepository
from schemas.article import (
    ArticleCreateRequest,
    ArticleResponse,
    ArticleUpdateRequest,
    PagedResponse,
)
from services.article_service import ArticleService

router = APIRouter(prefix="/articles", tags=["articles"])

def _make_service(db: Session) -> ArticleService:
    """Wire the service with its repository in one place."""
    return ArticleService(repo=ArticleRepository(db), session=db)

# ------------------------------------------------------------------
# POST /articles  - create
# ------------------------------------------------------------------

@router.post("/", response_model=ArticleResponse, status_code=201)
def create_article(
    body: ArticleCreateRequest,
    db: DBSession,
    current_user: CurrentUser,
) -> ArticleResponse:
    article = _make_service(db).create_article(
        author_id=current_user.id,
        title=body.title,
        body=body.body,
    )
    return ArticleResponse.model_validate(article)

# ------------------------------------------------------------------
# GET /articles  - paginated list (public)
# ------------------------------------------------------------------

@router.get("/", response_model=PagedResponse[ArticleResponse])
def list_articles(
    db: DBSession,
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=20, ge=1, le=100),
) -> PagedResponse[ArticleResponse]:
    items, total = _make_service(db).list_feed(page=page, page_size=page_size)
    return PagedResponse.build(
        items=[ArticleResponse.model_validate(a) for a in items],
        total=total,
        page=page,
        page_size=page_size,
    )

# ------------------------------------------------------------------
# GET /articles/{article_id}  - single item, authenticated
# ------------------------------------------------------------------

@router.get("/{article_id}", response_model=ArticleResponse)
def get_article(
    article_id: int,
    db: DBSession,
    current_user: CurrentUser,
) -> ArticleResponse:
    # NotFoundError is raised inside the service; the global handler maps it to 404.
    article = _make_service(db).get_article(article_id)
    return ArticleResponse.model_validate(article)

# ------------------------------------------------------------------
# PATCH /articles/{article_id}  - update with IDOR guard
# ------------------------------------------------------------------

@router.patch("/{article_id}", response_model=ArticleResponse)
def update_article(
    article_id: int,
    body: ArticleUpdateRequest,
    db: DBSession,
    current_user: CurrentUser,
) -> ArticleResponse:
    # Object-level authorisation: the service checks ownership.
    # If article.author_id != current_user.id -> ForbiddenError (403).
    # This prevents Insecure Direct Object Reference (IDOR) attacks.
    article = _make_service(db).update_article(
        article_id=article_id,
        requesting_user_id=current_user.id,
        title=body.title,
        body=body.body,
        published=body.published,
    )
    return ArticleResponse.model_validate(article)

# ------------------------------------------------------------------
# DELETE /articles/{article_id}  - owner or admin only
# ------------------------------------------------------------------

@router.delete("/{article_id}", status_code=204)
def delete_article(
    article_id: int,
    db: DBSession,
    current_user: CurrentUser,
) -> None:
    _make_service(db).delete_article(
        article_id=article_id,
        requesting_user_id=current_user.id,
        is_admin=current_user.is_admin,
    )

# ---------------------------------------------------------------------------
# services/article_service.py  - excerpt showing the IDOR guard pattern
# ---------------------------------------------------------------------------

class ArticleService:
    def __init__(self, repo, session):
        self._repo = repo
        self._session = session

    def _fetch_or_404(self, article_id: int):
        article = self._repo.get_by_id(article_id)
        if article is None:
            raise NotFoundError(f"Article {article_id} not found")
        return article

    def _assert_owner(self, article, requesting_user_id: int) -> None:
        """Centralised IDOR check - call before any mutation."""
        if article.author_id != requesting_user_id:
            # Return 403 Forbidden, NOT 404.  Returning 404 here would
            # silently hide the resource from attackers but also from
            # legitimate error messages.  403 is the correct signal when
            # the resource exists but access is denied.
            raise ForbiddenError("You do not own this article")

    def get_article(self, article_id: int):
        return self._fetch_or_404(article_id)

    def update_article(
        self,
        article_id: int,
        requesting_user_id: int,
        title: str | None,
        body: str | None,
        published: bool | None,
    ):
        article = self._fetch_or_404(article_id)
        self._assert_owner(article, requesting_user_id)

        if title is not None:
            article.title = title
        if body is not None:
            article.body = body
        if published is not None:
            article.published = published

        saved = self._repo.save(article)
        self._session.commit()
        return saved

    def delete_article(
        self, article_id: int, requesting_user_id: int, is_admin: bool
    ) -> None:
        article = self._fetch_or_404(article_id)
        if not is_admin:
            self._assert_owner(article, requesting_user_id)
        self._repo.delete(article_id)
        self._session.commit()
```

### 3.9 Cloud (AWS; applies only to AWS deployments)

#### AWS Compute Targets

*Source: standards/cloud/aws/compute-targets.md*

Select and configure Lambda or ECS/Fargate correctly; misfit compute is the primary source of latency, cost, and reliability failures.

##### When to Choose Which

- Lambda: event-driven, spiky, or infrequent workloads with execution time <=15 min; stateless transforms, fan-out consumers, API backends with sub-second p50 targets.
- Fargate: long-running processes, streaming, WebSocket servers, CPU/memory-intensive batch jobs, workloads that hold in-memory state across requests, or anything needing persistent TCP connections.
- Default to Fargate when cold-start latency is unacceptable and provisioned concurrency cost is not justified.
- Never fight the execution model: if a Lambda function routinely approaches 15 minutes or manages connection pools, move it to Fargate.

##### Lambda: Handler & Initialization (Performance Efficiency)

- Keep the handler thin; place all business logic in discrete, testable units called by the handler.
- Initialize SDK clients, DB connections, HTTP clients, and heavy objects OUTSIDE the handler at module scope so they survive and are reused across warm invocations.
- Never re-initialize a client on every invocation; treat the execution environment as a micro-process pool with an uncertain but non-zero warm lifetime.
- Use RDS Proxy - never open a raw DB connection per invocation; Lambda concurrency multiplied by per-invocation connection overhead will exhaust the DB connection limit.

##### Lambda: Timeouts & Size Limits (Reliability)

- Set function timeout below the invoking service limit: API Gateway hard-caps at 29 s; SQS visibility timeout must exceed function timeout to prevent duplicate processing.
- 15 min is the hard ceiling; any workload that routinely approaches it belongs on Fargate or a Step Functions workflow.
- Respect the 250 MB unzipped deployment package limit and per-layer limits; use container images (up to 10 GB) for large runtimes or ML models.

##### Lambda: Ephemeral State & Storage (Security, Reliability)

- Only /tmp (512 MB default, configurable up to 10 GB) is writable; it is ephemeral and MAY be shared across warm invocations - never store secrets, tokens, or user-scoped data there.
- Hold no durable local state; offload to S3, DynamoDB, ElastiCache, or a managed data store.
- Treat every invocation as potentially the first and potentially a duplicate.

##### Lambda: Cold-Start Mitigation (Performance Efficiency)

- Minimize dependency surface; prefer tree-shakeable packages and strip unused SDK sub-modules.
- Use lazy imports for infrequently hit code paths to reduce initialization time on cold starts.
- Enable Provisioned Concurrency for latency-critical, customer-facing functions; budget it explicitly against the traffic pattern.
- VPC-attach only when private resource access is required; PrivateLink, RDS Proxy, or Lambda URLs eliminate the VPC cold-start penalty for many access patterns.

##### Lambda: Secrets & Credentials (Security)

- Never store secrets, API keys, or database passwords as plaintext Lambda environment variables; retrieve them at cold-start time from Secrets Manager or SSM Parameter Store and cache the value in a module-level variable.
- Assign a function execution role with least-privilege IAM policies; do not attach AdministratorAccess or use long-lived access keys in any form.

##### Lambda: Execution Model & Idempotency (Reliability, Operational Excellence)

- No background work after the response returns; the execution environment freezes immediately after the handler exits.
- Async invocations and all event sources (SQS, SNS, Kinesis, S3 notifications, EventBridge) retry on failure - design every handler idempotent using idempotency keys, conditional writes, or the Lambda Powertools Idempotency utility.
- Dead-letter queues or on-failure event destinations are mandatory on every async trigger and event-source mapping.
- Use Lambda Powertools structured logging, tracing (X-Ray), and metrics; never rely on print statements alone.

##### Fargate: Process Lifecycle (Reliability)

- Handle SIGTERM in the application; drain in-flight work and exit cleanly within the ECS stop timeout (default 30 s, configurable up to 120 s on the task definition).
- Expose a dedicated health and readiness HTTP endpoint; the ALB target group health check must target it, not the application root path.
- Do not accept production traffic until the readiness check passes; configure a startPeriod on the container health check to prevent premature deregistration.
- Use ECS service deployment circuit breakers with rollback enabled so a bad deploy does not fully replace a healthy fleet.

##### Fargate: Security & Runtime (Security)

- Run as a non-root user; set USER in the Dockerfile; enable a read-only root filesystem where the application permits.
- Drop all Linux capabilities not explicitly required by the application.
- Listen on the port declared in the task definition; never assume port 80 or hardcode an ephemeral port.
- Use task IAM roles for all AWS API access - never bake access keys into the image, environment variables, or Secrets Manager paths sourced at build time.
- Pull secrets at runtime via the ECS Secrets integration (Secrets Manager or Parameter Store); rotate them without image rebuilds.

##### Fargate: Observability & Resources (Operational Excellence, Cost Optimization)

- Ship logs via the awslogs driver or a FireLens/Fluentd sidecar; never rely on local log files inside the container.
- Set explicit cpu and memory hard limits in the task definition; under-provisioned tasks cause OOM kills, over-provisioned tasks inflate cost without bound.
- Hold no durable local state; the container filesystem is ephemeral - persist data to EFS, S3, Aurora, or a managed store.
- Follow 12-factor app principles: config via environment variables, stateless processes, explicit port binding, treat backing services as attached resources.
- Tag tasks with application, environment, and cost-center labels for cost allocation. (Cost Optimization)

##### Reject if

- Lambda handler initializes SDK clients, DB connections, or HTTP clients inside the handler body on every invocation.
- Lambda connects directly to RDS without RDS Proxy, enabling connection exhaustion under burst concurrency.
- Lambda function timeout equals or exceeds the invoking service timeout (e.g., set to 29 s or more behind API Gateway).
- Lambda stores secrets, session data, or user-scoped data in /tmp, assuming isolation between invocations.
- Lambda performs work or spawns background threads after the response is returned to the invoking service.
- Lambda async trigger or event-source mapping lacks a dead-letter queue or on-failure event destination.
- Lambda function stores secrets, API keys, or DB passwords as plaintext environment variables instead of retrieving them from Secrets Manager or Parameter Store.
- Fargate task runs as root user or omits a task IAM role and uses hardcoded AWS credentials instead.
- Fargate container does not handle SIGTERM and will be force-killed after the stop timeout, dropping in-flight requests.
- Fargate task definition omits explicit cpu and memory reservations.
- Fargate service lacks an ALB health or readiness endpoint, causing the load balancer to route traffic before the app is ready.
- Either compute target emits logs only to local disk with no log driver or sidecar configured.
- Workload requires persistent TCP connections or routinely runs longer than 15 minutes but Lambda was chosen anyway.

#### Config & Secrets - AWS Standard

*Source: standards/cloud/aws/config-secrets-aws.md*

Govern how non-secret configuration and sensitive credentials are sourced, cached, scoped, and rotated on AWS (Security pillar).

##### Parameter Store - Non-Secret Config

- Store all non-secret runtime configuration in SSM Parameter Store (Standard tier for low-volume, Advanced for throughput or policies).
- Namespace every parameter under a path that encodes environment and service: /app/{env}/{service}/{key}. Never share a path across environments.
- Inject config into Fargate tasks via the `secrets` / `environment` block in the task definition pointing to Parameter Store ARNs - never inline literal values.
- For Lambda, prefer fetching parameters at init time via the AWS Parameters and Secrets Lambda extension or a startup fetch; CloudFormation SSM dynamic references resolve once at stack deploy and will not reflect subsequent SSM changes until the next stack update - do not use them for values that change independently of deployments.
- Encrypt all parameters with a KMS CMK (not the AWS-managed key) so the key policy can enforce least-privilege access.

##### Secrets Manager - Secret Values

- Store every credential, token, API key, and certificate private key in AWS Secrets Manager.
- Acceptable alternative: SSM SecureString encrypted with a service-specific KMS CMK - only when Secrets Manager rotation is not required.
- Never hardcode a secret value in source code, IaC, Dockerfile, or any artifact committed to version control.
- Never bake a secret into a container image at build time. CI must not emit secrets into layer cache or build logs.
- Never store a secret in plaintext in S3, DynamoDB, Parameter Store (non-SecureString), or any other persistent store.
- Lambda: never store a plaintext secret in a Lambda environment variable. Use the AWS Parameters and Secrets Lambda extension (preferred) or fetch via Secrets Manager API during the init phase; cache in module-level scope.
- Fargate: inject secrets via the ECS task definition `secrets` array pointing to the Secrets Manager ARN; the ECS agent resolves and injects at task launch - never pass them as plain environment values in the task definition JSON.
- Never use IAM long-term access keys (AKID + secret) in any compute workload. All Lambda and Fargate execution must use IAM roles with temporary credentials; there is no legitimate reason for an access key pair in these environments.

##### Caching - Avoid Per-Request Fetch Cost

- Cache resolved secrets and parameters in process memory with a TTL of 5-15 minutes. Do not fetch on every invocation or request.
- Lambda: the AWS Parameters and Secrets Lambda extension handles caching automatically (default TTL 300 s); when using SDK calls directly, cache at module scope and re-fetch only on TTL expiry or on a `SecretNotFound` / `AccessDenied` exception.
- Fargate: use an in-process singleton cache or the AWS Parameters and Secrets sidecar container; refresh on TTL or application restart.
- Never cache secrets to disk, shared memory volumes, or any storage that outlives the process or container.

##### KMS Encryption

- Every Secrets Manager secret and SSM SecureString must reference a service-specific KMS CMK, not `aws/ssm` or `aws/secretsmanager` default keys.
- Enable automatic KMS key rotation (annual minimum). Disable key deletion without a 30-day pending window.
- Grant `kms:Decrypt` only to the exact IAM role that needs it; scope via `kms:EncryptionContext` conditions where possible.

##### IAM Scoping

- Each service/Lambda execution role gets `secretsmanager:GetSecretValue` or `ssm:GetParameter(s)` scoped to its own secret/parameter ARNs only - no wildcard resource.
- Use resource-based policies on secrets for cross-account access; never relax the execution-role policy to accommodate cross-account.
- Deny `secretsmanager:DeleteSecret`, `secretsmanager:RotateSecret` (trigger), and `ssm:DeleteParameter` from application execution roles; those actions belong to deployment pipelines or rotation Lambdas only.
- When workloads run inside a VPC, create VPC endpoints (Interface endpoints) for Secrets Manager and SSM so secret fetch traffic never traverses the public internet.

##### Secret Rotation

- Enable Secrets Manager automatic rotation for every database credential and API key that supports a rotation Lambda (RDS, Redshift, DocumentDB natively; custom Lambda for third-party).
- Set rotation interval to 30 days maximum for database passwords; 90 days maximum for API keys.
- Test rotation in non-prod before enabling in prod; confirm the application re-reads on the next TTL cycle without downtime.
- Application code must handle `AWSPENDING` / `AWSCURRENT` stages gracefully - never pin to a specific version ID.

##### Startup Fail-Fast

- Resolve all required parameters and secrets during application initialisation (Lambda init phase; Fargate container entrypoint) before accepting traffic.
- If any required value is absent or inaccessible, emit a structured error to CloudWatch Logs and terminate with a non-zero exit code - do not start in a degraded state.
- Surface config misses in deployment pipelines via Fargate health check failures (task never reaches RUNNING) or Lambda init-phase errors (invocation returns an error before any handler logic runs); do not rely on reserved-concurrency manipulation as a health signal.

##### Logging & Observability

- Never log a secret value, decrypted parameter value, or any derivative of them. Mask or omit before writing to CloudWatch Logs, X-Ray traces, or any telemetry sink.
- Log only the parameter/secret name and whether resolution succeeded or failed.
- Enable CloudTrail data events for `secretsmanager:GetSecretValue` and `ssm:GetParameters` to maintain an access audit trail (Security pillar - Detective Controls).

##### Reject if

- Any secret, API key, password, or token is hardcoded in source code, IaC, or a Dockerfile.
- A secret is stored in a plaintext Lambda environment variable (not resolved via Secrets Manager or SSM SecureString at runtime).
- A container image is built with a secret baked into a layer or injected at build time via ARG/ENV.
- Parameters or secrets share a path across environments (e.g., /app/db/password used by both prod and dev).
- A secret or SecureString is encrypted with the default AWS-managed KMS key instead of a service-specific CMK.
- Secrets Manager automatic rotation is disabled for any database credential.
- An application execution role has wildcard resource on `secretsmanager:GetSecretValue` or `ssm:GetParameter`.
- Secrets are fetched on every Lambda invocation or HTTP request without a TTL-based in-process cache.
- A secret value appears in any CloudWatch log group, X-Ray trace, or structured log field.
- The service starts successfully (passes health checks) when a required secret or config parameter is unreachable.
- IAM long-term access keys (AKID + secret access key) are present in any Lambda, ECS task, or container environment variable.
- A workload running inside a VPC fetches secrets over the public internet (no VPC endpoint for Secrets Manager or SSM configured).

#### AWS Cost Optimization & Tagging

*Source: standards/cloud/aws/cost-tagging.md*

Mandatory governance rules for cost allocation, right-sizing, and waste elimination across all AWS workloads. (Cost Optimization + Sustainability pillars)

##### Mandatory Resource Tags

- Every billable resource MUST carry four tags at creation time: `owner` (team or individual), `environment` (prod/staging/dev), `service` (logical service name), `cost-center` (finance allocation code).
- Enforce tags via AWS Config rule `required-tags` and SCP-level tag policies; untagged resources fail deployment pipelines.
- Tag Lambda functions, ECS task definitions, DynamoDB tables, S3 buckets, RDS instances, ElastiCache clusters, API Gateways, and VPC NAT Gateways without exception.
- Use AWS Cost Explorer tag-based grouping and monthly per-service cost reviews against the `service` + `cost-center` dimensions.

##### Compute Right-Sizing

- Lambda: tune memory in 64 MB increments using AWS Lambda Power Tuning; memory directly controls vCPU allocation and determines both cost and latency - never leave it at the 128 MB default without profiling.
- Lambda: initialize SDK clients, DB connections, and heavy objects at module scope (outside the handler) so they are reused across warm invocations; creating a new DB connection per invocation burns compute time and inflates cost.
- Lambda: set reserved concurrency on high-traffic functions to cap runaway spend; use provisioned concurrency only when p99 cold-start latency is measurably unacceptable in production.
- Lambda: delete unused function versions and unused Lambda Layers; stale versions accumulate storage cost and add operational noise.
- Fargate: set `cpu` and `memory` task-definition fields to the nearest size above the p95 observed utilization, not to arbitrary round numbers; review Container Insights metrics monthly and resize.
- Fargate Spot: use for batch, async workers, and any fault-tolerant workload; always pair with a fallback capacity-provider strategy targeting on-demand Fargate.
- Never run a Fargate task or Lambda function at default resource settings in production without a documented load-test justification.

##### Pricing Model Selection

- Lambda: commit to Compute Savings Plans once invocation volume has been stable for 3 consecutive months above ~1M GB-seconds/month; "evaluate later" is not acceptable once the threshold is confirmed.
- Fargate: commit to Compute Savings Plans for baseline capacity running >160 hours/month; reserve the remainder for on-demand or Spot.
- DynamoDB: use on-demand capacity for workloads with spiky or unpredictable traffic; switch to provisioned with Application Auto Scaling and target-tracking for stable, high-throughput tables - model break-even at ~50% sustained utilization.
- RDS / ElastiCache: use Reserved Instances (1-year minimum) for any instance running continuously in production.
- Document the pricing-model decision and review quarterly; treat silent drift to on-demand as a cost defect.

##### Log Retention

- Every CloudWatch Log Group MUST have an explicit retention policy set at infrastructure provisioning time.
- Default retention: 30 days for non-production, 90 days for production application logs, 365 days for audit/compliance logs.
- Never leave retention set to "Never expire" unless a written compliance requirement mandates indefinite retention; treat unbounded log groups as a cost violation.
- Export logs older than the hot-retention window to S3 with the appropriate storage class for long-term compliance needs.

##### S3 Storage Classes & Lifecycle

- All S3 buckets serving non-critical or infrequently accessed objects MUST have a lifecycle policy transitioning objects to S3 Intelligent-Tiering or S3 Standard-IA after 30 days, and to Glacier Instant Retrieval after 90 days.
- Enable S3 Intelligent-Tiering for buckets with unknown or variable access patterns rather than manually managing transitions.
- Set lifecycle expiration rules for temporary objects (uploads, build artifacts, logs) - never rely on manual cleanup.
- Avoid high-frequency small-object PUT or LIST storms against S3; batch writes and use S3 Event Notifications or SQS fan-out to prevent request-cost amplification.

##### Idle & Orphaned Resource Elimination

- Pipeline checks MUST flag: unattached EBS volumes, unassociated Elastic IPs, unused NAT Gateways, idle RDS instances (zero connections >7 days), snapshots older than the retention policy, and unused Lambda versions or layers.
- Delete old AMIs and their backing snapshots as part of the CI/CD image-bake pipeline; retain only the last N images per service (N defined per team, default 3).
- Run AWS Trusted Advisor or AWS Compute Optimizer weekly; treat P1 recommendations as mandatory remediation within one sprint.

##### Budget Alarms & Anomaly Detection

- Every AWS account AND every service-level cost center MUST have an AWS Budgets alert at 80% and 100% of the monthly forecast; both scopes are required.
- Enable AWS Cost Anomaly Detection with a per-service monitor and a per-account monitor; set notification threshold at 20% or $50 above the rolling 7-day baseline, whichever is lower.
- Anomaly alerts route to the same incident channel as operational alerts - cost spikes are incidents.

##### Caching & API Call Reduction

- Cache read-heavy DynamoDB or RDS query results in ElastiCache (DAX for DynamoDB hot paths) to cut redundant DB read capacity consumption.
- Use API Gateway caching for GET endpoints with stable response data; set TTL based on data freshness SLA, not convenience.
- Enforce rate limiting on all external-facing APIs per rate-limiting.md to bound downstream AWS service call volume and prevent cost amplification from abuse or traffic spikes.

##### Reject if

- Any billable resource is deployed without all four mandatory tags (`owner`, `environment`, `service`, `cost-center`).
- Lambda memory is left at 128 MB default in production without a Power Tuning profile documenting the choice.
- A Lambda function creates a new DB or SDK client connection inside the handler on every invocation instead of reusing a module-level singleton.
- A Fargate task definition uses a `cpu`/`memory` combination not validated against observed p95 Container Insights metrics.
- A CloudWatch Log Group has no retention policy or is set to "Never expire" without a documented compliance mandate.
- A DynamoDB table in production uses on-demand capacity when 90-day CloudWatch metrics show consistently predictable traffic above the provisioned break-even point.
- An S3 bucket storing objects older than 30 days has no lifecycle transition or expiration rule.
- Unattached EBS volumes, unassociated Elastic IPs, or snapshots beyond the retention policy exist in any account for more than 7 days without a recorded exception.
- No AWS Budgets alert exists at both the account level and the service cost-center level at the time of production deployment.
- Cost Anomaly Detection is not enabled at the account or service level, or anomaly alerts do not route to an actionable incident channel.
- Provisioned concurrency is enabled on a Lambda function without documented p99 cold-start evidence from production load testing.
- Fargate Spot is used as the sole capacity provider for a stateful or non-fault-tolerant workload.
- External-facing APIs are deployed without rate-limiting controls per rate-limiting.md, leaving AWS service call volume unbounded.

#### Data & State on AWS

*Source: standards/cloud/aws/data-state-aws.md*

Services are stateless by contract; all durable state lives in managed stores, encrypted, private, and lifecycle-governed. (Reliability + Security)

##### Statelessness

- Lambda: /tmp (512 MB-10 GB ephemeral) persists across warm invocations - data written in one request MAY be read by a later request from a different user; treat as scratch space only, never write sensitive or user-scoped state to /tmp.
- Lambda: warm-instance reuse is accidental and not guaranteed; never rely on /tmp surviving across invocations as a durable store.
- Fargate: container local disk is ephemeral and lost on task replacement; no durable local disk assumption for any data that must outlive the task.
- Session state belongs in ElastiCache (Redis/Valkey) or DynamoDB - never in process memory or local files.
- Request-scoped scratch work only is permitted in Lambda /tmp; treat as disposable and never write PII or credentials there.

##### Choose the Right Store

- S3: objects, large files, data-lake assets, static artifacts, ML model weights. Not a database.
- RDS/Aurora: relational workloads requiring ACID transactions, complex joins, strong consistency.
- Aurora Serverless v2: variable/bursty relational load; prefer over provisioned when peak-to-trough ratio exceeds 4x.
- DynamoDB: high-scale key-value, single-digit-millisecond latency, event-driven workloads; design access patterns before schema.
- ElastiCache (Redis/Valkey): application cache, session store, rate-limit counters, pub/sub fan-out.
- Do not store large blobs in DynamoDB or RDS - keep DynamoDB items well under the 400 KB item limit; store binary/blob data in S3 and reference by key/URL.

##### DynamoDB Design

- Choose capacity mode deliberately: on-demand for unpredictable/spiky traffic; provisioned + auto-scaling for steady, cost-sensitive workloads.
- Partition keys must distribute load uniformly - no hot partitions; use write-sharding or composite keys where cardinality is low.
- Enable DynamoDB TTL on ephemeral or time-bounded data (sessions, idempotency keys, rate-limit windows).
- Enable point-in-time recovery (PITR) on all tables that hold business data.
- Prefer single-table design with GSIs over multiple tables; define GSIs at design time, not retrofit.
- Ensure writes to DynamoDB are idempotent (use conditional expressions or idempotency keys); never retry a non-idempotent write without deduplication.

##### Connection Management

- Lambda: initialise the DB client/connection object at module level (outside the handler) so it is reused across warm invocations; use RDS Proxy to cap the total RDS connection count and pool connections across concurrent invocations.
- Fargate: size the connection pool to task count x pool size - must not exceed RDS max_connections; use RDS Proxy when task count is dynamic or auto-scaling.
- Idle connections must have a timeout configured on both client and server sides.
- Connection strings and credentials sourced exclusively from Secrets Manager (with auto-rotation enabled) or SSM Parameter Store SecureString - never hard-coded in source or passed as plaintext environment variables.

##### Secrets & Credentials

- All secrets (API keys, DB credentials, tokens, signing keys) stored in Secrets Manager or SSM Parameter Store SecureString; never in source code, plaintext env vars, or unencrypted SSM parameters.
- Enable Secrets Manager auto-rotation for all RDS/Aurora and ElastiCache credentials.
- Lambda and Fargate tasks access secrets via IAM role at runtime; no static access keys embedded in task definitions or function configuration.

##### Encryption

- S3: SSE-KMS on all buckets containing sensitive or regulated data; SSE-S3 minimum on all others - no unencrypted buckets. (Security)
- RDS/Aurora: storage encryption enabled at cluster creation (cannot be enabled post-creation); use CMK for regulated data.
- DynamoDB: encryption at rest enabled by default; use CMK (aws/dynamodb or customer-managed) for regulated workloads.
- EBS/EFS volumes attached to Fargate tasks: encrypted with KMS.
- All data in transit over TLS 1.2+; no plaintext endpoints; enforce HTTPS on S3 bucket policies and API Gateway.

##### S3 Access Control

- Block Public Access enabled at account level and per-bucket - no exceptions without documented, time-bounded justification. (Security)
- No public ACLs or public bucket policies; use pre-signed URLs (recommended maximum TTL 1 hour for sensitive objects; absolute maximum 7 days) for time-limited client access.
- Bucket policies enforce aws:SecureTransport = true; deny all non-TLS requests.
- Cross-account access via resource-based policies + IAM roles, not bucket ACLs.
- S3 Object Ownership set to BucketOwnerEnforced; ACLs disabled.

##### Lifecycle & Retention

- S3 lifecycle policies mandatory on all buckets: transition infrequently accessed data to S3-IA or Glacier after defined retention thresholds; delete expired objects explicitly.
- Enable S3 Versioning on buckets storing critical state or audit artifacts; pair with lifecycle rules to expire old versions.
- RDS/Aurora: automated backups enabled, retention >= 7 days (>= 35 days for production); PITR enabled.
- DynamoDB PITR enabled for all tables holding non-ephemeral business data.

##### Data Processing in Code

- Stream large S3 objects (ranged/streaming/multipart reads) - never load a whole large object into memory; respect the compute memory ceiling (Lambda 128 MB-10 GB).
- Read DynamoDB/RDS results with pagination or server-side cursors; never pull an unbounded result set into memory in one call.
- Use batch APIs (DynamoDB BatchWriteItem/BatchGetItem, S3 multipart, SQS SendMessageBatch) and handle UnprocessedItems / partial-batch failures with backoff - never one item per call in a hot loop.
- Lambda /tmp staging must stay within the ephemeral size, be written and cleaned up within the invocation, and never be assumed to persist.
- For the generic record-handling rules (idempotent writes, deterministic transforms, Decimal/UTC, external-record validation, resource cleanup, restartability) follow `standards/principles/data-handling.md`.

##### Reject if

- A large S3 object or unbounded query result is read fully into memory instead of streamed or paginated.
- Items are written one-per-call where a batch API applies, or DynamoDB BatchWriteItem UnprocessedItems are ignored without retry.
- Any service writes sensitive, user-scoped, or durable state to Lambda /tmp or Fargate local disk, or assumes /tmp data will not be seen by a subsequent warm invocation from a different user.
- Session state stored in process memory or local files instead of ElastiCache or DynamoDB.
- RDS or Aurora deployed without RDS Proxy when invoked from Lambda or auto-scaling Fargate tasks.
- Any S3 bucket has Block Public Access disabled or a public-read/public-read-write ACL/policy without documented exception.
- Storage encryption (S3 SSE, RDS, DynamoDB, EBS) is absent or uses SSE-S3 where CMK is required by policy.
- Secrets or connection strings hard-coded in source, passed as plaintext environment variables, or stored in unencrypted SSM parameters instead of Secrets Manager or SSM SecureString.
- DynamoDB item stores binary blob data pushing items toward or beyond the 400 KB item limit, or RDS column stores large binary blobs that belong in S3.
- DynamoDB provisioned capacity deployed with no auto-scaling policy and no documented traffic model.
- Partition key chosen without cardinality analysis - single-value or low-cardinality keys on high-throughput tables.
- Automated backups or PITR disabled on any RDS, Aurora, or DynamoDB table holding business-critical data.
- Data transmitted over plaintext HTTP to any internal or external endpoint.
- A non-idempotent write is retried without deduplication logic (idempotency key or conditional expression).

#### IAM Least-Privilege Standard

*Source: standards/cloud/aws/iam-least-privilege.md*

Enforce minimal, role-based AWS access across every compute target and deployment pipeline. (Security pillar)

##### Identity & Credential Model

- Never embed AWS access keys or secret keys in source code, environment variables, Docker images, or CI artefacts.
- Lambda: attach an IAM execution role; ECS/Fargate: use an ECS task role (not the task execution role) for data-plane calls; EC2: use an instance profile; EKS: use IRSA (IAM Roles for Service Accounts) - never mount credentials as env vars or files into pods.
- Never store application secrets (DB passwords, API tokens, TLS certs) in plaintext Lambda or ECS environment variables - retrieve them at runtime from Secrets Manager or SSM Parameter Store using the task/execution role.
- Rotate or eliminate every existing static credential; treat any discovered static key as a P0 incident requiring immediate revocation.
- Cross-account access must use AssumeRole with a customer-supplied ExternalId; never share account-local roles across trust boundaries.
- Require MFA for all human IAM users with console access and for any sensitive API call (iam:*, sts:AssumeRole on break-glass roles) via aws:MultiFactorAuthPresent condition.

##### Least-Privilege Policy Authoring

- No Action:"*" and no Resource:"*" in any customer-managed policy - scope both to the minimum set of specific actions and exact ARNs.
- Never attach AWS-managed AdministratorAccess, PowerUserAccess, or AmazonS3FullAccess (or equivalent) to any workload role.
- S3 policies: restrict to the specific bucket ARN and prefix (arn:aws:s3:::bucket/prefix/*); separate read and write statements.
- KMS policies: grant kms:Decrypt/GenerateDataKey only to the specific key ARN; never kms:* on *.
- Secrets Manager / SSM Parameter Store: scope GetSecretValue or GetParameter to the exact secret/parameter ARN, not the entire namespace.
- DynamoDB: grant table-level actions (dynamodb:GetItem, PutItem, ...) on the specific table ARN; add index ARN when GSI access is required.
- SQS/SNS: grant sqs:SendMessage or sns:Publish to the specific queue/topic ARN only.
- Lambda resource-based policies: never grant lambda:InvokeFunction to Principal:"*"; scope to the specific service principal (e.g., apigateway.amazonaws.com) with a SourceArn condition.

##### Role Architecture

- One distinct IAM role per service, Lambda function, or Fargate task - never share a role across independent services or trust boundaries.
- Separate the ECS task execution role (ECR pull, CloudWatch Logs) from the ECS task role (application data-plane permissions).
- Use IAM permission boundaries when delegating role creation to application teams or CI pipelines; the boundary caps what the delegated admin can grant.
- Tag every role with Service, Owner, and Environment; enforce tagging via SCP or IAM condition (aws:RequestedRegion, aws:ResourceTag).
- Break-glass roles (emergency-access only) must be separately named, require MFA, have CloudTrail alerts on use, and be reviewed quarterly; they are the sole exception to the no-AdministratorAccess rule.

##### Conditions & Guardrails

- Add IAM condition keys wherever they reduce blast radius: aws:SourceVpc, aws:SourceVpce, aws:PrincipalOrgID, s3:prefix, kms:ViaService, sts:ExternalId.
- Policies grant nothing unless explicitly listed (implicit deny covers normal scoping); use explicit Deny statements for guardrails that must hold regardless of other grants - e.g., deny-unless-MFA, deny cross-region writes, deny disabling logging.
- Apply Service Control Policies (SCPs) at the OU level to enforce org-wide guardrails (e.g., deny IAM key creation, deny disabling CloudTrail).
- Use resource-based policies (S3 bucket policy, KMS key policy, SQS policy) as a second layer; they must not grant more than the identity policy.

##### Deployment & Pipeline Roles

- The CI/CD deploy role is a workload role and must follow the same least-privilege rules - no AdministratorAccess for Terraform/CDK pipelines.
- Scope the deploy role to only the CloudFormation/Terraform state actions and the specific resource types being deployed.
- Use short-lived OIDC-federated credentials in CI (GitHub Actions, GitLab CI) instead of long-lived IAM user keys.
- Never reuse a production deploy role for staging/dev; maintain environment-scoped roles.

##### Observability & Audit

- Enable CloudTrail in all regions with a multi-region trail; ship to an immutable S3 bucket in a dedicated logging account with MFA-delete and Object Lock enabled.
- Enable AWS Config with the iam-no-inline-policy, iam-policy-no-statements-with-admin-access, and access-keys-rotated managed rules.
- Enable IAM Access Analyzer for each account and for the organization; review and resolve all external-access and unused-access findings within 30 days.
- Review IAM policy diffs in every code review as a first-class artefact alongside application code; policy changes require a named approver.
- Use IAM policy simulator and Access Advisor to validate effective permissions before merging.

##### Reject if

- Any policy document contains Action:"*" or Resource:"*".
- A workload authenticates with a hardcoded access key ID / secret key in code, config, Dockerfile, or CI secret.
- Application secrets (passwords, tokens) are stored as plaintext Lambda or ECS environment variables instead of Secrets Manager or SSM Parameter Store.
- Credentials are mounted as environment variables or files into EKS pods instead of using IRSA.
- AdministratorAccess, PowerUserAccess, or equivalent broad managed policy is attached to any non-break-glass role.
- Two independent services or functions share the same IAM role.
- A cross-account AssumeRole trust is configured without an ExternalId condition.
- A deploy/pipeline role has broader permissions than the resources it provisions.
- CloudTrail is disabled, single-region only, or logging to a bucket in the same account with no MFA-delete protection.
- Any KMS, S3, or Secrets Manager policy grants access to * (all resources in that service namespace) rather than specific ARNs.
- A Lambda resource-based policy grants invoke to Principal:"*" without a restricting SourceArn condition.
- A permission boundary is absent when a role is delegated creation rights to application teams.
- IAM policy changes are merged without a named reviewer sign-off in the pull request.

#### observability-aws - Structured observability standards for AWS workloads

*Source: standards/cloud/aws/observability-aws.md*

Defines mandatory logging, tracing, metrics, and alerting patterns for Lambda and Fargate services. (Operational Excellence)

##### Logging

- Emit structured JSON to stdout/stderr on every request and error; never use unstructured string logs.
- Lambda: logs flow to CloudWatch Logs automatically; no additional agent required.
- Fargate: configure the `awslogs` log driver or FireLens (Fluent Bit) sidecar to ship container stdout/stderr to CloudWatch Logs.
- Set an explicit log group retention policy on every log group (e.g., 7, 14, 30, 90 days); never leave the default "Never expire" - it creates unbounded cost.
- Never log PII, credentials, tokens, or secrets; scrub before emission.
- Every log line must carry: `service`, `environment`, `correlation_id` / `request_id`, `x_amzn_trace_id`, `level`, `timestamp` (ISO-8601 UTC), and `message`; field names must be identical across all services to enable cross-service log insights queries.
- Suppress DEBUG logs in production via log-level environment variable; do not gate on runtime conditionals that still serialise the payload.

##### Sensitive Data Protection (defense in depth)

- Enable **CloudWatch Logs Data Protection** policies on every log group that can carry user data, as a defense-in-depth backstop to code-level scrubbing. The data-protection policy audits and masks sensitive data in log events at the platform layer, after the application has already scrubbed at emission.
- Configure **managed data identifiers** for the relevant classes (credentials / AWS secret keys, email addresses, names, phone numbers, addresses) PLUS **custom data identifiers** (regex) for domain-specific PII the managed set does not cover (e.g. employee IDs, national IDs, internal account numbers).
- Set both a `Mask` audit action and a delivery destination for findings (a separate CloudWatch Logs group, S3, or Firehose) so masked-data events are tracked, not silently dropped.
- Code-level scrubbing (see `principles/logging.md` Redaction & Sanitization) remains mandatory and PRIMARY; Data Protection is the backstop. Never rely on Data Protection alone - it is pattern-based and will miss anything its identifiers don't match.

##### Secrets & Configuration

- Never store secrets, API keys, or credentials as plaintext Lambda environment variables or ECS task definition environment fields.
- Fetch secrets at cold-start (or on a TTL refresh) from AWS Secrets Manager or SSM Parameter Store using IAM role permissions; cache in memory for the lifetime of the execution environment.
- Rotate secrets via Secrets Manager rotation; do not bake secret values into container images or IaC state files.

##### Distributed Tracing

- Instrument all services with AWS X-Ray or OpenTelemetry (ADOT collector) - never leave tracing disabled in production.
- Lambda: enable active tracing (`TracingConfig: Active`) in the function configuration.
- Fargate: run the AWS Distro for OpenTelemetry (ADOT) sidecar container; export traces to X-Ray.
- Propagate the trace context header (`X-Amzn-Trace-Id`) across all synchronous and asynchronous boundaries: API Gateway, SQS, SNS, EventBridge, Step Functions.
- Annotate X-Ray segments with `service`, `environment`, and `version`; add metadata for slow-path debug fields only - not hot-path payload dumps.
- Set sampling to reservoir + fixed-rate (e.g., 1 req/s reservoir, 5% fixed rate) as a production baseline; never hard-code 100% sampling at high RPS; make the rate configurable via environment variable.

##### Metrics

- Emit all custom business and operational metrics using CloudWatch Embedded Metric Format (EMF) to stdout; never call PutMetricData synchronously in the hot path.
- Keep metric dimension cardinality bounded - no user IDs, request IDs, or unbounded enum values as dimension keys.
- Lambda: instrument `cold_start` (boolean flag and duration), `billed_duration_ms`, `concurrent_executions`, `throttles`, and `iterator_age` (stream sources).
- Fargate: enable CloudWatch Container Insights on the ECS cluster to capture CPU, memory, network, and storage per task/service.
- Publish SLO error-budget burn metrics (error rate, latency p99) as EMF so they appear in CloudWatch without extra API calls.

##### Alarms & Alerting

- Create CloudWatch Alarms for: error rate (Lambda Errors / Invocations > threshold), throttles, DLQ depth > 0, latency p99 breaching SLO, iterator age for stream consumers, and cold-start rate exceeding SLO threshold.
- Fargate: alarm on task CPU utilisation > 80%, memory utilisation > 80%, and ALB 5xx rate.
- Route all alarms to SNS topics; never hard-code email endpoints - use SNS subscriptions managed in IaC.
- Use composite alarms to reduce alert noise; avoid alarm storms from correlated signals.
- All alarms must define an explicit `TreatMissingData` policy (`notBreaching` for sparse metrics, `breaching` for heartbeat checks).

##### Dashboards

- Every service must have a dedicated CloudWatch Dashboard covering: request rate, error rate, latency percentiles (p50/p95/p99), and the top alarm states.
- Lambda dashboard: include cold-start rate and duration, concurrent executions, and throttle count.
- Fargate dashboard: include Container Insights task health, CPU/memory, and ALB metrics.
- Dashboards are IaC-managed (CloudFormation / Terraform); no manually created dashboards in production.

##### Cost & Cardinality Control

- Aggregate high-frequency metrics at the function/task level before emitting; never emit one metric per request as a PutMetricData call.
- Avoid creating new metric namespaces per deployment; reuse a shared namespace with service/environment dimensions.
- Review log volume monthly; apply sampling or log-level promotion to suppress verbose INFO logs from high-RPS paths.

##### Reject if

- Any log group has no explicit retention policy set (default "Never expire").
- Structured JSON log fields are missing `correlation_id`, `x_amzn_trace_id`, or `service`, or field names differ across services.
- PII or secrets appear in any log field, trace annotation, or metric dimension.
- A log group that can carry user data has no CloudWatch Logs Data Protection policy configured as a backstop (managed + custom data identifiers).
- Code-level scrubbing is omitted on the assumption that CloudWatch Logs Data Protection alone will mask sensitive data.
- Secrets or credentials are stored as plaintext Lambda environment variables or ECS task definition environment fields rather than fetched from Secrets Manager or SSM at runtime.
- X-Ray / ADOT tracing is disabled (`PassThrough` mode) in production Lambda or Fargate.
- Trace context is not propagated across an SQS, SNS, or EventBridge boundary.
- Custom metrics are emitted via synchronous PutMetricData calls inside the request hot path.
- A metric dimension uses an unbounded value (user ID, request ID, arbitrary string).
- No CloudWatch Alarm exists for the service's error rate, throttles, DLQ depth, or cold-start rate.
- Alarms route to a hard-coded email address rather than an SNS topic.
- CloudWatch Container Insights is disabled on the ECS cluster (Fargate workloads).
- Dashboards are created manually in the console and not represented in IaC.

#### Packaging & Deployment

*Source: standards/cloud/aws/packaging-deployment.md*

Defines enforceable rules for containerising workloads, Lambda packaging, IaC authoring, and CI/CD pipeline gates - tool-neutral (CDK / SAM / Terraform / CloudFormation all apply). (Operational Excellence)

##### Dockerfile

- Multi-stage build: a build/compile stage and a separate, minimal runtime stage; no build toolchains in the final image.
- Base image must be minimal/slim, distroless, or an official AWS base (e.g. `public.ecr.aws/lambda/python`); no full OS images unless explicitly justified.
- Pin the base image by digest (`@sha256:...`), not by a mutable tag such as `latest`.
- Final stage runs as a non-root, non-privileged user with a numeric UID; never `USER root`.
- No secrets, credentials, tokens, or `.env` files in any layer or passed as `ARG`/`ENV` at build time.
- Commit a `.dockerignore`; exclude `.git`, local config, test fixtures, and any file not needed at runtime.
- Final image size must be demonstrably minimised; remove package manager caches and temp files in the same `RUN` instruction that installs them.
- Define a container `HEALTHCHECK` (`HEALTHCHECK CMD ...`) so the orchestrator can detect unhealthy instances. (Reliability)
- Build images in CI only; never build production images on developer laptops.

##### Lambda Packaging

- Respect the 50 MB zipped / 250 MB unzipped deployment-package hard limits; treat 40 MB as the soft alarm threshold.
- Extract large, infrequently changing dependencies (ML libs, SDKs) into Lambda Layers; share layers across functions.
- For dependencies that exceed layer limits, switch to container-image packaging (`FROM public.ecr.aws/lambda/...`) - same Dockerfile rules apply above.
- Never include test frameworks, dev dependencies, or documentation in the deployment package.
- Pin all third-party dependency versions in the lock file that is committed to source control.
- Initialise database connections, SDK clients, and heavy objects outside the handler function (in the module/global scope) so they are reused across warm invocations; never open a new DB connection per invocation.
- Lambda deployment zips stored in S3 must use a private, non-public bucket with server-side encryption and versioning enabled.
- Lambda environment variables that hold sensitive config must be encrypted with a customer-managed KMS key; plaintext env vars must not contain credentials or tokens.

##### Infrastructure as Code

- Every cloud resource - compute, networking, IAM, storage, observability, DNS - is defined in IaC. Zero console click-ops in any environment.
- IaC is stored in the same repository as the service code it provisions (or a dedicated infra monorepo with clear ownership).
- IaC modules/stacks are parameterised by environment; no hard-coded account IDs, region strings, or environment-specific literals outside parameter/variable files.
- Environment parity: dev, staging, and production use the same IaC templates; only parameter values differ.
- IAM roles and policies for deployed resources are declared in IaC with least-privilege boundaries; no `*` resource or action without a documented justification. (Security)
- Fargate task definitions must declare a separate task execution role and a task role; neither may share permissions or be substituted with an admin role.
- State/lock files (Terraform) or changeset history (CloudFormation/CDK) must be stored in versioned, encrypted backends with access logging.

##### Artifact Immutability

- Build once, promote the same artifact (container image digest or Lambda zip hash) through dev -> staging -> production. Never rebuild for a later stage.
- Container images are tagged with the Git commit SHA and a semantic version; the `latest` tag is never deployed.
- Deployment manifests or IaC parameter files reference the immutable digest/hash, not a mutable tag.
- Artifact repositories (ECR, S3) have immutable-tag/object-lock settings and ECR Enhanced Scanning (scan on push) enabled for all production repositories.

##### CI/CD Pipeline Gates

- Pipeline executes in order: lint/unit tests -> integration tests -> dependency vulnerability scan (e.g. Trivy, Snyk, ECR Enhanced Scanning) -> image/package security scan -> deploy.
- A failing gate at any stage aborts the pipeline; no manual override to skip security scans in production.
- The deploy role assumed by the pipeline is a least-privilege IAM role scoped to exactly the resources it must modify; it is never an admin or power-user role. (Security)
- No human has standing write access to production infrastructure; all production changes flow through the pipeline.

##### Deployment Strategy & Rollback

- Production deployments use blue/green or canary strategy (e.g. CodeDeploy `LINEAR_10PERCENT_EVERY_1MINUTES` or equivalent); no all-at-once deploys in production.
- Lambda: use CodeDeploy with a deployment configuration and `BeforeAllowTraffic`/`AfterAllowTraffic` hooks to validate before shifting traffic.
- Fargate/ECS: use ECS blue/green deployment backed by a CodeDeploy deployment group; rollback trigger on CloudWatch alarm breach.
- Automated rollback is configured and tested; manual rollback is a documented break-glass procedure only.
- Canary alarms must define an explicit evaluation period and breach threshold before full traffic shift; alarms without thresholds are treated as absent.

##### Config & Secrets Injection

- Configuration (feature flags, endpoints, tuning values) is injected at deploy time via environment variables, Parameter Store, or AppConfig - never baked into the artifact.
- Secrets (credentials, API keys, certificates) are stored in Secrets Manager and referenced by ARN at runtime; never stored as plaintext in environment variables, SSM plain-text parameters, or source code. (Security)
- Rotation must be enabled in Secrets Manager for all long-lived credentials; disable rotation is a documented exception requiring security-team sign-off.

##### Reject if

- Dockerfile runs any process as root, omits a `HEALTHCHECK`, or pins the base image by a mutable tag instead of a digest.
- Any secret, credential, or token appears in a Dockerfile layer, build arg, committed config file, or plaintext Lambda/container environment variable.
- A cloud resource in any environment was created or modified via the AWS Console and not reflected in IaC.
- The CI/CD pipeline can be manually bypassed to deploy to production without a security scan passing.
- The deploy IAM role has `*` actions or `*` resources on any policy statement without a documented exception approved by the security team.
- Different artifact builds are used for different environments, or container images are deployed using a mutable tag (including `latest`).
- Lambda deployment package exceeds 40 MB without a documented justification and a layer/container-image refactor plan.
- Production uses an all-at-once deployment with no automated rollback configuration, or canary alarms lack explicit evaluation-period and threshold definitions.
- Lambda handler opens a new database connection on every invocation rather than reusing a module-level connection.
- Lambda deployment zips are stored in a public or unencrypted S3 bucket.
- ECR repositories used in production do not have immutable tags and scan-on-push enabled.
- Secrets Manager rotation is disabled for a long-lived credential without documented security-team sign-off.

#### resilience-aws - Resilience patterns for AWS services and SDKs (Reliability pillar)

*Source: standards/cloud/aws/resilience-aws.md*

##### SDK Retry Configuration

- Set the AWS SDK retry mode to `adaptive` (preferred) or `standard`; never leave it at `legacy`.
- Cap `max_attempts` at 3-5; unbounded retries amplify cascading failures under load.
- Apply retry config at client construction time - never override per-call except to disable retries for non-idempotent writes.
- Adaptive mode automatically reduces request rate on throttling; standard mode requires you to add jitter manually.

##### Throttling and Backoff

- Treat ThrottlingException, ProvisionedThroughputExceededException, RequestLimitExceeded, and TooManyRequestsException as retryable with full jitter exponential backoff.
- Full jitter formula: sleep = random(0, min(cap, base * 2^attempt)); cap at 20-30 s for interactive paths, 60-120 s for background workers.
- Lambda: surface throttle metrics via CloudWatch and set reserved concurrency + scaling alarms before they cascade to callers.
- Fargate: use ECS Service Connect or ALB-level retry only when the downstream operation is idempotent; never retry blind POSTs.

##### Idempotency

- SQS delivers at-least-once; every consumer MUST be idempotent - use a deduplication key (MessageDeduplicationId for FIFO, or a DynamoDB conditional write keyed on message ID).
- Async Lambda invocations and all event-source mappings (SQS, Kinesis, DynamoDB Streams, SNS) retry on function errors; design handlers to tolerate redelivery without side-effect duplication.
- Use DynamoDB conditional writes (ConditionExpression) or the AWS Lambda Powertools Idempotency utility backed by DynamoDB for all write operations that must not double-apply.
- Pass idempotency tokens (ClientToken / ClientRequestToken) on all service APIs that accept them (SFN, Bedrock, EC2 RunInstances, etc.).
- Never blindly retry a non-idempotent write (financial transaction, email send, external API POST) without first confirming the prior attempt definitively failed.

##### SDK Client Reuse

- Lambda: instantiate all SDK clients outside the handler function in the module/global scope so they survive across warm invocations.
- Fargate: instantiate clients once at process startup; share them across request goroutines/threads via connection pools.
- Never create a new SDK client inside a request handler, loop body, or per-invocation path - doing so leaks connections and defeats HTTP keep-alive.

##### Timeouts

- Set a tight SDK connect timeout (<= 1 s) on hot-path calls, where fast-fail-and-retry beats waiting. For cold-start or infrequent setup calls that traverse VPC interface endpoints (e.g. fetching SSM parameters or Secrets Manager values at process startup), a higher connect timeout (~ 3-5 s) is acceptable and often necessary - first-connection latency through a VPC endpoint can exceed 1 s, and a 1 s ceiling there causes spurious startup failures. Choose deliberately and document the value when it exceeds 1 s.
- Set the read/socket timeout to no more than the platform limit minus 1 s (Lambda: function timeout; Fargate: ALB idle timeout) - shorter than the platform ceiling, not equal to it.
- Lambda: total downstream call chain (retries x (timeout + backoff)) must fit inside the function timeout with headroom for teardown.
- Fargate: set HTTP client idle-connection timeout below ALB/NLB idle timeout (default 60 s) to avoid stale-connection errors under low traffic.

##### Multi-AZ and Stateful Dependencies

- Deploy all stateful dependencies (RDS, ElastiCache, OpenSearch, MSK) across >= 2 AZs; use Multi-AZ or cluster mode - single-AZ is a hard reject.
- Route traffic through regional endpoints, not AZ-specific DNS, so the SDK/client transparently fails over.
- For RDS with Lambda, front every database with RDS Proxy - Lambda's concurrency model exhausts direct connection limits; RDS Proxy is not optional.
- For Fargate long-running services, enable RDS Proxy or PgBouncer when connection count per task x task count approaches the engine's max_connections.

##### Async Work and Dead-Letter Queues

- Every SQS queue that feeds a Lambda or Fargate worker MUST have a Dead-Letter Queue (DLQ) with a redrive policy (maxReceiveCount 3-5 for Lambda, tuned for Fargate workers).
- Set SQS visibility timeout to at least 6x the Lambda function timeout (minimum 30 s for Lambda ESM) to prevent premature redelivery during retries.
- Lambda async invocations (triggered by S3, SNS, EventBridge, or direct async invoke) have a built-in retry of 2 attempts; configure an on-failure destination (DLQ or EventBridge destination) on every async Lambda invocation path so events are never silently dropped after exhausted retries.
- SNS subscriptions that deliver to Lambda must also have a dead-letter queue configured on the subscription to capture delivery failures.
- Monitor DLQ depth via CloudWatch alarm; a non-zero DLQ depth must page on-call.

##### Partial Batch Failure Reporting

- Lambda SQS event source: enable ReportBatchItemFailures and return batchItemFailures in the response so only failed message IDs re-enter the queue; never throw an unhandled exception that fails the entire batch.
- Lambda DynamoDB Streams / Kinesis: return batchItemFailures with the sequence number of the first failed record; also enable BisectBatchOnFunctionError so a poison-pill record does not block the shard indefinitely.
- Fargate: implement equivalent partial-ack logic when polling SQS manually - delete only successfully processed messages; leave failures for redelivery.

##### Circuit Breaking

- Implement circuit-breaker logic (open/half-open/closed states) around all synchronous downstream HTTP/gRPC calls; do not let a degraded dependency saturate your thread pool or Lambda concurrency.
- Use AWS App Mesh, Resilience Hub fault injection, or a library such as Resilience4j / Polly for Fargate services.
- Lambda: use SQS or Step Functions as a buffer rather than calling a degraded downstream synchronously inside a tight retry loop.
- Set reserved concurrency on critical Lambda functions to prevent noisy-neighbour workloads from exhausting the account-level concurrency limit and throttling them.

##### Reject if

- SDK retry mode is `legacy` or retries are disabled on any client that calls AWS APIs.
- max_attempts is unbounded or set above 10 on any synchronous customer-facing path.
- A non-idempotent write is unconditionally retried without checking whether the first attempt succeeded.
- An SDK client is instantiated inside a Lambda handler function or inside a per-request code path in Fargate.
- RDS (PostgreSQL or MySQL) is accessed directly from Lambda without RDS Proxy.
- Any SQS queue that drives async workloads lacks a configured DLQ with a redrive policy.
- Lambda SQS event source does not use ReportBatchItemFailures, or Lambda DynamoDB-Streams/Kinesis event source lacks both ReportBatchItemFailures and BisectBatchOnFunctionError, causing entire batches or shards to stall on a single failed record.
- Any async Lambda invocation path (S3, SNS, EventBridge, direct async) lacks an on-failure destination or DLQ, allowing events to be silently dropped.
- Any stateful dependency (RDS, ElastiCache, MSK, OpenSearch) is deployed in a single AZ.
- SDK connect or read timeout is set equal to or greater than the Lambda function timeout or the Fargate ALB idle timeout.
- Throttling responses are swallowed or logged-and-ignored rather than retried with backoff.
- No circuit-breaker or bulkhead isolates a synchronous call to a downstream service whose SLA is weaker than the caller's.

#### AWS Well-Architected Deployment Readiness

*Source: standards/cloud/aws/well-architected.md*

Master checklist executed by the aws-readiness agent on every audit. Each pillar maps to hard gate criteria - a failed check blocks deploy.

---

##### Operational Excellence

- All infrastructure defined as code (CDK, Terraform, CloudFormation); no console-click resources in production.
- Structured logs (JSON) emitted to CloudWatch Logs or a centralised log aggregator; unstructured text logs are rejected.
- Deployments are small, reversible, and pipeline-gated; big-bang releases are prohibited.
- Every service has runbooks stored in version control and alarms wired to an SNS topic or PagerDuty integration.
- Change sets and deployment previews (`cdk diff`, Terraform plan) reviewed before apply in all non-dev environments.
- Distributed tracing enabled via AWS X-Ray (or OpenTelemetry with X-Ray backend) on every Lambda function and Fargate task; trace sampling rate >=5% in production.

---

##### Security

- All AWS API access uses IAM roles attached to compute (instance profile, task role, Lambda execution role); long-lived IAM user access keys are forbidden. See iam-least-privilege.md.
- Every role obeys least privilege: `*` actions and `*` resources require written justification and security sign-off. See iam-least-privilege.md.
- Secrets, connection strings, and API keys stored exclusively in AWS Secrets Manager or SSM Parameter Store (SecureString); plaintext in environment variables or source code is a blocker. See config-secrets-aws.md.
- All data encrypted at rest (KMS CMK or AWS-managed key) and in transit (TLS 1.2+); HTTP endpoints are rejected.
- Resources deployed into a VPC; public subnets limited to load balancers and NAT Gateways; compute in private subnets; Security Groups follow deny-by-default.
- VPC endpoints (Gateway type for S3 and DynamoDB; Interface type for SSM, Secrets Manager, ECR, and CloudWatch) provisioned so traffic to AWS services does not traverse the public internet or NAT Gateway.
- S3 buckets: Block Public Access enabled at account and bucket level; bucket policies deny non-HTTPS access.
- CloudTrail enabled in all regions; GuardDuty active; findings routed to the security event bus.
- EC2 and ECS-on-EC2 instances enforce IMDSv2 (HttpTokens=required); IMDSv1 is disabled to prevent SSRF credential theft.

---

##### Reliability

- Stateless services deployed across a minimum of two Availability Zones. Lambda is multi-AZ by default; Fargate task counts span at least two AZs explicitly. See resilience-aws.md.
- All outbound calls implement exponential backoff with jitter and a circuit breaker (AWS App Mesh, Resilience4j, or equivalent); raw retries without backoff are rejected.
- Lambda reserved concurrency set per function in production to prevent a single function exhausting the account concurrency limit and starving other services.
- Idempotency guaranteed for all write operations and message consumers; duplicate delivery must produce the same outcome.
- Health checks registered on every target group and ECS service; failing tasks replaced automatically.
- Every SQS queue that feeds a Lambda or Fargate worker has a Dead Letter Queue configured with a retention period of at least 14 days. See resilience-aws.md.
- Graceful degradation implemented via explicit timeouts and a fallback path (cached response, feature flag off, or degraded-mode response); "we log and hope" is not a degradation strategy.
- RDS/Aurora: Multi-AZ enabled; automated backups retained >=7 days; point-in-time recovery tested in runbook.

---

##### Performance Efficiency

- Compute target chosen deliberately: Lambda for event-driven/short-duration workloads; Fargate for long-running, CPU/memory-intensive, or streaming tasks. See compute-targets.md.
- AWS SDK clients and database connection pools initialised outside the Lambda handler (module scope) or shared across Fargate request lifecycle; cold-path initialisation penalises every invocation.
- RDS Proxy or PgBouncer used when Lambda functions connect to RDS/Aurora to prevent connection exhaustion under burst concurrency.
- Caching applied at the appropriate layer: ElastiCache (Redis/Memcached) for hot data, DAX for DynamoDB read amplification, CloudFront for edge-cacheable responses.
- Async and event-driven patterns preferred for non-latency-critical workloads; synchronous chains longer than three hops reviewed for decoupling via SQS/SNS/EventBridge.
- Lambda: memory allocation tuned with Lambda Power Tuning before production; default 128 MB is not acceptable for compute-heavy functions.
- Fargate: CPU and memory values profiled under load; over-provisioning by more than 50% above p99 peak triggers right-sizing.

---

##### Cost Optimization

- Every resource tagged with `Environment`, `Service`, `Owner`, and `CostCenter`; untagged resources fail the tagging gate. See cost-tagging.md.
- Right-sizing validated at least once per quarter; Cost Explorer anomaly detection enabled with an alert threshold.
- Pricing model matched to workload: Savings Plans or Reserved Instances for predictable baseline; Spot for fault-tolerant batch; On-Demand only for spiky or unpredictable load.
- CloudWatch Logs retention set explicitly (<=30 days for non-compliance logs, <=90 days for access logs); indefinite retention rejected.
- S3 lifecycle policies defined on every bucket: transition to Intelligent-Tiering or Glacier after a documented period; no bucket may have zero lifecycle rules in production.
- No idle resources: unused NAT Gateways, stopped EC2 instances, orphaned EBS volumes, and empty load balancers flagged and removed within one sprint.
- Lambda: provisioned concurrency used only when cold-start p99 SLO breach is demonstrated; pre-warming without measurement is rejected.

---

##### Sustainability

- Serverless and managed services (Lambda, Fargate, Aurora Serverless, DynamoDB) preferred over self-managed EC2 fleets where the workload fits; justify EC2 in the ADR.
- Graviton (ARM64) instances and Lambda architectures used for new workloads unless a third-party binary dependency prevents it.
- Batch and background jobs scheduled for off-peak hours where latency SLOs permit, reducing peak-demand resource provisioning.
- Over-provisioned resources reviewed at each architecture review; idle capacity that cannot be justified is decommissioned.

---

##### References

- AWS Well-Architected Framework overview: https://aws.amazon.com/architecture/well-architected/
- Framework whitepaper: https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html
- Sibling standards: iam-least-privilege.md, config-secrets-aws.md, resilience-aws.md, compute-targets.md, cost-tagging.md

---

##### Reject if

- Any IAM long-lived access key is created or used by a compute workload.
- A secret, password, or API key appears in source code, a CloudFormation/CDK parameter in plaintext, or an unencrypted environment variable.
- A service is deployed into a single Availability Zone with no documented exception.
- An SQS-triggered Lambda or Fargate consumer has no Dead Letter Queue configured.
- Any resource is missing one or more mandatory cost tags (`Environment`, `Service`, `Owner`, `CostCenter`).
- Infrastructure was created or modified via the AWS Console without a corresponding IaC change committed to version control.
- CloudWatch Logs retention is set to "Never expire" on any log group.
- A public S3 bucket exists without an approved exception signed by the security owner, OR any S3 bucket has no lifecycle policy in production.
- An HTTP (non-TLS) endpoint is exposed on any load balancer listener or API Gateway stage.
- A Lambda function uses default 128 MB memory allocation in a production environment without a Power Tuning result on file.
- A Fargate service runs with fewer than two tasks across fewer than two AZs in any environment receiving production traffic.
- A deploy proceeds without a `cdk diff` / Terraform plan review in staging or production.

### 3.10 Automation (AGITS projects only)

#### Agent and LLM Boundary Standards

*Source: standards/automation/agent-llm-boundary.md*

Governs how LLM agents interact with the deterministic core in the AGITS platform.

##### The Boundary Rule

- The LLM planning layer terminates at the IR document boundary.
- Agents (in `agits/agents/`) produce structured output; they never drive a browser, write files to disk, or execute shell commands.
- Everything downstream of the IR boundary - action library, wait chain, healer Tier 1, step definitions - is LLM-free by construction.

##### LLM Call Discipline

- Every LLM call must be guarded: check `ctx.llm.available` before calling; return a graceful `AgentResult` when unavailable.
- Never embed prompt text inline in the calling function; store prompts in named constants or in the agent's skill Markdown file.
- LLM responses are parsed with a typed parser; raw `json.loads(llm_response)` with no validation is rejected.
- The LLM is asked for structured output; the schema is always validated before the output is used.

##### Skill Files (Fable-5 authored)

- Skill files live in `agits/skills/` as `*.md` files with YAML frontmatter.
- Skill files are authored by fable-5 and reviewed by opus-4.8; sonnet and haiku must not author or modify skill files.
- A skill file defines: name, purpose, input schema, output schema, and the prompt template - nothing else.
- Skill files must not contain Python code; they are prompt-and-contract definitions only.

##### Failure Classification and Learning

- When a test execution fails, `LearningAgent` classifies the failure using `FailureAnalyst`.
- Only failures classified as `LOCATOR_ISSUE` or `SCRIPT_ISSUE` create a `Learning` row; all other categories are logged and skipped.
- Learning rows have `status=pending`; they require explicit admin approval before any library patch is applied.
- No agent may apply a library patch without an approved `Learning` row - the approval gate is mandatory.

##### Agent Output Must Be Auditable

- Every agent write (Learning, ImprovementSuggestion, IRDocument update, RequirementGap) must be paired with an `AuditLog` row and an outbox event in the same PG transaction.
- Agent actions must never be silent; every decision that changes PG state must produce a human-readable `summary` in the `AgentResult`.

##### Reject if

- An agent directly instantiates another agent and calls `run()` on it (chain must go through PG).
- `ctx.llm.complete(...)` is called without first checking `ctx.llm.available`.
- An LLM response is used without schema validation.
- A `Learning` row with `status=pending` triggers an automatic library patch without admin approval.
- Prompt text is defined inline at the call site instead of in a named constant or skill file.
- An agent raises an exception to its caller instead of returning `AgentResult(ok=False, ...)`.
- A skill Markdown file is authored or modified by a model other than fable-5.

#### IR (Intermediate Representation) Contract Standards

*Source: standards/automation/ir-contract.md*

Governs the versioned JSON contract that separates LLM output from deterministic execution in the AGITS platform.

##### The IR as the Sole Interface

- The IR document is the ONLY wire between the LLM planning layer and the execution layer.
- No LLM output may pass directly to Selenium or any action; it must first be validated as a well-formed IR document.
- The IR version field (`ir_version`) is a semantic version string; execution refuses to run an IR with a version it does not support.

##### Vocabulary Enforcement

- The `ACTION_VOCABULARY` frozenset is the single authority for permitted action names: `{click, fill, select, hover, navigate, upload, assert, wait, scroll}`.
- Any IR step whose `action` field is not in `ACTION_VOCABULARY` must be rejected at validation time with a typed `IRValidationError`, not silently dropped.
- New actions require a new version of the vocabulary, a new `ActionDef` row in PG, and a graph node - they cannot be added by editing a single call site.

##### IR Document Shape

- Every IR document must carry: `ir_version`, `test_case_id`, `title`, `metadata`, and `steps`.
- Every IR step must carry: `action`, `locator` (with `strategy`, `value`, `frame_path`, `window_hint`), `data`, `evidence_policy`, and `oq_step_ref`.
- Optional fields may be absent; required fields must be present and non-null - the validator raises on any missing required field.
- `evidence_policy` is a closed enum: `always`, `on_failure`, `never`; any other value is rejected.

##### Serialization and Round-trip Integrity

- `IRDocument.to_dict()` and `IRDocument.from_dict()` must be inverse operations: `from_dict(to_dict(doc)) == doc` for all valid documents.
- Round-trip tests are mandatory; they are the acceptance test for any change to the IR contract.
- Never serialize an IR document to raw JSON with `json.dumps(dataclass_instance)` - always call `to_dict()` first to ensure the canonical representation.

##### Versioning and Persistence

- Every change to an existing test case's IR creates a new `TestCaseVersion` row in PG - it does not overwrite the previous version.
- `IRDocument.is_current = True` for the latest version; all prior versions have `is_current = False`.
- When a new IR version is persisted, all `ScriptFile` rows for the test case must be invalidated (`is_current = False`) so the workspace is regenerated before the next run.

##### Outbox Coupling

- Every IR document write (create or update) must emit an `ir_document.updated` outbox event in the same PG transaction.
- The outbox event payload carries at minimum `test_case_id` and `ir_version`.
- Neo4j projection of IR changes is handled by the outbox worker; the IR layer must not call `GraphRepository` directly.

##### Reject if

- An action name outside `ACTION_VOCABULARY` is accepted by the validator without raising.
- An IR document is persisted without a matching `TestCaseVersion` row.
- An IR document is stored without emitting an outbox event in the same transaction.
- `is_current = True` exists on more than one `IRDocument` row for the same `test_case_id` after any write path.
- A step definition or action function accepts a raw `dict` where it should accept a typed `IRStep`.
- The IR serialization round-trip produces a different document than the input.

#### Outbox Pattern + PG->Neo4j Sync Standards

*Source: standards/automation/outbox-graph-sync.md*

Governs the transactional outbox, the background sync worker, and the dual-store read layer in AGITS.

##### PG is System of Record

- PostgreSQL is always written first; Neo4j is derived data.
- No fact may exist in Neo4j that is not already persisted in PG.
- If a PG write fails, no outbox event is emitted and no graph write occurs - the operation is atomic or nothing.

##### Outbox Event Emission Rules

- Every PG write that must be reflected in Neo4j must pair its insert/update with an `outbox_events` row in the SAME database transaction.
- Use `emit_event(session, aggregate_type, aggregate_id, event_type, payload)` - the helper that inserts the `OutboxEvent` row. Never write to `outbox_events` with raw SQL outside this helper.
- The call to `emit_event` must appear BEFORE `session.commit()` in the same `with`/`try` block; post-commit emission is a correctness defect.
- Outbox events are delivered at-least-once; handlers must be idempotent (MERGE, not INSERT, in Neo4j).

##### Outbox Worker

- The worker is the ONLY component that reads `outbox_events` and calls `GraphRepository` write methods.
- The worker marks events `processed` on success; `failed` after the configured maximum retries.
- The worker must not raise an unhandled exception to its thread - log and mark `failed`, continue to the next event.
- Neo4j downtime must not block the PG write path; the worker retries independently on a schedule.
- Each handler registered with `register_handler(event_type, handler_fn)` must be idempotent; test idempotency by calling the handler twice and asserting identical graph state.

##### GraphRepository Read Fallback

- All Neo4j reads in the repository layer must have a PG fallback: try Neo4j first; on `ServiceUnavailable` or timeout, query PG and log a warning.
- The fallback must never silently degrade to returning an empty result when PG has data - it must actually query PG.
- Do not retry the Neo4j read inside the fallback path; fall through to PG immediately on any connection error.

##### Cypher Query Safety

- All Cypher queries use parameterized syntax exclusively: `$param_name` placeholders, never f-string or concatenated Cypher.
- Node labels and relationship types are constants (defined in `graph_schema.py`); they must not be constructed from user-supplied strings.
- MERGE is used for upserts; CREATE is used only when uniqueness is guaranteed; never INSERT equivalent.
- Every Cypher write is preceded by a MERGE on the node's unique constraint property so duplicate-event replay produces the same graph state.

##### Dual-Store Consistency Guarantees

- PG and Neo4j are eventually consistent; code must not assume they are immediately consistent after a write.
- Test the read fallback explicitly: mock Neo4j to raise `ServiceUnavailable`; assert PG fallback is invoked and returns correct data.
- Do not JOIN across PG and Neo4j in application code; each store is queried independently.

##### Reject if

- An `OutboxEvent` row is inserted outside the same transaction as the business data it tracks.
- The outbox worker calls `GraphRepository` inside the same transaction used for marking the event processed (the worker's PG write and the Neo4j write must be decoupled).
- A Cypher query contains f-string or string-concatenation of user-controlled or runtime-computed values.
- A `GraphRepository` method is called directly from service or route code instead of going through the outbox worker.
- The read fallback returns an empty result when PG has data (silent degradation).
- An outbox handler is not idempotent (calling it twice on the same event produces a different graph state than calling it once).

#### Selenium + Behave Automation Standards

*Source: standards/automation/selenium-behave.md*

Core rules for the AGITS action library, step definitions, page objects, and test execution layer.

##### The Golden Constraint: LLM Never Executes

- No LLM call - `llm.complete`, `ctx.llm`, any Anthropic/OpenAI SDK call - may appear inside:
  - The action library (`automation_workspace/core/actions/`)
  - The wait chain (`automation_workspace/core/waits.py`)
  - The Tier 1 healer (`automation_workspace/core/healing/tier1_healer.py`)
  - Any behave step definition (`automation_workspace/steps/`)
  - The behave `environment.py` hooks
- LLM calls are permitted ONLY in: `agits/agents/`, `agits/emitters/` (code-generation phase), and `automation_workspace/core/healing/tier2_healer.py` (with explicit fallback).
- Reviewer: grep for `llm` in the files above and reject immediately on any hit.

##### Action Library Rules

- Every action module owns exactly one action verb (one file per verb in `core/actions/`).
- Every action function signature: `def run(driver, locator, data, evidence_policy, ctx) -> ActionResult`.
- Return `ActionResult` always - never raise from an action; capture the exception and set `ActionResult.ok = False`.
- The `dispatch` function in `core/actions/__init__.py` is the ONLY caller of individual action modules; step definitions call `dispatch`, not individual action files directly.
- Action functions must not contain `time.sleep`; use the wait chain (`wait_exists`, `wait_visible`, `wait_clickable`).

##### Locator Strategy

- Prefer ID, then `data-testid`, then aria-label, then CSS, then XPath - in that order.
- XPath built for the Tier 1 healer must follow the 9-level ladder defined in `tier1_healer.py`; ad-hoc XPaths in step definitions are rejected.
- Locators are data - they live in the IR document and in page-object locator maps; they must not be hardcoded inside action functions or step definitions.

##### Wait Chain

- Always call the wait chain before any interaction with a DOM element.
- Default strategy per action: `click` -> `wait_clickable`; `fill` -> `wait_visible`; `assert` -> `wait_exists`.
- No implicit WebDriver wait (`driver.implicitly_wait`) and no `time.sleep` - all waits go through the explicit wait chain.
- Configure timeout via `config/settings.yaml`; never hardcode timeout values.

##### Evidence Policy

- Evidence collection is controlled by the `evidence_policy` field in the IR step: `always`, `on_failure`, `never`.
- Screenshots are taken via the CDP `Page.captureScreenshot(captureBeyondViewport=True)` path, not `driver.save_screenshot`.
- Evidence files are written to the path supplied by `ctx.evidence_dir`; no hardcoded paths.

##### Step Definitions

- Step definitions are THIN: one line that calls `dispatch(...)` or `assert_*`; zero business logic.
- Step text is generated from the IR document by the emitter; step definitions must never be hand-authored for individual test cases.
- `@given`, `@when`, `@then` decorators match parameterized patterns, not literal test-case text.

##### Page Objects

- Page objects own locator maps only - no wait logic, no assertion logic.
- Locators are class-level constants; page objects have no instance state and no `__init__` beyond a driver reference.
- Page objects must not call the wait chain directly; callers (step definitions via dispatch) own the wait.

##### Reject if

- Any LLM call appears inside an action module, wait module, step definition, or Tier 1 healer.
- An action function raises an exception instead of returning `ActionResult(ok=False, ...)`.
- `time.sleep` or `driver.implicitly_wait` appears anywhere in the automation workspace.
- A locator value is hardcoded inside an action function or step definition instead of coming from the IR or a page-object locator map.
- A step definition contains conditional logic (if/else, try/except) beyond a single `dispatch` call.
- Evidence is written to a hardcoded path instead of the context-supplied `evidence_dir`.

## 4. Module templates (rules only)

### Application Module Template

#### What belongs here

Application-specific applicability rules, retrieval and ingestion profiles, execution
policy, evidence presentation, prompts, and the application handler registration.

#### What does not belong here

Document extraction, chunking, embedding, vector search, reranking, deduplication,
context expansion, generic ingestion workflow, authorization mechanics, caching, audit,
or answer infrastructure. These are platform modules that the application composes
through public APIs.

#### Handler shape

```python
class ApplicationHandler(Protocol):
    @property
    def application_id(self) -> str: ...

    async def discover_resources(
        self, request: ResourceDiscoveryRequest
    ) -> ResourceDiscoveryResult: ...

    async def execute(
        self, request: ExecutionRequest
    ) -> ExecutionResult: ...
```

### LangGraph Module Template

A small subgraph (about six nodes or fewer, under about 250 lines) may keep a single
nodes.py. Split it by concern as soon as either limit is exceeded.

#### Rules
- State is typed and holds no SDK clients or open connections.
- Nodes are thin adapters that delegate to application service interfaces and ports.
  They never import implementations or adapters.
- Edges are pure, synchronous predicate functions with no I/O. Every loop is bounded
  here: an attempt cap and a remaining-deadline check.
- Fan-out uses Send, with an Annotated list reducer for results and max_concurrency on
  the run configuration.
- Group nodes by concern; one oversized nodes.py is rejected.
- The compiled graph is built once, in the service implementation constructor or a
  bootstrap graph factory, never per request.
- No network I/O at import time.
- Progress event types are typed and documented.
- The service that runs the graph follows
  standards/architecture/service-interface-implementation.md: the interface in
  application/<name>.py, the graph-running implementation in
  application/impl/<name>_impl.py.

#### Node shape
```python
class PlanningNodes:
    """Planning-stage nodes; dependencies are service interfaces."""

    def __init__(self, planner: PlannerService) -> None:
        self._planner = planner

    async def plan(self, state: AskState) -> dict[str, object]:
        """Delegates planning to the planner service."""
        plan = await self._planner.plan(state["question"], state["deadline"])
        return {"plan": plan}
```

#### Edge shape
```python
def after_search(state: RetrievalState) -> str:
    """Strong grade finishes; weak grade rewrites while allowed, else no answer."""
    if state["grade"] >= state["profile"].min_grade:
        return FINISH
    if state["rewrites"] < state["profile"].max_rewrites and state["deadline"].has_at_least(8.0):
        return REWRITE
    return NO_ANSWER
```

### Placeholder Adapter Template

A placeholder contains:

- An implementation of the production outbound port.
- Typed adapter settings.
- Stable reason and outcome codes.
- Capability metadata that identifies placeholder mode.
- Structured, sanitized logging.
- Contract tests shared with the future production adapter.
- A production-profile prohibition where the capability must be real before launch.

A placeholder must not contain a method that raises `NotImplementedError`, wildcard
access grants, fabricated confidence, a claimed durable audit write, or an
unauthenticated download URL.

#### Replacement procedure

1. Implement the production adapter against the same port.
2. Run the existing contract tests unchanged.
3. Change the bootstrap provider selection.
4. Remove the production-profile prohibition.
5. Leave every consumer untouched.

If step 5 requires a consumer change, the port was wrong, not the placeholder.

### Platform Module Template

#### Required artifacts
- Every application service as an ABC interface plus one Impl
  (standards/architecture/service-interface-implementation.md).

- A public API with intentional exports and a documented stability contract.
- A manifest declaring module identifier, version, provided capabilities, required
  capabilities, router prefix, and enabled flag.
- A README covering responsibilities, explicit exclusions, ports, settings, error codes,
  emitted events, and an example composition.
- Contract tests for every outbound port that has more than one implementation.
- Architecture test registration for the module boundary.

#### Example public API

```python
from modules.retrieval.domain.models.query import RetrievalRequest
from modules.retrieval.domain.models.result import RetrievalResult
from modules.retrieval.ports.inbound.retrieval_use_case import RetrievalUseCase

__all__ = ["RetrievalRequest", "RetrievalResult", "RetrievalUseCase"]
```

## 5. Checklists

### New Platform Module Checklist

- The responsibility is one reusable capability, describable without the word "and".
- `public.py`, `manifest.py`, and `README.md` exist.
- Domain, application, ports, adapters, configuration, and tests are separated.
- Every application service is an ABC interface with one Impl under application/impl/.
- Any branching, looping, or fan-out flow is a LangGraph graph with bounded loops.
- Dependencies point inward; no domain import of adapters or infrastructure.
- Cross-module consumers use the public API only.
- Every external client sits behind an outbound port.
- Placeholder behavior, if used, is typed, explicit, and safe.
- Configuration is typed and injected; no literals in business logic.
- Architecture tests include the new module boundary.
- Unit and contract tests pass.
- Source and templates are ASCII only.

### New Application Checklist

- The manifest declares application identifier, supported intents, resource types,
  operations, and handler class.
- Application-specific behavior is separated from reusable platform capability.
- A resource semantic-profile builder is defined.
- An ingestion strategy or profile is selected.
- Resource-discovery and execution handlers are implemented.
- The authorization policy is explicit and default deny.
- The evidence mapper is typed.
- Taxonomy is optional unless an approved business requirement states otherwise.
- The application imports platform-module public APIs only.
- The application does not import another application package.
- Golden questions and negative authorization cases exist.

### Code Review Checklist

- The correct module owns the change.
- Public contract impact is identified and versioned if breaking.
- Domain and application code contain no concrete SDK imports.
- Routers and graph nodes are thin.
- New or changed service methods are declared abstract on the interface first, and no
  consumer depends on an Impl.
- Graph loops are bounded in edges.py, and fan-out uses Send with max_concurrency.
- All boundary data is typed and validated.
- Exceptions are typed, chained with `from`, and logged once at the boundary.
- Mandatory authorization filters are applied before retrieval or execution.
- Async calls have timeouts and bounded concurrency.
- No print statements, emojis, decorative Unicode, or dumping-ground modules.
- No mutable default arguments.
- Tests cover success, empty, boundary, timeout, cancellation, and dependency failure.
- Architecture and contract tests pass.

### Production Readiness Checklist

- No prohibited placeholder is enabled in the production profile.
- Health and readiness expose critical adapter state without leaking secrets.
- Configuration is validated before the listener accepts traffic.
- Logs are structured, sanitized, and correlated.
- Metrics and traces cover module boundaries and error paths.
- Authorization, audit, and guardrails fail closed where required.
- Rate, payload, fan-out, candidate, token, and concurrency limits are configured.
- Vector-store payload schema and collection aliases are versioned.
- Backup, restore, rollback, and migration procedures are documented and tested.
- Golden, integration, security, resilience, and load tests pass.

