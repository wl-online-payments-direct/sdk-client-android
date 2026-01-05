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

import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile
import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField
import com.onlinepayments.sdk.client.android.domain.validation.ValidationErrorMessage
import com.onlinepayments.sdk.client.android.domain.validation.ValidationResult
import java.io.Serializable

class PaymentRequest(
    val paymentProduct: PaymentProduct,
    private var accountOnFile: AccountOnFile? = null,
    private var tokenize: Boolean = false
) : Serializable {
    private val fields = mutableMapOf<String, PaymentRequestField>()

    fun getField(fieldId: String): PaymentRequestField {
        var readOnly = false
        if (accountOnFile != null) {
            readOnly = !accountOnFile!!.isWritable(fieldId)
        }

        if (!fields.containsKey(fieldId)) {
            val definition = paymentProduct.getField(fieldId)
                ?: throw InvalidArgumentException("Field $fieldId not found")

            fields[fieldId] = PaymentRequestField(definition, readOnly)
        }

        return fields[fieldId]!!
    }

    fun getValues(): Map<String, String> {
        val result = mutableMapOf<String, String>()

        for ((key, field) in fields) {
            val value = field.getValue()
            if (value != null) {
                result[key] = value
            }
        }

        return result
    }

    fun setAccountOnFile(accountOnFile: AccountOnFile?) {
        if (accountOnFile == null) {
            return
        }

        val fieldsToRemove = mutableListOf<String>()
        for ((_, field) in fields) {
            if (!accountOnFile.isWritable(field.getId())) {
                fieldsToRemove.add(field.getId())
            }
        }

        fieldsToRemove.forEach { fields.remove(it) }
        this.accountOnFile = accountOnFile
    }

    fun getAccountOnFile(): AccountOnFile? {
        return accountOnFile
    }

    fun getTokenize(): Boolean {
        return tokenize
    }

    fun setTokenize(tokenize: Boolean) {
        this.tokenize = tokenize
    }

    fun validate(): ValidationResult {
        val allErrors = mutableListOf<ValidationErrorMessage>()

        if (accountOnFile != null && accountOnFile!!.getRequiredAttributes().isNotEmpty()) {
            val requiredAttributes = accountOnFile!!.getRequiredAttributes()

            val requiredFields = paymentProduct.fields
                .filter { field -> requiredAttributes.any { attr -> field.id == attr.key } }

            validateFields(requiredFields, allErrors)
        } else {
            validateFields(paymentProduct.fields, allErrors)
        }

        return ValidationResult(allErrors.isEmpty(), allErrors)
    }

    // Backward compatibility
    fun getValue(fieldId: String): String? {
        return fields[fieldId]?.getValue()
    }

    fun setValue(fieldId: String, value: String) {
        getField(fieldId).setValue(value)
    }

    private fun validateFields(fields: List<PaymentProductField>, errors: MutableList<ValidationErrorMessage>) {
        if (fields.isEmpty()) {
            return
        }

        fields.forEach { fieldDefinition ->
            val field = getField(fieldDefinition.id)
            errors.addAll(field.validate().errors)
        }
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 2619483756191827465L
    }
}
