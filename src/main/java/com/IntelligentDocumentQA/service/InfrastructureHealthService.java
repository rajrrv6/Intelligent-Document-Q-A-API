package com.IntelligentDocumentQA.service;

import com.IntelligentDocumentQA.config.AppOllamaProperties;
import com.IntelligentDocumentQA.config.OllamaConnectivityClient;
import com.IntelligentDocumentQA.dto.SystemStatusResponse;
import com.IntelligentDocumentQA.exception.DatabaseUnavailableException;
import com.IntelligentDocumentQA.exception.OllamaUnavailableException;
import com.IntelligentDocumentQA.exception.VectorStoreInitializationException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class InfrastructureHealthService {

	private static final Logger log = LoggerFactory.getLogger(InfrastructureHealthService.class);
	private static final String PGVECTOR_EXTENSION_QUERY =
			"SELECT extname FROM pg_extension WHERE extname = 'vector'";

	private final DataSource dataSource;
	private final AppOllamaProperties ollamaProperties;
	private final OllamaConnectivityClient ollamaConnectivityClient;
	private final ObjectProvider<ChatModel> chatModel;
	private final ObjectProvider<EmbeddingModel> embeddingModel;
	private final ObjectProvider<VectorStore> vectorStore;

	public InfrastructureHealthService(
			DataSource dataSource,
			AppOllamaProperties ollamaProperties,
			OllamaConnectivityClient ollamaConnectivityClient,
			@Qualifier("ollamaChatModel") ObjectProvider<ChatModel> chatModel,
			@Qualifier("ollamaEmbeddingModel") ObjectProvider<EmbeddingModel> embeddingModel,
			ObjectProvider<VectorStore> vectorStore) {
		this.dataSource = dataSource;
		this.ollamaProperties = ollamaProperties;
		this.ollamaConnectivityClient = ollamaConnectivityClient;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
		this.vectorStore = vectorStore;
	}

	public SystemStatusResponse getSystemStatus() {
		boolean databaseUp = isDatabaseUp();
		boolean pgvectorEnabled = databaseUp && isPgVectorExtensionEnabled();
		boolean ollamaUp = ollamaConnectivityClient.isReachable();
		Set<String> availableModels = ollamaUp ? ollamaConnectivityClient.listModelNames() : Set.of();
		boolean chatModelLoaded = chatModel.getIfAvailable() != null
				&& ollamaUp
				&& OllamaConnectivityClient.isModelAvailable(ollamaProperties.chatModel(), availableModels);
		boolean embeddingModelLoaded = embeddingModel.getIfAvailable() != null
				&& ollamaUp
				&& OllamaConnectivityClient.isModelAvailable(ollamaProperties.embeddingModel(), availableModels);

		return new SystemStatusResponse(
				databaseUp ? "UP" : "DOWN",
				ollamaUp ? "UP" : "DOWN",
				embeddingModelLoaded,
				chatModelLoaded,
				pgvectorEnabled);
	}

	public void verifyDatabaseOrThrow() {
		if (!isDatabaseUp()) {
			throw new DatabaseUnavailableException("PostgreSQL connectivity check failed");
		}
	}

	public void verifyOllamaOrThrow() {
		if (!ollamaConnectivityClient.isReachable()) {
			throw new OllamaUnavailableException(
					"Ollama is unreachable at " + ollamaProperties.baseUrl()
							+ " within connect timeout " + ollamaProperties.connectTimeout());
		}
	}

	public void verifyVectorStoreOrThrow() {
		if (vectorStore.getIfAvailable() == null) {
			throw new VectorStoreInitializationException("Spring AI VectorStore bean is not available");
		}
		if (!isPgVectorExtensionEnabled()) {
			throw new VectorStoreInitializationException(
					"pgvector extension is not installed (expected extname = 'vector')");
		}
	}

	public boolean isDatabaseUp() {
		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT 1")) {
			return resultSet.next() && resultSet.getInt(1) == 1;
		}
		catch (Exception ex) {
			log.debug("Database connectivity check failed: {}", ex.getMessage());
			return false;
		}
	}

	public boolean isPgVectorExtensionEnabled() {
		try (Connection connection = dataSource.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(PGVECTOR_EXTENSION_QUERY)) {
			return resultSet.next();
		}
		catch (Exception ex) {
			log.debug("pgvector extension check failed: {}", ex.getMessage());
			return false;
		}
	}

	public String sanitizeDatasourceUrl() {
		try {
			String url = dataSource.getConnection().getMetaData().getURL();
			return redactCredentialsFromUrl(url);
		}
		catch (Exception ex) {
			log.debug("Unable to read datasource URL: {}", ex.getMessage());
			return "unknown";
		}
	}

	static String redactCredentialsFromUrl(String url) {
		if (url == null) {
			return "unknown";
		}
		return url.replaceAll("(?i)(password=)[^&]*", "$1***");
	}
}
