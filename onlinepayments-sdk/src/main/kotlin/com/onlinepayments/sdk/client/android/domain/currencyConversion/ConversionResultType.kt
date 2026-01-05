/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.currencyConversion

import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Suppress("unused")
enum class ConversionResultType : Serializable {
    @SerializedName("Allowed")
    ALLOWED,

    @SerializedName("InvalidCard")
    INVALID_CARD,

    @SerializedName("InvalidMerchant")
    INVALID_MERCHANT,

    @SerializedName("NoRate")
    NO_RATE,

    @SerializedName("NotAvailable")
    NOT_AVAILABLE;

    companion object {
        private const val serialVersionUID = 1L
    }
}

