# Project Plan: Intelligent Document Q&A API

This document serves as the high-level roadmap and plan for implementing the Intelligent Document Q&A API.

## Phase 1: Foundational Setup
- [x] Initial codebase setup (Spring Boot, dependencies in `pom.xml`)
- [x] Folder structure and essential documentation (`AGENTS.md`, `README.md`, `.env.example`, `.gitignore`)

## Phase 2: Database & Model Infrastructure
- [x] Set up PostgreSQL and vector database schema (dev auto-init; prod manual)
- [x] Configure Spring AI Ollama chat and embedding models (auto-config + diagnostics)
- [x] Configure Spring AI PGVector vector store (auto-config + diagnostics)
- [x] Establish health checks and connectivity verifications (`/api/v1/system/status`, Actuator)

## Phase 3: Document Processing & Embeddings Pipeline
- [x] Implement file upload controllers and services
- [x] Build document parsing and chunking mechanisms
- [x] Implement chunk embeddings stored in PostgreSQL/pgvector (`document_chunks`)

## Phase 4: Context Retrieval & Querying (RAG)
- [x] Implement vector search querying
- [x] Integrate prompt templates and context building
- [x] Create HTTP endpoints for document Q&A

## Phase 5: Verification & Deployment
- [ ] Implement comprehensive integration tests (e.g., utilizing Testcontainers)
- [ ] Complete end-to-end user path testing
- [ ] Package and containerize the application
