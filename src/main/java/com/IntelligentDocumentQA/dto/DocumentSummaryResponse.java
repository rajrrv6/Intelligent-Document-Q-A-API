package com.IntelligentDocumentQA.dto;

import java.time.Instant;

public record DocumentSummaryResponse(
		Long documentId, String fileName, Instant uploadTime, long chunkCount) {
}
