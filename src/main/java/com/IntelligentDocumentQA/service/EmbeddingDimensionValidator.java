package com.IntelligentDocumentQA.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingDimensionValidator {

	private static final Logger log = LoggerFactory.getLogger(EmbeddingDimensionValidator.class);
	private static final String PROBE_TEXT = "embedding-dimension-probe";

	private final ObjectProvider<EmbeddingModel> embeddingModel;
	private final int expectedDimensions;

	public EmbeddingDimensionValidator(
			@Qualifier("ollamaEmbeddingModel") ObjectProvider<EmbeddingModel> embeddingModel,
			@Value("${PGVECTOR_DIMENSIONS:768}") int expectedDimensions) {
		this.embeddingModel = embeddingModel;
		this.expectedDimensions = expectedDimensions;
	}

	public void validate() {
		EmbeddingModel model = embeddingModel.getIfAvailable();
		if (model == null) {
			log.warn("Skipping embedding dimension validation because OllamaEmbeddingModel is not available");
			return;
		}
		try {
			float[] embedding = model.embed(PROBE_TEXT);
			if (embedding.length != expectedDimensions) {
				log.error(
						"Embedding dimension mismatch: configured PGVECTOR_DIMENSIONS={} but model returned length={}",
						expectedDimensions,
						embedding.length);
			}
			else {
				log.info("Embedding dimension validation passed ({} dimensions)", embedding.length);
			}
		}
		catch (Exception ex) {
			log.warn("Could not validate embedding dimensions at startup: {}", ex.getMessage());
		}
	}
}
