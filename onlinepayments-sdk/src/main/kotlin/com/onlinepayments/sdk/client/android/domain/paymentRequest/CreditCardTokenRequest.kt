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

package com.onlinepayments.sdk.client.android.domain.paymentRequest

import java.io.Serializable

open class CreditCardTokenRequest @JvmOverloads constructor(
    var cardNumber: String? = null,
    var cardholderName: String? = null,
    var expiryDate: String? = null,
    var securityCode: String? = null,
    var paymentProductId: Int? = null,
) : Serializable {

    fun getValues(): Map<String, Any?> {
        return mapOf(
            "cardNumber" to cardNumber,
            "cardholderName" to cardholderName,
            "expiryDate" to expiryDate,
            "cvv" to securityCode,
            "paymentProductId" to paymentProductId
        )
    }

    private companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -115547161880629623L
    }
}
