/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct

import com.google.gson.annotations.SerializedName
import com.onlinepayments.sdk.client.android.formatter.StringFormatter
import com.onlinepayments.sdk.client.android.model.FormatResult
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsProductFields
import com.onlinepayments.sdk.client.android.model.validation.ValidationErrorMessage
import java.io.Serializable

/**
 * Represents a PaymentProductField object.
 */
data class PaymentProductField(
    val id: String,
    val type: Type?,
    val displayHints: DisplayHintsProductFields = DisplayHintsProductFields(),
    val dataRestrictions: DataRestrictions = DataRestrictions(),
) : Serializable {

    @Suppress("Unused")
    enum class Type {
        @SerializedName("string")
        STRING,

        @SerializedName("integer")
        INTEGER,

        @SerializedName("numericstring")
        NUMERICSTRING,

        @SerializedName("expirydate")
        EXPIRYDATE,

        @SerializedName("boolean")
        BOOLEAN,

        @SerializedName("date")
        DATE
    }

    fun setValidationRules() {
        dataRestrictions.setValidationRules()
    }

    fun validateValue(paymentRequest: PaymentRequest): List<ValidationErrorMessage> {
        val value = paymentRequest.getValue(id)
        val errorMessageIds: MutableList<ValidationErrorMessage> = mutableListOf()

        if (dataRestrictions.isRequired() && value.isNullOrEmpty()) {
            errorMessageIds.add(ValidationErrorMessage("required", id, null))
        } else {
            dataRestrictions.getValidationRules().forEach { rule -> // Direct access to validationRules
                if (!rule.validate(paymentRequest, id)) {
                    errorMessageIds.add(ValidationErrorMessage(rule.messageId, id, rule))
                }
            }
        }

        return errorMessageIds.toList() // Return a copy
    }

    fun applyMask(
        newValue: String?,
        oldValue: String?,
        start: Int,
        count: Int,
        after: Int
    ): FormatResult? {
        val mask = displayHints.mask

        return when {
            mask == null && after == 0 -> FormatResult(newValue, start)
            mask == null -> FormatResult(newValue, (start - count) + after)
            else -> StringFormatter.applyMask(mask, newValue, oldValue, start, count, after)
        }
    }

    fun applyMask(newValue: String, oldValue: String, cursorIndex: Int): FormatResult? {
        val count = oldValue.substring(cursorIndex).length
        val after = newValue.substring(cursorIndex).length

        return applyMask(newValue, oldValue, cursorIndex, count, after)
    }

    fun applyMask(value: String?): String? {
        val mask = displayHints.mask

        return mask?.let { StringFormatter.applyMask(it, value) } ?: value
    }

    fun removeMask(value: String?): String? {
        val mask = displayHints.mask

        return mask?.let { StringFormatter.removeMask(it, value) } ?: value
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 7731107337899853223L
    }
}
