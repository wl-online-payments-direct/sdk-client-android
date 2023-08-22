/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.api;

import java.io.Serializable;
import java.util.List;

/**
 * The error from the API response body.
 */
public class ApiError implements Serializable {

    private static final long serialVersionUID = 8917688214136217124L;

    /**
     * The error ID
     */
    public String errorId;

    /**
     * The collection of errors, if this was returned from the server/API.
     */
    public List<ApiErrorItem> errors;
}
