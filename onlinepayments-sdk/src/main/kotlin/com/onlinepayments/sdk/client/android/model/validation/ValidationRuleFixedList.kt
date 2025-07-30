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
 * Validation rule for fixed list.
 */
class ValidationRuleFixedList internal constructor(
    listValues: MutableList<String?>
) : AbstractValidationRule("fixedList", ValidationType.FIXEDLIST) {

    val listValues: List<String?> = listValues.toList() // Immutable copy

    init {
        if (listValues.isEmpty()) {
            throw InvalidParameterException("Error initialising ValidationRuleFixedList, listValues may not be empty.")
        }
    }

    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        var text = getUnmaskedValue(paymentRequest, fieldId) ?: return false

        return listValues.contains(text)
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -1388124383409175742L
    }
}