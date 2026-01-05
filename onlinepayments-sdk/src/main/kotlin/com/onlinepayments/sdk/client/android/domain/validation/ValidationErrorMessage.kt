/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.validation

import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import java.io.Serializable

/**
 * Contains error message information for a specific field.
 */
data class ValidationErrorMessage(
    val errorMessage: String,
    val paymentProductFieldId: String,
    val type: String
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 5842038484067693459L

        fun required(fieldId: String) = ValidationErrorMessage(
            errorMessage = "Field required.",
            paymentProductFieldId = fieldId,
            type = ValidationRuleType.REQUIRED.toString()
        )
    }
}
