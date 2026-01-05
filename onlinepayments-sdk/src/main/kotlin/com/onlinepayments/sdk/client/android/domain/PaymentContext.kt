/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain

import java.io.Serializable

/**
 * Data class that contains PaymentContext information.
 * It contains information about a payment, like its [AmountOfMoney] and countryCode.
 */
open class PaymentContext(
    var amountOfMoney: AmountOfMoney,
    var countryCode: String? = null,
    var isRecurring: Boolean? = false
) : Serializable {

    constructor(amountOfMoney: AmountOfMoney) : this(amountOfMoney, null, false)

    constructor(amountOfMoney: AmountOfMoney, countryCode: String?) : this(amountOfMoney, countryCode, false)

    fun toMap(): Map<String, String> = mapOf(
        "countryCode" to (countryCode ?: ""),
        "amount" to (amountOfMoney.amount?.toString() ?: ""),
        "isRecurring" to isRecurring.toString(),
        "currencyCode" to (amountOfMoney.currencyCode),
    )

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -4845945197600321181L
    }
}

class PaymentContextWithAmount(
    amountOfMoney: AmountOfMoneyWithAmount,
    countryCode: String? = null,
    isRecurring: Boolean = false
) : PaymentContext(amountOfMoney, countryCode, isRecurring)
