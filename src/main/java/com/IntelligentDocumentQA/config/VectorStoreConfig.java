package com.IntelligentDocumentQA.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * Spring AI auto-configures {@link org.springframework.ai.vectorstore.pgvector.PgVectorStore}
 * when {@code spring-ai-starter-vector-store-pgvector} is on the classpath. This class only
 * logs vector-store settings at startup.
 */
@Configuration
public class VectorStoreConfig {

	private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

	private final int dimensions;
	private final String tableName;
	private final boolean initializeSchema;
	private final ObjectProvider<VectorStore> vectorStore;

	public VectorStoreConfig(
			@Value("${spring.ai.vectorstore.pgvector.dimensions:384}") int dimensions,
			@Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}") String tableName,
			@Value("${spring.ai.vectorstore.pgvector.initialize-schema:false}") boolean initializeSchema,
			ObjectProvider<VectorStore> vectorStore) {
		this.dimensions = dimensions;
		this.tableName = tableName;
		this.initializeSchema = initializeSchema;
		this.vectorStore = vectorStore;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void logVectorStoreConfiguration() {
		log.info("PGVector dimensions: {}", dimensions);
		log.info("PGVector table: {}, initialize-schema={}", tableName, initializeSchema);
		log.info("Spring AI VectorStore bean present: {}", vectorStore.getIfAvailable() != null);
	}
}
