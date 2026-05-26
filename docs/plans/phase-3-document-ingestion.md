# Phase 3: Document Ingestion Pipeline

## Goals

- Accept PDF uploads via multipart API.
- Extract text (PDFBox), chunk with overlap, embed with Ollama via Spring AI.
- Persist `documents` + `document_chunks` with pgvector embeddings (768d).
- List uploaded documents with chunk counts.

## Architecture

| Component | Role |
|-----------|------|
| `DocumentController` | `POST /upload`, `GET /` |
| `DocumentIngestionService` | Transactional orchestration |
| `PdfExtractionService` | PDFBox per-page extraction |
| `TextChunkingService` | Sanitize + sentence-aware overlap chunking |
| `EmbeddingService` | Batched `EmbeddingModel.embed` calls |
| `document_chunks.embedding` | `vector(768)` via Hibernate `SqlTypes.VECTOR` |

**Storage:** `document_chunks` only. Spring AI `vector_store` is **not** written in Phase 3.

## Embedding workflow

1. Save `Document` metadata (`fileName`, `uploadTime`, `fileSizeBytes`, `contentType`).
2. Extract and chunk text.
3. Call `EmbeddingModel` in batches (default size 16).
4. Save each `DocumentChunk` with `float[]` mapped to `vector(768)`.
5. On any failure, transaction rolls back (no partial chunks).

Startup `EmbeddingDimensionValidator` compares probe embedding length to `PGVECTOR_DIMENSIONS` (default **768** with `nomic-embed-text`).

## pgvector storage approach

```sql
embedding vector(768) NOT NULL
```

Hibernate mapping:

```java
@JdbcTypeCode(SqlTypes.VECTOR)
@Column(nullable = false, columnDefinition = "vector(768)")
private float[] embedding;
```

Reference SQL: `src/main/resources/db/migration/V3__document_chunks_pgvector.sql`

## API

### `POST /api/v1/documents/upload`

- `multipart/form-data`, field name **`file`**
- PDF only, max size from `INGESTION_MAX_UPLOAD_SIZE` (default **10MB**; see README)
- **201 Created** with `documentId`, `fileName`, `uploadTime`, `chunkCount`

### `GET /api/v1/documents`

Returns array of `{ documentId, fileName, uploadTime, chunkCount }`.

## Risks

| Risk | Mitigation |
|------|------------|
| Ollama overload on large PDFs | Batched embeddings (size 16) |
| Dimension mismatch | Startup validator; use README defaults (`PGVECTOR_DIMENSIONS=768`) |
| Partial ingest on error | `@Transactional` on ingestion service |
| H2 tests vs pgvector | Unit/controller tests mock persistence; PostgreSQL used locally |

## Manual verification checklist

- [ ] `ollama pull nomic-embed-text`
- [ ] `CREATE EXTENSION vector;` on target database
- [ ] `.env` matches README defaults (`PGVECTOR_DIMENSIONS=768`, `INGESTION_CHUNK_SIZE=1000`, `INGESTION_CHUNK_OVERLAP=200`)
- [ ] Upload PDF via curl (see README)
- [ ] `GET /api/v1/documents` shows chunk count
- [ ] SQL: `SELECT id, chunk_index, page_number, vector_dims(embedding) FROM document_chunks LIMIT 5;`
- [ ] Stop Ollama → upload returns 503 with clear error

## Test strategy

- `PdfExtractionServiceTest`, `TextChunkingServiceTest`, `PdfValidationUtilsTest`
- `DocumentIngestionServiceTest` (mocked collaborators)
- `DocumentControllerTest` (MockMvc)

## Next phase (not in scope)

- Similarity search / retrieval
- RAG prompts and chat endpoint
- Optional sync to `vector_store`
