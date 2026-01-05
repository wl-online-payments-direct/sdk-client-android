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
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleIBAN
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationRuleIbanTest {
    private fun createValidator(): ValidationRuleIBAN {
        return ValidationRuleIBAN()
    }

    @Test
    fun shouldValidateCorrectIbanNumbers() {
        val validator = createValidator()

        // Valid IBANs from different countries
        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("DE89370400440532013000")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("GB82WEST12345698765432")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("FR1420041010050500013M02606")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("NL91ABNA0417164300")
        )
    }

    @Test
    fun shouldValidateIbanWithSpaces() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("DE89 3704 0044 0532 0130 00")
        )

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("GB82 WEST 1234 5698 7654 32")
        )
    }

    @Test
    fun shouldRejectInvalidIbanNumbers() {
        val validator = createValidator()

        // Invalid check digits
        assertEquals(
            RuleValidationResult(valid = false, message = "IBAN is not in the correct format."),
            validator.validate("DE89370400440532013001")
        )

        // Too short
        assertEquals(
            RuleValidationResult(valid = false, message = "IBAN is not in the correct format."),
            validator.validate("DE8937040044")
        )

        // Invalid country code
        assertEquals(
            RuleValidationResult(valid = false, message = "IBAN is not in the correct format."),
            validator.validate("XX89370400440532013000")
        )
    }

    @Test
    fun shouldRejectNonIbanValues() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(valid = false, message = "IBAN is not in the correct format."),
            validator.validate("not-an-iban")
        )

        assertEquals(
            RuleValidationResult(valid = false, message = "IBAN is not in the correct format."),
            validator.validate("1234567890")
        )

        assertEquals(
            RuleValidationResult(valid = false, message = "IBAN is not in the correct format."),
            validator.validate("")
        )
    }

    @Test
    fun testValidationRuleType() {
        val validator = createValidator()

        assertEquals(
            ValidationRuleType.IBAN,
            validator.type,
            "ValidationRuleIBAN should have correct type"
        )
    }

    @Test
    fun testMessageId() {
        val validator = createValidator()

        assertEquals(
            "iban",
            validator.messageId,
            "ValidationRuleIBAN should have correct messageId"
        )
    }
}
