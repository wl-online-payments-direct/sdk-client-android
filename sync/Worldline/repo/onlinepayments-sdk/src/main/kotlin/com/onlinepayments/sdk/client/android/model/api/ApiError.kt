/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.api

import java.io.Serializable

/**
 * The error from the API response body.
 */
class ApiError : Serializable {
    /**
     * The error ID
     */
    var errorId: String? = null

    /**
     * The collection of errors, if this was returned from the server/API.
     */
    var errors: List<ApiErrorItem>? = null

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 8917688214136217124L
    }
}
