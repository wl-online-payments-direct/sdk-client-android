/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.validation.rules

import com.onlinepayments.sdk.client.android.domain.validation.RuleValidationResult
import java.math.BigInteger

/**
 * Validation rule for IBAN.
 */
class ValidationRuleIBAN internal constructor() : ValidationRule("iban", ValidationRuleType.IBAN) {

    /**
     * Validates that the value in the field with ID fieldId is a valid IBAN.
     *
     * @param value A value to validate.
     *
     * @return true, if the value is a proper IBAN; otherwise, false
     */
    override fun validate(value: String?): RuleValidationResult {
        val error = "IBAN is not in the correct format."

        //remove spaces between characters
        var newAccountNumber = value
            ?.trim()
            ?.replace("\\s+".toRegex(), "")
            ?.uppercase()

        val isValid = if (newAccountNumber?.matches(
                "^[A-Z]{2}[0-9]{2}[A-Z0-9]{4}[0-9]{7}([A-Z0-9]?){0,16}$".toRegex()
            ) == true
        ) {
            // Move the four initial characters to the end of the string.
            newAccountNumber = newAccountNumber.substring(4) + newAccountNumber.take(4)

            // Replace each letter with digits (A=10..Z=35).
            val numericAccountNumber = StringBuilder()
            for (i in 0 until newAccountNumber.length) {
                numericAccountNumber.append(Character.getNumericValue(newAccountNumber[i]))
            }

            // Compute modulo 97.
            val ibanNumber = BigInteger(numericAccountNumber.toString())
            ibanNumber.mod(IBAN_NUMBER_MODULO).toInt() == 1
        } else {
            false
        }

        return RuleValidationResult(
            valid = isValid,
            message = if (isValid) "" else error
        )
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -2638250936233171926L

        private val IBAN_NUMBER_MODULO = BigInteger("97")
    }
}

