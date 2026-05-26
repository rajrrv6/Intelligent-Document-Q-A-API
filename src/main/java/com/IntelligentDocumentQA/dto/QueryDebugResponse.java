package com.IntelligentDocumentQA.dto;

import java.util.List;

public record QueryDebugResponse(
		List<DebugChunkDto> retrievedChunks,
		List<Double> similarityScores,
		String generatedPrompt,
		boolean fallbackFlag) {
}
