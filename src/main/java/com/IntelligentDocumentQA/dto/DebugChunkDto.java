package com.IntelligentDocumentQA.dto;

public record DebugChunkDto(
		Long chunkId,
		Long documentId,
		String text,
		Integer pageNumber,
		double similarity) {
}
