package com.IntelligentDocumentQA.dto;

public record SourceChunkDto(
		Long chunkId,
		Long documentId,
		String fileName,
		String text,
		Integer pageNumber,
		double similarity) {
}
