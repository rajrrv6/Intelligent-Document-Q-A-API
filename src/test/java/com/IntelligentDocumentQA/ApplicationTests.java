package com.IntelligentDocumentQA;

import com.IntelligentDocumentQA.config.OllamaConnectivityClient;
import com.IntelligentDocumentQA.service.DocumentIngestionService;
import com.IntelligentDocumentQA.service.EmbeddingDimensionValidator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
class ApplicationTests {

	@MockitoBean
	private OllamaConnectivityClient ollamaConnectivityClient;

	@MockitoBean
	private DocumentIngestionService documentIngestionService;

	@MockitoBean
	private EmbeddingDimensionValidator embeddingDimensionValidator;

	@BeforeEach
	void stubInfrastructure() {
		when(ollamaConnectivityClient.isReachable()).thenReturn(false);
		when(ollamaConnectivityClient.listModelNames()).thenReturn(Set.of());
		doNothing().when(embeddingDimensionValidator).validate();
	}

	@Test
	void contextLoads() {
	}

}
