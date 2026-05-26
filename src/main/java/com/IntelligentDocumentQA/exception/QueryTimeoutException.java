package com.IntelligentDocumentQA.exception;

public class QueryTimeoutException extends RuntimeException {

	public QueryTimeoutException(String message) {
		super(message);
	}

	public QueryTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}
