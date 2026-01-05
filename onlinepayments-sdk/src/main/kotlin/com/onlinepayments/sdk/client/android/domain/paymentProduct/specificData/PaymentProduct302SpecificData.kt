/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentProduct.specificData

import java.io.Serializable

/**
 * Data class which holds the payment product 302 specific properties.
 */
@ConsistentCopyVisibility
data class PaymentProduct302SpecificData internal constructor(
    val networks: MutableList<String?>? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 4006738016411138300L
    }
}

