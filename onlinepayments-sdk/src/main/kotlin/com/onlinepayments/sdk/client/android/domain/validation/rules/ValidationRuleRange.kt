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
 * Validation rule for range.
 */
class ValidationRuleRange internal constructor(
    private val minValue: Int,
    private val maxValue: Int
) : ValidationRule("range", ValidationRuleType.RANGE) {

    /**
     * Validates that the value has a value within the set bounds.
     *
     * @param value A value to validate.
     *
     * @return true, if the value is in the correct range; otherwise, false
     */
    override fun validate(value: String?): RuleValidationResult {
        val result = try {
            if (value.isNullOrEmpty()) {
                false
            } else {
                val enteredValue = value.toInt()
                enteredValue in minValue..maxValue
            }
        } catch (_: NumberFormatException) {
            false
        }

        return RuleValidationResult(
            valid = result,
            message = if (!result) "Provided value must be between $minValue and $maxValue." else ""
        )
    }

    @Suppress("Unused")
    fun getMinValue(): Int {
        return minValue
    }

    @Suppress("Unused")
    fun getMaxValue(): Int {
        return maxValue
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 1199939638104378041L
    }
}

