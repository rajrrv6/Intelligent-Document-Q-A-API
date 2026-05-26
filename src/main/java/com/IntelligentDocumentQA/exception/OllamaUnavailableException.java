package com.IntelligentDocumentQA.exception;

public class OllamaUnavailableException extends InfrastructureException {

	public OllamaUnavailableException(String message) {
		super(message);
	}

	public OllamaUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
