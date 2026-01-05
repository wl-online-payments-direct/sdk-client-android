/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.iin

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Enum containing all the possible card types for [IinDetailsResponse] and [IinDetail].
 */
@Suppress("Unused")
enum class CardType : Serializable {
    @SerializedName("Credit")
    CREDIT,

    @SerializedName("Debit")
    DEBIT,

    @SerializedName("Prepaid")
    PREPAID;

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 7649203816450928471L
    }
}
