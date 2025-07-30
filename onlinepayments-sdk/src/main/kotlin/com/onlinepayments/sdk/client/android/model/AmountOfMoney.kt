/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model

import java.io.Serializable

/**
 * Data class that contains money information for a payment.
 *
 * @param amount the amount in the smallest possible denominator of the provided currency
 * @param currencyCode the ISO-4217 Currency Code as a String
 * @see [ISO 4217 Currency Codes](https://www.iso.org/iso-4217-currency-codes.html)
 */
data class AmountOfMoney(
    val amount: Long? = 0L,
    val currencyCode: String? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 3077405745639575054L
    }
}