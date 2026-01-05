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
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleTermsAndConditions
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import kotlin.test.Test
import kotlin.test.assertEquals

class ValidationRuleTermsAndConditionsTest {

    private fun createValidator(): ValidationRuleTermsAndConditions {
        return ValidationRuleTermsAndConditions()
    }

    @Test
    fun shouldValidateBooleanTrue() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("true")
        )
    }

    @Test
    fun shouldValidateStringTrue() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(valid = true, message = ""),
            validator.validate("true")
        )
    }

    @Test
    fun shouldRejectBooleanFalse() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Please accept terms and conditions."
            ),
            validator.validate("false")
        )
    }

    @Test
    fun shouldRejectStringFalse() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Please accept terms and conditions."
            ),
            validator.validate("false")
        )
    }

    @Test
    fun shouldRejectOtherValues() {
        val validator = createValidator()

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Please accept terms and conditions."
            ),
            validator.validate("yes")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Please accept terms and conditions."
            ),
            validator.validate("1")
        )

        assertEquals(
            RuleValidationResult(
                valid = false,
                message = "Please accept terms and conditions."
            ),
            validator.validate("")
        )
    }

    @Test
    fun testValidationRuleType() {
        val validator = createValidator()
        assertEquals(
            ValidationRuleType.TERMSANDCONDITIONS,
            validator.type,
            "ValidationRuleTermsAndConditions should have correct type"
        )
    }

    @Test
    fun testMessageId() {
        val validator = createValidator()
        assertEquals(
            "termsAndConditions",
            validator.messageId,
            "ValidationRuleTermsAndConditions should have correct messageId"
        )
    }
}
