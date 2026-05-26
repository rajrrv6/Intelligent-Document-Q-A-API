<<<<<<< HEAD
````md
# Intelligent Document Q&A API

An intelligent Retrieval Augmented Generation (RAG) backend system built using Java Spring Boot, Spring AI, PostgreSQL + pgvector, and Ollama local LLM integration.

This project allows users to upload PDF documents, automatically extract and chunk text, generate vector embeddings, store them inside PostgreSQL using pgvector, and ask natural language questions grounded strictly on uploaded document context.

The system performs semantic retrieval using cosine similarity search and generates grounded responses using locally hosted LLMs through Ollama.

---

# Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Backend Framework | Spring Boot |
| AI Framework | Spring AI |
| Database | PostgreSQL |
| Vector Database | pgvector |
| ORM | Hibernate / JPA |
| Embedding Model | nomic-embed-text |
| LLM Runtime | Ollama |
| Chat Models | phi3 / tinyllama / llama3 |
| PDF Parsing | Apache PDFBox |
| Build Tool | Maven |
| API Testing | Postman / curl |

---

# Features Implemented

## 1. Document Ingestion Pipeline

Implemented API:

```http
POST /api/v1/documents/upload
````

Capabilities:

* PDF upload support
* Multipart file handling
* PDF validation
* File size validation
* Text extraction using PDFBox
* Sentence-aware chunking
* Overlapping chunk generation
* Embedding generation using Ollama
* Batch embedding processing
* Metadata persistence
* Vector storage using pgvector
* Transaction-safe ingestion

Stored metadata:

* filename
* page number
* chunk index
* upload timestamp
* vector embedding

---

## 2. Question Answering Pipeline

Implemented API:

```http
POST /api/v1/query
```

Capabilities:

* Natural language query handling
* Question embedding generation
* Cosine similarity retrieval
* Top-K semantic search
* Similarity threshold filtering
* Prompt construction
* Context-grounded answering
* Local LLM integration via Ollama
* Structured JSON responses
* Fallback handling
* Timeout handling
* Prompt sanitization

---

## 3. Document Listing API

Implemented API:

```http
GET /api/v1/documents
```

Capabilities:

* List uploaded documents
* Return upload metadata
* Return chunk counts
* Return upload timestamps

---

## 4. Infrastructure Monitoring

Implemented endpoints:

```http
GET /actuator/health
GET /actuator/info
GET /api/v1/system/status
```

Capabilities:

* Database health monitoring
* Ollama availability checks
* Embedding model checks
* pgvector validation
* Infrastructure diagnostics

---

# RAG Architecture Overview

This project follows a Retrieval Augmented Generation (RAG) architecture.

The pipeline works in the following stages:

1. Document Upload
2. Text Extraction
3. Chunk Generation
4. Embedding Creation
5. Vector Storage
6. Semantic Retrieval
7. Prompt Construction
8. LLM Answer Generation

The system ensures that answers are generated strictly from uploaded document context.

---

# End-to-End Flow

```text
User Uploads PDF
        ↓
PDF Text Extraction
        ↓
Sentence-aware Chunking
        ↓
Overlapping Chunk Creation
        ↓
Embedding Generation using Ollama
        ↓
Store Embeddings in PostgreSQL pgvector
        ↓
User Asks Question
        ↓
Question Embedding Generation
        ↓
Cosine Similarity Retrieval
        ↓
Retrieve Top Relevant Chunks
        ↓
Prompt Construction
        ↓
LLM Grounded Answer Generation
        ↓
Structured JSON Response
```

---

# Project Structure

```text
src/main/java/com/IntelligentDocumentQA
│
├── config
├── controller
├── dto
├── entity
├── exception
├── repository
├── service
├── util
└── Application.java
```

---

# Database Design

## documents table

Stores uploaded document metadata.

| Column          | Description       |
| --------------- | ----------------- |
| id              | Document ID       |
| file_name       | Original filename |
| upload_time     | Upload timestamp  |
| file_size_bytes | File size         |
| content_type    | MIME type         |

---

## document_chunks table

Stores chunk data and embeddings.

| Column      | Description     |
| ----------- | --------------- |
| id          | Chunk ID        |
| document_id | Parent document |
| chunk_text  | Chunk content   |
| page_number | PDF page        |
| chunk_index | Chunk order     |
| embedding   | vector(768)     |

---

# Chunking Strategy

The application uses sentence-aware overlapping chunking.

Configuration:

```text
Chunk Size = 1000 characters
Chunk Overlap = 200 characters
```

The chunk overlap preserves semantic continuity between chunks.

This improves:

* retrieval quality
* context preservation
* answer grounding
* semantic matching

---

# Why Overlapping Chunks?

Without overlap:

* important information may split across chunks
* semantic continuity breaks
* retrieval quality decreases

With overlap:

* neighboring context is preserved
* retrieval becomes more accurate
* answers become more grounded
* semantic continuity improves

---

# Embedding Generation

Embedding model used:

```text
nomic-embed-text
```

Embedding dimensions:

```text
768
```

Embeddings are generated using Ollama locally.

---

# Vector Similarity Search

The system uses cosine similarity search via pgvector.

Formula:

```text
similarity = 1 - cosine_distance
```

Higher similarity indicates higher semantic relevance.

---

# Why Cosine Similarity?

Cosine similarity is ideal for semantic search because:

* compares vector direction
* independent of vector magnitude
* highly effective for embeddings
* improves semantic retrieval accuracy

---

# Prompt Engineering Strategy

The LLM receives only retrieved chunk context.

The prompt instructs the model:

* answer strictly from provided context
* do not hallucinate
* do not use external knowledge
* do not fabricate answers
* say when information is unavailable

This significantly reduces hallucinations.

---

# Similarity Threshold Filtering

Implemented configurable threshold filtering:

```text
app.rag.min-similarity=0.65
```

Benefits:

* removes irrelevant chunks
* improves answer quality
* reduces hallucinations
* improves retrieval precision

---

# Prompt Size Protection

Implemented configurable prompt size limits.

If prompt size exceeds limit:

* lowest ranked chunks are removed first
* highest similarity chunks are preserved

This prevents oversized prompts and LLM instability.

---

# Timeout Handling

Implemented explicit timeout handling for:

* embedding generation
* LLM chat completion

Default timeout:

```text
15 seconds
```

Timeout response example:

```json
{
  "error": "QUERY_TIMEOUT",
  "status": 504
}
```

---

# Fallback Handling

If retrieval returns no relevant chunks:

* LLM invocation is skipped
* fallback response is returned directly

Fallback response:

```text
The uploaded documents do not contain enough information to answer this question.
```

Benefits:

* reduces hallucinations
* prevents unsupported answers
* improves groundedness

---

# DTO Projection Optimization

The retrieval pipeline uses DTO projections instead of Hibernate-managed entities.

Benefits:

* eliminates LazyInitializationException
* reduces memory overhead
* improves retrieval performance
* prevents proxy-loading issues

---

# Logging Features

The application logs:

* upload duration
* extraction duration
* embedding duration
* query duration
* similarity score
* chunk counts
* timeout events
* fallback usage

---

# Error Handling

Centralized exception handling implemented using:

```text
GlobalExceptionHandler
```

Handled scenarios:

* invalid uploads
* oversized uploads
* empty questions
* invalid questions
* timeout failures
* database errors
* Ollama failures
* validation errors

---

# Supported Models

## Embedding Model

```text
nomic-embed-text
```

---

## Chat Models Tested

| Model     | Status |
| --------- | ------ |
| phi3      | Tested |
| tinyllama | Tested |
| llama3    | Tested |

---

# Local Setup

## Prerequisites

Install:

* Java 21
* PostgreSQL
* Ollama
* Maven

---

# PostgreSQL + pgvector Setup

Start PostgreSQL container:

```bash
docker run --name postgres-pgvector \
-e POSTGRES_PASSWORD=postgres \
-p 5432:5432 \
-d pgvector/pgvector:pg16
```

Create database:

```sql
CREATE DATABASE ragdb;
```

Enable pgvector:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

---

# Ollama Setup

Install required models:

```bash
ollama pull phi3
ollama pull nomic-embed-text
```

Alternative lightweight models:

```bash
ollama pull tinyllama
ollama pull llama3
```

---

# Environment Variables

Create `.env` file using `.env.example`.

Example:

```env
DB_URL=jdbc:postgresql://localhost:5432/ragdb
DB_USERNAME=postgres
DB_PASSWORD=postgres

OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_CHAT_MODEL=phi3
OLLAMA_EMBEDDING_MODEL=nomic-embed-text

PGVECTOR_DIMENSIONS=768

INGESTION_MAX_UPLOAD_SIZE=10MB
INGESTION_CHUNK_SIZE=1000
INGESTION_CHUNK_OVERLAP=200

SERVER_PORT=8080
```

---

# Run Application

```bash
./mvnw spring-boot:run
```

---

# Run Tests

```bash
./mvnw test
```

---

# Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

---

# Upload Document Example

```bash
curl -X POST http://localhost:8080/api/v1/documents/upload \
-F "file=@sample.pdf"
```

Response:

```json
{
  "documentId": 2,
  "fileName": "sample.pdf",
  "uploadTime": "2026-05-26T10:00:00Z",
  "chunkCount": 54
}
```

---

# List Documents Example

```bash
curl http://localhost:8080/api/v1/documents
```

---

# Query Example

```bash
curl -X POST http://localhost:8080/api/v1/query \
-H "Content-Type: application/json" \
-d "{\"question\":\"What is object oriented programming?\"}"
```

---

# Query Response Example

```json
{
  "answer": "Object-oriented programming is a programming paradigm based on objects and classes.",
  "sources": [
    {
      "chunkId": 439,
      "documentId": 12,
      "fileName": "java.pdf",
      "pageNumber": 1,
      "similarity": 0.61
    }
  ],
  "queryTimeMs": 2518,
  "fallbackUsed": false
}
```

---

# Infrastructure Verification

## Verify Docker

```bash
docker ps
```

---

## Verify Ollama

```bash
ollama list
```

---

## Verify pgvector

```sql
SELECT extname FROM pg_extension WHERE extname = 'vector';
```

---

# Performance Notes

Observed local timings:

| Operation            | Approx Time |
| -------------------- | ----------- |
| PDF ingestion        | 10–20 sec   |
| Embedding generation | 5–15 sec    |
| Query retrieval      | < 1 sec     |
| LLM response         | 3–15 sec    |

Performance depends on:

* CPU
* RAM
* Ollama model size
* document size

---

# Automated Test Coverage

Implemented tests for:

* controllers
* services
* utilities
* chunking logic
* ingestion pipeline
* retrieval pipeline
* timeout handling
* validation logic
* fallback logic

---

# Future Improvements

Planned future enhancements:

* DOCX ingestion
* Hybrid retrieval
* Reranking
* Conversation memory
* Authentication
* Streaming responses
* Multi-user document isolation
* GPU acceleration
* HNSW indexing

---

# Submission Deliverables

Included:

* Source code
* README.md
* chunking-strategy.md
* .env.example
* curl examples
* PostgreSQL + pgvector integration
* Ollama integration
* automated tests

---

# Author

Chandan Raj

Backend Developer Assignment Submission

Implemented using:

* Java 21
* Spring Boot
* Spring AI
* PostgreSQL + pgvector
* Ollama local LLM integration

```
```
=======
# Intelligent-Document-Q-A-API
AI-powered RAG-based backend API that enables users to upload PDF/DOCX documents, generate vector embeddings, perform semantic search, and ask natural language questions using locally hosted LLMs via Ollama. Built with Spring Boot, Spring AI, and PGVector.
>>>>>>> 454b9780a0ec1d23be0236a5e49d57a7ec02a89d
