# Phase 2: Spring AI + PGVector Infrastructure

## Goals

- Wire Spring AI Ollama chat and embedding models via auto-configuration.
- Wire Spring AI `PgVectorStore` via auto-configuration (shared PostgreSQL `DataSource`).
- Add startup and runtime infrastructure diagnostics without ingestion/RAG logic.

## Architecture Decisions

| Decision | Rationale |
|----------|-----------|
| **Spring AI auto-config first** | `OllamaChatModel`, `OllamaEmbeddingModel`, and `PgVectorStore` are provided by starters; no duplicate beans unless auto-config is disabled. |
| **Non-fatal Ollama startup** | Local/dev environments often start the API before Ollama; failing fast would block `/api/v1/system/status` and Actuator. |
| **Dedicated connectivity client** | `OllamaConnectivityClient` applies connect/read timeouts for probes only; model inference stays on Spring AI beans. |
| **pgvector extension check** | Explicit `SELECT extname FROM pg_extension WHERE extname = 'vector'` verifies extension install separate from Spring AI table init. |
| **Schema init profile split** | `initialize-schema=true` only in `dev`; `prod` expects ops-managed DDL. |
| **Dual diagnostics** | Custom `/api/v1/system/status` for assignment contract; Actuator `/actuator/health` and `/actuator/info` for standard ops tooling. |

## Spring AI Wiring

### Ollama

Environment variables (`OLLAMA_BASE_URL`, `OLLAMA_CHAT_MODEL`, `OLLAMA_EMBEDDING_MODEL`) bind to `app.ollama.*`, then `spring.ai.ollama.*`. See README **Environment variables** for the canonical list.

Spring Boot auto-configuration creates:

- `ollamaChatModel` (`ChatModel`)
- `ollamaEmbeddingModel` (`EmbeddingModel`)

`OllamaConfig` logs bean presence and configured model names at `ApplicationReadyEvent`.

### PGVector

Properties under `spring.ai.vectorstore.pgvector.*` configure the auto-configured `VectorStore` bean (`PgVectorStore`), using the same `DataSource` as JPA.

`VectorStoreConfig` logs dimensions, table name, and bean presence at startup.

## pgvector Integration Notes

1. Install extension in PostgreSQL: `CREATE EXTENSION IF NOT EXISTS vector;`
2. **Dev**: `spring.ai.vectorstore.pgvector.initialize-schema=true` creates the Spring AI `vector_store` table.
3. **Prod**: `initialize-schema=false`; apply migrations/runbooks before deploy.
4. Set `PGVECTOR_DIMENSIONS=768` when using `nomic-embed-text` (see README).

## Files Added / Updated

| File | Purpose |
|------|---------|
| `config/OllamaConfig.java` | Properties + startup logging |
| `config/VectorStoreConfig.java` | Vector store startup logging |
| `config/OllamaConnectivityClient.java` | Timed Ollama HTTP probes |
| `config/AppOllamaProperties.java` | `app.ollama` binding |
| `config/ApplicationInfoContributor.java` | `/actuator/info` details |
| `service/InfrastructureHealthService.java` | Status aggregation + verify* methods |
| `service/StartupVerificationRunner.java` | Non-blocking startup checks |
| `controller/SystemStatusController.java` | `GET /api/v1/system/status` |
| `exception/*` + `GlobalExceptionHandler` | Infrastructure error mapping (503) |

## Risks

| Risk | Mitigation |
|------|------------|
| Embedding dimension mismatch | Use README defaults (`PGVECTOR_DIMENSIONS=768`); startup validator checks probe embedding length. |
| Dual vector storage (JPA chunks vs `vector_store`) | Design ingestion sync in Phase 3/4. |
| Ollama model tags vs configured name | `phi3` vs `phi3:latest` handled in `OllamaConnectivityClient.isModelAvailable`. |
| Slow tests if Ollama probed live | `@MockitoBean` on `OllamaConnectivityClient` in `ApplicationTests`. |
| Spring AI milestone API drift | Pin BOM; follow Spring AI 2.0 reference. |

## Manual Verification Checklist

- [ ] `docker ps` shows PostgreSQL (pgvector) container running
- [ ] `ollama list` shows `phi3` and `nomic-embed-text`
- [ ] `SELECT extname FROM pg_extension WHERE extname = 'vector';` returns one row
- [ ] `curl http://localhost:8080/actuator/health`
- [ ] `curl http://localhost:8080/actuator/info`
- [ ] `curl http://localhost:8080/api/v1/system/status`
- [ ] Startup logs show sanitized JDBC URL, models, and PGVector dimensions
- [ ] Stop Ollama → app still starts; `ollama` field is `DOWN` in system status

## Test Strategy

- `SystemStatusControllerTest` — MockMvc JSON contract
- `InfrastructureHealthServiceTest` — mocked JDBC/Ollama behavior
- `ApplicationTests` — context load with mocked Ollama probes

```bash
./mvnw test
```
