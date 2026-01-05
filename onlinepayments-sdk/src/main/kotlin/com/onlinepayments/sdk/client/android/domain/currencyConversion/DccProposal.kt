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
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import java.io.Serializable

/**
 * @param baseAmount the base amount for this currency conversion
 * @param targetAmount the target amount for this currency conversion
 * @param rate contains information about the conversion rate
 * @param disclaimerReceipt card scheme disclaimer to print within cardholder receipt
 * @param disclaimerDisplay card scheme disclaimer to present to the cardholder
 */
@ConsistentCopyVisibility
data class DccProposal internal constructor(
    @SerializedName("baseAmount")
    val baseAmount: AmountOfMoney,

    @SerializedName("targetAmount")
    val targetAmount: AmountOfMoney,

    @SerializedName("rate")
    val rate: RateDetails,

    @SerializedName("disclaimerReceipt")
    val disclaimerReceipt: String?,

    @SerializedName("disclaimerDisplay")
    val disclaimerDisplay: String?
) : Serializable {
    companion object {
        @Suppress("unused")
        private const val serialVersionUID = 1879456289L
    }
}
