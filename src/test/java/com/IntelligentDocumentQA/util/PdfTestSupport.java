package com.IntelligentDocumentQA.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public final class PdfTestSupport {

	private PdfTestSupport() {
	}

	public static byte[] createSamplePdf(String... lines) throws IOException {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);
			PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.beginText();
				contentStream.setFont(font, 12);
				contentStream.newLineAtOffset(50, 700);
				float leading = 14f;
				for (int index = 0; index < lines.length; index++) {
					if (index > 0) {
						contentStream.newLineAtOffset(0, -leading);
					}
					contentStream.showText(lines[index]);
				}
				contentStream.endText();
			}
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			document.save(outputStream);
			return outputStream.toByteArray();
		}
	}
}
