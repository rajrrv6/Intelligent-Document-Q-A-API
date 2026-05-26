package com.IntelligentDocumentQA.repository;

import com.IntelligentDocumentQA.entity.DocumentChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.IntelligentDocumentQA.dto.ChunkProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

	List<DocumentChunk> findByDocument_IdOrderByChunkIndexAsc(Long documentId);

	@Query("""
			SELECT c.id as id,
			       c.document.id as documentId,
			       c.chunkText as chunkText,
			       c.document.fileName as fileName,
			       c.pageNumber as pageNumber,
			       c.chunkIndex as chunkIndex,
			       (1.0 - cosine_distance(c.embedding, :queryEmbedding)) as similarity
			FROM DocumentChunk c
			WHERE 1.0 - cosine_distance(c.embedding, :queryEmbedding) >= :minSimilarity
			ORDER BY cosine_distance(c.embedding, :queryEmbedding) ASC
			""")
	List<ChunkProjection> findSimilarChunks(
			@Param("queryEmbedding") float[] queryEmbedding,
			@Param("minSimilarity") double minSimilarity,
			Pageable pageable);
}
