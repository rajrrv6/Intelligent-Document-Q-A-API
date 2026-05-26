package com.IntelligentDocumentQA.exception;

public class DatabaseUnavailableException extends InfrastructureException {

	public DatabaseUnavailableException(String message) {
		super(message);
	}

	public DatabaseUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
