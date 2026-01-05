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
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleFixedList
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationRuleFixedListTest {

    private fun createValidator(allowedValues: MutableList<String?>): ValidationRuleFixedList {
        return ValidationRuleFixedList(allowedValues)
    }

    @Test
    fun shouldValidateValueThatIsInTheAllowedList() {
        val validator = createValidator(mutableListOf("visa", "mastercard", "amex"))

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("visa")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("mastercard")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("amex")
        )
    }

    @Test
    fun shouldRejectValueThatIsNotInTheAllowedList() {
        val validator = createValidator(mutableListOf("visa", "mastercard", "amex"))

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Provided value is not allowed."
            ),
            validator.validate("discover")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Provided value is not allowed."
            ),
            validator.validate("jcb")
        )
    }

    @Test
    fun testValidationRuleType() {
        val validator = createValidator(mutableListOf("visa", "mastercard", "amex"))

        assertEquals(
            ValidationRuleType.FIXEDLIST,
            validator.type,
            "ValidationRuleFixedList should have correct type"
        )
    }

    @Test
    fun testMessageId() {
        val validator = createValidator(mutableListOf("visa", "mastercard", "amex"))

        assertEquals(
            "fixedList",
            validator.messageId,
            "ValidationRuleFixedList should have correct messageId"
        )
    }
}

