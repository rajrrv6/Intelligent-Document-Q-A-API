package com.IntelligentDocumentQA.dto;

public record TextChunk(int chunkIndex, String text, Integer pageNumber, Integer pageStart, Integer pageEnd) {
}
