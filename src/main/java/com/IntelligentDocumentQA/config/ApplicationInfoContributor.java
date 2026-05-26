package com.IntelligentDocumentQA.config;

import com.IntelligentDocumentQA.service.InfrastructureHealthService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInfoContributor implements InfoContributor {

	private final AppOllamaProperties ollamaProperties;
	private final InfrastructureHealthService healthService;
	private final ObjectProvider<ChatModel> chatModel;
	private final ObjectProvider<EmbeddingModel> embeddingModel;

	@Value("${spring.ai.vectorstore.pgvector.dimensions:384}")
	private int vectorDimensions;

	public ApplicationInfoContributor(
			AppOllamaProperties ollamaProperties,
			InfrastructureHealthService healthService,
			@Qualifier("ollamaChatModel") ObjectProvider<ChatModel> chatModel,
			@Qualifier("ollamaEmbeddingModel") ObjectProvider<EmbeddingModel> embeddingModel) {
		this.ollamaProperties = ollamaProperties;
		this.healthService = healthService;
		this.chatModel = chatModel;
		this.embeddingModel = embeddingModel;
	}

	@Override
	public void contribute(Info.Builder builder) {
		Map<String, Object> infrastructure = new LinkedHashMap<>();
		infrastructure.put("datasourceUrl", healthService.sanitizeDatasourceUrl());
		infrastructure.put("ollamaBaseUrl", ollamaProperties.baseUrl());
		infrastructure.put("chatModel", ollamaProperties.chatModel());
		infrastructure.put("embeddingModel", ollamaProperties.embeddingModel());
		infrastructure.put("pgvectorDimensions", vectorDimensions);
		infrastructure.put("chatModelBeanPresent", chatModel.getIfAvailable() != null);
		infrastructure.put("embeddingModelBeanPresent", embeddingModel.getIfAvailable() != null);
		builder.withDetail("infrastructure", infrastructure);
	}
}
