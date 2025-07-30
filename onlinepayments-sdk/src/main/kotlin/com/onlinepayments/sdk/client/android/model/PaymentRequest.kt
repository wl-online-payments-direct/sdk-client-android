/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model

import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFile
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.model.validation.ValidationErrorMessage
import java.io.Serializable
import java.lang.NullPointerException
import java.security.InvalidParameterException

/**
 * Contains all payment request data needed for doing a payment.
 */
open class PaymentRequest @JvmOverloads constructor(
    var paymentProduct: PaymentProduct? = null,
    var accountOnFile: AccountOnFile? = null,
    var tokenize: Boolean = false
) : Serializable {

    private val fieldValues: MutableMap<String, String> = mutableMapOf()
    private val errorMessageIds: MutableList<ValidationErrorMessage> = mutableListOf()

    constructor(paymentProduct: PaymentProduct?, tokenize: Boolean) : this(paymentProduct, null, tokenize)

    /**
     * Validates all fields based on their value and their validation rules.
     * If a field is prefilled from the [AccountOnFile], but it has been altered, it will be validated.
     *
     * @return list of errorMessageIds
     */
    fun validate(): List<ValidationErrorMessage> {
        errorMessageIds.clear()

        val product = paymentProduct ?: throw NullPointerException("paymentProduct must be set.")

        product.getPaymentProductFields().forEach { field ->
            if (!isFieldInAccountOnFileAndNotAltered(field)) {
                errorMessageIds.addAll(field.validateValue(this))
            }
        }

        // return immutable copy
        return errorMessageIds.toList()
    }

    private fun isFieldInAccountOnFileAndNotAltered(field: PaymentProductField): Boolean {
        val aof = accountOnFile ?: return false

        if (!paymentProductHasAccountOnFile(aof)) return false

        return aof.attributes.any { attribute ->
            attribute.key == field.id && (!attribute.isEditingAllowed() || getValue(field.id) == null)
        }
    }

    private fun paymentProductHasAccountOnFile(aof: AccountOnFile): Boolean {
        return paymentProduct?.getAccountsOnFile()?.any { it.id == aof.id } == true
    }

    /**
     * Add value to the paymentProductFields map.
     *
     * @param paymentProductFieldId the id of the [PaymentProductField] for which the value will be set
     * @param value the value to set for the corresponding [PaymentProductField]
     */
    fun setValue(paymentProductFieldId: String, value: String) {
        fieldValues[paymentProductFieldId] = value
    }

    /**
     * Gets the value of given paymentProductFieldId.
     *
     * @param paymentProductFieldId the id of the [PaymentProductField] the value should be retrieved from
     *
     * @return the value of the [PaymentProductField]
     */
    fun getValue(paymentProductFieldId: String): String? {
        return fieldValues[paymentProductFieldId]
    }

    /**
     * Removes the value of given paymentProductFieldId
     *
     * @param paymentProductFieldId the id of the [PaymentProductField] the value should be removed from
     */
    @Suppress("Unused")
    fun removeValue(paymentProductFieldId: String) {
        fieldValues.remove(paymentProductFieldId)
    }

    private fun getPaymentProductField(paymentProductFieldId: String): PaymentProductField? {
        return paymentProduct?.getPaymentProductFields()?.firstOrNull { it.id == paymentProductFieldId }
    }

    fun getValues(): Map<String, String> {
        return fieldValues
    }

    @Suppress("Unused")
    fun getMaskedValue(
        paymentProductFieldId: String,
        newValue: String,
        oldValue: String,
        cursorIndex: Int
    ): FormatResult? {
        val field = getPaymentProductField(paymentProductFieldId) ?: return null

        return field.applyMask(newValue, oldValue, cursorIndex)
    }

    @Suppress("Unused")
    fun getMaskedValue(paymentProductFieldId: String): String? {
        val field = getPaymentProductField(paymentProductFieldId) ?: return null
        val value = getValue(paymentProductFieldId)

        return field.applyMask(value)
    }

    open fun getUnmaskedValue(paymentProductFieldId: String, value: String): String {
        val field = getPaymentProductField(paymentProductFieldId) ?: throw InvalidParameterException("Field not found.")

        return field.removeMask(value) ?: value
    }

    fun getUnmaskedValue(paymentProductFieldId: String): String? {
        val field = getPaymentProductField(paymentProductFieldId) ?: return null
        val value = getValue(paymentProductFieldId)

        return field.removeMask(value)
    }

    @Suppress("Unused")
    fun getUnmaskedValues(): Map<String, String> {
        return fieldValues.mapValues { (key, value) ->
            paymentProduct?.getPaymentProductFields()?.firstOrNull { it.id == key }?.removeMask(value) ?: value
        }
    }

    @Suppress("Unused")
    fun getMaskedValues(): Map<String, String> {
        return fieldValues.mapValues { (key, value) ->
            paymentProduct?.getPaymentProductFields()?.firstOrNull { it.id == key }?.applyMask(value) ?: value
        }
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 1553481971640554760L
    }
}
