package com.IntelligentDocumentQA.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.IntelligentDocumentQA.config.AppOllamaProperties;
import com.IntelligentDocumentQA.config.OllamaConnectivityClient;
import com.IntelligentDocumentQA.dto.SystemStatusResponse;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class InfrastructureHealthServiceTest {

	@Mock
	private DataSource dataSource;

	@Mock
	private Connection connection;

	@Mock
	private Statement statement;

	@Mock
	private ResultSet selectOneResultSet;

	@Mock
	private ResultSet pgvectorResultSet;

	@Mock
	private DatabaseMetaData databaseMetaData;

	@Mock
	private OllamaConnectivityClient ollamaConnectivityClient;

	@Mock
	private ObjectProvider<ChatModel> chatModel;

	@Mock
	private ObjectProvider<EmbeddingModel> embeddingModel;

	@Mock
	private ObjectProvider<VectorStore> vectorStore;

	private InfrastructureHealthService healthService;

	@BeforeEach
	void setUp() throws Exception {
		AppOllamaProperties properties = new AppOllamaProperties(
				"http://localhost:11434", "phi3", "nomic-embed-text", Duration.ofSeconds(5), Duration.ofSeconds(15));
		healthService = new InfrastructureHealthService(
				dataSource, properties, ollamaConnectivityClient, chatModel, embeddingModel, vectorStore);
	}

	@Test
	void getSystemStatusWhenAllComponentsHealthy() throws Exception {
		mockDatabaseUp();
		mockPgVectorEnabled();
		when(ollamaConnectivityClient.isReachable()).thenReturn(true);
		when(ollamaConnectivityClient.listModelNames()).thenReturn(Set.of("phi3:latest", "nomic-embed-text:latest"));
		when(chatModel.getIfAvailable()).thenReturn(org.mockito.Mockito.mock(ChatModel.class));
		when(embeddingModel.getIfAvailable()).thenReturn(org.mockito.Mockito.mock(EmbeddingModel.class));

		SystemStatusResponse status = healthService.getSystemStatus();

		assertThat(status.database()).isEqualTo("UP");
		assertThat(status.ollama()).isEqualTo("UP");
		assertThat(status.chatModelLoaded()).isTrue();
		assertThat(status.embeddingModelLoaded()).isTrue();
		assertThat(status.pgvectorEnabled()).isTrue();
	}

	@Test
	void getSystemStatusWhenOllamaUnavailable() throws Exception {
		mockDatabaseUp();
		mockPgVectorEnabled();
		when(ollamaConnectivityClient.isReachable()).thenReturn(false);

		SystemStatusResponse status = healthService.getSystemStatus();

		assertThat(status.ollama()).isEqualTo("DOWN");
		assertThat(status.chatModelLoaded()).isFalse();
		assertThat(status.embeddingModelLoaded()).isFalse();
	}

	@Test
	void redactCredentialsFromUrl() {
		assertThat(InfrastructureHealthService.redactCredentialsFromUrl(
						"jdbc:postgresql://localhost:5432/ragdb?password=secret"))
				.isEqualTo("jdbc:postgresql://localhost:5432/ragdb?password=***");
	}

	private void mockDatabaseUp() throws Exception {
		when(dataSource.getConnection()).thenReturn(connection);
		when(connection.createStatement()).thenReturn(statement);
		when(statement.executeQuery(anyString())).thenAnswer(invocation -> {
			String sql = invocation.getArgument(0, String.class);
			if (sql.contains("pg_extension")) {
				when(pgvectorResultSet.next()).thenReturn(true);
				return pgvectorResultSet;
			}
			when(selectOneResultSet.next()).thenReturn(true);
			when(selectOneResultSet.getInt(1)).thenReturn(1);
			return selectOneResultSet;
		});
	}

	private void mockPgVectorEnabled() {
		// Covered by mockDatabaseUp query routing.
	}
}
