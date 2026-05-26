package com.IntelligentDocumentQA.dto;

import java.util.List;

public record QueryResponse(
		String answer,
		List<SourceChunkDto> sources,
		long queryTimeMs,
		boolean fallbackUsed) {
}
