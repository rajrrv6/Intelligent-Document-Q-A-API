package com.IntelligentDocumentQA.dto;

import jakarta.validation.constraints.NotBlank;

public record QueryRequest(
		@NotBlank(message = "Question must not be blank")
		String question,
		Integer topK,
		Double similarityThreshold) {
}
