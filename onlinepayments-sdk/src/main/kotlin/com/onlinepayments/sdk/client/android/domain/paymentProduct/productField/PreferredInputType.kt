/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentProduct.productField

import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Suppress("unused")
enum class PreferredInputType : Serializable {
    @SerializedName("IntegerKeyboard")
    INTEGER_KEYBOARD,

    @SerializedName("StringKeyboard")
    STRING_KEYBOARD,

    @SerializedName("PhoneNumberKeyboard")
    PHONE_NUMBER_KEYBOARD,

    @SerializedName("EmailAddressKeyboard")
    EMAIL_ADDRESS_KEYBOARD,

    @SerializedName("DateKeyboard")
    DATE_PICKER;

    companion object {
        private const val serialVersionUID = 1L
    }
}
