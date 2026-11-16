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

package com.onlinepayments.sdk.client.android.domain.surchargeCalculation

import com.google.gson.annotations.SerializedName
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import java.io.Serializable

/**
 * @param paymentProductId payment product identifier
 * @param result Token describing result. OK - Surcharge amount was successfully calculated. NO_SURCHARGE - Configured surcharge rate could not be found for the payment product
 * @param netAmount Net amount of the payment, excluding the surcharge amount
 * @param surchargeAmount Surcharge amount of the payment
 * @param totalAmount Total amount of the payment, including the surcharge amount
 * @param surchargeRate Summary of surcharge details used in the calculation of the surcharge amount. Null if the result is NO_SURCHARGE
 */
class Surcharge internal constructor(
    @SerializedName("paymentProductId")
    val paymentProductId: Int,

    @SerializedName("result")
    val result: SurchargeResult,

    @SerializedName("netAmount")
    val netAmount: AmountOfMoney,

    @SerializedName("surchargeAmount")
    val surchargeAmount: AmountOfMoney,

    @SerializedName("totalAmount")
    val totalAmount: AmountOfMoney,

    @SerializedName("surchargeRate")
    val surchargeRate: SurchargeRate?
) : Serializable {
    /**
     * The [netAmount], or `null` when the API sent none.
     *
     * The API leaves out the amounts when [result] is [SurchargeResult.NO_SURCHARGE]. [netAmount],
     * [surchargeAmount] and [totalAmount] keep their non-null types for compatibility, but are `null` at
     * runtime in that case. Check [result] before reading them, or use these properties.
     */
    val netAmountOrNull: AmountOfMoney?
        get() = netAmount

    /** The [surchargeAmount], or `null` when the API sent none. See [netAmountOrNull]. */
    val surchargeAmountOrNull: AmountOfMoney?
        get() = surchargeAmount

    /** The [totalAmount], or `null` when the API sent none. See [netAmountOrNull]. */
    val totalAmountOrNull: AmountOfMoney?
        get() = totalAmount

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -4590044758612969969L
    }
}
