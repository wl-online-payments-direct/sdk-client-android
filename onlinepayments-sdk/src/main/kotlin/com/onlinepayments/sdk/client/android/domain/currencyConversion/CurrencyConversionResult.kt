/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.currencyConversion

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @param result the result type for this conversion
 * @param resultReason plain text explaining the result of the currency conversion request
 */
@ConsistentCopyVisibility
data class CurrencyConversionResult internal constructor(
    @SerializedName("result")
    val result: ConversionResultType,

    @SerializedName("resultReason")
    val resultReason: String?
) : Serializable {
    companion object {
        @Suppress("unused")
        private const val serialVersionUID = 966467767646532L
    }
}
