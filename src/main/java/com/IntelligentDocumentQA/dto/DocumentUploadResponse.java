package com.IntelligentDocumentQA.dto;

import java.time.Instant;

public record DocumentUploadResponse(
		Long documentId, String fileName, Instant uploadTime, int chunkCount) {
}
