package com.IntelligentDocumentQA.repository;

import com.IntelligentDocumentQA.dto.DocumentSummaryResponse;
import com.IntelligentDocumentQA.entity.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

	@Query("""
			SELECT new com.IntelligentDocumentQA.dto.DocumentSummaryResponse(
			    d.id, d.fileName, d.uploadTime, COUNT(c))
			FROM Document d
			LEFT JOIN DocumentChunk c ON c.document = d
			GROUP BY d.id, d.fileName, d.uploadTime
			ORDER BY d.uploadTime DESC
			""")
	List<DocumentSummaryResponse> findAllSummaries();
}
