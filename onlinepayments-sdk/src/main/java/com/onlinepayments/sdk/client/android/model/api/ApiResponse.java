package com.onlinepayments.sdk.client.android.model.api;

import androidx.annotation.Nullable;

/**
 * Holds the data that got returned from a asynctask for a listener
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class ApiResponse<T> {

    @Nullable
    public T data;

    @Nullable
    public ErrorResponse error;
}
