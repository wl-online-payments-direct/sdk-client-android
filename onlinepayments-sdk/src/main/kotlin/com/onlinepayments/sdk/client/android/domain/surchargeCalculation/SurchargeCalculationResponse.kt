/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.surchargeCalculation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * The data class for the surcharge calculation response.
 *
 * @param surcharges list of surcharge calculations matching the bin and (optional) paymentProductId
 */
class SurchargeCalculationResponse internal constructor(
    @SerializedName("surcharges")
    val surcharges: List<Surcharge>
) : Serializable {
    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -4596644758612969969L
    }
}
