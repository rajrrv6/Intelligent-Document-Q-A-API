package com.IntelligentDocumentQA.controller;

import com.IntelligentDocumentQA.dto.DocumentSummaryResponse;
import com.IntelligentDocumentQA.dto.DocumentUploadResponse;
import com.IntelligentDocumentQA.service.DocumentIngestionService;
import com.IntelligentDocumentQA.util.PdfValidationUtils;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

	private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

	private final DocumentIngestionService documentIngestionService;

	public DocumentController(DocumentIngestionService documentIngestionService) {
		this.documentIngestionService = documentIngestionService;
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestPart("file") MultipartFile file) {
		long startNanos = System.nanoTime();
		log.info("Upload request received for multipart field '{}'", PdfValidationUtils.MULTIPART_FIELD_NAME);
		DocumentUploadResponse response = documentIngestionService.ingest(file);
		long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
		log.info(
				"Upload request completed: documentId={}, chunkCount={}, durationMs={}",
				response.documentId(),
				response.chunkCount(),
				durationMs);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	public ResponseEntity<List<DocumentSummaryResponse>> listDocuments() {
		return ResponseEntity.ok(documentIngestionService.listDocuments());
	}
}
