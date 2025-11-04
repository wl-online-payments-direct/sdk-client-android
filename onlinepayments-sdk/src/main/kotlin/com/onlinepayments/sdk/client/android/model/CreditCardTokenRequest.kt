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

open class CreditCardTokenRequest @JvmOverloads constructor(
    var cardNumber: String? = null,
    var cardHolderName: String? = null,
    var expiryDate: String? = null,
    var securityCode: String? = null,
    var paymentProductId: Int? = null,
) : Serializable {

    fun getValues(): Map<String, Any?> {
        return mapOf(
            "cardNumber" to cardNumber,
            "cardHolderName" to cardHolderName,
            "expiryDate" to expiryDate,
            "securityCode" to securityCode,
            "paymentProductId" to paymentProductId
        )
    }

    private companion object {
        @Suppress("Unused")
        private val serialVersionUID = -115547161880629623L
    }
}