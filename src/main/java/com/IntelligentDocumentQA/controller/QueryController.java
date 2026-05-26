package com.IntelligentDocumentQA.controller;

import com.IntelligentDocumentQA.dto.QueryDebugResponse;
import com.IntelligentDocumentQA.dto.QueryRequest;
import com.IntelligentDocumentQA.dto.QueryResponse;
import com.IntelligentDocumentQA.service.QueryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/query")
public class QueryController {

	private static final Logger log = LoggerFactory.getLogger(QueryController.class);

	private final QueryService queryService;

	public QueryController(QueryService queryService) {
		this.queryService = queryService;
	}

	@PostMapping
	public ResponseEntity<QueryResponse> query(@Valid @RequestBody QueryRequest request) {
		log.info("Received query request: question length={}", request.question().length());
		QueryResponse response = queryService.query(
				request.question(),
				request.topK(),
				request.similarityThreshold()
		);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/debug")
	public ResponseEntity<QueryDebugResponse> queryDebug(
			@RequestParam("question") String question,
			@RequestParam(value = "topK", required = false) Integer topK,
			@RequestParam(value = "similarityThreshold", required = false) Double similarityThreshold) {
		log.info("Received debug query request: question length={}", question != null ? question.length() : 0);
		QueryDebugResponse response = queryService.queryDebug(question, topK, similarityThreshold);
		return ResponseEntity.ok(response);
	}
}
