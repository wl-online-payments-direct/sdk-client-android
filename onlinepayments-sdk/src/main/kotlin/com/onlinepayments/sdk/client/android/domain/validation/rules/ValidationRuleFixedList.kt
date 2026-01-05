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
 * Validation rule for fixed list.
 */
class ValidationRuleFixedList internal constructor(
    listValues: MutableList<String?>
) : ValidationRule("fixedList", ValidationRuleType.FIXEDLIST) {

    val listValues: List<String?> = listValues.toList() // Immutable copy

    init {
        if (listValues.isEmpty()) {
            throw InvalidArgumentException("Error initialising ValidationRuleFixedList, listValues may not be empty.")
        }
    }

    /**
     * Validates whether the provided value is in the list of allowed values.
     *
     * @param value A value to validate.
     *
     * @return true, if the provided value is a valid; otherwise, false.
     */
    override fun validate(value: String?): RuleValidationResult {
        val error = "Provided value is not allowed."
        if (value.isNullOrEmpty()) {
            return RuleValidationResult(false, error)
        }

        val isValid = listValues.contains(value)

        return RuleValidationResult(
            valid = isValid,
            message = if (isValid) "" else error
        )
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -1388124383409175742L
    }
}

