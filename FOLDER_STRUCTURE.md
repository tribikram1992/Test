```
netra/
├── .import_linter_cache
│   ├── .gitignore
│   ├── 8a6327d3fe02da999b6960b8eed14b100b93e8a4.data.json
│   ├── CACHEDIR.TAG
│   └── netra.meta.json
├── config
│   ├── ingestion_profiles
│   │   └── ask_hr.yml
│   ├── retrieval_profiles
│   │   └── ask_hr.yml
│   ├── skills
│   │   ├── ask_hr.yml
│   │   └── jde.yml
│   ├── access_fixture.local.yml
│   ├── databases.yml.example
│   ├── model_routing.yml
│   └── vocabulary.yml
├── deploy
│   ├── ci
│   │   └── docker-compose.test.yml
│   ├── iis
│   │   ├── README.md
│   │   └── web.config.example
│   └── winsw
│       ├── netra-api.xml
│       └── netra-worker.xml
├── docs
│   ├── adr
│   │   ├── 0001-package-per-capability.md
│   │   ├── 0002-local-structured-extraction.md
│   │   ├── 0003-document-first-retrieval.md
│   │   ├── 0004-transactional-outbox-publication.md
│   │   └── 0005-caches.md
│   ├── ARCHITECTURE.md
│   ├── FOLDER_STRUCTURE.md
│   ├── PAYLOAD_CONTRACT.md
│   └── STORY_MAP.md
├── scripts
│   ├── migrate_payload_v1_to_v2.py
│   ├── new_module.py
│   └── print_tree.py
├── src
│   └── netra
│       ├── apps
│       │   ├── ask_hr
│       │   │   ├── metadata
│       │   │   │   ├── __init__.py
│       │   │   │   ├── column_mapping.py
│       │   │   │   └── workbook_reader.py
│       │   │   ├── prompts
│       │   │   │   ├── __init__.py
│       │   │   │   └── hr_answer_style.md
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── applicability.py
│       │   │   ├── config.py
│       │   │   ├── manifest.py
│       │   │   ├── profiles.py
│       │   │   └── skills.py
│       │   ├── jde
│       │   │   ├── prompts
│       │   │   │   ├── __init__.py
│       │   │   │   └── jde_sql_style.md
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── datasources.py
│       │   │   ├── manifest.py
│       │   │   ├── skills.py
│       │   │   └── sql_policy.py
│       │   ├── uploads
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── upload_service.py
│       │   │   │   └── upload_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── manifest.py
│       │   │   └── skills.py
│       │   ├── __init__.py
│       │   └── manifest.py
│       ├── bootstrap
│       │   ├── __init__.py
│       │   ├── api_app.py
│       │   ├── cli.py
│       │   ├── container.py
│       │   ├── module_registry.py
│       │   ├── settings.py
│       │   └── worker.py
│       ├── common
│       │   ├── types
│       │   │   ├── __init__.py
│       │   │   ├── access.py
│       │   │   ├── access_rules.py
│       │   │   ├── applicability.py
│       │   │   ├── evidence.py
│       │   │   ├── health.py
│       │   │   ├── outcome.py
│       │   │   ├── paging.py
│       │   │   └── taxonomy.py
│       │   ├── __init__.py
│       │   ├── clock.py
│       │   ├── constants.py
│       │   ├── context.py
│       │   ├── deadline.py
│       │   ├── error_codes.py
│       │   ├── errors.py
│       │   ├── ids.py
│       │   └── manifest.py
│       ├── infrastructure
│       │   ├── cache
│       │   │   ├── __init__.py
│       │   │   ├── cache.py
│       │   │   ├── in_memory_cache.py
│       │   │   ├── keys.py
│       │   │   ├── postgres_cache.py
│       │   │   └── single_flight.py
│       │   ├── db
│       │   │   ├── __init__.py
│       │   │   ├── migrator.py
│       │   │   ├── outbox_relay.py
│       │   │   ├── postgres.py
│       │   │   └── transaction.py
│       │   ├── files
│       │   │   ├── __init__.py
│       │   │   ├── file_store.py
│       │   │   └── local_file_store.py
│       │   ├── http
│       │   │   ├── __init__.py
│       │   │   └── client_factory.py
│       │   ├── logging
│       │   │   ├── __init__.py
│       │   │   ├── redaction.py
│       │   │   └── setup.py
│       │   ├── resilience
│       │   │   ├── __init__.py
│       │   │   ├── bulkhead.py
│       │   │   ├── circuit_breaker.py
│       │   │   └── retry.py
│       │   ├── telemetry
│       │   │   ├── __init__.py
│       │   │   ├── metrics.py
│       │   │   └── tracing.py
│       │   ├── web
│       │   │   ├── middleware
│       │   │   │   ├── __init__.py
│       │   │   │   ├── deadline.py
│       │   │   │   ├── load_shedding.py
│       │   │   │   ├── rate_limit.py
│       │   │   │   ├── request_context.py
│       │   │   │   └── sse.py
│       │   │   ├── __init__.py
│       │   │   ├── auth.py
│       │   │   ├── envelopes.py
│       │   │   ├── error_boundary.py
│       │   │   └── health.py
│       │   └── __init__.py
│       ├── modules
│       │   ├── access
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── cache
│       │   │   │   ├── __init__.py
│       │   │   │   └── scope_cache.py
│       │   │   ├── clients
│       │   │   │   ├── __init__.py
│       │   │   │   ├── directory_client.py
│       │   │   │   ├── jwks_cache.py
│       │   │   │   └── microsoft_graph_client.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   └── codes.py
│       │   │   ├── migrations
│       │   │   │   └── 0001_access_schema.sql
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── resolved_scope.py
│       │   │   ├── providers
│       │   │   │   ├── __init__.py
│       │   │   │   ├── chain_scope_resolver.py
│       │   │   │   ├── entra_identity_provider.py
│       │   │   │   ├── identity_provider.py
│       │   │   │   ├── role_policy.py
│       │   │   │   ├── scope_resolver.py
│       │   │   │   └── static_providers.py
│       │   │   ├── repositories
│       │   │   │   ├── __init__.py
│       │   │   │   └── scope_override_repository.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── access_service.py
│       │   │   │   └── access_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── answer
│       │   │   ├── citations
│       │   │   │   ├── __init__.py
│       │   │   │   └── citation_builder.py
│       │   │   ├── confidence
│       │   │   │   ├── __init__.py
│       │   │   │   └── confidence_calculator.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   ├── fixed_messages.py
│       │   │   │   └── thresholds.py
│       │   │   ├── generation
│       │   │   │   ├── __init__.py
│       │   │   │   ├── answer_generator.py
│       │   │   │   └── gateway_answer_generator.py
│       │   │   ├── grounding
│       │   │   │   ├── __init__.py
│       │   │   │   ├── claim_groundedness_checker.py
│       │   │   │   ├── groundedness_checker.py
│       │   │   │   └── numeric_groundedness_checker.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── answer.py
│       │   │   ├── prompts
│       │   │   │   ├── __init__.py
│       │   │   │   └── grounded_answer.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── answer_service.py
│       │   │   │   └── answer_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── answer_cache
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── thresholds.py
│       │   │   ├── migrations
│       │   │   │   └── 0001_answer_cache_schema.sql
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── entry.py
│       │   │   ├── repositories
│       │   │   │   ├── __init__.py
│       │   │   │   └── entry_repository.py
│       │   │   ├── search
│       │   │   │   ├── __init__.py
│       │   │   │   ├── cache_search.py
│       │   │   │   └── vector_cache_search.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── answer_cache_service.py
│       │   │   │   └── answer_cache_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── audit
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   └── codes.py
│       │   │   ├── migrations
│       │   │   │   └── 0001_audit_schema.sql
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── audit_record.py
│       │   │   ├── repositories
│       │   │   │   ├── __init__.py
│       │   │   │   ├── audit_repository.py
│       │   │   │   └── postgres_audit_repository.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── audit_service.py
│       │   │   │   └── audit_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── chat_history
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── cache
│       │   │   │   ├── __init__.py
│       │   │   │   └── session_state_cache.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── limits.py
│       │   │   ├── migrations
│       │   │   │   ├── 0001_chat_history_schema.sql
│       │   │   │   └── 0002_domain_context.sql
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── chat.py
│       │   │   ├── repositories
│       │   │   │   ├── __init__.py
│       │   │   │   ├── chat_repository.py
│       │   │   │   ├── in_memory_chat_repository.py
│       │   │   │   └── postgres_chat_repository.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── chat_history_service.py
│       │   │   │   ├── chat_history_service_impl.py
│       │   │   │   ├── ledger_updater.py
│       │   │   │   └── summarizer.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── extraction
│       │   │   ├── cache
│       │   │   │   ├── __init__.py
│       │   │   │   ├── extraction_cache.py
│       │   │   │   └── in_memory_extraction_cache.py
│       │   │   ├── clients
│       │   │   │   ├── __init__.py
│       │   │   │   ├── baxi_extract_client.py
│       │   │   │   ├── baxi_extract_mapper.py
│       │   │   │   ├── baxi_extract_models.py
│       │   │   │   ├── fake_ocr_client.py
│       │   │   │   ├── ocr_client.py
│       │   │   │   └── rapid_ocr_client.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   ├── formats.py
│       │   │   │   └── limits.py
│       │   │   ├── extractors
│       │   │   │   ├── __init__.py
│       │   │   │   ├── docx_extractor.py
│       │   │   │   ├── legacy_converter.py
│       │   │   │   ├── pdf_extractor.py
│       │   │   │   ├── pdf_heading_detector.py
│       │   │   │   ├── pptx_extractor.py
│       │   │   │   └── structured_extractor.py
│       │   │   ├── images
│       │   │   │   ├── __init__.py
│       │   │   │   └── image_text_enricher.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── document.py
│       │   │   ├── probe
│       │   │   │   ├── __init__.py
│       │   │   │   ├── file_probe.py
│       │   │   │   └── local_file_prober.py
│       │   │   ├── readers
│       │   │   │   ├── __init__.py
│       │   │   │   ├── csv_reader.py
│       │   │   │   ├── row_serializer.py
│       │   │   │   ├── tabular_reader.py
│       │   │   │   ├── tabular_router.py
│       │   │   │   ├── text_reader.py
│       │   │   │   └── xlsx_reader.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── extraction_service.py
│       │   │   │   ├── extraction_service_impl.py
│       │   │   │   └── format_detection.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── feedback
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── clients
│       │   │   │   ├── __init__.py
│       │   │   │   └── message_ownership_client.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── limits.py
│       │   │   ├── migrations
│       │   │   │   └── 0001_feedback_schema.sql
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── feedback.py
│       │   │   ├── repositories
│       │   │   │   ├── __init__.py
│       │   │   │   ├── feedback_repository.py
│       │   │   │   └── postgres_feedback_repository.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── feedback_service.py
│       │   │   │   └── feedback_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── guardrail
│       │   │   ├── clients
│       │   │   │   ├── __init__.py
│       │   │   │   ├── baxiguard_client.py
│       │   │   │   ├── fake_guardrail_client.py
│       │   │   │   └── guardrail_client.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   └── codes.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── screening.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── guardrail_service.py
│       │   │   │   └── guardrail_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── ingestion
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── chunking
│       │   │   │   ├── __init__.py
│       │   │   │   ├── boilerplate_detector.py
│       │   │   │   ├── chunker.py
│       │   │   │   ├── common.py
│       │   │   │   ├── context_label.py
│       │   │   │   ├── page_window_chunker.py
│       │   │   │   ├── paragraph_joiner.py
│       │   │   │   ├── row_chunker.py
│       │   │   │   ├── section_chunker.py
│       │   │   │   ├── section_tree.py
│       │   │   │   ├── sentence_splitter.py
│       │   │   │   ├── slide_chunker.py
│       │   │   │   ├── strategy_selector.py
│       │   │   │   └── table_stitcher.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── limits.py
│       │   │   ├── graph
│       │   │   │   ├── __init__.py
│       │   │   │   ├── builder.py
│       │   │   │   ├── edges.py
│       │   │   │   ├── nodes.py
│       │   │   │   └── state.py
│       │   │   ├── metadata
│       │   │   │   ├── __init__.py
│       │   │   │   ├── normalizer.py
│       │   │   │   └── validator.py
│       │   │   ├── migrations
│       │   │   │   ├── 0001_ingestion_schema.sql
│       │   │   │   └── 0002_outbox.sql
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   ├── chunk.py
│       │   │   │   ├── document.py
│       │   │   │   ├── metadata.py
│       │   │   │   ├── publication.py
│       │   │   │   └── run.py
│       │   │   ├── repositories
│       │   │   │   ├── __init__.py
│       │   │   │   ├── chunk_repository.py
│       │   │   │   ├── document_repository.py
│       │   │   │   ├── in_memory_repositories.py
│       │   │   │   ├── outbox_repository.py
│       │   │   │   ├── postgres_repositories.py
│       │   │   │   └── run_repository.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── document_service.py
│       │   │   │   ├── document_service_impl.py
│       │   │   │   ├── ingestion_service.py
│       │   │   │   ├── ingestion_service_impl.py
│       │   │   │   ├── payload_mapper.py
│       │   │   │   ├── publication_service.py
│       │   │   │   └── publication_service_impl.py
│       │   │   ├── sources
│       │   │   │   ├── __init__.py
│       │   │   │   ├── document_source.py
│       │   │   │   └── metadata_reader.py
│       │   │   ├── taxonomy
│       │   │   │   ├── __init__.py
│       │   │   │   ├── ai_suggester.py
│       │   │   │   ├── card_builder.py
│       │   │   │   ├── path_deriver.py
│       │   │   │   ├── precedence.py
│       │   │   │   └── title_deriver.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── intent_routing
│       │   │   ├── cache
│       │   │   │   ├── __init__.py
│       │   │   │   └── routing_cache.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── thresholds.py
│       │   │   ├── detectors
│       │   │   │   ├── __init__.py
│       │   │   │   ├── model_router.py
│       │   │   │   ├── pattern_detector.py
│       │   │   │   └── vocabulary_detector.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── decision.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── intent_routing_service.py
│       │   │   │   └── intent_routing_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── model_gateway
│       │   │   ├── clients
│       │   │   │   ├── __init__.py
│       │   │   │   ├── chat_model_client.py
│       │   │   │   ├── embedding_client.py
│       │   │   │   ├── fake_clients.py
│       │   │   │   ├── http_clients.py
│       │   │   │   └── reranker_client.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── limits.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   ├── requests.py
│       │   │   │   └── responses.py
│       │   │   ├── routing
│       │   │   │   ├── __init__.py
│       │   │   │   ├── cost_calculator.py
│       │   │   │   └── model_router.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── model_gateway_service.py
│       │   │   │   ├── model_gateway_service_impl.py
│       │   │   │   └── usage_recorder.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── orchestration
│       │   │   ├── agents
│       │   │   │   ├── __init__.py
│       │   │   │   ├── agent.py
│       │   │   │   ├── database_agent.py
│       │   │   │   ├── document_agent.py
│       │   │   │   ├── registry.py
│       │   │   │   └── upload_agent.py
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── budgets.py
│       │   │   │   ├── codes.py
│       │   │   │   └── fixed_messages.py
│       │   │   ├── context
│       │   │   │   ├── __init__.py
│       │   │   │   ├── context_assembler.py
│       │   │   │   ├── conversation_rewriter.py
│       │   │   │   ├── gateway_conversation_rewriter.py
│       │   │   │   └── window_policy.py
│       │   │   ├── graph
│       │   │   │   ├── __init__.py
│       │   │   │   ├── builder.py
│       │   │   │   ├── edges.py
│       │   │   │   ├── nodes.py
│       │   │   │   └── state.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   ├── ask.py
│       │   │   │   ├── plan.py
│       │   │   │   └── skill.py
│       │   │   ├── planner
│       │   │   │   ├── __init__.py
│       │   │   │   ├── model_planner.py
│       │   │   │   ├── planner.py
│       │   │   │   └── rules_planner.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── ask_service.py
│       │   │   │   └── ask_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── retrieval
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── thresholds.py
│       │   │   ├── graph
│       │   │   │   ├── __init__.py
│       │   │   │   ├── builder.py
│       │   │   │   ├── edges.py
│       │   │   │   ├── nodes.py
│       │   │   │   └── state.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   ├── candidates.py
│       │   │   │   ├── filters.py
│       │   │   │   ├── profile.py
│       │   │   │   └── result.py
│       │   │   ├── parents
│       │   │   │   ├── __init__.py
│       │   │   │   ├── parent_section_provider.py
│       │   │   │   └── registry_parent_section_provider.py
│       │   │   ├── rerank
│       │   │   │   ├── __init__.py
│       │   │   │   ├── gateway_reranker.py
│       │   │   │   └── reranker.py
│       │   │   ├── rewrite
│       │   │   │   ├── __init__.py
│       │   │   │   ├── gateway_query_rewriter.py
│       │   │   │   └── query_rewriter.py
│       │   │   ├── scoring
│       │   │   │   ├── __init__.py
│       │   │   │   └── scoring.py
│       │   │   ├── search
│       │   │   │   ├── __init__.py
│       │   │   │   ├── catalog_search.py
│       │   │   │   ├── filter_builder.py
│       │   │   │   └── vector_searches.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── retrieval_service.py
│       │   │   │   └── retrieval_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── sql_execution
│       │   │   ├── clients
│       │   │   │   ├── __init__.py
│       │   │   │   ├── driver_sql_clients.py
│       │   │   │   ├── sql_client.py
│       │   │   │   └── sqlite_sql_client.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── limits.py
│       │   │   ├── generation
│       │   │   │   ├── __init__.py
│       │   │   │   ├── gateway_sql_generator.py
│       │   │   │   └── sql_generator.py
│       │   │   ├── graph
│       │   │   │   ├── __init__.py
│       │   │   │   ├── builder.py
│       │   │   │   ├── edges.py
│       │   │   │   ├── nodes.py
│       │   │   │   └── state.py
│       │   │   ├── introspection
│       │   │   │   ├── __init__.py
│       │   │   │   └── profile_builder.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   ├── datasource.py
│       │   │   │   └── query.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── sql_execution_service.py
│       │   │   │   └── sql_execution_service_impl.py
│       │   │   ├── validators
│       │   │   │   ├── __init__.py
│       │   │   │   ├── sql_validator.py
│       │   │   │   └── sqlglot_validator.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── ticketing
│       │   │   ├── api
│       │   │   │   ├── __init__.py
│       │   │   │   ├── routes.py
│       │   │   │   └── schemas.py
│       │   │   ├── clients
│       │   │   │   ├── __init__.py
│       │   │   │   ├── jira_client.py
│       │   │   │   ├── placeholder_ticketing_client.py
│       │   │   │   └── ticketing_client.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── limits.py
│       │   │   ├── migrations
│       │   │   │   └── 0001_ticketing_schema.sql
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── ticket.py
│       │   │   ├── repositories
│       │   │   │   ├── __init__.py
│       │   │   │   ├── in_memory_ticket_repository.py
│       │   │   │   ├── postgres_ticket_repository.py
│       │   │   │   └── ticket_repository.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── dispatcher.py
│       │   │   │   ├── ticketing_service.py
│       │   │   │   └── ticketing_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── vector_index
│       │   │   ├── clients
│       │   │   │   ├── __init__.py
│       │   │   │   ├── in_memory_vector_store_client.py
│       │   │   │   ├── qdrant_filters.py
│       │   │   │   ├── qdrant_vector_store_client.py
│       │   │   │   └── vector_store_client.py
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   ├── codes.py
│       │   │   │   └── collections.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   ├── filters.py
│       │   │   │   ├── payloads.py
│       │   │   │   └── points.py
│       │   │   ├── schemas
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── vector_index_service.py
│       │   │   │   └── vector_index_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   ├── vocabulary
│       │   │   ├── constants
│       │   │   │   ├── __init__.py
│       │   │   │   └── codes.py
│       │   │   ├── loader
│       │   │   │   ├── __init__.py
│       │   │   │   └── yaml_loader.py
│       │   │   ├── models
│       │   │   │   ├── __init__.py
│       │   │   │   └── vocabulary.py
│       │   │   ├── services
│       │   │   │   ├── __init__.py
│       │   │   │   ├── vocabulary_service.py
│       │   │   │   └── vocabulary_service_impl.py
│       │   │   ├── README.md
│       │   │   ├── __init__.py
│       │   │   ├── config.py
│       │   │   ├── exceptions.py
│       │   │   └── manifest.py
│       │   └── __init__.py
│       └── __init__.py
├── tests
│   ├── architecture
│   │   ├── __init__.py
│   │   └── test_boundaries.py
│   ├── fixtures
│   │   ├── sample
│   │   │   ├── deck.pptx
│   │   │   ├── entitlements.xlsx
│   │   │   ├── leave.docx
│   │   │   ├── metadata.csv
│   │   │   └── policy.pdf
│   │   └── build_documents.py
│   ├── integration
│   │   ├── __init__.py
│   │   └── test_postgres_and_qdrant.py
│   ├── unit
│   │   ├── api
│   │   │   ├── __init__.py
│   │   │   └── test_http_contracts.py
│   │   ├── common
│   │   │   ├── __init__.py
│   │   │   └── test_common.py
│   │   ├── infrastructure
│   │   │   ├── __init__.py
│   │   │   └── test_resilience_and_cache.py
│   │   ├── modules
│   │   │   ├── answer
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_answer.py
│   │   │   ├── answer_cache
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_answer_cache.py
│   │   │   ├── chat_history
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_chat_history.py
│   │   │   ├── extraction
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_extractors.py
│   │   │   ├── ingestion
│   │   │   │   ├── __init__.py
│   │   │   │   ├── test_ingestion_end_to_end.py
│   │   │   │   ├── test_metadata_and_taxonomy.py
│   │   │   │   ├── test_publication_state.py
│   │   │   │   └── test_section_chunker_edge_cases.py
│   │   │   ├── intent_routing
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_routing.py
│   │   │   ├── orchestration
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_ask_graph.py
│   │   │   ├── retrieval
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_retrieval_graph.py
│   │   │   ├── sql_execution
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_sql.py
│   │   │   ├── vector_index
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_filters_and_payloads.py
│   │   │   ├── vocabulary
│   │   │   │   ├── __init__.py
│   │   │   │   └── test_vocabulary.py
│   │   │   └── __init__.py
│   │   └── __init__.py
│   ├── __init__.py
│   └── conftest.py
├── .env.example
├── .gitignore
├── .pre-commit-config.yaml
├── README.md
└── pyproject.toml
```
