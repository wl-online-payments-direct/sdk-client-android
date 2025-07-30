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
import java.lang.NumberFormatException

/**
 * Validation rule for range.
 */
class ValidationRuleRange(
    private val minValue: Int,
    private val maxValue: Int
) : AbstractValidationRule("range", ValidationType.RANGE) {

    /**
     * Validates that the value in the field with fieldId has a value within the set bounds.
     *
     * @param paymentRequest the fully filled [PaymentRequest] that will be used for doing a payment
     * @param fieldId the ID of the field to which to apply the current validator
     *
     * @return true, if the value in the field with fieldId is in the correct range; false, if it is out of bounds or if the fieldId could not be found
     */
    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        var text = getUnmaskedValue(paymentRequest, fieldId) ?: return false

        try {
            val enteredValue = text.toInt()
            return enteredValue > minValue && enteredValue < maxValue
        } catch (_: NumberFormatException) {
            return false
        }
    }

    @Suppress("Unused")
    fun getMinValue(): Int {
        return minValue
    }

    @Suppress("Unused")
    fun getMaxValue(): Int {
        return maxValue
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 1199939638104378041L
    }
}
