package com.IntelligentDocumentQA.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.IntelligentDocumentQA.config.QueryProperties;
import com.IntelligentDocumentQA.dto.ChunkProjection;
import com.IntelligentDocumentQA.dto.QueryDebugResponse;
import com.IntelligentDocumentQA.dto.QueryResponse;
import com.IntelligentDocumentQA.entity.Document;
import com.IntelligentDocumentQA.entity.DocumentChunk;
import com.IntelligentDocumentQA.exception.QueryTimeoutException;
import com.IntelligentDocumentQA.repository.DocumentChunkRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class QueryServiceTest {

	@Mock
	private DocumentChunkRepository documentChunkRepository;

	@Mock
	private EmbeddingService embeddingService;

	@Mock
	private ObjectProvider<ChatModel> chatModelProvider;

	@Mock
	private ChatModel chatModel;

	private QueryProperties queryProperties;
	private QueryService queryService;

	@BeforeEach
	void setUp() {
		queryProperties = new QueryProperties(5, 0.65, 4000, 1000, Duration.ofMillis(200));
		queryService = new QueryService(documentChunkRepository, embeddingService, chatModelProvider, queryProperties);
	}

	@Test
	void queryValidQuestionReturnsAnswer() {
		String question = "What is the capital of France?";
		float[] dummyEmbedding = new float[768];
		dummyEmbedding[0] = 1.0f;
		dummyEmbedding[1] = 0.5f;

		TestChunkProjection chunk = new TestChunkProjection(
				10L,
				1L,
				"Paris is the capital of France.",
				"geography.pdf",
				1,
				0,
				1.0
		);

		when(embeddingService.embedQuestion(question)).thenReturn(dummyEmbedding);
		when(documentChunkRepository.findSimilarChunks(any(), anyDouble(), any()))
				.thenReturn(List.of(chunk));
		when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
		when(chatModel.call(anyString())).thenReturn("Paris");

		QueryResponse response = queryService.query(question, 5, 0.65);

		assertThat(response.answer()).isEqualTo("Paris");
		assertThat(response.fallbackUsed()).isFalse();
		assertThat(response.sources()).hasSize(1);
		assertThat(response.sources().get(0).text()).isEqualTo("Paris is the capital of France.");
		assertThat(response.sources().get(0).similarity()).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-5));
	}

	@Test
	void queryEmptyMatchesReturnsFallbackWithoutCallingLLM() {
		String question = "What is quantum computing?";
		float[] dummyEmbedding = new float[768];

		when(embeddingService.embedQuestion(question)).thenReturn(dummyEmbedding);
		when(documentChunkRepository.findSimilarChunks(any(), anyDouble(), any()))
				.thenReturn(Collections.emptyList());

		QueryResponse response = queryService.query(question, 5, 0.65);

		assertThat(response.answer()).isEqualTo(QueryService.FALLBACK_ANSWER);
		assertThat(response.fallbackUsed()).isTrue();
		assertThat(response.sources()).isEmpty();
		verify(chatModelProvider, never()).getIfAvailable();
	}

	@Test
	void queryRejectsBlankQuestion() {
		assertThatThrownBy(() -> queryService.query("   ", 5, 0.65))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Question must not be blank");
	}

	@Test
	void queryRejectsTooLongQuestion() {
		String longQuestion = "a".repeat(1001);
		assertThatThrownBy(() -> queryService.query(longQuestion, 5, 0.65))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Question exceeds maximum length");
	}

	@Test
	void queryHandlesTimeoutCorrectly() {
		String question = "Explain general relativity.";
		float[] dummyEmbedding = new float[768];

		TestChunkProjection chunk = new TestChunkProjection(
				20L,
				1L,
				"Einstein proposed general relativity.",
				"physics.pdf",
				1,
				0,
				1.0
		);

		when(embeddingService.embedQuestion(question)).thenReturn(dummyEmbedding);
		when(documentChunkRepository.findSimilarChunks(any(), anyDouble(), any()))
				.thenReturn(List.of(chunk));
		when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
		when(chatModel.call(anyString())).thenAnswer(invocation -> {
			Thread.sleep(500); // Exceeds 200ms timeout
			return "General relativity is...";
		});

		assertThatThrownBy(() -> queryService.query(question, 5, 0.65))
				.isInstanceOf(QueryTimeoutException.class)
				.hasMessageContaining("timed out");
	}

	@Test
	void queryPromptTruncationRemovesLowestSimilarityChunksFirst() {
		// Set a maxPromptSize that fits instructions + chunk1 but truncates chunk2
		queryProperties = new QueryProperties(5, 0.65, 580, 1000, Duration.ofSeconds(2));
		queryService = new QueryService(documentChunkRepository, embeddingService, chatModelProvider, queryProperties);

		String question = "Truncation test?";
		float[] dummyEmbedding = new float[768];

		TestChunkProjection chunk1 = new TestChunkProjection(
				1L,
				1L,
				"First highly relevant context sentence that takes up some character space.",
				"test.pdf",
				1,
				0,
				0.9
		);
		TestChunkProjection chunk2 = new TestChunkProjection(
				2L,
				1L,
				"Second less relevant context sentence that should be dropped because we run out of prompt budget.",
				"test.pdf",
				2,
				1,
				0.7
		);

		when(embeddingService.embedQuestion(question)).thenReturn(dummyEmbedding);
		when(documentChunkRepository.findSimilarChunks(any(), anyDouble(), any()))
				.thenReturn(List.of(chunk1, chunk2)); // Order: higher, then lower
		when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
		when(chatModel.call(anyString())).thenReturn("Processed");

		queryService.query(question, 5, 0.65);

		org.mockito.ArgumentCaptor<String> promptCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
		verify(chatModel).call(promptCaptor.capture());

		String generatedPrompt = promptCaptor.getValue();
		// Verify Chunk 1 is in the prompt, but Chunk 2 was truncated/omitted
		assertThat(generatedPrompt).contains("First highly relevant context sentence");
		assertThat(generatedPrompt).doesNotContain("Second less relevant context sentence");
	}

	@Test
	void queryDebugSuccess() {
		String question = "Where is Rome?";
		float[] dummyEmbedding = new float[768];
		dummyEmbedding[0] = 0.8f;

		TestChunkProjection chunk = new TestChunkProjection(
				15L,
				1L,
				"Rome is in Italy.",
				"history.pdf",
				1,
				0,
				0.8
		);

		when(embeddingService.embedQuestion(question)).thenReturn(dummyEmbedding);
		when(documentChunkRepository.findSimilarChunks(any(), anyDouble(), any()))
				.thenReturn(List.of(chunk));
		when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
		when(chatModel.call(anyString())).thenReturn("Rome is the capital of Italy.");

		QueryDebugResponse debugResponse = queryService.queryDebug(question, 5, 0.65);

		assertThat(debugResponse.fallbackFlag()).isFalse();
		assertThat(debugResponse.generatedPrompt()).contains("Rome is in Italy.");
		assertThat(debugResponse.retrievedChunks()).hasSize(1);
		assertThat(debugResponse.retrievedChunks().get(0).text()).isEqualTo("Rome is in Italy.");
		assertThat(debugResponse.similarityScores()).hasSize(1);
		assertThat(debugResponse.similarityScores().get(0)).isGreaterThan(0.0);
	}

	@Test
	void queryPreservesShortGroundedAnswers() {
		String question = "What is the capital of France?";
		float[] dummyEmbedding = new float[768];

		TestChunkProjection chunk = new TestChunkProjection(
				10L, 1L, "Paris is the capital of France.", "geography.pdf", 1, 0, 1.0
		);

		when(embeddingService.embedQuestion(question)).thenReturn(dummyEmbedding);
		when(documentChunkRepository.findSimilarChunks(any(), anyDouble(), any()))
				.thenReturn(List.of(chunk));
		when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
		when(chatModel.call(anyString())).thenReturn("Paris");

		QueryResponse response = queryService.query(question, 5, 0.65);

		assertThat(response.answer()).isEqualTo("Paris");
		assertThat(response.fallbackUsed()).isFalse();
	}

	@Test
	void queryDoesNotFallbackOnRefusalPhrases() {
		String question = "What is the capital of France?";
		float[] dummyEmbedding = new float[768];

		TestChunkProjection chunk = new TestChunkProjection(
				10L, 1L, "Paris is the capital of France.", "geography.pdf", 1, 0, 1.0
		);

		when(embeddingService.embedQuestion(question)).thenReturn(dummyEmbedding);
		when(documentChunkRepository.findSimilarChunks(any(), anyDouble(), any()))
				.thenReturn(List.of(chunk));
		when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
		when(chatModel.call(anyString())).thenReturn("I am sorry, but there is not enough information to satisfy this request directly, although the document mentions Paris.");

		QueryResponse response = queryService.query(question, 5, 0.65);

		assertThat(response.answer()).isEqualTo("I am sorry, but there is not enough information to satisfy this request directly, although the document mentions Paris.");
		assertThat(response.fallbackUsed()).isFalse();
	}

	@Test
	void queryTriggersFallbackOnBlankOrNullAnswer() {
		String question = "What is the capital of France?";
		float[] dummyEmbedding = new float[768];

		TestChunkProjection chunk = new TestChunkProjection(
				10L, 1L, "Paris is the capital of France.", "geography.pdf", 1, 0, 1.0
		);

		when(embeddingService.embedQuestion(question)).thenReturn(dummyEmbedding);
		when(documentChunkRepository.findSimilarChunks(any(), anyDouble(), any()))
				.thenReturn(List.of(chunk));
		when(chatModelProvider.getIfAvailable()).thenReturn(chatModel);
		
		// Case 1: Null response
		when(chatModel.call(anyString())).thenReturn(null);
		QueryResponse responseNull = queryService.query(question, 5, 0.65);
		assertThat(responseNull.answer()).isEqualTo(QueryService.FALLBACK_ANSWER);
		assertThat(responseNull.fallbackUsed()).isTrue();

		// Case 2: Blank response
		when(chatModel.call(anyString())).thenReturn("   ");
		QueryResponse responseBlank = queryService.query(question, 5, 0.65);
		assertThat(responseBlank.answer()).isEqualTo(QueryService.FALLBACK_ANSWER);
		assertThat(responseBlank.fallbackUsed()).isTrue();
	}

	private static record TestChunkProjection(
			Long id,
			Long documentId,
			String chunkText,
			String fileName,
			Integer pageNumber,
			Integer chunkIndex,
			Double similarity
	) implements ChunkProjection {
		@Override public Long getId() { return id; }
		@Override public Long getDocumentId() { return documentId; }
		@Override public String getChunkText() { return chunkText; }
		@Override public String getFileName() { return fileName; }
		@Override public Integer getPageNumber() { return pageNumber; }
		@Override public Integer getChunkIndex() { return chunkIndex; }
		@Override public Double getSimilarity() { return similarity; }
	}
}
