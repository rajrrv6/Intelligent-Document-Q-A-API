package com.IntelligentDocumentQA.service;

import com.IntelligentDocumentQA.config.IngestionProperties;
import com.IntelligentDocumentQA.dto.DocumentSummaryResponse;
import com.IntelligentDocumentQA.dto.DocumentUploadResponse;
import com.IntelligentDocumentQA.dto.ExtractedPage;
import com.IntelligentDocumentQA.dto.TextChunk;
import com.IntelligentDocumentQA.entity.Document;
import com.IntelligentDocumentQA.entity.DocumentChunk;
import com.IntelligentDocumentQA.exception.DocumentIngestionException;
import com.IntelligentDocumentQA.repository.DocumentChunkRepository;
import com.IntelligentDocumentQA.repository.DocumentRepository;
import com.IntelligentDocumentQA.util.PdfValidationUtils;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentIngestionService {

	private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

	private final DocumentRepository documentRepository;
	private final DocumentChunkRepository documentChunkRepository;
	private final PdfExtractionService pdfExtractionService;
	private final TextChunkingService textChunkingService;
	private final EmbeddingService embeddingService;
	private final IngestionProperties ingestionProperties;

	public DocumentIngestionService(
			DocumentRepository documentRepository,
			DocumentChunkRepository documentChunkRepository,
			PdfExtractionService pdfExtractionService,
			TextChunkingService textChunkingService,
			EmbeddingService embeddingService,
			IngestionProperties ingestionProperties) {
		this.documentRepository = documentRepository;
		this.documentChunkRepository = documentChunkRepository;
		this.pdfExtractionService = pdfExtractionService;
		this.textChunkingService = textChunkingService;
		this.embeddingService = embeddingService;
		this.ingestionProperties = ingestionProperties;
	}

	@Transactional
	public DocumentUploadResponse ingest(MultipartFile file) {
		long startNanos = System.nanoTime();
		String fileName = file.getOriginalFilename();
		log.info("Document upload started: fileName={}, sizeBytes={}", fileName, file.getSize());

		PdfValidationUtils.validatePdfUpload(file, ingestionProperties.maxUploadSize().toBytes());

		Document document = documentRepository.save(Document.builder()
				.fileName(fileName)
				.uploadTime(Instant.now())
				.fileSizeBytes(file.getSize())
				.contentType(PdfValidationUtils.PDF_CONTENT_TYPE)
				.build());

		try {
			List<ExtractedPage> pages = pdfExtractionService.extractPages(readPdfBytes(file));
			if (pages.isEmpty()) {
				throw new DocumentIngestionException("PDF contains no extractable text");
			}

			List<TextChunk> chunks = textChunkingService.chunkPages(pages);
			if (chunks.isEmpty()) {
				throw new DocumentIngestionException("No text chunks were produced from the PDF");
			}
			log.info("Chunking completed for documentId={}: {} chunks", document.getId(), chunks.size());

			List<String> chunkTexts = chunks.stream().map(TextChunk::text).toList();
			List<float[]> embeddings = embeddingService.embedInBatches(chunkTexts);
			if (embeddings.size() != chunks.size()) {
				throw new DocumentIngestionException(
						"Embedding count mismatch: expected " + chunks.size() + " but received " + embeddings.size());
			}

			for (int index = 0; index < chunks.size(); index++) {
				TextChunk chunk = chunks.get(index);
				documentChunkRepository.save(DocumentChunk.builder()
						.document(document)
						.chunkText(chunk.text())
						.chunkIndex(chunk.chunkIndex())
						.embedding(embeddings.get(index))
						.pageNumber(chunk.pageNumber())
						.pageStart(chunk.pageStart())
						.pageEnd(chunk.pageEnd())
						.build());
			}

			long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
			log.info(
					"Document ingestion completed: documentId={}, fileName={}, chunkCount={}, durationMs={}",
					document.getId(),
					document.getFileName(),
					chunks.size(),
					durationMs);

			return new DocumentUploadResponse(
					document.getId(), document.getFileName(), document.getUploadTime(), chunks.size());
		}
		catch (IOException ex) {
			throw new DocumentIngestionException("Failed to read uploaded PDF", ex);
		}
	}

	private byte[] readPdfBytes(MultipartFile file) throws IOException {
		return file.getBytes();
	}

	@Transactional(readOnly = true)
	public List<DocumentSummaryResponse> listDocuments() {
		return documentRepository.findAllSummaries();
	}
}
