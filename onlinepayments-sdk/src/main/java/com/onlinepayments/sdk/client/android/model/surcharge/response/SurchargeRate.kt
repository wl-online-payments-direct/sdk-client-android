/*
 * Copyright 2023 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.surcharge.response

data class SurchargeRate internal constructor(
    val surchargeProductTypeId: String,
    val surchargeProductTypeVersion: String,
    val adValoremRate: Double,
    val specificRate: Int
)
