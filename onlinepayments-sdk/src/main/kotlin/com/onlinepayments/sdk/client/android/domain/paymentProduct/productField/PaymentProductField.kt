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

import com.onlinepayments.sdk.client.android.domain.validation.ValidationErrorMessage
import com.onlinepayments.sdk.client.android.infrastructure.utils.StringFormatter
import java.io.Serializable

class PaymentProductField internal constructor(
    val id: String,
    val type: PaymentProductFieldType?,
    val displayHints: ProductFieldDisplayHints,
    val dataRestrictions: DataRestrictions
) : Serializable {

    val label: String
        get() = displayHints.label ?: id

    val placeholder: String?
        get() = displayHints.placeholderLabel

    val isRequired: Boolean
        get() = dataRestrictions.isRequired()

    val shouldObfuscate: Boolean
        get() = displayHints.obfuscate == true

    fun applyMask(value: String?): String? {
        val mask = displayHints.mask ?: return value
        return StringFormatter.applyMask(mask, value)
    }

    fun removeMask(value: String?): String? {
        val mask = displayHints.mask ?: return value
        return StringFormatter.removeMask(mask, value)
    }

    fun validate(value: String?): List<ValidationErrorMessage> {
        return if (!value.isNullOrEmpty()) {
            dataRestrictions.validationRules.mapNotNull { validator ->
                val result = validator.validate(value)
                if (!result.valid) {
                    ValidationErrorMessage(
                        errorMessage = result.message,
                        paymentProductFieldId = id,
                        type = validator.type.toString()
                    )
                } else {
                    null
                }
            }
        } else if (dataRestrictions.isRequired()) {
            listOf(ValidationErrorMessage.required(id))
        } else {
            emptyList()
        }
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 5997830569478299372L
    }
}
