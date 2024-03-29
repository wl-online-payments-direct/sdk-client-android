/*
 * Copyright 2024 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.currencyconversion

import com.google.gson.annotations.SerializedName

enum class ConversionResultType {
    @SerializedName("Allowed")
    ALLOWED,
    @SerializedName("InvalidCard")
    INVALID_CARD,
    @SerializedName("InvalidMerchant")
    INVALID_MERCHANT,
    @SerializedName("NoRate")
    NO_RATE,
    @SerializedName("NotAvailable")
    NOT_AVAILABLE
}
