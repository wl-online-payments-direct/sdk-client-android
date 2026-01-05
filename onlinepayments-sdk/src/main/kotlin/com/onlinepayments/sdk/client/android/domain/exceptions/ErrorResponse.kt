/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.exceptions

/**
 * The error response from the async method.
 */
data class ErrorResponse(
    /**
     * The error message
     */
    var message: String? = null,

    /**
     * The exception that has occurred.
     */
    var throwable: Throwable? = null,

    /**
     * The error from the API, if this was made available from the API.
     */
    var apiError: ApiError? = null
)
