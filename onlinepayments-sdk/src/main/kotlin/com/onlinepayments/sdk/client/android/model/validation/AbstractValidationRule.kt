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
import java.io.Serializable

/**
 * Abstract class which contains functionality to handle validation.
 */
abstract class AbstractValidationRule internal constructor(
    @Suppress("Unused")
    val messageId: String,
    val type: ValidationType
) : Serializable, ValidationRule {

    protected fun getUnmaskedValue(paymentRequest: PaymentRequest, fieldId: String): String? {
        var text = paymentRequest.getValue(fieldId) ?: return null

        return paymentRequest.getUnmaskedValue(fieldId, text)
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -1068723487645115780L
    }
}
