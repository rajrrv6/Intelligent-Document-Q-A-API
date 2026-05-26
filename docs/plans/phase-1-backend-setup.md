# Phase 1: Backend Setup (Spring AI)

## Goals

- Establish a layered Spring Boot 21 / Maven project structure.
- Add PostgreSQL + JPA domain models for documents and chunks.
- Declare Spring AI dependencies for Ollama chat, Ollama embeddings, and PGVector (configuration only).
- Expose a versioned health endpoint at `GET /api/v1/health`.
- Keep the codebase startup-ready without RAG, embedding generation, or vector search logic.

## Files Created / Updated

| Area | Files |
|------|--------|
| Build | `pom.xml` |
| Config | `application.yml`, `application-dev.yml`, `application-prod.yml` |
| Entities | `entity/Document.java`, `entity/DocumentChunk.java` |
| Repositories | `repository/DocumentRepository.java`, `repository/DocumentChunkRepository.java` |
| API | `controller/HealthController.java`, `dto/HealthResponse.java` |
| Layers (placeholders) | `config/AppConfig.java`, `service/`, `exception/`, `util/` |
| Tests | `controller/HealthControllerTest.java`, `test/resources/application.yml` |
| Docs | `docs/plans/phase-1-backend-setup.md`, `README.md`, `AGENTS.md`, `.env.example` |

## Next Implementation Phases

### Phase 2: Database & Spring AI wiring

- Enable `spring.ai.vectorstore.pgvector.initialize-schema` where appropriate.
- Migrate `DocumentChunk.embedding` from `String` placeholder to pgvector type.
- Add Flyway/Liquibase migrations for `documents` and `document_chunks`.
- Verify Ollama chat and embedding model connectivity.

### Phase 3: Document ingestion

- Upload API (controller + service + DTOs).
- PDF parsing with Apache PDFBox.
- Chunking pipeline and persistence.

### Phase 4: RAG & Q&A

- Embed chunks via Spring AI `EmbeddingModel`.
- Store/search vectors with `PgVectorStore`.
- Q&A endpoint using retrieved context + Ollama chat model.

### Phase 5: Hardening

- Integration tests (Testcontainers: PostgreSQL + pgvector).
- Error handling, validation, and API documentation.

## Risks

| Risk | Mitigation |
|------|------------|
| Spring AI milestone API changes | Pin BOM version; follow Spring AI 2.0 reference docs. |
| Embedding dimension mismatch | Set `PGVECTOR_DIMENSIONS=768` for `nomic-embed-text` (see README environment variables). |
| Dual storage (JPA chunks vs Spring AI `vector_store`) | Phase 4 design: decide single source of truth or sync strategy. |
| Local env without PostgreSQL/Ollama | Document prerequisites; use `dev` profile and Docker compose (future). |
| `ddl-auto: validate` in default profile | Use `dev` profile locally until migrations exist. |

## Test Strategy

| Test | Scope |
|------|--------|
| `HealthControllerTest` | `@WebMvcTest` — JSON contract for `/api/v1/health` |
| `ApplicationTests` | `@SpringBootTest` — context loads with H2 and Spring AI auto-config excluded |
| Future | Repository tests with Testcontainers PostgreSQL; Ollama integration tests behind a profile |

Run tests:

```bash
./mvnw test
```

Run locally (requires PostgreSQL and `.env`):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
