# Chunking Strategy

This document explains how the Intelligent Document Q&A API splits PDF text into chunks before embedding and storage.

## Configuration

Chunk size and overlap are set with `INGESTION_CHUNK_SIZE` (**1000**) and `INGESTION_CHUNK_OVERLAP` (**200**). Embeddings use `OLLAMA_EMBEDDING_MODEL=nomic-embed-text` at **768** dimensions (`PGVECTOR_DIMENSIONS=768`).

For the full variable list, see [README.md — Environment variables](../README.md#environment-variables).

Embedding requests are processed in batches of 16 by default (`app.ingestion.embedding-batch-size` in `application.yml`; not a separate documented env var).

## Pipeline steps

1. **Extract** text per PDF page (PDFBox).
2. **Sanitize** whitespace (normalize spaces, collapse blank lines, trim).
3. **Skip** empty pages.
4. **Chunk** per page using sentence-aware grouping.
5. **Embed** chunk texts in batches (`nomic-embed-text`, 768 dimensions).
6. **Persist** rows in `document_chunks` with pgvector column `embedding vector(768)`.

## Why overlapping chunks improve semantic continuity

RAG retrieval returns a small set of chunks. If chunk boundaries fall in the middle of a definition, list, or argument, the embedding may not represent the full idea.

**Overlap** copies a tail of the previous chunk into the start of the next chunk so concepts that span boundaries still appear intact in at least one chunk. That improves the chance the retriever finds usable context.

## Why too much overlap increases vector redundancy

Overlap is not free:

- More chunks are produced for the same document.
- Storage and embedding API costs grow.
- Neighboring vectors become highly similar, which can cause redundant hits during later similarity search (Phase 4).

We use **200 characters** overlap (~20% of a 1000-character chunk) as a practical balance for prose-heavy PDFs.

## Why chunk size affects retrieval precision

| Smaller chunks | Larger chunks |
|----------------|---------------|
| More precise match to a specific sentence | More surrounding context per hit |
| Risk missing broader topic context | Risk diluting the embedding with unrelated text |

**1000 characters** is large enough to capture a paragraph-level idea but small enough to avoid mixing unrelated sections on typical assignment PDFs.

## Sentence boundary preservation

The chunker splits on sentence terminators (`.`, `!`, `?`) before packing sentences up to the size limit. When overlap is applied, the tail is trimmed to start near a sentence boundary where possible, reducing mid-word cuts.

## Duplicate filenames

Uploads are identified by database `documentId`. The same `fileName` may appear multiple times; uniqueness is not enforced on filename.

## Out of scope (Phase 3)

- Vector similarity search
- RAG answer generation
- Writes to Spring AI `vector_store` (no dual-write in this phase)

---

## Summary

The chunking strategy used in this project balances:

- semantic continuity
- retrieval precision
- vector efficiency
- contextual grounding

By combining sentence-aware chunking with controlled overlap, the system improves Retrieval Augmented Generation (RAG) quality while avoiding excessive vector redundancy and storage overhead.