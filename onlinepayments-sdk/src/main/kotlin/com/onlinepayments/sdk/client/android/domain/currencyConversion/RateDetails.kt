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
 * @param exchangeRate expressed as a percentage, applied to convert the original amount into the resulting amount without charge
 * @param invertedExchangeRate exchange rate, expressed as a percentage, applied to convert the resulting amount into the original amount
 * @param markUpRate the markup is the percentage added to the exchange rate by a provider when they sell you currency
 * @param quotationDateTime date and time at which the exchange rate has been quoted
 * @param source indicates the exchange rate source name. The rate source is supplied for receipt printing purposes and to meet regulatory requirements where applicable
 */
@ConsistentCopyVisibility
data class RateDetails internal constructor(
    @SerializedName("exchangeRate")
    val exchangeRate: Double,

    @SerializedName("invertedExchangeRate")
    val invertedExchangeRate: Double,

    @SerializedName("markUpRate")
    val markUpRate: Double,

    @SerializedName("quotationDateTime")
    val quotationDateTime: String,

    @SerializedName("source")
    val source: String
) : Serializable {
    companion object {
        @Suppress("unused")
        private const val serialVersionUID = 1L
    }
}
