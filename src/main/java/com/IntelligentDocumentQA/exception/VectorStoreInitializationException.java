package com.IntelligentDocumentQA.exception;

public class VectorStoreInitializationException extends InfrastructureException {

	public VectorStoreInitializationException(String message) {
		super(message);
	}

	public VectorStoreInitializationException(String message, Throwable cause) {
		super(message, cause);
	}
}
