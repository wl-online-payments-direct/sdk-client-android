/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
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
     * @param cause Throwable object that contains the exception that occurred.
     */
    fun onException(cause: Throwable)
}
