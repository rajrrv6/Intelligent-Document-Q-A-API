# Agent Instructions & Guidelines

This document outlines the development guidelines, architecture rules, and workflow processes for the Intelligent Document Q&A API. All autonomous agents and developers working on this codebase must adhere strictly to these practices.

## Tech Stack
* **Java**: Version 21
* **Spring Boot**: Backend REST API, dependency injection, and configuration management
* **Spring AI**: LLM orchestration — Ollama chat/embedding models and PGVector vector store
* **PostgreSQL + pgvector**: Relational storage and vector search
* **Ollama**: Local LLM and embedding inference
* **Apache PDFBox**: PDF document parsing (future phases)
* **Maven**: Dependency management and build tool

## Workflow Rules
1. **Plan before coding**: Every non-trivial change requires a structured plan before execution. Create or update the implementation plan and get approval.
2. **Keep diffs small**: Ensure code changes are targeted, minimal, and reviewable. Avoid large PRs with scattered changes.
3. **One task per PR**: Focus on resolving one specific feature or bug at a time.
4. **Never hardcode secrets**: Do not embed credentials, database passwords, API keys, or configurations in source code.
5. **Use environment variables only**: Load all system configurations dynamically from the environment.
6. **Every feature must include tests**: Provide comprehensive unit and/or integration tests for all newly added logic.

## Architecture Rules
1. **Layered Architecture**: Adhere to the classic tiered layout:
   * **Controller**: HTTP request handling, validation, mapping DTOs to/from domain models.
   * **Service**: Core business logic, transaction boundaries, orchestration of DB operations and AI services.
   * **Repository**: Direct database access and persistence operations.
2. **Separate Controller/Service/Repository**: Avoid leaking database logic into controllers, or HTTP logic into services.
3. **DTO-based APIs**: Never expose database entities directly to the client. Use Data Transfer Objects (DTOs) for request payloads and response bodies.
4. **Reuse Existing Patterns**: Follow the structure, naming conventions, and coding style established in the existing codebase.

## AI Rules (Spring AI)
1. **Never generate fake/mock RAG responses**: The API must always run real queries through the RAG pipeline. Do not return hardcoded or mock answers for AI tasks in production code.
2. **Always use real embeddings and vector search**: Retrieval must use Spring AI `EmbeddingModel` and `PgVectorStore` (pgvector) with embeddings from Ollama.
3. **Use Spring AI abstractions**: Prefer `ChatModel`, `EmbeddingModel`, and `VectorStore` over ad-hoc HTTP calls to Ollama.
4. **Do not modify unrelated files**: Limit file edits exclusively to the files relevant to the active task.
