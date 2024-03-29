package com.onlinepayments.sdk.client.android.model.iin

import com.google.gson.annotations.SerializedName

/**
 * Enum containing all the possible card types for [IinDetailsResponse] and [IinDetail].
 */
enum class CardType {
    @SerializedName("Credit")
    CREDIT,
    @SerializedName("Debit")
    DEBIT,
    @SerializedName("Prepaid")
    PREPAID
}
