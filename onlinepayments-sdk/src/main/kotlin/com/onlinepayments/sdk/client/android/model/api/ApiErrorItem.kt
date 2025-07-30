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
 * The error item in the error body.
 */
data class ApiErrorItem(
    /** The error code returned by the server (read-only). */
    val errorCode: String? = null,

    /** The category the error belongs to (read-only). */
    val category: String? = null,

    /** The HTTP status code if available (read-only). */
    val httpStatusCode: Int? = null,

    /** The error id if available (read-only). */
    val id: String? = null,

    /** The name of the property that triggered the error if available (read-only). */
    val propertyName: String? = null,

    /** Indicates whether the request is retriable (read-only). */
    val retriable: Boolean = true,

    /**
     * A code used by older calls. Marked deprecated; new code should rely on [errorCode].
     * The default is "This error does not contain a code".
     */
    @Deprecated("In a future release, this property will be removed. Use errorCode instead.")
    var code: String = "This error does not contain a code",

    /**
     * A human-readable error message. Defaults to "This error does not contain a message".
     */
    var message: String = "This error does not contain a message"
) : Serializable {

    fun isRetriable(): Boolean = retriable

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 1983759919374923872L
    }
}
