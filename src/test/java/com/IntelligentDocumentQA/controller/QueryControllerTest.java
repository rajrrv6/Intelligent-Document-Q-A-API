package com.IntelligentDocumentQA.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.IntelligentDocumentQA.dto.DebugChunkDto;
import com.IntelligentDocumentQA.dto.QueryDebugResponse;
import com.IntelligentDocumentQA.dto.QueryResponse;
import com.IntelligentDocumentQA.dto.SourceChunkDto;
import com.IntelligentDocumentQA.exception.OllamaUnavailableException;
import com.IntelligentDocumentQA.exception.QueryTimeoutException;
import com.IntelligentDocumentQA.service.QueryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QueryController.class)
class QueryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private QueryService queryService;

	@Test
	void querySuccessReturnsOk() throws Exception {
		QueryResponse mockResponse = new QueryResponse(
				"Paris",
				List.of(new SourceChunkDto(1L, 1L, "file.pdf", "Paris capital", 1, 0.95)),
				150,
				false
		);

		when(queryService.query(any(), any(), any())).thenReturn(mockResponse);

		String requestJson = """
				{
				  "question": "What is the capital of France?",
				  "topK": 5,
				  "similarityThreshold": 0.65
				}
				""";

		mockMvc.perform(post("/api/v1/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.answer").value("Paris"))
				.andExpect(jsonPath("$.fallbackUsed").value(false))
				.andExpect(jsonPath("$.sources[0].fileName").value("file.pdf"))
				.andExpect(jsonPath("$.sources[0].similarity").value(0.95));
	}

	@Test
	void queryBlankQuestionReturnsBadRequest() throws Exception {
		String requestJson = """
				{
				  "question": "   ",
				  "topK": 3
				}
				""";

		mockMvc.perform(post("/api/v1/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void queryValidationFailureFromServiceReturnsBadRequest() throws Exception {
		when(queryService.query(any(), any(), any()))
				.thenThrow(new IllegalArgumentException("Question exceeds maximum length of 1000"));

		String requestJson = """
				{
				  "question": "Too long question..."
				}
				""";

		mockMvc.perform(post("/api/v1/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_QUERY"))
				.andExpect(jsonPath("$.message").value("Question exceeds maximum length of 1000"));
	}

	@Test
	void queryTimeoutReturnsGatewayTimeout() throws Exception {
		when(queryService.query(any(), any(), any()))
				.thenThrow(new QueryTimeoutException("Ollama query timed out"));

		String requestJson = """
				{
				  "question": "Slow question"
				}
				""";

		mockMvc.perform(post("/api/v1/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isGatewayTimeout())
				.andExpect(jsonPath("$.error").value("QUERY_TIMEOUT"))
				.andExpect(jsonPath("$.message").value("Ollama query timed out"));
	}

	@Test
	void queryOllamaUnavailableReturnsServiceUnavailable() throws Exception {
		when(queryService.query(any(), any(), any()))
				.thenThrow(new OllamaUnavailableException("Ollama is not reachable"));

		String requestJson = """
				{
				  "question": "Unavailable model query"
				}
				""";

		mockMvc.perform(post("/api/v1/query")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestJson))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.error").value("OLLAMA_UNAVAILABLE"));
	}

	@Test
	void queryDebugSuccessReturnsOk() throws Exception {
		QueryDebugResponse mockDebugResponse = new QueryDebugResponse(
				List.of(new DebugChunkDto(10L, 1L, "Some text content", 2, 0.88)),
				List.of(0.88),
				"System instructions and context Rome is...",
				false
		);

		when(queryService.queryDebug(any(), any(), any())).thenReturn(mockDebugResponse);

		mockMvc.perform(get("/api/v1/query/debug")
						.param("question", "Where is Rome?")
						.param("topK", "5")
						.param("similarityThreshold", "0.65"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fallbackFlag").value(false))
				.andExpect(jsonPath("$.generatedPrompt").value(org.hamcrest.Matchers.containsString("Rome is...")))
				.andExpect(jsonPath("$.retrievedChunks[0].chunkId").value(10))
				.andExpect(jsonPath("$.similarityScores[0]").value(0.88));
	}
}
