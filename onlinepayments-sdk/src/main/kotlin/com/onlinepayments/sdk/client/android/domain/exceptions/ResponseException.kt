/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.exceptions

class ResponseException @JvmOverloads constructor(
    val httpStatusCode: Int? = null,
    message: String = "",
    val apiError: ApiError,
    cause: Throwable? = null,
) : SdkException(
    message = message,
    code = SdkExceptionType.CLIENT_ERROR,
    metadata =
        mutableMapOf<String, Any?>().apply {
            put("errorId", apiError.errorId)
            put("errors", apiError.errors)
        },
    cause = cause
) {
    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 8462751193846502828L
    }
}
