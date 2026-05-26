package com.IntelligentDocumentQA.dto;

public interface ChunkProjection {
	Long getId();
	Long getDocumentId();
	String getChunkText();
	String getFileName();
	Integer getPageNumber();
	Integer getChunkIndex();
	Double getSimilarity();
}
