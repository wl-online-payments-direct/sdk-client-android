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
 * Validation rule for email address.
 */
class ValidationRuleEmailAddress internal constructor() : AbstractValidationRule(
    "emailAddress",
    ValidationType.EMAILADDRESS
) {
    /**
     * Validates an email address.
     *
     * @param paymentRequest the fully filled [PaymentRequest] that will be used for doing a payment.
     * @param fieldId the ID of the field to which to apply the current validator.
     *
     * @return true, if the value in the field with fieldId is a valid e-mail address;
     *  false, if it is not a valid email address or if the fieldId could not be found.
     */
    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        var text = getUnmaskedValue(paymentRequest, fieldId) ?: return false

        return text.matches(EMAIL_REGEX.toRegex()) == true
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -2476401279131525956L

        @Suppress("RegExpRedundantEscape")
        private const val EMAIL_REGEX =
            "^(?!.*\\.\\.)(?!\\.)(?!.*\\.\$)[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?!\\d+\\.\\d+\\.\\d+\\.\\d+)([A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}\$"
    }
}
