/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct.specificdata

import java.io.Serializable

/**
 * Data class which holds the payment product 320 specific properties.
 */
data class PaymentProduct320SpecificData(
    val gateway: String? = null,
    val networks: MutableList<String?>? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 8538500042642795722L
    }
}
