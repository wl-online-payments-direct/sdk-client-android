/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.surchargeCalculation

import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Suppress("unused")
enum class SurchargeResult : Serializable {
    @SerializedName("OK")
    OK,

    @SerializedName("NO_SURCHARGE")
    NO_SURCHARGE;

    companion object {
        private const val serialVersionUID = -8911144758612969969L
    }
}
