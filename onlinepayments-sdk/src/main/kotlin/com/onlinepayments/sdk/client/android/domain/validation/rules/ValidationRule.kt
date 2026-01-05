/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.validation.rules

import com.onlinepayments.sdk.client.android.domain.validation.RuleValidationResult
import java.io.Serializable

/**
 * Abstract class which defines functionality to handle validation.
 */
abstract class ValidationRule internal constructor(
    val messageId: String,
    val type: ValidationRuleType
) : Serializable {
    /**
     * Validate method which validates a text.
     *
     * @param value used for doing a payment
     *
     * @return true, if the text is valid; otherwise, false
     */
    abstract fun validate(value: String?): RuleValidationResult

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -1068723487645115780L
    }
}
