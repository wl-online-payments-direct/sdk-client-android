/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.displayHints

import com.google.gson.annotations.SerializedName
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PreferredInputType

/**
 * Data class that represents an DisplayHintsProductFields object.
 */
internal data class ProductFieldDisplayHintsDto(
    @SerializedName("alwaysShow")
    val alwaysShow: Boolean? = null,

    @SerializedName("obfuscate")
    val obfuscate: Boolean? = null,

    @SerializedName("displayOrder")
    val displayOrder: Int? = null,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("placeholderLabel")
    val placeholderLabel: String? = null,

    @SerializedName("mask")
    var mask: String? = null,

    @SerializedName("preferredInputType")
    val preferredInputType: PreferredInputType? = null,

    @SerializedName("tooltip")
    val tooltip: TooltipDto? = null,

    @SerializedName("formElement")
    val formElement: FormElementDto? = null
)

