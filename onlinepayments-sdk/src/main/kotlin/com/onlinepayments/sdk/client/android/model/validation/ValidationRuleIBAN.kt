/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.validation

import com.onlinepayments.sdk.client.android.model.PaymentRequest
import java.math.BigInteger

/**
 * Validation rule for IBAN.
 */
class ValidationRuleIBAN internal constructor() : AbstractValidationRule("iban", ValidationType.IBAN) {

    /**
     * Validates that the value in the field with ID fieldId is a valid IBAN.
     *
     * @param paymentRequest the fully filled [PaymentRequest] that will be used for doing a payment
     * @param fieldId the ID of the field to which to apply the current validator
     *
     * @return true, if the value in the field with fieldId is a proper IBAN; false, if it is not or if the fieldId could not be found
     */
    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        var newAccountNumber = paymentRequest.getValue(fieldId)?.trim()

        if (newAccountNumber?.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$".toRegex()) == true) {
            // Move the four initial characters to the end of the string.

            newAccountNumber = newAccountNumber.substring(4) + newAccountNumber.take(4)

            // Replace each letter in the string with two digits, thereby expanding the string, where A = 10, B = 11, ..., Z = 35.
            val numericAccountNumber = StringBuilder()
            for (i in 0 until newAccountNumber.length) {
                numericAccountNumber.append(Character.getNumericValue(newAccountNumber[i]))
            }

            // Interpret the string as a decimal integer and compute the remainder of that number on division by 97.
            val ibanNumber = BigInteger(numericAccountNumber.toString())

            return ibanNumber.mod(IBAN_NUMBER_MODULO).toInt() == 1
        }

        return false
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -2638250936233171926L

        private val IBAN_NUMBER_MODULO = BigInteger("97")
    }
}
