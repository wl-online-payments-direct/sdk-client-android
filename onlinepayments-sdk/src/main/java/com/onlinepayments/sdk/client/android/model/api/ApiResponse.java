/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.api;

import androidx.annotation.Nullable;

/**
 * Holds the data that got returned from an AsyncTask for a listener.
 *
 * @deprecated In a future release, this class will be made internal to the SDK.
 */
@Deprecated
public class ApiResponse<T> {

    @Nullable
    public T data;

    @Nullable
    public ErrorResponse error;
}
