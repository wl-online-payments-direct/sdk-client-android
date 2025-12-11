/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.exception

class EncryptDataException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 1060449781983665636L
    }
}
