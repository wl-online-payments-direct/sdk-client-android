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
    /**
     * The [dccSessionId], or `null` when the API sent none.
     *
     * The API sends no session id (or an empty one) and no proposal unless [CurrencyConversionResult.result] is
     * [ConversionResultType.ALLOWED]. [dccSessionId] and [proposal] keep their non-null types for
     * compatibility, but are `null` at runtime in that case. Check the result before reading them, or use
     * this property and [proposalOrNull].
     */
    val dccSessionIdOrNull: String?
        get() = dccSessionId

    /** The [proposal], or `null` when the API sent none. See [dccSessionIdOrNull]. */
    val proposalOrNull: DccProposal?
        get() = proposal

    companion object {
        @Suppress("unused")
        private const val serialVersionUID = 197614528L
    }
}
