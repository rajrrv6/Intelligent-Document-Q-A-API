package com.IntelligentDocumentQA.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.ingestion")
public record IngestionProperties(
		int chunkSize,
		int chunkOverlap,
		int embeddingBatchSize,
		DataSize maxUploadSize) {
}
