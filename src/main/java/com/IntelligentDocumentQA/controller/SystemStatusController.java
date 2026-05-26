package com.IntelligentDocumentQA.controller;

import com.IntelligentDocumentQA.dto.SystemStatusResponse;
import com.IntelligentDocumentQA.service.InfrastructureHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SystemStatusController {

	private final InfrastructureHealthService infrastructureHealthService;

	public SystemStatusController(InfrastructureHealthService infrastructureHealthService) {
		this.infrastructureHealthService = infrastructureHealthService;
	}

	@GetMapping("/system/status")
	public ResponseEntity<SystemStatusResponse> systemStatus() {
		return ResponseEntity.ok(infrastructureHealthService.getSystemStatus());
	}
}
