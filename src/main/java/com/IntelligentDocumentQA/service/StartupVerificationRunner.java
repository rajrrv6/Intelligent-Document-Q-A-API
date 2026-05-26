package com.IntelligentDocumentQA.service;

import com.IntelligentDocumentQA.config.AppOllamaProperties;
import com.IntelligentDocumentQA.config.OllamaConnectivityClient;
import com.IntelligentDocumentQA.dto.SystemStatusResponse;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Runs non-blocking infrastructure checks after startup. Ollama or pgvector issues are logged
 * but do not abort application startup so {@code /api/v1/system/status} remains available.
 */
@Component
public class StartupVerificationRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(StartupVerificationRunner.class);

	private final InfrastructureHealthService healthService;
	private final AppOllamaProperties ollamaProperties;
	private final OllamaConnectivityClient ollamaConnectivityClient;
	private final ObjectProvider<ChatModel> chatModel;
	private final ObjectProvider<EmbeddingModel> embeddingModel;
	private final EmbeddingDimensionValidator embeddingDimensionValidator;

	@Value("${spring.ai.vectorstore.pgvector.dimensions:768}")
	private int vectorDimensions;

	public StartupVerificationRunner(
			InfrastructureHealthService healthService,
			AppOllamaProperties ollamaProperties,
			OllamaConnectivityClient ollamaConnectivityClient,
			@Qualifier("ollamaChatModel") ObjectProvider<ChatModel> chatModel,
			@Qualifier("ollamaEmbeddingModel") ObjectProvider<EmbeddingModel> embeddingModel,
			EmbeddingDimensionValidator embeddingDimensionValidator) {
		this.healthService = healthService;
		this.ollamaProperties = ollamaProperties;
		this.ollamaConnectivityClient = ollamaConnectivityClient;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
		this.embeddingDimensionValidator = embeddingDimensionValidator;
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Datasource URL (sanitized): {}", healthService.sanitizeDatasourceUrl());
		log.info("PGVector dimensions: {}", vectorDimensions);
		log.info("Active Ollama chat model (configured): {}", ollamaProperties.chatModel());
		log.info("Active Ollama embedding model (configured): {}", ollamaProperties.embeddingModel());

		if (!healthService.isDatabaseUp()) {
			log.error("PostgreSQL connectivity check failed. Verify DB_URL, DB_USERNAME, and DB_PASSWORD.");
		}
		else {
			log.info("PostgreSQL connectivity check passed.");
		}

		if (!healthService.isPgVectorExtensionEnabled()) {
			log.error(
					"pgvector extension not found. Run: CREATE EXTENSION IF NOT EXISTS vector; on the target database.");
		}
		else {
			log.info("pgvector extension check passed (extname = 'vector').");
		}

		if (!ollamaConnectivityClient.isReachable()) {
			log.warn(
					"Ollama is unavailable at {}. The application will continue so diagnostics remain available at /api/v1/system/status.",
					ollamaProperties.baseUrl());
		}
		else {
			Set<String> models = ollamaConnectivityClient.listModelNames();
			log.info("Ollama connectivity check passed. Models reported by /api/tags: {}", models);
			if (!OllamaConnectivityClient.isModelAvailable(ollamaProperties.chatModel(), models)) {
				log.warn("Configured chat model '{}' was not found in Ollama model list.", ollamaProperties.chatModel());
			}
			if (!OllamaConnectivityClient.isModelAvailable(ollamaProperties.embeddingModel(), models)) {
				log.warn(
						"Configured embedding model '{}' was not found in Ollama model list.",
						ollamaProperties.embeddingModel());
			}
		}

		if (chatModel.getIfAvailable() == null) {
			log.warn("Spring AI OllamaChatModel bean is not available.");
		}
		if (embeddingModel.getIfAvailable() == null) {
			log.warn("Spring AI OllamaEmbeddingModel bean is not available.");
		}
		else {
			embeddingDimensionValidator.validate();
		}

		try {
			healthService.verifyVectorStoreOrThrow();
			log.info("Spring AI VectorStore bean is available.");
		}
		catch (Exception ex) {
			log.error("Vector store readiness check failed: {}", ex.getMessage());
		}

		SystemStatusResponse status = healthService.getSystemStatus();
		log.info(
				"Infrastructure summary: database={}, ollama={}, chatModelLoaded={}, embeddingModelLoaded={}, pgvectorEnabled={}",
				status.database(),
				status.ollama(),
				status.chatModelLoaded(),
				status.embeddingModelLoaded(),
				status.pgvectorEnabled());
	}
}
