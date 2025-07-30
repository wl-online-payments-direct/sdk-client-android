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

/**
 * Validation rule for luhn check.
 */
class ValidationRuleLuhn internal constructor() : AbstractValidationRule("luhn", ValidationType.LUHN) {
    /**
     * Validates that the value in the field with fieldId passes the Luhn check.
     *
     * @param paymentRequest the fully filled [PaymentRequest] that will be used for doing a payment
     * @param fieldId the ID of the field to which to apply the current validator
     *
     * @return true, if the value in the field with fieldId passes the Luhn check; false, if it doesn't or if the fieldId could not be found
     */
    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        var text = paymentRequest.getValue(fieldId) ?: return false

        text = text.replace(" ".toRegex(), "")
        if (text.length < 12) {
            return false
        }

        var sum = 0
        var alternate = false

        for (i in text.length - 1 downTo 0) {
            var n = text[i].digitToIntOrNull() ?: return false

            if (alternate) {
                n *= 2

                if (n > 9) {
                    n = (n % 10) + 1
                }
            }

            sum += n
            alternate = !alternate
        }

        return sum % 10 == 0
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -6609650480352325271L
    }
}
