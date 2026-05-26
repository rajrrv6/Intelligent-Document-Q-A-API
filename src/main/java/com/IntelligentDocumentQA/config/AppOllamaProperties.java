package com.IntelligentDocumentQA.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ollama")
public record AppOllamaProperties(
		String baseUrl,
		String chatModel,
		String embeddingModel,
		Duration connectTimeout,
		Duration readTimeout) {
}
