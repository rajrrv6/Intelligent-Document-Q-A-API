package com.IntelligentDocumentQA.service;

import com.IntelligentDocumentQA.dto.ExtractedPage;
import com.IntelligentDocumentQA.exception.DocumentIngestionException;
import com.IntelligentDocumentQA.util.TextSanitizer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PdfExtractionService {

	private static final Logger log = LoggerFactory.getLogger(PdfExtractionService.class);

	public List<ExtractedPage> extractPages(byte[] pdfBytes) {
		long startNanos = System.nanoTime();
		try (PDDocument document = Loader.loadPDF(pdfBytes)) {
			PDFTextStripper stripper = new PDFTextStripper();
			int pageCount = document.getNumberOfPages();
			List<ExtractedPage> pages = new ArrayList<>();
			for (int page = 1; page <= pageCount; page++) {
				stripper.setStartPage(page);
				stripper.setEndPage(page);
				String pageText = TextSanitizer.sanitize(stripper.getText(document));
				if (!pageText.isBlank()) {
					pages.add(new ExtractedPage(page, pageText));
				}
			}
			long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
			log.info("PDF text extraction completed in {} ms ({} non-empty pages)", durationMs, pages.size());
			return pages;
		}
		catch (IOException ex) {
			throw new DocumentIngestionException("Failed to extract text from PDF", ex);
		}
	}
}
