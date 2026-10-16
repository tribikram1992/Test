# Netra 2.0 Architecture

Layout vocabulary: `common`, `infrastructure`, `modules/<name>/{api,services,repositories,clients,models,constants,...}`, `apps`, `bootstrap`.
The older scaffold names (kernel, platform, ports, adapters, public.py) are gone; the rules below are unchanged.

Audience: engineers building Netra and the RBAC workstream plugging into it. This document is
the map; module READMEs are the territory. Every boundary rule here is machine-enforced by
`import-linter` contracts in `pyproject.toml` and the gate in `tests/architecture/`.

## 1. Goals and constraints

| Goal | Where it is enforced |
|---|---|
| Production-grade from day one; no POC shortcuts | gates: ruff, mypy --strict, pytest, import-linter, architecture tests |
| Modular monolith now, services later without rewrites | ADR-0001; ports at every module edge; schema-per-module data ownership |
| Java-style `XService` interface + one `XServiceImpl` | `application/<n>_service.py` (ABC) + `application/impl/<n>_service_impl.py`; tested |
| SOLID where it applies | section 4 |
| Typed errors, central public error codes, module reason codes | `kernel/errors.py`, `kernel/error_codes.py`, `modules/*/constants/codes.py` |
| No `None` as a signal; absence is typed | `kernel/contracts/outcome.py`; `NotFoundError` for lookups; Pydantic at boundaries |
| Constants package per module | `modules/*/constants/` (`codes.py`, `limits.py`, `formats.py`, `thresholds.py`) |
| API endpoints and contracts per module | `modules/*/api/{router,contracts}.py`, envelope and error boundary in `api/` |
| LangGraph for branching or looping flows, loops bounded in edges | `modules/retrieval/graph/`, template for `orchestration`, `sql_execution`, `ingestion` |
| Answer only from evidence; code builds citations and confidence | `kernel/contracts/evidence.py`; `answer` module contract |
| Access decided by deterministic filters inside the search, never by a model | `retrieval/domain/models.py::MandatoryFilter` |
| 20-40 s answers, never above 60 s | `kernel/deadline.py` propagated into every call; `MAX_REQUEST_DEADLINE_SECONDS` |
| Windows Server, Python 3.12, WinSW behind IIS/ARR | `api` and `worker` are two entry points of one package (section 9) |

Recorded owner decisions that this layout keeps (handoff Part A): LangGraph for orchestration,
Qdrant as the only vector store and never the system of record, audience-driven access, every
visible document downloadable, duplicates kept, invalid dates rejected not corrected, taxonomy
optional and never a hard gate.

## 2. Layering

```
src/netra/
  common/          shared building blocks: ids, clock, deadline, context, errors, error codes, types/
  infrastructure/  http client factory, retry, breaker, bulkhead, cache tiers, db pool + migrator + outbox relay, files, logging, web plumbing
  modules/         17 capabilities, one bounded context each (see FOLDER_STRUCTURE.md)
  apps/            ask_hr, jde, uploads: application-specific behaviour only; never import each other
  bootstrap/       settings, container (constructs every Impl, repository and client), api_app, worker, cli
```

Dependency direction (enforced):

- `common` imports nothing above itself.
- `infrastructure` never imports `modules`, `apps`, `bootstrap` (`infrastructure/web/auth.py` depends on the access *interface* only).
- `modules` never import `apps` or `bootstrap`; a module imports another module as `netra.modules.<name>` (its `__init__.py`), never deeper.
- `models/`, `services/` and `graph/edges.py` never import SDKs, drivers or FastAPI; those live in `clients/`, `repositories/`, `extractors/`, `readers/`, `api/`.
- Only `bootstrap` constructs `*ServiceImpl`, repositories and clients (architecture test).

## 3. Module anatomy (the contract every module follows)

```
modules/<name>/
  __init__.py            one-line purpose
  public.py              the only import surface: interfaces, domain types, errors, reason codes,
                         plus Impl and adapter classes exported for bootstrap
  manifest.py            module id, version, provides, requires, router prefix, enabled
  README.md              responsibilities, explicit exclusions, ports, settings, error codes, example composition
  constants/             codes.py (StrEnum reason codes), limits.py, formats.py / thresholds.py
  errors.py              AppError subclasses pinning a public ErrorCode; module reason attached
  config.py              frozen Pydantic settings slice; bootstrap composes it from the environment
  domain/                frozen dataclasses, StrEnums, pure functions; no SDK or framework types
  ports/outbound.py      narrow Protocols the Impl depends on; caller-owned, never mirroring a vendor SDK
  application/<n>_service.py        ABC: every public method abstract, documented Args/Returns/Raises
  application/impl/<n>_service_impl.py   the one Impl; constructor injection only
  adapters/              implementations of the ports; the only place SDKs are imported
  api/{router,contracts}.py          optional: build_router(service, current_caller) -> APIRouter
  graph/{state,edges,nodes,builder}.py   optional: LangGraph subgraph; edges pure and synchronous
```

`scripts/new_module.py module <name> "<responsibility>"` generates this shape; the generated
skeleton passes every gate as-is. `scripts/new_module.py app <name> ...` generates an application
package (manifest, skills, profiles, applicability).

Cross-module calls: a consumer declares an outbound port for what it needs (for example
`feedback/ports::MessageOwnershipPort`); bootstrap wires an in-process adapter over the provider's
public service. The consumer never imports the provider's internals, so the adapter can later be
an HTTP client without consumer changes.

## 4. SOLID, applied

- **Single responsibility**: a module is one capability describable without "and"; a service
  coordinates one use case; a node does one step.
- **Open/closed**: routing by data tables (`extraction/constants/formats.py::ROUTING_TABLE`),
  registries (`TabularReaderRouter`, skill descriptors), modes as enums (`DocStageMode`). Adding a
  format, reader or skill adds a row or a class; it does not edit the service.
- **Liskov**: every adapter passes the same contract tests as the production adapter; placeholders
  implement the full port and fail with typed reasons, never `NotImplementedError`.
- **Interface segregation**: ports are narrow (`CatalogSearchPort`, `ChunkSearchPort`,
  `RerankerPort`, `ParentSectionPort` instead of one "vector store" god-port).
- **Dependency inversion**: services depend on ABC interfaces and Protocols; only bootstrap knows
  the concrete classes.

## 5. Error handling

- Every exception derives from `kernel.errors.AppError`. A subclass pins a public `ErrorCode`;
  HTTP status, retryability and the safe user message come from `ERROR_CODE_REGISTRY`, never from
  literals at the raise site. `developer_detail` is logged once at the boundary and never returned.
- Public codes (`error.code` in responses) live only in `kernel/error_codes.py`; they are immutable
  once published. Module reason codes (quarantine reasons, cache misses, fallback levels) are
  diagnostics and live in `modules/*/constants/codes.py`. The architecture test fails if an
  `ErrorCode` lacks a registry entry.
- Adapters catch third-party exceptions and re-raise typed errors with `from exc`. A raw
  `httpx` or `asyncio.TimeoutError` never leaves an adapter; timeouts become
  `DependencyTimeoutError(operation, target, elapsed_seconds)`.
- `api/error_handlers.py` is the single boundary: `AppError` -> envelope, FastAPI validation ->
  `validation_failed` with field details, anything else -> `internal_error` logged at CRITICAL.
  Retryable 5xx responses carry `Retry-After`.
- Fail-closed by design: embedding, vector index, Postgres, audit, guardrail unavailable. Fail-open
  with a recorded penalty: reranker only.

## 6. Null handling

- Lookups raise `NotFoundError`; they never return `None`.
- Expected negative outcomes (cache miss, reranker unavailable, no evidence) are `Outcome[T]`
  (`Ok(value)` or `Miss(reason_code)`); callers must handle both branches (`is_ok` narrows).
- External input (HTTP bodies, OCR responses, spreadsheet cells, environment) is validated by
  Pydantic at the boundary; nullable wire fields are declared `X | None` and resolved there.
- Domain objects are frozen and constructed fully or not at all; `X | None` appears only where
  absence is a real state (for example `rerank_score: float | None` before reranking).
- Collections are returned empty, never `None`. Spreadsheet cells keep `None` for absent values
  and are omitted from the serialized text; empty strings are never fabricated.
- `mypy --strict` runs on the whole package in CI.

## 7. Constants and configuration

- No literal in logic: timeouts, limits, thresholds, statuses, header names and paths are named in
  a `constants/` package (module) or `kernel/constants.py` (cross-cutting).
- Closed sets are `StrEnum`s and are compared as enums, never as strings.
- Settings are frozen Pydantic models per module (`config.py`), composed in
  `bootstrap/settings.py` from `NETRA_*` environment variables (`__` nests). Startup fails fast on
  missing or invalid values and the production profile refuses prohibited placeholders.
- Thresholds in `retrieval/constants/thresholds.py` are initial values pending golden-set
  calibration; they are not production facts.

## 8. API contracts

- Success: `{data: ...}`; collections `{data: [...], meta: {has_more, cursor, total_count?}}`.
  Errors: `{error: {code, message, trace_id, details?}}`. Never a naked object or array.
- `X-Request-ID` is echoed (or generated) on every response; `traceparent` is accepted and its
  trace id is used for `trace_id`.
- Retryable `POST`s take `Idempotency-Key`; replay with the same payload returns the stored result,
  a different payload returns 422 `idempotency_conflict`.
- Versioned under `/api/v1`; fields are added, never renamed or retyped within a version.
- Routers are thin: deserialize, call the service, serialize. They raise; the boundary formats.
- Request and response models are separate classes; every field's nullability is explicit.

Endpoints in this scaffold: `POST /api/v1/chats/{chat_id}/messages/{message_id}/feedback`,
`POST /api/v1/tickets`, `GET /api/v1/tickets/{ticket_id}`, `GET /health/live`. The chat, history,
download and admin-ingestion endpoints are owned by `orchestration`, `chat_history`, `ingestion`
and mount the same way.

## 9. Data ownership, consistency and the path to services

- Each module owns one Postgres schema (`ingestion`, `chat_history`, `audit`, `feedback`,
  `ticketing`, `fast_lane`, `access`). No cross-schema foreign keys; references by id only.
  Migrations live with the module.
- Postgres is the system of record; the vector index and the ticket provider are projections.
  Any write that must reach another store goes through a transactional outbox row written in the
  same transaction, relayed by the worker with bounded retries and a dead-letter table, consumed
  idempotently. `ingestion/domain/publication.py` is the state machine the relay drives
  (`STAGED -> PENDING_APPROVAL -> PUBLISHING -> LIVE -> SUPERSEDED`), so nothing partially indexed is
  retrievable and there is no window with two or zero live versions.
- Two entry points, one package: `api` (stateless, load-shedding, 55 s deadline) and `worker`
  (outbox relay, upload sweep, reconciler). On Windows these are two WinSW services. SSE streaming
  through IIS/ARR requires response buffering disabled and idle timeouts above the deadline.
- Extracting a module into a service: (1) give its inbound service an HTTP/gRPC transport (its
  `api/router.py` already is one for user-facing modules); (2) replace consumers' in-process port
  adapters with client adapters implementing the same ports in bootstrap; (3) move its schema and
  migrations; (4) point its outbox relay at a broker instead of in-process handlers. No consumer,
  domain or service code changes.

## 10. Resilience

Every outbound call has explicit connect and read timeouts bounded by the request deadline
(`Deadline.bounded`). Retries only on transient, idempotent operations, exponential backoff with
full jitter, capped attempts. One circuit breaker per dependency with an `is_failure` predicate so
configuration errors (missing collection, 4xx rejections) never open it. Bulkheads per heavy
dependency (OCR, GPU models). Ingress load shedding returns 503 with `Retry-After`. Liveness and
readiness are separate; readiness checks pools, never upstream services.

## 11. Extraction (Baxi Extract first)

| Format | Route | Notes |
|---|---|---|
| PDF, DOC, DOCX, PPT, PPTX | Baxi Extract (OCR) | Office formats are converted to PDF by the service (`convert_to_pdf=true`) |
| PNG, JPEG | Baxi Extract (OCR) | Sent as-is |
| XLSX, CSV | local row readers | One `TABLE_ROW` block per row, headers attached to every value |
| TXT, MD | local reader | Paragraphs; Markdown headings detected |

Format is detected from content (signatures, zip members) before the extension is trusted.
Every file is fingerprinted (SHA-256) once; the fingerprint is the version identity and the
extraction-cache key, so re-runs never re-upload unchanged files.

Recorded limitation (ADR-0002): the OCR wrapper returns page text only. Headings, font signals
and slide notes are unavailable, so section chunking degrades to page-aware sliding windows and
each remote extraction carries the `layout_unavailable` warning. If the wrapper exposes Azure
Document Intelligence layout, only `adapters/baxi_extract/mapper.py` changes.

## 12. Retrieval (document-first, measured)

```
embed once -> mandatory filter from AccessScope
  stage A  catalog search (one point per document version; title + description + outline +
           generated "questions this answers" profile; hybrid dense + sparse)
  stage B  chunk search grouped per selected document (chunks_per_document each)
  grade    0.6 x top + 0.25 x mean(top 3) + 0.15 x coverage above the floor
  weak?    widen (top_k x factor + in-scope union, no LLM)  ->  rewrite (Sonnet, once, needs 8 s)
  finish   dedupe -> rerank (Miss = degrade, reranked=false) -> keep top k -> parent expansion ->
           retrieval strength -> evidence assembled by code with provenance and diagnostics
```

- `DocStageMode` per application: `gate` (handoff A5 as written), `boost` (union with the in-scope
  global search; selected-document chunks get a rerank bonus), `off` (control). Decide on the
  golden set, not by argument (ADR-0003).
- The mandatory filter (skill, current, approved, effective date, region, audience or access
  group) is applied inside every search and is only ever narrowed (`restricted_to`), never relaxed.
- Every fallback level used is in `RetrievalDiagnostics.fallback_levels`; silent fallback does
  not exist.
- Chunk accuracy levers in order of expected value: the context label embedded with every chunk;
  the document card with a content-derived profile (also flags shifted descriptions as a
  diagnostic); grouped stage B; reranking; sparse vectors at stage A for exact names and form
  codes. Contextual retrieval (an LLM-written one-line context per chunk at ingestion) is the
  next step if the golden set shows residual misses; it costs one cheap model call per chunk once.
- Speed: at this corpus size retrieval is sub-second; the 20-40 s budget is spent in the planner,
  rewrite and answer model calls. Optimise those before optimising vector search.

## 13. The RBAC seam

The platform receives an `AccessScope`; it never computes one. `modules/access` exposes
`IdentityProviderPort`, `ScopeResolverPort`, `RolePolicyPort` and the `AccessService` ABC.
Consumers (API auth dependency, graphs, downloads) depend on the ABC and on
`kernel/contracts/access.py` only. The RBAC workstream implements the three ports (Entra JWT,
Graph attributes, group-to-role map), passes the shared contract tests, switches the bootstrap
selection and removes the production-profile prohibition. See `modules/access/README.md`.

## 14. Testing and gates

- Unit tests through interfaces with fakes injected via public constructors; no monkey-patching.
- Adapter tests against mocked transports (`httpx.MockTransport`) and real temp files.
- Integration tests at persistence boundaries against ephemeral containers (Postgres, Qdrant); they
  need a Docker-capable CI runner.
- `tests/architecture` and `lint-imports` are blocking. A new boundary rule is mutation-checked:
  add a violation, see the gate fail, revert.
- Golden evaluation set per domain is the prerequisite for every threshold above.

## 15. What is implemented in this scaffold

Implemented and tested: kernel, platform resilience, `extraction` (Baxi adapter, tabular and text
readers, cache, service), `retrieval` (graph, scoring, service), `vector_index` (port, payloads,
Qdrant adapter), `access` (ports, service, placeholders), `feedback` and `ticketing` (services,
repositories, routers, contracts), `ingestion` publication state machine and reason codes, the API
boundary, bootstrap and worker entry points. Skeletons: `ingestion` services, `model_gateway`,
`guardrail`, `audit`, `chat_history`, `orchestration`, `answer`, `fast_lane`, `sql_execution`,
`vocabulary`, and the three application packages. The story-level status is in `STORY_MAP.md`.
