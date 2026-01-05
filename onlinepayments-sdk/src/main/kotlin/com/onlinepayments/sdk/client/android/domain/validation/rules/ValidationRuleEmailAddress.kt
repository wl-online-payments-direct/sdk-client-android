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
 * Validation rule for email address.
 */
class ValidationRuleEmailAddress internal constructor() : ValidationRule(
    "emailAddress",
    ValidationRuleType.EMAILADDRESS
) {
    /**
     * Validates an email address.
     *
     * @param value A value to validate.
     *
     * @return true, if the value is a valid e-mail address; otherwise, false.
     */
    override fun validate(value: String?): RuleValidationResult {
        val isValid = value?.matches(EMAIL_REGEX.toRegex()) == true

        return RuleValidationResult(
            valid = isValid,
            message = if (isValid) "" else "Email address is not in the correct format."
        )
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -2476401279131525956L

        @Suppress("RegExpRedundantEscape", "MaxLineLength")
        private const val EMAIL_REGEX =
            "^(?!.*\\.\\.)(?!\\.)(?!.*\\.\$)[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?!\\d+\\.\\d+\\.\\d+\\.\\d+)([A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}\$"
    }
}

