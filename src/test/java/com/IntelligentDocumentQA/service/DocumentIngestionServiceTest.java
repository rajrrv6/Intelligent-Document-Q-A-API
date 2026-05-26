package com.IntelligentDocumentQA.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.IntelligentDocumentQA.config.IngestionProperties;
import com.IntelligentDocumentQA.dto.DocumentUploadResponse;
import com.IntelligentDocumentQA.entity.Document;
import com.IntelligentDocumentQA.repository.DocumentChunkRepository;
import com.IntelligentDocumentQA.repository.DocumentRepository;
import com.IntelligentDocumentQA.util.PdfTestSupport;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

@ExtendWith(MockitoExtension.class)
class DocumentIngestionServiceTest {

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private DocumentChunkRepository documentChunkRepository;

	@Mock
	private PdfExtractionService pdfExtractionService;

	@Mock
	private TextChunkingService textChunkingService;

	@Mock
	private EmbeddingService embeddingService;

	private DocumentIngestionService documentIngestionService;

	@BeforeEach
	void setUp() {
		IngestionProperties properties =
				new IngestionProperties(1000, 200, 16, DataSize.ofMegabytes(10));
		documentIngestionService = new DocumentIngestionService(
				documentRepository,
				documentChunkRepository,
				pdfExtractionService,
				textChunkingService,
				embeddingService,
				properties);
	}

	@Test
	void ingestPersistsDocumentChunksWithEmbeddings() throws Exception {
		byte[] pdfBytes = PdfTestSupport.createSamplePdf("Ingestion pipeline test sentence.");
		MockMultipartFile file =
				new MockMultipartFile("file", "report.pdf", "application/pdf", pdfBytes);

		Document savedDocument = Document.builder().id(1L).fileName("report.pdf").build();
		when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
		when(pdfExtractionService.extractPages(pdfBytes))
				.thenReturn(List.of(new com.IntelligentDocumentQA.dto.ExtractedPage(1, "Ingestion pipeline test sentence.")));
		when(textChunkingService.chunkPages(anyList()))
				.thenReturn(List.of(new com.IntelligentDocumentQA.dto.TextChunk(0, "Ingestion pipeline test sentence.", 1, 1, 1)));
		when(embeddingService.embedInBatches(anyList())).thenReturn(List.of(new float[768]));

		DocumentUploadResponse response = documentIngestionService.ingest(file);

		assertThat(response.documentId()).isEqualTo(1L);
		assertThat(response.chunkCount()).isEqualTo(1);
		ArgumentCaptor<com.IntelligentDocumentQA.entity.DocumentChunk> chunkCaptor =
				ArgumentCaptor.forClass(com.IntelligentDocumentQA.entity.DocumentChunk.class);
		verify(documentChunkRepository).save(chunkCaptor.capture());
		assertThat(chunkCaptor.getValue().getEmbedding()).hasSize(768);
	}
}
