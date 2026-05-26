-- Phase 3 reference migration for PostgreSQL (apply manually in prod if not using ddl-auto).
-- Dev profile may rely on Hibernate ddl-auto=update instead.

ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS file_size_bytes BIGINT,
    ADD COLUMN IF NOT EXISTS content_type VARCHAR(127);

ALTER TABLE document_chunks
    DROP COLUMN IF EXISTS embedding;

ALTER TABLE document_chunks
    ADD COLUMN IF NOT EXISTS embedding vector(768),
    ADD COLUMN IF NOT EXISTS page_number INTEGER,
    ADD COLUMN IF NOT EXISTS page_start INTEGER,
    ADD COLUMN IF NOT EXISTS page_end INTEGER;

-- If embedding column already exists as varchar, drop and recreate as shown above.
-- After migration, backfill or re-ingest documents as needed.
