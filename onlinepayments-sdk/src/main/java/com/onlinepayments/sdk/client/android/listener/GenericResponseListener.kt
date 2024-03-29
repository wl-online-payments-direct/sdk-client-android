/*
 * Copyright 2023 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.listener

import com.onlinepayments.sdk.client.android.model.api.ErrorResponse

/**
 * Generic typed interface that serves as the base for the typed Response Listeners used for API requests.
 * [T] The type of the Response Listener. An object of this type represents the response type of the API call.
 * [onSuccess] This callback will be invoked when the API call completes successfully.
 * [onApiError] In case of an error during execution, this callback will be invoked.
 * [onException] In case of an exception during execution, this callback will be invoked.
 */
interface GenericResponseListener<T> {

    /**
     * Invoked when the request was successful and data is available.
     *
     * @param response the [T] which contains the typed data
     */
    fun onSuccess(response: T)

    /**
     * Invoked when the request failed due to a network error.
     *
     * @param error Error object that contains more information about the error that occurred.
     */
    fun onApiError(error: ErrorResponse)

    /**
     * Invoked when the request failed due to an exception.
     *
     * @param t Throwable object that contains the exception that occurred.
     */
    fun onException(t: Throwable)
}
