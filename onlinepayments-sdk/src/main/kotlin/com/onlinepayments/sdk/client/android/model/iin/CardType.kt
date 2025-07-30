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

import com.google.gson.annotations.SerializedName

/**
 * Enum containing all the possible card types for [IinDetailsResponse] and [IinDetail].
 */
@Suppress("Unused")
enum class CardType {
    @SerializedName("Credit")
    CREDIT,
    @SerializedName("Debit")
    DEBIT,
    @SerializedName("Prepaid")
    PREPAID
}
