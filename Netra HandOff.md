# Netra Backend: Full Handoff (Intent, Decisions and Implemented Logic)

Audience: an engineer or LLM continuing work on the Netra backend (codebase version
1.2.0, coding standards plugin netra-coder 1.1.0).

How to read it:
- Part A is the product owner's intent and every decision taken so far. Treat it as
  requirements; do not reverse a decision without asking.
- Part B is what is implemented and how it works.
- Part C is what is open, pending or known to be wrong.
- The coding rules live in the separate file `NETRA_CODER_PLUGIN_RULES.md`. Follow both.

---

## Part A. Intent and decisions (the product owner's thinking)

### A1. What Netra is

- Netra is an enterprise conversational BI and retrieval platform built as a modular
  monolith. One platform serves several applications:
  - **Ask HR**: approved HR documents (policies, guides, job descriptions, manager toolkits).
  - **JDE**: live JDE EnterpriseOne data on Oracle (sales orders, order-to-cash), plus
    JDE documentation.
  - **CBI** and other future applications.
  - **Uploaded files**: documents a user uploads to a chat.
- Applications must stay separate: their authorization and data handling differ.
- Retrieval, ingestion, metadata classification, vector indexing, caching, audit and
  orchestration are reusable platform capabilities, never hard-coded under one
  application.
- Production-grade from the start: maintainable, scalable, generic (not tied to one
  domain), with clear module boundaries and separation of concerns.

### A2. Non-negotiable answer quality

- No agent may invent, imagine or assume an answer. Everything must come from the data
  available (documents or table rows).
- Accuracy should improve over time based on the data available.
- When the data does not support an answer, say so cleanly. A wrong answer is worse
  than no answer.

### A3. Latency

- Target 20 to 40 seconds per answer. Never more than 60 seconds; users get frustrated.

### A4. The request flow the owner designed

1. The user asks a question.
2. The chat is stored in PostgreSQL with these columns: UUIDv7 id, chat_id, user_id,
   domain, intent, chat, user_type, created_date, user_name, user_email (nullable).
3. The orchestrator identifies the list of domains the question belongs to (for example
   uploaded file, JDE, JDE documents, HR documents, tables, documents).
4. When calling the LLM, it sends: the chat id, the chat summary, the last messages
   related to the question's domain, the user's intent, and at most the last 10
   messages of the chat.
5. An agent decides how to process the question and hands it to the planner.
6. The planner decides which agent to call: UploadFileAgent, DBAgent or DocumentAgent.
7. Those agents split the task further into specialised skills, such as SalesOrder,
   O2C, JDEDocumentation and HRDocuments.

### A5. Metadata-first HR retrieval

- `Netra_MetaData.xlsx` holds the human metadata and taxonomy for HR documents (one
  sheet per HR site, plus a Summary sheet).
- Retrieval must first find the relevant documents from the metadata, then search
  chunks only inside those documents, to make answers specific and accurate.
- Taxonomy is useful but optional in general: when it is available use it; when it is
  missing, log a warning and continue. It must never block a multi-application
  onboarding.

### A6. Fast answers without the LLM

- **Query templates:** around the top 100 basic database questions are answered from
  approved, parameterised SQL templates (a "query graph").
- **OKF cards:** approved answer cards work like FAQs and answer without the LLM.
- Cards are vectorised, so a question worded differently from the card can still match
  without an LLM call.

### A7. Model routing

| Model | Price (USD per 1M tokens, input / output) | Used for |
|---|---|---|
| Claude Sonnet 5.5 | 2.20 / 11.00 | Complex work |
| GPT 5.6 Luna | 0.20 / 1.20 | Simple work |

- The corrective query rewrite must use Sonnet.
- Luna is also used for chart rendering in related work, so chart prompts must be
  generic, not domain-specific.

### A8. Ingestion decisions

- **Formats:** DOC, DOCX, XLS, XLSX, PPT, PPTX, PDF, TXT, MD, JPEG, JPG, PNG, GIF and WEBP.
- XLS is converted to XLSX (headless LibreOffice) rather than read with xlrd, because
  openpyxl cannot read the binary .xls format. DOC and PPT are converted the same way.
- **Vocabulary:** inconsistent metadata values ("GLOBAL", "EMEA " with a trailing space,
  "Manager" vs "Managers") are normalised against one concrete controlled vocabulary.
  NLP may suggest a mapping, but only an approved alias maps a value.
- Trailing quotes and spaces in values and paths are normalised.
- **Dates:** every date is converted to UTC (Excel serial numbers and MM/DD/YYYY
  strings). A year such as 3036 is a typo and is rejected, never auto-corrected.
- **Duplicates:** keep both copies for now; the owner will review them later.
- **Missing values:** keep them missing. Only documents that are approved, have an
  approval date and are current are ingested. The version is part of the taxonomy.
- **Columns not needed:** owner, downloadable, download_label. Every document is
  downloadable by the users allowed to see it.

### A9. Access decisions

- **Audience** decides who can retrieve and download a document:
  - Managers see manager documents and employee documents.
  - Employees never see or download manager-only documents.
  - HR documents are visible only to HR.
  - New Hires documents count as employee content.
- **Access_Tag** names a group that has direct access.
- **Region** comes from the user's Entra attributes combined with per-application RBAC
  rules. For example, an India employee may be granted US policies but denied APAC.
- **RBAC is a separate design and out of scope here.** The platform only receives an
  `AccessScope` and never computes it.

### A10. DB agent decisions

- A JDE query once took more than 15 minutes because the LLM picked a 2-million-row
  audit table instead of the 10k-row operational table. The 1-hour IIS timeout was a
  workaround, not a requirement. The DBAgent will be tuned after this implementation.

### A11. Architecture decisions

- **LangGraph** was used in the POC for a reason and must be used for orchestration.
- **Java-style service interface and implementation:** every service has an abstract
  interface and a separate implementation class, so new features can be added to a
  module safely.
- **Qdrant** is the only vector store. It is never the system of record for metadata,
  authorization, audit or downloads.

### A12. Environment

- Windows Server, Python 3.12.
- Services run under WinSW behind IIS/ARR. The backend runs on port 8080.
- The private package index is `https://aihub-pypiserver.aws.baxter.com/simple`
  (needed for baxicore, which is optional).
- Credentials in POC `.env` files were already rotated and must be ignored.

---

## Part B. What is implemented (codebase 1.2.0)

Facts:
- About 370 Python files and 15,000 lines.
- 3,463 tests pass, including a blocking architecture gate.
- ruff is clean, and the import-linter contracts hold.
- Nothing has run against live services yet: PostgreSQL, Qdrant, Oracle, the LLM
  gateway, BaxiGuard and Baxi Extract were all faked in tests.
- These ran against real inputs:
  - the real metadata workbook (494 rows);
  - real LibreOffice DOC/XLS conversion;
  - generated PDF, DOCX, PPTX, XLSX and Markdown files.

### B1. Module map

| Area | Package | Responsibility |
|---|---|---|
| Shared kernel | `shared/` | Error codes, AccessScope, Identity, Deadline, request context, JSON logging with redaction, progress events, prompt template, module manifest |
| Infrastructure | `infrastructure/` | PostgreSQL pool and migration runner, HTTP client factory, retry with full jitter, circuit breaker, task supervisor, prompt loader |
| Platform modules | `modules/vocabulary` | Controlled vocabulary, value and date normalisation |
| | `modules/model_gateway` | LLM routing (Sonnet / Luna), embeddings, reranker |
| | `modules/vector_store` | Qdrant adapter behind a typed port (the only qdrant_client import) |
| | `modules/guardrail` | BaxiGuard input and output screening |
| | `modules/access` | Identity and AccessScope ports, with static placeholders |
| | `modules/audit` | Append-only audit (fail closed) |
| | `modules/chat_history` | Messages, context, summaries |
| | `modules/ingestion` | Extraction, chunking, staged bulk runs, publication, uploads, schema ingestion, downloads |
| | `modules/retrieval` | Two-stage document retrieval (LangGraph subgraph) and payload contracts |
| | `modules/sql_execution` | DB agent (LangGraph subgraph), SQL validator, executors, templates |
| | `modules/fast_lane` | OKF cards and query templates |
| | `modules/answer` | Grounded answer, citations, confidence |
| | `modules/orchestration` | Ask graph, planner, agents, skill registry, recorder |
| Applications | `apps/ask_hr` | `hr_documents` skill and the Netra_MetaData Excel reader |
| | `apps/jde` | `sales_order`, `o2c` (database) and `jde_documentation` (document) skills |
| | `apps/uploads` | `uploaded_file` skill |
| Delivery | `api/`, `bootstrap/` | Thin routers, error envelope, middleware, settings, wiring, app factory, CLI |

How every module is organised:
- `public.py` (the only importable surface), `manifest.py`, `README.md`.
- `domain/`, `ports/`, `adapters/`.
- `application/<name>.py` holds the ABC interface; `application/impl/<name>_impl.py`
  holds the implementation.
- `graph/` exists where the flow branches or loops.

All 30 application services follow the interface / Impl split. Bootstrap constructs
every Impl and injects it typed as the interface.

### B2. The ask graph (LangGraph, `modules/orchestration/graph/`)

```text
screen_input -> (blocked) record
screen_input -> load_context -> fast_lane -> (hit / chips) record
fast_lane -> plan -> reply (greeting, out of scope, clarification, no skill) -> record
plan -> follow_up_fast_lane (follow-up was rewritten) -> (hit) record | prepare_dispatch | reply
plan -> prepare_dispatch -> Send x N -> run_agent -> compose -> record -> END
```

Nodes are split by concern into `nodes/`: `preparation.py`, `planning.py`,
`execution.py`, `synthesis.py`, plus `fast_lane_step.py`, `deps.py` and `progress.py`.

1. **screen_input:** BaxiGuard screens the question (fail closed by default). A block
   becomes a refusal with a reference code (x01 to x06).
2. **load_context:** resolves the AccessScope (no scope fails the request), loads the
   chat context, and embeds the question once.
3. **fast_lane:** an OKF card or query template answers directly, or 0.82 to 0.92
   matches return "did you mean" chips. No LLM call.
4. **plan:** one orchestrator call on Luna, which escalates once to Sonnet if the
   output fails schema validation. It returns a `PlanDecision`:
   - the standalone (history-resolved) question;
   - the intent;
   - `is_follow_up`;
   - up to `MAX_SUB_QUERIES` sub-queries, each targeted at one authorized skill;
   - an optional clarification question.

   Unauthorized or unknown skills are dropped. Domains come from the chosen skills.
5. **follow_up_fast_lane:** when a follow-up was rewritten, the fast lane is retried
   with the standalone question.
6. **prepare_dispatch:** embeds all sub-queries in one call and builds one task per
   sub-query. The agent deadline reserves `ANSWER_RESERVE_SECONDS` for the answer stage.
7. **run_agent:** runs once per sub-query through `Send`, bounded by `max_concurrency`
   (`AGENT_CONCURRENCY`). A failure becomes a FAILED outcome, so the other agents still
   answer.
8. **compose:** answers from evidence only (see B7) and applies the 0.90 confidence bar.
9. **record:**
   - the output guardrail screens the complete answer;
   - the audit write must succeed or the answer is not returned;
   - the chat history is written;
   - the per-domain summary is refreshed in a supervised background task.

Intents: lookup, aggregate, compare, document_request, chart_request,
clarification_reply, greeting, out_of_scope.

Answer statuses: answered, no_answer, refused, clarification, suggestions.

Route paths: fast_lane_card, fast_lane_template, fast_lane_suggestions, agents,
conversational, guardrail.

Fixed messages: the no-answer and low-confidence texts tell the user to add detail
(document, country, period) or raise a ticket.

### B3. Agents and skills

| Agent | Skills | Uses |
|---|---|---|
| DocumentSkillAgent (DOCUMENT) | `hr_documents`, `jde_documentation` | Retrieval subgraph |
| DocumentSkillAgent (UPLOAD) | `uploaded_file` | Retrieval subgraph restricted to the caller's chat uploads |
| DatabaseSkillAgent (DATABASE) | `sales_order`, `o2c` | SQL subgraph |

- Skills are configuration (`SkillDescriptor`), not code.
- Adding a skill is a registration in `config/apps/*.yml` plus the app's
  `build_*_skills`. The plan schema does not change.

### B4. Chat history (`modules/chat_history`, migration 0001)

- **`chat_message`** has the requested columns plus `role`, `rewritten_question`,
  `answer_status` and `route_path`.
  - `domain` is `text[]` with a GIN index.
  - `message_id` is UUIDv7, generated in the app, so ordering by id is chronological.
- **`chat_summary`** has one row per (chat_id, domain).
- **Orchestrator context:** the last `CHAT_HISTORY_MESSAGES` (10) messages plus every
  summary.
- **Answer context:** only the messages and summaries whose domains overlap the
  detected domains.
- **Ownership:** a chat owned by another user returns not_found, so chat ids cannot be
  probed.
- **Summaries** are refreshed after the response on Luna; they never block a request.

### B5. Ingestion (`modules/ingestion`, migrations 0003 and 0004)

**Format detection** reads the file content, never only the extension or the Excel
`Document_Type` column:
- magic bytes for PDF, PNG, JPEG, GIF and WEBP;
- zip member names for DOCX, XLSX and PPTX;
- the OLE signature plus the extension for DOC, XLS and PPT;
- the extension plus a no-NUL check for TXT and MD.

**Extractors** (one per format, behind `ExtractorPort`):

| Format | How it is extracted |
|---|---|
| PDF | PyMuPDF: headings inferred from font signals, tables captured. A scanned PDF goes to Baxi Extract OCR |
| DOCX | python-docx: heading styles, lists and tables in body order |
| XLSX | openpyxl: one ROW block per data row, each value prefixed with its header |
| PPTX | One slide per block, including notes and tables |
| TXT | Encoding detected; paragraphs split on blank lines |
| MD | Headings, lists and pipe tables |
| JPEG / PNG | OCR |
| GIF / WEBP | Converted to PNG frames, then OCR |
| DOC / XLS / PPT | Headless LibreOffice in a sandboxed subprocess with a timeout, then the OOXML extractor |

If OCR is not configured or an image has no text, the file is quarantined. Captions are
never generated.

**Chunking.** The strategy is chosen from the document's structure:
- `section`: groups by heading path;
- `sliding_window`: flat text;
- `slide`: one slide per chunk;
- `row`: one spreadsheet row per chunk.

Sizes: target 1,500 characters, 15% sentence overlap, minimum 200, hard maximum 3,000.
Parents are capped at 4,500 characters.

Every chunk gets a context label (`title - heading path - vVERSION`). The label is
embedded with the text, but only the text is stored for display and citation. Parent
sections are stored for context expansion.

**Metadata** (`apps/ask_hr` Excel reader plus `MetadataService`):
- Header variants are aliased: `Heading_Section_Path` is the full path key; `URL` uses
  the hyperlink target; "Approved Date" / "Approved date" and "Aprrover" / "Approver"
  are both accepted.
- Coded fields are normalised against `config/vocabulary.yml`:
  - an unknown required field (region, audience) blocks the row;
  - an unknown optional field produces a warning and a suggestion.
- Dates are converted to UTC. A date more than `METADATA_MAX_FUTURE_YEARS` (2) ahead
  is rejected.
- Paths are normalised (slashes, quotes, case), and matched to files by path suffix.
- A duplicate full_path blocks every copy.

**Eligibility:** approval status is approved, an approval date exists, and the document
is current. Otherwise the row is skipped with a reason code. Missing values are never
defaulted.

**Bulk run** (LLD part E) has seven stages; stages 1 to 6 run in
`netra ingest-documents`:
1. Check the folder location.
2. Scan files and count formats.
3. Validate metadata.
4. Match metadata to files.
5. Per file: fingerprint, extract, chunk, stage.
6. Save the report with status `awaiting_approval`.
7. Publish, but only on approval (`netra approve-run` or the admin API).

How the run behaves:
- One bad file never stops the run.
- Version ids derive from content fingerprints, so re-runs are idempotent.
- `--dry-run` validates without staging.

**Publication order:**
1. Embed and upsert the points as not current.
2. Copy the file to the object store.
3. Flip the registry in one transaction (supersede the old version, set the new one
   current).
4. Flip `is_current` on the points.
5. Delete the superseded version's points.
6. Deactivate OKF cards built from the superseded version.

Nothing is searchable before approval.

**Uploads:** per chat and per owner, expiring after `UPLOAD_TTL_HOURS` (24). A
background sweep deletes expired chunks every `UPLOAD_SWEEP_INTERVAL_MINUTES` (30). No
approval step.

**Schema ingestion:** `netra ingest-schema` indexes one definition chunk per table from
the POC-generated JDE metadata JSON. Live rows are never ingested.

**Downloads:** `GET /api/v1/documents/{id}/download` re-checks the caller's scope. A
document the caller cannot see returns 404.

### B6. Retrieval (`modules/retrieval`, LangGraph subgraph)

```text
route_documents -> search -> finish | rewrite -> (grade again) | no_answer
```

1. **Mandatory filter**, applied inside the Qdrant search:
   - skill;
   - `is_current` and `is_approved`;
   - effective date at or before now, or no date;
   - `country_region` in the allowed regions;
   - `audience` in the allowed audiences, or `access_tag` in the caller's groups.

   No fallback ever removes this filter.
2. **Stage A (metadata first):** the catalog collection holds one vector per document
   version (title, description, topic and category labels). Documents scoring at least
   `catalog_min_score` (0.35) are selected, `catalog_top_k` (8) at most.
3. **Stage B:** chunk search restricted to the selected document ids, or the full
   authorized scope when none qualify (recorded as fallback_level 1).
4. **Grade** = 0.6 x top score + 0.25 x mean of the top 3 + 0.15 x coverage above the
   0.45 floor. If the grade is below 0.55, rewrite on Sonnet:
   - at most `max_rewrites` (1 in interactive use);
   - only while at least 8 s of budget remain.

   If it is still weak, return empty evidence, which becomes a clean no-answer.
5. **Finish:**
   - deduplicate by content hash;
   - rerank with bge-reranker; if it is unavailable, keep the search order and apply a
     confidence penalty;
   - keep the top 3;
   - expand to parent sections from PostgreSQL;
   - compute retrieval strength = 0.7 x top + 0.3 x mean;
   - assemble the evidence, with provenance taken from the payload.

**Payload contracts** (`schema_version` 1, flat):
- `DocumentChunkPayload`, `DocumentCatalogPayload` and `UploadChunkPayload`, owned by
  retrieval;
- `TableSchemaPayload`, owned by sql_execution;
- `VariantPayload`, owned by fast_lane.

**How ingestion and retrieval are coupled.** They never call each other; they share
only stored fields:
- the same embedding model;
- vocabulary codes;
- human metadata copied onto every point;
- `chunk_id` used as the point id;
- `parent_section_id`;
- `is_current`, `is_approved` and `effective_from_epoch`.

### B7. Answer, grounding and confidence (`modules/answer`)

**What the model sees and returns:**
- It sees only numbered, already-authorized evidence, plus domain-filtered conversation
  context. The context is not treated as a source.
- It returns `is_answerable`, the answer Markdown and `used_sources`.
- It never produces links, versions or a confidence value.
- Temperature is 0, and the prompts are versioned resource files.

**Citations** are built by code from the evidence:
- title, page or slide, version and source link;
- a download id for published documents;
- the executed SQL and row count for database evidence.

**Grounding check** (deterministic, takes milliseconds):
- Every number in the answer must appear in the cited evidence, after normalising
  thousands separators and trailing zeros.
- Any URL counts as unsupported.

**Routing and escalation:**
- Single-source lookups use Luna. If the answer is not fully grounded, it is
  regenerated once on Sonnet when budget allows.
- Multi-source, compare and aggregate answers use Sonnet.

**Confidence** = 0.4 x retrieval strength + 0.6 x groundedness, minus a reranker
penalty (0.10) and a fallback penalty (0.03 per level).

**Confidence bar:** an answer below 0.90 is withheld and replaced with the
low-confidence message. Confidence is returned to administrators only.

### B8. DB agent (`modules/sql_execution`, LangGraph subgraph)

```text
check_scope -> load_schema -> generate -> execute -> (repair -> execute) -> finalize
```

1. **Allowlist:** the skill's allowlist intersected with `AccessScope.allowed_tables`
   (when given). Deny patterns block history, audit and log tables (`F42199$`,
   `F49211$`, `F03B112$`, `AUDIT`, `_LOG$`, `_HIST$`).
2. **Schema RAG:** retrieves the most relevant permitted table definitions.
3. **SQL generation on Sonnet**, with the JDE notes:
   - amounts / 100;
   - quantities and prices / 10000;
   - Julian dates in CYYDDD format;
   - UDC decoding through `EUCTL.F0005`.
4. **Deterministic validator**, run before anything executes:
   - single SELECT or WITH statement only;
   - no comments and no semicolons outside string literals;
   - forbidden keywords and package calls rejected (DML, DDL, INTO, FOR UPDATE, DBMS_,
     UTL_, XP_, SP_ and similar);
   - every FROM or JOIN table must be on the allowlist; CTE names are excluded from
     that check.

   Rejected SQL never runs.
5. **Execution:**
   - Oracle uses python-oracledb in thick mode (for Native Network Encryption), with
     `call_timeout` set per query (20 s default) and a pool per datasource.
   - SQL Server uses pyodbc; PostgreSQL datasources use asyncpg, read-only.
   - At most `max_rows` (200) rows are fetched; one extra row is fetched to detect
     truncation.
   - Parameters are always bound (`:name`).
6. **Repair:** at most 1, and only when at least 12 s remain. Only the database error
   code (for example ORA-00904) is shown to the model.
7. **Answers come only from returned rows.** An empty result means "no rows".

Datasource passwords are read from `<ID>_PWD` or the POC form `<ID>_<SCHEMA>_PWD`.

### B9. Fast lane (`modules/fast_lane`, migration 0005)

**OKF cards:**
- Static: approved Markdown, with citations.
- Dynamic: one approved template row fills `$placeholders`.

**Query templates:** approved parameterised SQL, validated with the same allowlist and
validator as the DB agent. Parameters (year, integer, code, date) are extracted
deterministically, and "this year" and "last year" are resolved. A missing required
parameter falls back to the normal path.

**Matching** (no LLM at request time):
- Every approved question wording is its own vector.
- Hybrid score = 0.8 x vector similarity + 0.2 x token-set keyword similarity.
- Slot guard: a region or year named in the question must not contradict the entry's
  slots.
- Bands: 0.92 or above answers directly; 0.82 to 0.92 returns chips; below that, the
  normal path runs.
- Variants are filtered by region and audience (or access tag) inside the search.
- Any dependency failure counts as a miss, never an error.

**Publishing:** `netra publish-fast-lane --file <yml>`. Every entry needs
`approved_by` and `approved_at`. An example is in `config/fast_lane/catalog.example.yml`.

### B10. Model gateway (`modules/model_gateway`)

**Routing table** (code, not an LLM):

| Stage | Tier |
|---|---|
| Orchestrator | Luna; escalates once to Sonnet on invalid output |
| Corrective rewrite | Sonnet |
| SQL generation and repair | Sonnet |
| Answer, single source | Luna; escalates once if not grounded |
| Answer, multi source | Sonnet |
| Chat summary | Luna |
| Card paraphrase | Luna |

Escalation needs at least `LLM_MIN_SECONDS_FOR_ESCALATION` (10 s) of budget left.

**Chat calls:** an OpenAI-compatible `/chat/completions` request with a forced tool
call for JSON output, validated against the Pydantic schema. Every call records tier,
model, prompt id, tokens and USD cost for audit.

**Embeddings:** Ollama `/api/embed` or OpenAI-compatible `/embeddings`, in batches. The
vector count and dimension are validated against `EMBEDDING_MODEL_DIMENSION`.

**Reranker:** the bge-reranker HTTP API.

### B11. Guardrail, access, audit

**Guardrail:** BaxiGuard `/analyze/prompt` for input. The output path defaults to
`/analyze/output`, which is unconfirmed. The policy is fail closed; the placeholder
adapter returns not evaluated.

**Access placeholders**, until RBAC exists:
- **Identity:** bearer tokens from the `STATIC_IDENTITIES_JSON` secret, compared by
  SHA-256 in constant time.
- **Scope:** `config/access_scopes.yml`, keyed by user id. A user with no entry is
  denied.

**AccessScope** fields: allowed_regions, allowed_audiences, access_groups,
allowed_tables (optional), allowed_skills (optional), scope_hash.

**Audit** (`audit_message`, migration 0002): one row per request with:
- the question, rewritten question, status and answer;
- cited source references (never copied text);
- confidence and its basis;
- retrieval detail and route path;
- whether the fast lane answered;
- domains and intent;
- the models used, with cost;
- the guardrail verdicts and the scope hash.

A trigger makes the table append-only. If the write fails, the answer is not returned.

**Production profile:** startup is rejected if any of these is true:
- the audit adapter is non-durable;
- the guardrail adapter is the placeholder, or fail-closed is off;
- the static identity or scope providers are used without
  `ALLOW_STATIC_ACCESS_IN_PRODUCTION=true`.

### B12. API

All responses use `{ "data": ... }` or
`{ "error": { "code", "message", "trace_id" } }`. Every response has an X-Request-ID
header, and W3C traceparent is accepted. Load shedding returns 503 with Retry-After.

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/chat` | Ask a question (JSON). Body: `{chat_id?, question}` |
| POST | `/api/v1/chat/stream` | SSE progress events, then one `response` or `error` event. The answer is sent only after output screening |
| GET | `/api/v1/chats/{chat_id}/messages` | The caller's chat history |
| POST | `/api/v1/chats/{chat_id}/files` | Upload a file to the chat |
| GET | `/api/v1/documents/{document_id}/download` | Authorized download |
| GET | `/api/v1/admin/ingestion/runs/{run_id}` | Run summary (admin) |
| POST | `/api/v1/admin/ingestion/runs/{run_id}/approve` | Publish a staged run (admin) |
| GET | `/health/live`, `/health/ready` | Probes (ready = PostgreSQL ping within 500 ms) |

Error codes (stable contract): validation_failed, unauthenticated, forbidden,
access_scope_unavailable, not_found, conflict, payload_too_large, unsupported_format,
dependency_unavailable, dependency_timeout, deadline_exceeded, model_output_invalid,
audit_write_failed, guardrail_unavailable, configuration_invalid, ingestion_failed,
internal_error.

### B13. CLI and run order

```powershell
pip install -e ".[dev,aws]" --extra-index-url https://aihub-pypiserver.aws.baxter.com/simple
netra migrate
netra init-collections
netra ingest-schema --datasource jde --file config/schema_metadata/jde_oracle_EUDTA.json --file config/schema_metadata/jde_oracle_EUCTL.json
netra ingest-documents --skill hr_documents --folder <root> --metadata <xlsx> --dry-run
netra ingest-documents --skill hr_documents --folder <root> --metadata <xlsx>
netra approve-run <run_id> --approver "<name>"
netra publish-fast-lane --file config/fast_lane/catalog.yml
uvicorn netra.bootstrap.app_factory:create_app --factory --host 0.0.0.0 --port 8080
```

`netra init-collections` creates five collections, each with cosine distance and the
payload indexes it needs. It is safe to re-run.

| Collection | Contents |
|---|---|
| `netra_doc_chunks` | Document chunks |
| `netra_doc_catalog` | One vector per document version |
| `netra_upload_chunks` | Uploaded file chunks |
| `netra_db_schema` | JDE table definitions |
| `netra_fast_lane` | OKF card and template question wordings |

### B14. Configuration

**Settings:** `bootstrap/settings.py` is the only place that reads the environment.
It is typed and fails fast at startup.

| Group | Settings |
|---|---|
| PostgreSQL | `POSTGRES_DSN` |
| Qdrant | `QDRANT_URL`, the five `*_COLLECTION` names |
| Embeddings and reranker | `EMBEDDING_PROVIDER`, `EMBEDDING_MODEL_NAME`, `EMBEDDING_MODEL_BASE_URL`, `EMBEDDING_MODEL_DIMENSION`, `RERANKER_MODEL_NAME`, `RERANKER_MODEL_ENDPOINT` |
| LLM | `LLM_GATEWAY_ENDPOINT`, `LLM_GATEWAY_API_KEY`, `LLM_MODEL_COMPLEX`, `LLM_MODEL_SIMPLE`, price settings |
| Guardrail | `GUARDRAIL_*` |
| OCR and conversion | `BAXI_EXTRACT_ENDPOINT`, `BAXI_EXTRACT_LONG_LIVED_TOKEN`, `SOFFICE_BINARY` |
| Oracle | `ORACLE_CLIENT_LIB_DIR` |

Pipeline limits:

| Setting | Default |
|---|---|
| `REQUEST_DEADLINE_SECONDS` | 55 |
| `ANSWER_RESERVE_SECONDS` | 18 |
| `ANSWER_CONFIDENCE_BAR` | 0.90 |
| `MAX_SUB_QUERIES` | 3 |
| `AGENT_CONCURRENCY` | 4 |
| `CHAT_HISTORY_MESSAGES` | 10 |

**YAML files in `config/`:**

| File | Contents |
|---|---|
| `vocabulary.yml` | Controlled vocabulary |
| `databases.yml` | Datasources (from the POC) |
| `apps/ask_hr.yml` | HR skill and retrieval thresholds |
| `apps/jde.yml` | JDE skills, allowlists, deny patterns, SQL notes |
| `access_scopes.yml` | Placeholder scopes |
| `fast_lane/catalog.example.yml` | Example OKF card and query template |
| `schema_metadata/*.json` | JDE schema metadata |

**Vocabulary codes:**

| Field | Codes |
|---|---|
| Region | global, us, puerto_rico, emea, apac, japan, india |
| Audience | employees, managers, hr, new_hires, general_audience |
| Access tag | hr_only |
| Approval status | approved, pending, rejected, draft |

The vocabulary also defines 15 topic codes and 11 core-category codes.

### B15. Database tables (PostgreSQL migrations)

| Migration | Tables |
|---|---|
| 0001 | `chat_message`, `chat_summary` |
| 0002 | `audit_message` (append-only trigger) |
| 0003 | `document`, `document_version` (unique current version per document), `parent_section`, `chunk_record`, `staged_chunk_text` |
| 0004 | `ingestion_run` |
| 0005 | `okf_card`, `query_template` |

---

## Part C. Open items, pending decisions and known issues

### C1. Known issues

1. **Circuit breaker and a missing collection (fix written, not applied):**
   - **Bug:** a Qdrant 404 (missing collection) counts as a dependency failure. Five
     failed upload sweeps in a row would open the shared Qdrant breaker and block all
     searches.
   - **Fix:** add an `is_failure` predicate to `CircuitBreaker.call`, map a 404 to
     `ConfigurationError` in `QdrantVectorStore._guarded`, and add a unit test.
   - **Immediate workaround:** run `netra init-collections`.
2. **Collections not created yet in dev Qdrant:** the logs show
   `netra_upload_chunks` returning 404. Run `netra init-collections`.

### C2. Pending work

1. **Reuse the old POC collection (option B):**
   - Write a one-off migration script that maps old payloads to the new contract and
     writes catalog points and registry rows.
   - It reuses the vectors only if the old embedding model and dimension match.
   - Blocked on: one sample point and the old collection's config.
   - Otherwise, re-ingest (option A).
2. **RBAC module:** replace the static identity and scope providers with Entra plus
   RBAC through the existing ports. No consumer change is needed.
3. **LangGraph checkpointer:** `PipelineState` is passed by reference and is not
   serializable. Make it serializable before adding a Postgres checkpointer (needed for
   resume or human-in-the-loop clarification).
4. **mypy:** strict mode is configured but has never been run.
5. **Golden evaluation set per domain:** calibrate the catalog, grade, fast-lane and
   confidence thresholds. Every number above is an initial value.
6. **Accuracy loop (not implemented):**
   - mine no-answer, low-confidence and thumbs-down answers into a gap queue;
   - promote frequent, high-confidence answers to card or template candidates for human
     approval.
7. **Top 100 query templates and the OKF card catalog:** only one example of each
   exists. The owner and data teams must author and approve the rest.
8. **DBAgent tuning:** confirm the JDE allowlists and deny lists; the audit-table
   incident shows why they matter.
9. **CBI application:** not started.

### C3. Assumptions to confirm

1. The BaxiGuard output-screening path (`/analyze/output`).
2. The LLM gateway supports forced tool calls for both Sonnet and Luna.
3. Embeddings come from Ollama `/api/embed`.
4. Managers see employee-level documents as well as manager documents.
5. HR direct access through Access_Tag still respects the region filter.

### C4. Metadata data-quality issues (from Netra_MetaData.xlsx)

1. Future effective dates (year 3036) on many Job Description rows, the Leave of
   Absence guide and the Power BI access guide. These rows are rejected until they are
   fixed.
2. Descriptions shifted to the wrong rows, and some swapped pairs. This hurts stage A
   routing.
3. Wrong URLs on several rows (360 Feedback, Information Gathering, Managers Upskilling,
   two TRP PPTX files).
4. A stray trailing quote in some paths. The code now normalises it.
5. Duplicate content (TRP files in both PDF and PPTX, files listed in two sheets). Both
   copies are kept by decision.
6. A missing approver or approval date. Those rows are skipped by decision.

### C5. Rules for whoever continues

- Follow `NETRA_CODER_PLUGIN_RULES.md`.
- Every new application service needs an ABC interface plus one Impl.
- Every branching or looping flow is a LangGraph graph with its loops bounded in
  `edges.py`.
- Keep the architecture gate green (`pytest tests/architecture`). A new rule must be
  mutation-checked: add a deliberate violation, confirm the gate fails, then revert.
- Never weaken the access, approval or currency filters to make retrieval return more
  results.
- Never let the model produce citations, links, versions or confidence.
- Keep every stage within the request deadline. The total must stay under 60 seconds.
