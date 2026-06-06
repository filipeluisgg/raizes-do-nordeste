package com.raizesdonordeste.api.domain.exception;

public class NegocioException extends RuntimeException {

	private final String name;
	private final String action;
	private final int statusCode;

	public NegocioException(String name, String message, String action, int statusCode) {
		super(message);
		this.name = name;
		this.action = action;
		this.statusCode = statusCode;
	}

	public String getName() {
		return name;
	}

	public String getAction() {
		return action;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
