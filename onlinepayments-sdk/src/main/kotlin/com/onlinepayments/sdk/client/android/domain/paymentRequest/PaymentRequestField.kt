/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentRequest

import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductFieldType
import com.onlinepayments.sdk.client.android.domain.validation.ValidationResult
import java.io.Serializable

class PaymentRequestField internal constructor(
    private val definition: PaymentProductField,
    private val readOnly: Boolean
) : Serializable {

    private var value: String? = null

    fun setValue(newValue: String?) {
        if (readOnly) {
            throw InvalidArgumentException("Cannot write \"READ_ONLY\" field: ${definition.id}")
        }

        value = if (!newValue.isNullOrEmpty()) {
            definition.removeMask(newValue)
        } else {
            null
        }
    }

    fun getValue(): String? {
        return value
    }

    fun getMaskedValue(): String? {
        return definition.applyMask(value)
    }

    fun getId(): String {
        return definition.id
    }

    fun getLabel(): String {
        return definition.label
    }

    fun getPlaceholder(): String? {
        return definition.placeholder
    }

    fun isRequired(): Boolean {
        return definition.isRequired
    }

    fun shouldObfuscate(): Boolean {
        return definition.shouldObfuscate
    }

    fun getType(): PaymentProductFieldType? {
        return definition.type
    }

    fun clearValue(): PaymentRequestField = apply {
        value = null
    }

    fun validate(): ValidationResult {
        val errors = definition.validate(value)
        return ValidationResult(errors.isEmpty(), errors)
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 2618483756691827465L
    }
}
