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
 * Validation rule for terms and conditions.
 */
class ValidationRuleTermsAndConditions internal constructor(): AbstractValidationRule("termsAndConditions", ValidationType.TERMSANDCONDITIONS) {

    /**
     * Validates that the terms and conditions have been accepted.
     *
     * @param paymentRequest the fully filled [PaymentRequest] that will be used for doing a payment
     * @param fieldId the ID of the field to which to apply the current validator
     *
     * @return true, if the value in the field with fieldId is true; false, if the value in the field is false or if the fieldId could not be found
     */
    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        return paymentRequest.getValue(fieldId).toBoolean()
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 2209679897444037061L
    }
}
