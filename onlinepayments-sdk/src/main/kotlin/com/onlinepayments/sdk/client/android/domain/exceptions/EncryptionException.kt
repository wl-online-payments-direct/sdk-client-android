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

class EncryptionException @JvmOverloads constructor(
    message: String = "Encryption error",
    cause: Throwable? = null,
    metadata: SdkExceptionMetadata? = null
) : SdkException(
    message = message,
    code = SdkExceptionType.ENCRYPTION_EXCEPTION,
    metadata = metadata,
    cause = cause
) {
    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 1060449781983665636L
    }
}

