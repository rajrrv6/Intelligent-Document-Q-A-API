# Checkpoint: Phase 4 (RAG Query Pipeline)

## Completed Tasks
- [x] Implemented direct SQL pgvector similarity search on the `document_chunks` table utilizing the `<=>` operator.
- [x] Configured configurable properties `app.rag` mapping topK, similarity threshold, max prompt size, max question length, and execution timeout.
- [x] Built the `QueryService` orchestration pipeline, handling question embedding generation, short-circuit fallback without LLM invocation, grounded prompt formatting, prompt injection defense, prompt truncation (removing lowest-similarity chunks first), model timeout handles, and response sanitization.
- [x] Created `QueryController` exposing `POST /api/v1/query` and `GET /api/v1/query/debug`.
- [x] Map `QueryTimeoutException`, `IllegalArgumentException`, and validation errors inside `GlobalExceptionHandler`.
- [x] Implemented mock unit tests and controller integration tests.
- [x] Built the application and verified all tests pass (29 passing tests, 0 failures).

## Verification Status
* **Unit & Controller Tests**: Passed (`BUILD SUCCESS`).
* **Logs Inspection**: Verified that logs print top similarity score, filtered chunk count, and `llmSkipped` statuses.
