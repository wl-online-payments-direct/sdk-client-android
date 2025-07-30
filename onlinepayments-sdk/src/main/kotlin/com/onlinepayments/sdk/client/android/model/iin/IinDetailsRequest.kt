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
import com.onlinepayments.sdk.client.android.model.PaymentContext
import java.io.Serializable

/**
 * Data class that contains the request for IIN lookup.
 */
data class IinDetailsRequest(
    @SerializedName("bin") val ccPartial: String?,
    val paymentContext: PaymentContext? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 8401271765455867950L
    }
}
