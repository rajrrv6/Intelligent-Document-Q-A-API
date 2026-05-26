package com.IntelligentDocumentQA.util;

import com.IntelligentDocumentQA.exception.InvalidPdfException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public final class PdfValidationUtils {

	public static final String PDF_CONTENT_TYPE = "application/pdf";
	public static final String MULTIPART_FIELD_NAME = "file";

	private PdfValidationUtils() {
	}

	public static void validatePdfUpload(MultipartFile file, long maxUploadSizeBytes) {
		if (file == null || file.isEmpty()) {
			throw new InvalidPdfException("PDF file is required");
		}
		if (file.getSize() > maxUploadSizeBytes) {
			throw new InvalidPdfException(
					"PDF file exceeds maximum allowed size of " + maxUploadSizeBytes + " bytes");
		}
		String originalFilename = file.getOriginalFilename();
		if (!StringUtils.hasText(originalFilename) || !originalFilename.toLowerCase().endsWith(".pdf")) {
			throw new InvalidPdfException("Only PDF files are supported");
		}
		String contentType = file.getContentType();
		if (contentType != null && !PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)) {
			throw new InvalidPdfException("Invalid content type. Expected application/pdf");
		}
	}
}
