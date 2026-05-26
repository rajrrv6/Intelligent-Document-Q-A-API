package com.IntelligentDocumentQA.service;

import com.IntelligentDocumentQA.config.IngestionProperties;
import com.IntelligentDocumentQA.exception.DocumentIngestionException;
import com.IntelligentDocumentQA.exception.OllamaUnavailableException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

	private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

	private final ObjectProvider<EmbeddingModel> embeddingModel;
	private final IngestionProperties ingestionProperties;

	public EmbeddingService(
			@Qualifier("ollamaEmbeddingModel") ObjectProvider<EmbeddingModel> embeddingModel,
			IngestionProperties ingestionProperties) {
		this.embeddingModel = embeddingModel;
		this.ingestionProperties = ingestionProperties;
	}

	public List<float[]> embedInBatches(List<String> texts) {
		EmbeddingModel model = embeddingModel.getIfAvailable();
		if (model == null) {
			throw new OllamaUnavailableException("Ollama embedding model is not available");
		}
		if (texts.isEmpty()) {
			return List.of();
		}
		long startNanos = System.nanoTime();
		int batchSize = ingestionProperties.embeddingBatchSize();
		List<float[]> embeddings = new ArrayList<>(texts.size());

		try {
			for (int index = 0; index < texts.size(); index += batchSize) {
				int end = Math.min(index + batchSize, texts.size());
				List<String> batch = texts.subList(index, end);
				log.debug("Generating embeddings for batch {}-{} of {}", index + 1, end, texts.size());
				embeddings.addAll(model.embed(batch));
			}
		}
		catch (Exception ex) {
			if (isConnectivityFailure(ex)) {
				throw new OllamaUnavailableException("Failed to generate embeddings using Ollama", ex);
			}
			throw new DocumentIngestionException("Failed to generate embeddings", ex);
		}

		long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
		log.info("Embedding generation completed in {} ms for {} chunks (batch size={})",
				durationMs, texts.size(), batchSize);
		return embeddings;
	}

	public float[] embedQuestion(String question) {
		EmbeddingModel model = embeddingModel.getIfAvailable();
		if (model == null) {
			throw new OllamaUnavailableException("Ollama embedding model is not available");
		}
		try {
			log.debug("Generating embedding for question");
			return model.embed(question);
		}
		catch (Exception ex) {
			if (isConnectivityFailure(ex)) {
				throw new OllamaUnavailableException("Failed to generate embedding using Ollama", ex);
			}
			throw new DocumentIngestionException("Failed to generate embedding for question", ex);
		}
	}

	private boolean isConnectivityFailure(Exception ex) {
		String message = ex.getMessage();
		return message != null
				&& (message.toLowerCase().contains("connection")
						|| message.toLowerCase().contains("timeout")
						|| message.toLowerCase().contains("refused"));
	}
}
