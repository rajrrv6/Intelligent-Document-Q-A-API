package com.IntelligentDocumentQA.util;

public final class TextSanitizer {

	private TextSanitizer() {
	}

	public static String sanitize(String text) {
		if (text == null) {
			return "";
		}
		String normalized = text
				.replace('\u00A0', ' ')
				.replaceAll("\\r\\n?", "\n")
				.replaceAll("[ \\t\\f\\x0B]+", " ")
				.replaceAll("\\n{3,}", "\n\n")
				.trim();
		return normalized;
	}

	public static boolean isBlank(String text) {
		return sanitize(text).isBlank();
	}
}
