package com.IntelligentDocumentQA.controller;

import com.IntelligentDocumentQA.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

	private static final String SERVICE_NAME = "Intelligent Document Q&A API";

	@GetMapping("/health")
	public ResponseEntity<HealthResponse> health() {
		return ResponseEntity.ok(new HealthResponse("UP", SERVICE_NAME));
	}
}
