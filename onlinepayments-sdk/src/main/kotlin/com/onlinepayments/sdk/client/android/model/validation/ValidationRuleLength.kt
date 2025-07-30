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
import java.security.InvalidParameterException

/**
 * Validation rule for length.
 */
class ValidationRuleLength internal constructor(
    private val minLength: Int,
    private val maxLength: Int
) : AbstractValidationRule("length", ValidationType.LENGTH) {

    init {
        if (minLength < 0) {
            throw InvalidParameterException("Error initialising ValidationRuleLength, minLength must be non-negative.")
        }

        if (maxLength < minLength) {
            throw InvalidParameterException("Error initialising ValidationRuleLength, maxLength must be greater than or equal to minLength.")
        }
    }

    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        var text = getUnmaskedValue(paymentRequest, fieldId) ?: return minLength == 0

        return text.length in minLength..maxLength
    }

    @Suppress("Unused")
    fun getMinLength(): Int {
        return minLength
    }

    @Suppress("Unused")
    fun getMaxLength(): Int {
        return maxLength
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 6453263230504247824L
    }
}
