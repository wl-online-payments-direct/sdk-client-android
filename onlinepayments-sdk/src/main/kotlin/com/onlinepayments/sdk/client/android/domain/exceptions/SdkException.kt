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

import java.io.Serializable

enum class SdkExceptionType {
    INVALID_ARGUMENT_EXCEPTION,
    ILLEGAL_STATE_EXCEPTION,
    CONFIGURATION_EXCEPTION,
    COMMUNICATION_EXCEPTION,
    CLIENT_ERROR,
    ENCRYPTION_EXCEPTION,
    SDK_EXCEPTION
}

typealias SdkExceptionMetadata = Map<String, Any?>

open class SdkException(
    message: String,
    val code: SdkExceptionType? = SdkExceptionType.SDK_EXCEPTION,
    val metadata: SdkExceptionMetadata? = null,
    cause: Throwable? = null
) : Exception(message, cause), Serializable {
    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 8462752193846502828L
    }
}
