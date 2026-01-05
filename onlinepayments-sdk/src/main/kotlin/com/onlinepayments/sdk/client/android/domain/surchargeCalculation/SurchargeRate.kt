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
class SurchargeRate internal constructor(
    @SerializedName("surchargeProductTypeId")
    val surchargeProductTypeId: String,

    @SerializedName("surchargeProductTypeVersion")
    val surchargeProductTypeVersion: String,

    @SerializedName("adValoremRate")
    val adValoremRate: Double,

    @SerializedName("specificRate")
    val specificRate: Int
) : Serializable {
    companion object {
        private const val serialVersionUID = -4590044758612961119L
    }
}

