/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.validators

import com.onlinepayments.sdk.client.android.domain.validation.RuleValidationResult
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleRange
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationRuleRangeTest {
    private fun createValidator(): ValidationRuleRange {
        return ValidationRuleRange(1, 100)
    }

    @Test
    fun shouldValidateNumericValuesWithinRange() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("1")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("50")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("100")
        )
    }

    @Test
    fun shouldValidateStringValuesWithinRange() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("1")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("50")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("100")
        )
    }

    @Test
    fun shouldRejectNumericValuesOutsideRange() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Provided value must be between 1 and 100."
            ),
            validator.validate("0")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Provided value must be between 1 and 100."
            ),
            validator.validate("101")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Provided value must be between 1 and 100."
            ),
            validator.validate("-5")
        )
    }

    @Test
    fun shouldRejectStringValuesOutsideRange() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Provided value must be between 1 and 100."
            ),
            validator.validate("0")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Provided value must be between 1 and 100."
            ),
            validator.validate("101")
        )
    }

    @Test
    fun shouldRejectNonNumericStrings() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Provided value must be between 1 and 100."
            ),
            validator.validate("abc")
        )
    }

    @Test
    fun testValidationRuleType() {
        val validator = createValidator()

        assertEquals(
            ValidationRuleType.RANGE,
            validator.type,
            "ValidationRuleRange should have correct type"
        )
    }

    @Test
    fun testMessageId() {
        val validator = createValidator()

        assertEquals(
            "range",
            validator.messageId,
            "ValidationRuleRange should have correct messageId"
        )
    }
}
