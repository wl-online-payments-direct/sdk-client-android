package com.onlinepayments.sdk.client.android.model.api;

import androidx.annotation.Nullable;

/**
 * The error response from the async method
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class ErrorResponse {

    /**
     * The error message
     */
    public String message;

    /**
     * The error from the API, if this was made available from the API
     */
    @Nullable
    public ApiError apiError;

    public ErrorResponse(String message) {
        this.message = message;
    }
}
