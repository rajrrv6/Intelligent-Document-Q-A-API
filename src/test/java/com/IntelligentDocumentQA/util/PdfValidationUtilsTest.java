package com.IntelligentDocumentQA.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.IntelligentDocumentQA.exception.InvalidPdfException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class PdfValidationUtilsTest {

	@Test
	void rejectsEmptyFile() {
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", new byte[0]);
		assertThatThrownBy(() -> PdfValidationUtils.validatePdfUpload(file, 10_485_760))
				.isInstanceOf(InvalidPdfException.class)
				.hasMessageContaining("required");
	}

	@Test
	void rejectsNonPdfExtension() {
		MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", "hello".getBytes());
		assertThatThrownBy(() -> PdfValidationUtils.validatePdfUpload(file, 10_485_760))
				.isInstanceOf(InvalidPdfException.class)
				.hasMessageContaining("Only PDF");
	}

	@Test
	void rejectsOversizedFile() {
		byte[] bytes = new byte[20];
		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", bytes);
		assertThatThrownBy(() -> PdfValidationUtils.validatePdfUpload(file, 10))
				.isInstanceOf(InvalidPdfException.class)
				.hasMessageContaining("maximum");
	}
}
