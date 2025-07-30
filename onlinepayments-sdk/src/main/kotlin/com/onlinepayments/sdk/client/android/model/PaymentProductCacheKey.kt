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
 * Holds the PaymentProductCacheKey data.
 * It is used to determine if a PaymentProduct should be retrieved from the Online Payments platform, or retrieved from the memory cache.
 */
internal data class PaymentProductCacheKey(
    private val amount: Long,
    private val countryCode: String,
    private val currencyCode: String,
    private val isRecurring: Boolean,
    private val paymentProductId: String
): Serializable {

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -45L
    }
}
