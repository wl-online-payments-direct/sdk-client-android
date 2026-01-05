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
 * Validation rule for terms and conditions.
 */
class ValidationRuleTermsAndConditions internal constructor() :
    ValidationRule("termsAndConditions", ValidationRuleType.TERMSANDCONDITIONS) {

    /**
     * Validates that the terms and conditions have been accepted.
     *
     * @param value A value to validate.
     *
     * @return true, if the value is true; otherwise, false
     */
    override fun validate(value: String?): RuleValidationResult {
        val error = "Please accept terms and conditions."

        val isValid = value?.toBoolean() ?: false

        return RuleValidationResult(
            valid = isValid,
            message = if (isValid) "" else error
        )
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 2209679897444037061L
    }
}

