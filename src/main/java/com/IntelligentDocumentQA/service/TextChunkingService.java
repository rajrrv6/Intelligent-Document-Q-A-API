package com.IntelligentDocumentQA.service;

import com.IntelligentDocumentQA.config.IngestionProperties;
import com.IntelligentDocumentQA.dto.ExtractedPage;
import com.IntelligentDocumentQA.dto.TextChunk;
import com.IntelligentDocumentQA.util.TextSanitizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class TextChunkingService {

	private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("(?<=[.!?])\\s+");

	private final IngestionProperties ingestionProperties;

	public TextChunkingService(IngestionProperties ingestionProperties) {
		this.ingestionProperties = ingestionProperties;
	}

	public List<TextChunk> chunkPages(List<ExtractedPage> pages) {
		int chunkSize = ingestionProperties.chunkSize();
		int overlap = ingestionProperties.chunkOverlap();
		List<TextChunk> chunks = new ArrayList<>();
		int chunkIndex = 0;

		for (ExtractedPage page : pages) {
			String sanitized = TextSanitizer.sanitize(page.text());
			if (sanitized.isBlank()) {
				continue;
			}
			List<String> sentences = splitSentences(sanitized);
			StringBuilder current = new StringBuilder();

			for (String sentence : sentences) {
				String trimmedSentence = sentence.trim();
				if (trimmedSentence.isEmpty()) {
					continue;
				}
				int additionalLength = current.isEmpty()
						? trimmedSentence.length()
						: trimmedSentence.length() + 1;
				if (current.length() > 0 && current.length() + additionalLength > chunkSize) {
					chunks.add(new TextChunk(
							chunkIndex++,
							current.toString().trim(),
							page.pageNumber(),
							page.pageNumber(),
							page.pageNumber()));
					current = new StringBuilder(takeOverlapTail(current.toString(), overlap));
				}
				if (current.length() > 0) {
					current.append(' ');
				}
				current.append(trimmedSentence);
			}

			if (!current.isEmpty()) {
				String chunkText = current.toString().trim();
				if (!chunkText.isBlank()) {
					chunks.add(new TextChunk(
							chunkIndex++,
							chunkText,
							page.pageNumber(),
							page.pageNumber(),
							page.pageNumber()));
				}
			}
		}
		return chunks;
	}

	List<String> splitSentences(String text) {
		String[] parts = SENTENCE_BOUNDARY.split(text);
		List<String> sentences = new ArrayList<>();
		for (String part : parts) {
			if (!part.isBlank()) {
				sentences.add(part.trim());
			}
		}
		if (sentences.isEmpty() && !text.isBlank()) {
			sentences.add(text.trim());
		}
		return sentences;
	}

	String takeOverlapTail(String text, int overlap) {
		if (overlap <= 0 || text.length() <= overlap) {
			return text;
		}
		String tail = text.substring(text.length() - overlap).trim();
		int sentenceStart = Math.max(tail.lastIndexOf('.'), Math.max(tail.lastIndexOf('!'), tail.lastIndexOf('?')));
		if (sentenceStart >= 0 && sentenceStart + 1 < tail.length()) {
			return tail.substring(sentenceStart + 1).trim();
		}
		int space = tail.indexOf(' ');
		return space > 0 ? tail.substring(space + 1).trim() : tail;
	}
}
