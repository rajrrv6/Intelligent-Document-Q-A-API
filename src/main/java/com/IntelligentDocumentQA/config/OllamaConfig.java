package com.IntelligentDocumentQA.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Relies on Spring AI auto-configuration for {@code OllamaChatModel} and
 * {@code OllamaEmbeddingModel}. Missing Ollama at startup must not prevent the app from
 * starting; connectivity is verified asynchronously and exposed via diagnostics endpoints.
 */
@Configuration
@EnableConfigurationProperties({AppOllamaProperties.class, QueryProperties.class})
public class OllamaConfig {

	private static final Logger log = LoggerFactory.getLogger(OllamaConfig.class);

	private final AppOllamaProperties properties;
	private final ObjectProvider<ChatModel> chatModel;
	private final ObjectProvider<EmbeddingModel> embeddingModel;

	public OllamaConfig(
			AppOllamaProperties properties,
			@Qualifier("ollamaChatModel") ObjectProvider<ChatModel> chatModel,
			@Qualifier("ollamaEmbeddingModel") ObjectProvider<EmbeddingModel> embeddingModel) {
		this.properties = properties;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logOllamaConfiguration() {
		log.info("Ollama base URL: {}", properties.baseUrl());
		log.info("Configured chat model: {}", properties.chatModel());
		log.info("Configured embedding model: {}", properties.embeddingModel());
		log.info("Ollama connect/read timeout: {} / {}", properties.connectTimeout(), properties.readTimeout());
		log.info("Spring AI OllamaChatModel bean present: {}", chatModel.getIfAvailable() != null);
		log.info("Spring AI OllamaEmbeddingModel bean present: {}", embeddingModel.getIfAvailable() != null);
	}
}
