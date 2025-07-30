/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct.validation

import java.io.Serializable

/**
 * Data class which holds the Length data.
 * Used for validation.
 */
data class Length(
    val minLength: Int? = null,
    val maxLength: Int? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -8127911803708372125L
    }
}
