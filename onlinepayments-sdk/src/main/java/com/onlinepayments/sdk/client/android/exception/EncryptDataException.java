package com.onlinepayments.sdk.client.android.exception;

/**
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class EncryptDataException extends Exception {


	private static final long serialVersionUID = 1060449781983665636L;

	public EncryptDataException() {
		super();
	}

	public EncryptDataException(String message) {
		super(message);
	}

	public EncryptDataException(Throwable t) {
		super(t);
	}

	public EncryptDataException(String message, Throwable t) {
		super(message, t);
	}
}
