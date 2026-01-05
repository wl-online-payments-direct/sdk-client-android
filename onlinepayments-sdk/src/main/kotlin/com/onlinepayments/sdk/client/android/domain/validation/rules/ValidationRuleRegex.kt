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

/**
 * Validation rule for regex.
 */
class ValidationRuleRegex internal constructor(
    val pattern: String
) : ValidationRule("regularExpression", ValidationRuleType.REGULAREXPRESSION) {

    private val regex = pattern.toRegex()

    /**
     * Validates that the value matches the regular expression of this validator.
     *
     * @param value A value to validate.
     *
     * @return true, if the value matches the regex; otherwise, false
     */
    override fun validate(value: String?): RuleValidationResult {
        val error = "Provided value is not in the correct format."

        val isValid = value?.matches(regex) ?: false

        return RuleValidationResult(
            valid = isValid,
            message = if (isValid) "" else error
        )
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 5054525275294003657L
    }
}

