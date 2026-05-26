package com.IntelligentDocumentQA.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.rag")
public record QueryProperties(
		@DefaultValue("5") int defaultTopK,
		@DefaultValue("0.65") double defaultSimilarityThreshold,
		@DefaultValue("4000") int maxPromptSize,
		@DefaultValue("1000") int maxQuestionLength,
		@DefaultValue("15s") Duration timeout) {
}
