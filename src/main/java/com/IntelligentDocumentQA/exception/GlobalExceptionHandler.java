package com.IntelligentDocumentQA.exception;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(OllamaUnavailableException.class)
	public ResponseEntity<Map<String, Object>> handleOllamaUnavailable(OllamaUnavailableException ex) {
		log.error("Ollama unavailable: {}", ex.getMessage());
		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "OLLAMA_UNAVAILABLE", ex.getMessage());
	}

	@ExceptionHandler(DatabaseUnavailableException.class)
	public ResponseEntity<Map<String, Object>> handleDatabaseUnavailable(DatabaseUnavailableException ex) {
		log.error("Database unavailable: {}", ex.getMessage());
		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_UNAVAILABLE", ex.getMessage());
	}

	@ExceptionHandler(VectorStoreInitializationException.class)
	public ResponseEntity<Map<String, Object>> handleVectorStoreFailure(VectorStoreInitializationException ex) {
		log.error("Vector store initialization failure: {}", ex.getMessage());
		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "VECTOR_STORE_UNAVAILABLE", ex.getMessage());
	}

	@ExceptionHandler(InvalidPdfException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidPdf(InvalidPdfException ex) {
		log.warn("Invalid PDF upload: {}", ex.getMessage());
		return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PDF", ex.getMessage());
	}

	@ExceptionHandler(DocumentIngestionException.class)
	public ResponseEntity<Map<String, Object>> handleDocumentIngestion(DocumentIngestionException ex) {
		log.error("Document ingestion failed: {}", ex.getMessage());
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "DOCUMENT_INGESTION_FAILED", ex.getMessage());
	}

	@ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(java.util.stream.Collectors.joining(", "));
		log.warn("Validation failure: {}", errorMessage);
		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", errorMessage);
	}

	@ExceptionHandler(QueryTimeoutException.class)
	public ResponseEntity<Map<String, Object>> handleQueryTimeout(QueryTimeoutException ex) {
		log.error("Query timed out: {}", ex.getMessage());
		return buildResponse(HttpStatus.GATEWAY_TIMEOUT, "QUERY_TIMEOUT", ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("Invalid query request: {}", ex.getMessage());
		return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_QUERY", ex.getMessage());
	}

	private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status)
				.body(Map.of(
						"timestamp", Instant.now().toString(),
						"status", status.value(),
						"error", code,
						"message", message));
	}
}
