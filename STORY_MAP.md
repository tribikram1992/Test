# Story map: backlog ID -> owning module -> scaffold status

Status legend: **implemented** (working code in this scaffold), **contract** (interface, ports, constants
or state machine in place; Impl pending), **skeleton** (module shell only), **process** (not code),
**RBAC** (owned by the RBAC workstream through `modules/access` ports), **1.2.0** (exists in the
previous codebase and migrates into the named module).

| ID | Story | Module / owner | Status |
|---|---|---|---|
| FD-01 | Migrations framework + base schema | `platform/db` + per-module `migrations/` (schema per module) | contract |
| FD-02 | All tables from the data model | each owning module (`ingestion`, `chat_history`, `audit`, `feedback`, `ticketing`, `fast_lane`) | contract |
| FD-03 | Integrity rules + key indexes | owning modules; `vector_index/constants/collections.py` for payload indexes | contract |
| FD-04 | Controlled vocabulary + seed data | `modules/vocabulary` | skeleton (1.2.0) |
| FD-05 | Vector store provisioned | `modules/vector_index` (`netra init-collections`) | contract |
| FD-06 | Cache vector collection | `modules/vector_index` (`fast_lane` role) | contract |
| FD-07 | Config framework (nothing hardcoded) | `bootstrap/settings.py` + per-module `config.py` + `constants/` | implemented |
| FD-08 | Secret management | `SecretStr` fields; vault references in deployment | contract |
| IN-01 | Ingestion framework + handler registry | `modules/ingestion` + `extraction/constants/formats.py::ROUTING_TABLE` | contract |
| IN-02 | PDF extractor (text) | superseded: PDFs go to Baxi Extract (ADR-0002) | implemented |
| IN-03 | OCR via Baxi Extract adapter | `extraction/adapters/baxi_extract` | implemented |
| IN-04 | PowerPoint extractor | Baxi Extract (converted to PDF server-side); slide boundaries = pages | implemented |
| IN-05 | Excel extractor (row-level) | `extraction/adapters/tabular` (XLSX, CSV) | implemented |
| IN-06 | Normalized document model | `extraction/domain/models.py::ExtractedBlock` | implemented |
| IN-07 | Chunking: strategy selector | `modules/ingestion` (section / page_window / slide / row) | contract |
| IN-08 | Heading detection (PDF) | blocked by OCR page-text output; see ADR-0002 | contract |
| IN-09 | Section chunking + size/overlap | `modules/ingestion` | skeleton |
| IN-10 | Tables & lists handling | `modules/ingestion` | skeleton |
| IN-11 | Parent-child + context label | `modules/ingestion` (`parent_section`), consumed by `retrieval` | contract |
| IN-12 | Metadata file loader + validator | `apps/ask_hr` reader + `modules/vocabulary` | skeleton (1.2.0) |
| IN-13 | Metadata model + fan-out | `vector_index/domain/payloads.py` | implemented |
| IN-14 | Downloadable documents | withdrawn by decision A8 (every visible document is downloadable) | process |
| IN-15 | Bulk load: staged pipeline + CLI | `modules/ingestion` + `bootstrap/cli.py` | contract |
| IN-16 | Batching + checkpointing + idempotency | `modules/ingestion` (fingerprint = version identity) | contract |
| IN-17 | Embedding in ingestion (batched) | `ingestion/ports/outbound.py::EmbeddingPort` -> `model_gateway` | contract |
| IN-18 | Approve + publish transaction | `ingestion/domain/publication.py` + outbox (ADR-0001) | contract |
| IN-19 | Quarantine + run report | `ingestion/constants/codes.py::QuarantineReason` | contract |
| IN-20 | SharePoint / Employee Connect source | `modules/ingestion` source adapter (stream B) | skeleton |
| UI-01..UI-04 | Admin ingestion screens | frontend; backed by `ingestion/api` | process |
| UI-05 | Admin ingestion endpoints | `ingestion/api` (router prefix `/admin/ingestion`) | skeleton |
| AC-01 | Entra SSO | `access/ports::IdentityProviderPort` | RBAC (done) |
| AC-02 | Access resolution chain | `access/ports::ScopeResolverPort` (`ScopeSource` recorded) | RBAC |
| AC-03 | Attribute -> retrieval filter + vocab map | `retrieval/domain::MandatoryFilter.from_scope` + `vocabulary` | implemented / RBAC |
| AC-04 | Session attribute cache | `access/config::session_scope_ttl_seconds` | RBAC |
| AC-05 | Microsoft Graph directory lookup | `access` adapter | RBAC |
| RT-01 | Embed query once, reuse | `orchestration` (vector carried in `RetrievalQuery`) | contract |
| RT-02 | Hybrid search + deterministic filter | `retrieval` (filter) ; sparse vectors at stage A: ADR-0003 | contract |
| RT-03 | Grade + corrective retrieval | `retrieval/graph/edges.py` (widen, then rewrite; bounded) | implemented |
| RT-04 | Rerank + parent expansion | `retrieval/graph/nodes.py::finish` | implemented |
| RT-05 | retrieval_strength + evidence by code | `retrieval/domain/scoring.py`, `kernel/contracts/evidence.py` | implemented |
| RT-06 | Answer generation (grounded, cited) | `modules/answer` | skeleton (1.2.0) |
| RT-07 | Confidence scoring | `modules/answer` (recalibrate; see handoff review 2b) | skeleton |
| RT-08 | HR domain handler | `apps/ask_hr` (`profiles.py`, `skills.py`) | skeleton |
| RT-09 | LLM gateway + model routing | `modules/model_gateway` | skeleton (1.2.0) |
| MS-01..MS-03 | Model serving / AI Hub access | infrastructure + `model_gateway` adapters | process |
| RC-01..RC-08 | Routing and chat | `modules/orchestration`, `modules/chat_history` | skeleton (1.2.0) |
| OR-01 | Orchestrator + request state | `modules/orchestration` graph | skeleton (1.2.0) |
| OR-02 | Failure mapping + error catalogue | `kernel/error_codes.py`, `api/error_handlers.py` | implemented |
| OR-03 | Token economics recording | `modules/model_gateway` + `audit` | skeleton |
| OR-04 | Public ask + endpoints | module-owned routers mounted by `bootstrap` | implemented (pattern) |
| OR-05 | Rate limiting | `api/middleware` (token bucket, per subject) | contract |
| GD-01 | BaxiGuard adapter | `modules/guardrail` | skeleton (1.2.0) |
| CA-01..CA-03 | Curated answers (OKF cards) | `modules/fast_lane` | skeleton (1.2.0) |
| TK-01 | Provider-neutral ticketing adapter | `ticketing/ports::TicketingProviderPort` + placeholder | implemented (placeholder) |
| TK-02 | Offer/confirm + duplicate prevention | `ticketing/application` (idempotency key, open-duplicate check) | implemented |
| AU-01..AU-03 | Audit | `modules/audit` | skeleton (1.2.0) |
| FE-01..FE-03, FE-05 | Frontend screens | frontend | process |
| FE-04 | Feedback control | `modules/feedback` | implemented (backend) |
| EV-01..EV-04 | Golden set, harness, grader, tuning | `tests/eval/` (to add) | process (blocking for thresholds) |
| EV-05 | Unit tests | `tests/` | implemented (pattern) |
| EV-06 | Integration tests (stub handler) | `tests/integration/` with testcontainers | contract |
| EV-07 | Access-control tests | `tests/modules/access/contract/` | RBAC |
| EV-08..EV-10 | Safety, load, UAT | process | process |
| DP-01..DP-05 | Environments, CI/CD, observability, backup, runbook | process; `tests/architecture` is the CI gate | process |
| P2-01 | Ticket -> curated answer loop | `ticketing` (`ticket_event`) -> `fast_lane` | contract |
| P2-02, P2-03 | ASPX pages, video captions | `extraction` route + `ingestion` source | skeleton |
| RB-01 | Role model & permission matrix | `kernel/contracts/access.py::Role` | implemented / RBAC |
| RB-02 | Entra group -> app-role mapping | `access/ports::RolePolicyPort` | RBAC |
| RB-03 | Role enforcement on every endpoint | `AccessService.require_role` | implemented / RBAC |
| RB-04 | Content-level access hooks | `retrieval::MandatoryFilter` inside every search | implemented |
| RB-05 | Auditor role | `Role.AUDITOR` + `audit` read endpoint | RBAC |
| RB-06 | Role admin & assignment audit | `access` + `audit` | RBAC |
| KV-*, UA-*, TR-*, GL-*, HC-* | Testing (Kavach), UAT, training, go-live, hypercare | process | process |
