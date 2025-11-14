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

import com.onlinepayments.sdk.client.android.model.api.ErrorResponse

class CommunicationException(
    message: String? = null,
    cause: Throwable? = null,
    @Suppress("unused") val errorResponse: ErrorResponse? = null,
    @Suppress("unused") val responseBody: String? = null
) : Exception(message, cause) {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 378923281056384514L
    }
}
