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
 * Validation rule for regex.
 */
class ValidationRuleRegex internal constructor(
    val pattern: String
) : AbstractValidationRule("regularExpression", ValidationType.REGULAREXPRESSION) {

    private val regex = pattern.toRegex()

    /**
     * Validates that the value in the field with fieldId matches the regular expression of this validator.
     *
     * @param paymentRequest the fully filled [PaymentRequest] that will be used for doing a payment
     * @param fieldId the ID of the field to which to apply the current validator
     *
     * @return true, if the value in the field with fieldId matches the regex; false, if it doesn't or if the fieldId could not be found
     */
    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        val text = getUnmaskedValue(paymentRequest, fieldId) ?: return false

        return text.matches(regex)
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 5054525275294003657L
    }
}
