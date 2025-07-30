/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.model.surcharge.response

import com.onlinepayments.sdk.client.android.model.AmountOfMoney

/**
 * @param paymentProductId payment product identifier
 * @param result Token describing result. OK - Surcharge amount was successfully calculated. NO_SURCHARGE - Configured surcharge rate could not be found for the payment product
 * @param netAmount Net amount of the payment, excluding the surcharge amount
 * @param surchargeAmount Surcharge amount of the payment
 * @param totalAmount Total amount of the payment, including the surcharge amount
 * @param surchargeRate Summary of surcharge details used in the calculation of the surcharge amount. Null if the result is NO_SURCHARGE
 */
data class Surcharge(
    val paymentProductId: Int,
    val result: SurchargeResult,
    val netAmount: AmountOfMoney,
    val surchargeAmount: AmountOfMoney,
    val totalAmount: AmountOfMoney,
    val surchargeRate: SurchargeRate?
)
