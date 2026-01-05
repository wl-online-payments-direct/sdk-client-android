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
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleEmailAddress
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationRuleEmailAddressTest {
    private fun createValidator(): ValidationRuleEmailAddress {
        return ValidationRuleEmailAddress()
    }

    @Test
    fun shouldValidateCorrectEmailAddresses() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(
                valid = true,
                message = ""
            ),
            validator.validate("test@example.com")
        )

        assertEquals(
            RuleValidationResult(
                valid = true,
                message = ""
            ),
            validator.validate("user.name@example.com")
        )

        assertEquals(
            RuleValidationResult(
                valid = true,
                message = ""
            ),
            validator.validate("user+tag@example.co.uk")
        )
    }

    @Test
    fun shouldRejectInvalidEmailAddresses() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Email address is not in the correct format."
            ),
            validator.validate("invalid")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Email address is not in the correct format."
            ),
            validator.validate("@example.com")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Email address is not in the correct format."
            ),
            validator.validate("user@")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Email address is not in the correct format."
            ),
            validator.validate("user@.com")
        )
    }

    @Test
    fun testValidationRuleType() {
        val validator = createValidator()

        assertEquals(
            ValidationRuleType.EMAILADDRESS,
            validator.type,
            "ValidationRuleEmailAddress should have correct type"
        )
    }

    @Test
    fun testMessageId() {
        val validator = createValidator()

        assertEquals(
            "emailAddress",
            validator.messageId,
            "ValidationRuleEmailAddress should have correct messageId"
        )
    }
}
