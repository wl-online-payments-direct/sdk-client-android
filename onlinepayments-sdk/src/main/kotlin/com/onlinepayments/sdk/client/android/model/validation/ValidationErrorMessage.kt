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

import java.io.Serializable

/**
 * Contains error message information for a specific field.
 */
data class ValidationErrorMessage(
    val errorMessage: String,
    val paymentProductFieldId: String,
    val rule: ValidationRule? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 5842038484067693459L
    }
}