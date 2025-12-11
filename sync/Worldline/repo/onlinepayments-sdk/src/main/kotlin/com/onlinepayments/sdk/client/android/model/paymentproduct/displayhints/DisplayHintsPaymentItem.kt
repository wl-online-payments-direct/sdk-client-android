/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class that represents an DisplayHintsPaymentItem object.
 */
data class DisplayHintsPaymentItem(
    var displayOrder: Int? = null,
    val label: String? = null,
    @SerializedName("logo") var logoUrl: String? = null,
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 5783120855027244241L
    }
}
