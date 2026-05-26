package com.IntelligentDocumentQA.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Timed HTTP probes against Ollama for infrastructure diagnostics only.
 * Does not replace Spring AI {@code OllamaChatModel} / {@code OllamaEmbeddingModel} beans.
 */
@Component
public class OllamaConnectivityClient {

	private static final Logger log = LoggerFactory.getLogger(OllamaConnectivityClient.class);
	private static final Pattern MODEL_NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");

	private final AppOllamaProperties properties;
	private final RestClient restClient;

	public OllamaConnectivityClient(AppOllamaProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout((int) properties.connectTimeout().toMillis());
		requestFactory.setReadTimeout((int) properties.readTimeout().toMillis());
		this.restClient = RestClient.builder()
				.baseUrl(properties.baseUrl())
				.requestFactory(requestFactory)
				.build();
	}

	public boolean isReachable() {
		try {
			restClient.get().uri("/api/tags").retrieve().toBodilessEntity();
			return true;
		}
		catch (RestClientException ex) {
			log.debug("Ollama unreachable at {} (connectTimeout={}, readTimeout={}): {}",
					properties.baseUrl(), properties.connectTimeout(), properties.readTimeout(), ex.getMessage());
			return false;
		}
	}

	public Set<String> listModelNames() {
		try {
			String body = restClient.get().uri("/api/tags").retrieve().body(String.class);
			if (body == null || body.isBlank()) {
				return Collections.emptySet();
			}
			Set<String> names = new HashSet<>();
			Matcher matcher = MODEL_NAME_PATTERN.matcher(body);
			while (matcher.find()) {
				names.add(matcher.group(1));
			}
			return names;
		}
		catch (Exception ex) {
			log.debug("Failed to list Ollama models from {}: {}", properties.baseUrl(), ex.getMessage());
			return Collections.emptySet();
		}
	}

	public static boolean isModelAvailable(String configuredModel, Set<String> availableModels) {
		if (configuredModel == null || configuredModel.isBlank()) {
			return false;
		}
		return availableModels.stream().anyMatch(available -> available.equals(configuredModel)
				|| available.startsWith(configuredModel + ":"));
	}
}
