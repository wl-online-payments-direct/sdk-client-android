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
import com.onlinepayments.sdk.client.android.model.paymentproduct.FormElement
import com.onlinepayments.sdk.client.android.model.paymentproduct.Tooltip
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsProductFields.PreferredInputType
import java.io.Serializable

/**
 * Data class that represents an DisplayHintsProductFields object.
 */
data class DisplayHintsProductFields(
    val alwaysShow: Boolean? = null,
    val obfuscate: Boolean? = null,
    val displayOrder: Int? = null,
    val label: String? = null,
    val placeholderLabel: String? = null,
    var mask: String? = null,
    val preferredInputType: PreferredInputType? = null,
    val tooltip: Tooltip? = null,
    var formElement: FormElement? = null
) : Serializable {

    @Suppress("Unused")
    enum class PreferredInputType {
        @SerializedName("IntegerKeyboard")
        INTEGER_KEYBOARD,

        @SerializedName("StringKeyboard")
        STRING_KEYBOARD,

        @SerializedName("PhoneNumberKeyboard")
        PHONE_NUMBER_KEYBOARD,

        @SerializedName("EmailAddressKeyboard")
        EMAIL_ADDRESS_KEYBOARD,

        @SerializedName("DateKeyboard")
        DATE_PICKER
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -4396644758512959868L
    }
}
