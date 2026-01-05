/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.facade.listeners

import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException
import com.onlinepayments.sdk.client.android.domain.exceptions.ErrorResponse

/**
 * Generic typed interface that serves as the base for the typed Response Listeners used for API requests.
 *
 * @param T The type of the Response Listener. An object of this type represents the response type of the API call.
 *
 * ## Callback Methods
 *
 * - [onSuccess]: Invoked when the API call completes successfully
 * - [onFailure]: Invoked when the request fails with an SDK exception
 *
 * ## Deprecated Methods (For Backward Compatibility)
 *
 * The following methods are deprecated and will be removed in a future version:
 * - [onApiError]: Use [onFailure] instead
 * - [onException]: Use [onFailure] instead
 */
interface GenericResponseListener<T> {

    /**
     * Invoked when the request was successful and data is available.
     *
     * @param response the [T] which contains the typed data
     */
    fun onSuccess(response: T)

    /**
     * Invoked when the request failed.
     *
     * This is the main error callback that receives all types of SDK exceptions,
     * including network errors, API errors, and other failures.
     *
     * @param exception The SDK exception that caused the failure
     */
    fun onFailure(exception: SdkException) {
        // Default implementation calls deprecated methods for backward compatibility
        @Suppress("DEPRECATION")
        when (exception) {
            is ResponseException -> {
                onApiError(ErrorResponse(exception.message, exception.cause, exception.apiError))
            }

            else -> {
                onException(exception)
            }
        }
    }

    /**
     * Invoked when the request failed due to an API error.
     *
     * @param error The ResponseException containing API error details
     * @deprecated Use [onFailure] instead. This method will be removed in a future version.
     */
    @Deprecated(
        message = "Use onFailure(SdkException) instead",
        replaceWith = ReplaceWith("onFailure(error)")
    )
    fun onApiError(error: ErrorResponse) {
        // Default implementation delegates to onFailure
        onFailure(ResponseException(message = error.message!!, apiError = error.apiError!!))
    }

    /**
     * Invoked when the request failed due to an exception.
     *
     * @param cause The exception that caused the failure
     * @deprecated Use [onFailure] instead. This method will be removed in a future version.
     */
    @Deprecated(
        message = "Use onFailure(SdkException) instead",
        replaceWith = ReplaceWith("onFailure(cause as SdkException)")
    )
    fun onException(cause: Throwable) {
        // Default implementation delegates to onFailure
        onFailure(SdkException(message = cause.message ?: "Error in execution", cause = cause))
    }
}

