/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.iin

import java.io.Serializable

/**
 * POJO that contains IinDetail.
 */
data class IinDetail(
    val paymentProductId: String? = null,
    val isAllowedInContext: Boolean = false,
    val cardType: CardType? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 6951132953680660913L
    }
}
