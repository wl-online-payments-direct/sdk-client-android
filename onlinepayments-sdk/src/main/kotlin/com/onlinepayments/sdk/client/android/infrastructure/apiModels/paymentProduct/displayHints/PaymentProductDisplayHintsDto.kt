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

/**
 * Data class that represents an DisplayHintsPaymentItem object.
 */
internal data class PaymentProductDisplayHintsDto(
    @SerializedName("displayOrder")
    val displayOrder: Int? = null,

    @SerializedName("label")
    val label: String? = null,

    @SerializedName("logo")
    val logo: String? = null
)

