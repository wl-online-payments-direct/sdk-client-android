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
import java.io.Serializable

/**
 * Data class that represents a Form element object.
 * The FormElement is used for determining its list type (text, list, currency, date or boolean).
 */
@ConsistentCopyVisibility
data class FormElementDto internal constructor(
    var type: Type? = null
) : Serializable {
    /**
     */
    @Suppress("Unused")
    enum class Type {
        @SerializedName("text")
        TEXT,

        @SerializedName("list")
        LIST,

        @SerializedName("currency")
        CURRENCY,

        @SerializedName("date")
        DATE,

        @SerializedName("boolean")
        BOOLEAN,
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 7081218270681792356L
    }
}
