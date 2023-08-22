package com.onlinepayments.sdk.client.android.exception;

import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;

public class ApiErrorException extends Exception {

    /**
     * The error response, if this was returned from the server/API.
     */
    public ErrorResponse errorResponse;

    public ApiErrorException(String message,  ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }
}
