package com.IntelligentDocumentQA.controller;

import com.IntelligentDocumentQA.dto.SystemStatusResponse;
import com.IntelligentDocumentQA.service.InfrastructureHealthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemStatusController.class)
class SystemStatusControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InfrastructureHealthService infrastructureHealthService;

	@Test
	void systemStatusReturnsInfrastructureSummary() throws Exception {
		when(infrastructureHealthService.getSystemStatus())
				.thenReturn(new SystemStatusResponse("UP", "UP", true, true, true));

		mockMvc.perform(get("/api/v1/system/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.database").value("UP"))
				.andExpect(jsonPath("$.ollama").value("UP"))
				.andExpect(jsonPath("$.embeddingModelLoaded").value(true))
				.andExpect(jsonPath("$.chatModelLoaded").value(true))
				.andExpect(jsonPath("$.pgvectorEnabled").value(true));
	}
}
