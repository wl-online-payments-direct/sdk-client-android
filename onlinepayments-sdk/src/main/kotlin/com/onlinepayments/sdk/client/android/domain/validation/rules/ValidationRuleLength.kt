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

import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.validation.RuleValidationResult

/**
 * Validation rule for length.
 */
class ValidationRuleLength internal constructor(
    private val minLength: Int,
    private val maxLength: Int
) : ValidationRule("length", ValidationRuleType.LENGTH) {

    init {
        if (minLength < 0) {
            throw InvalidArgumentException(
                "Error initialising ValidationRuleLength, minLength must be non-negative."
            )
        }

        if (maxLength < minLength) {
            throw InvalidArgumentException(
                "Error initialising ValidationRuleLength, maxLength must be greater than or equal to minLength."
            )
        }
    }

    /**
     * Validates whether the provided value is of the required length.
     *
     * @param value A value to validate.
     *
     * @return true, if the provided value is a valid; otherwise, false.
     */
    override fun validate(value: String?): RuleValidationResult {
        val error = "Provided value does not have an allowed length."

        val isValid = if (value.isNullOrEmpty()) {
            minLength == 0
        } else {
            value.length in minLength..maxLength
        }

        return RuleValidationResult(
            valid = isValid,
            message = if (isValid) "" else error
        )
    }

    fun getMinLength(): Int {
        return minLength
    }

    fun getMaxLength(): Int {
        return maxLength
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 6453263230504247824L
    }
}

