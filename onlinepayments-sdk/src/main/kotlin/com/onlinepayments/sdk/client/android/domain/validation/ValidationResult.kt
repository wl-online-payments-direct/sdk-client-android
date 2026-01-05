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

import java.io.Serializable

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationErrorMessage> = emptyList()
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 5842038484067693451L
    }
}
