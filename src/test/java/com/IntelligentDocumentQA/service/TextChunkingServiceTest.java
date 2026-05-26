package com.IntelligentDocumentQA.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.IntelligentDocumentQA.config.IngestionProperties;
import com.IntelligentDocumentQA.dto.ExtractedPage;
import com.IntelligentDocumentQA.dto.TextChunk;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

class TextChunkingServiceTest {

	private TextChunkingService textChunkingService;

	@BeforeEach
	void setUp() {
		IngestionProperties properties =
				new IngestionProperties(80, 20, 16, DataSize.ofMegabytes(10));
		textChunkingService = new TextChunkingService(properties);
	}

	@Test
	void producesOrderedChunksWithOverlapAndNoEmptyChunks() {
		String sentence = "Semantic search works best when chunks preserve sentence boundaries.";
		String longText = (sentence + " ").repeat(6);
		List<TextChunk> chunks = textChunkingService.chunkPages(List.of(new ExtractedPage(1, longText)));

		assertThat(chunks).isNotEmpty();
		assertThat(chunks).extracting(TextChunk::chunkIndex).doesNotHaveDuplicates();
		assertThat(chunks.getFirst().chunkIndex()).isZero();
		assertThat(chunks).allSatisfy(chunk -> assertThat(chunk.text()).isNotBlank());
	}

	@Test
	void removesEmptyPages() {
		List<TextChunk> chunks = textChunkingService.chunkPages(List.of(
				new ExtractedPage(1, "   "),
				new ExtractedPage(2, "Valid sentence for chunking.")));

		assertThat(chunks).hasSize(1);
		assertThat(chunks.getFirst().pageNumber()).isEqualTo(2);
	}

	@Test
	void splitSentencesPreservesSentenceBoundaries() {
		List<String> sentences = textChunkingService.splitSentences("First sentence. Second sentence! Third?");
		assertThat(sentences).hasSize(3);
	}
}
