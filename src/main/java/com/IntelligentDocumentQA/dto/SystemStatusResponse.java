package com.IntelligentDocumentQA.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SystemStatusResponse(
		String database,
		String ollama,
		@JsonProperty("embeddingModelLoaded") boolean embeddingModelLoaded,
		@JsonProperty("chatModelLoaded") boolean chatModelLoaded,
		@JsonProperty("pgvectorEnabled") boolean pgvectorEnabled) {
}
