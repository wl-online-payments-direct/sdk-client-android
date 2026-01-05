/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.utils

import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.exceptions.ApiError
import com.onlinepayments.sdk.client.android.domain.exceptions.ApiErrorItem

internal object SupportedProductsUtil {
    fun isSupportedInSdk(id: Int?): Boolean {
        return id !in Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS
    }

    fun get404Error(): ApiError {
        return ApiError(
            errorId = "48b78d2d-1b35-4f8b-92cb-57cc2638e901",
            errors = listOf(
                ApiErrorItem(
                    errorCode = "1007",
                    propertyName = "productId",
                    message = "UNKNOWN_PRODUCT_ID",
                    httpStatusCode = 404
                )
            )
        )
    }
}


