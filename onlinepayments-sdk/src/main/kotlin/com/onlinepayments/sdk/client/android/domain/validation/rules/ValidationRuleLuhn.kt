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
 * Validation rule for luhn check.
 */
class ValidationRuleLuhn internal constructor() : ValidationRule("luhn", ValidationRuleType.LUHN) {
    /**
     * Validates that the value passes the Luhn check.
     *
     * @param value A value to validate.
     *
     * @return true, if the value passes the Luhn check; otherwise, false
     */
    override fun validate(value: String?): RuleValidationResult {
        val error = "Card number is in invalid format."

        val result = try {
            if (value.isNullOrEmpty()) {
                false
            } else {
                val text = value.replace(" ".toRegex(), "")
                if (text.length < 12) {
                    false
                } else {
                    var sum = 0
                    var alternate = false

                    for (i in text.length - 1 downTo 0) {
                        var n = text[i].digitToIntOrNull() ?: return RuleValidationResult(false, error)

                        if (alternate) {
                            n *= 2
                            if (n > 9) {
                                n = (n % 10) + 1
                            }
                        }

                        sum += n
                        alternate = !alternate
                    }

                    sum % 10 == 0
                }
            }
        } catch (_: Exception) {
            false
        }

        return RuleValidationResult(
            valid = result,
            message = if (result) "" else error
        )
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -6609650480352325271L
    }
}

