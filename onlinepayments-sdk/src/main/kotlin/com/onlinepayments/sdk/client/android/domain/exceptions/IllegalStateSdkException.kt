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

class IllegalStateSdkException(
    message: String,
    cause: Throwable? = null
) : SdkException(
    message = message,
    code = SdkExceptionType.ILLEGAL_STATE_EXCEPTION,
    metadata = null,
    cause = cause
) {
    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 8462750193846502728L
    }
}
