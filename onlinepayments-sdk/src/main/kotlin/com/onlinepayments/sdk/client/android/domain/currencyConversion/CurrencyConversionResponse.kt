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
 * @param dccSessionId the identifier of the Dynamic Currency Conversion(DCC) session that has been created
 * @param result result of a requested currency conversion
 * @param proposal details of currency conversion to be proposed to the cardholder
 */
@ConsistentCopyVisibility
data class CurrencyConversionResponse internal constructor(
    @SerializedName("dccSessionId")
    val dccSessionId: String,
    @SerializedName("result")
    val result: CurrencyConversionResult,
    @SerializedName("proposal")
    val proposal: DccProposal
) : Serializable {
    companion object {
        @Suppress("unused")
        private const val serialVersionUID = 197614528L
    }
}
