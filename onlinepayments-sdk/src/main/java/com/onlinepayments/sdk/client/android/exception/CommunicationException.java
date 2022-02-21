package com.onlinepayments.sdk.client.android.exception;

import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;

/**
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class CommunicationException extends Exception {

	private static final long serialVersionUID = 378923281056384514L;

	/**
	 * The error response, if this was returned from the server/API
	 */
	public ErrorResponse errorResponse;

	public CommunicationException() {
		super();
	}

	public CommunicationException(String message) {
		super(message);
	}

	public CommunicationException(Throwable t) {
		super(t);
	}

	public CommunicationException(String message, Throwable t) {
		super(message, t);
	}

	public CommunicationException(String message,  ErrorResponse errorResponse) {
		super(message);
		this.errorResponse = errorResponse;
	}

}
