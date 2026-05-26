package com.IntelligentDocumentQA.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.IntelligentDocumentQA.dto.ExtractedPage;
import com.IntelligentDocumentQA.util.PdfTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;

class PdfExtractionServiceTest {

	private final PdfExtractionService pdfExtractionService = new PdfExtractionService();

	@Test
	void extractsNonEmptyTextFromPdf() throws Exception {
		byte[] pdfBytes = PdfTestSupport.createSamplePdf(
				"Retrieval augmented generation improves answer quality.");

		List<ExtractedPage> pages = pdfExtractionService.extractPages(pdfBytes);

		assertThat(pages).isNotEmpty();
		assertThat(pages.getFirst().text()).contains("Retrieval augmented generation");
	}
}
