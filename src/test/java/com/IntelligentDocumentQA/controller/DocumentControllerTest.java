package com.IntelligentDocumentQA.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.IntelligentDocumentQA.dto.DocumentUploadResponse;
import com.IntelligentDocumentQA.exception.InvalidPdfException;
import com.IntelligentDocumentQA.service.DocumentIngestionService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DocumentIngestionService documentIngestionService;

	@Test
	void uploadReturnsCreatedWithResponseBody() throws Exception {
		when(documentIngestionService.ingest(any()))
				.thenReturn(new DocumentUploadResponse(1L, "report.pdf", Instant.parse("2026-05-23T00:00:00Z"), 3));

		MockMultipartFile file =
				new MockMultipartFile("file", "report.pdf", "application/pdf", "%PDF-sample".getBytes());

		mockMvc.perform(multipart("/api/v1/documents/upload").file(file))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.documentId").value(1))
				.andExpect(jsonPath("$.fileName").value("report.pdf"))
				.andExpect(jsonPath("$.chunkCount").value(3));
	}

	@Test
	void uploadValidationFailureReturnsBadRequest() throws Exception {
		when(documentIngestionService.ingest(any())).thenThrow(new InvalidPdfException("Only PDF files are supported"));

		MockMultipartFile file = new MockMultipartFile("file", "notes.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

		mockMvc.perform(multipart("/api/v1/documents/upload").file(file))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_PDF"));
	}
}
