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

import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleLuhn
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationRuleLuhnTest {

    private lateinit var validationRule: ValidationRuleLuhn

    @BeforeTest
    fun setup() {
        validationRule = ValidationRuleLuhn()
    }

    @Test
    fun testValidCreditCardNumbers() {
        val validCardNumbers = listOf(
            "4111111111111111", // Visa test card
            "4000000000000002", // Visa test card
            "5555555555554444", // MasterCard test card
            "5105105105105100", // MasterCard test card
            "378282246310005",  // American Express test card
            "371449635398431",  // American Express test card
            "30569309025904",   // Diners Club test card
            "38520000023237",   // Diners Club test card
            "6011111111111117", // Discover test card
            "6011000990139424"  // Discover test card
        )

        validCardNumbers.forEach { cardNumber ->
            assertTrue(
                validationRule.validate(cardNumber).valid,
                "Card number '$cardNumber' should be valid"
            )
        }
    }

    @Test
    fun testValidCreditCardNumbersWithSpaces() {
        val validCardNumbersWithSpaces = listOf(
            "4111 1111 1111 1111", // Visa test card with spaces
            "5555 5555 5555 4444", // MasterCard test card with spaces
            "3782 822463 10005",   // American Express test card with spaces
            "6011 1111 1111 1117"  // Discover test card with spaces
        )

        validCardNumbersWithSpaces.forEach { cardNumber ->
            assertTrue(
                validationRule.validate(cardNumber).valid,
                "Card number '$cardNumber' should be valid"
            )
        }
    }

    @Test
    fun testInvalidCreditCardNumbers() {
        val invalidCardNumbers = listOf(
            "4111111111111112",
            "4000000000000001",
            "5555555555554445",
            "378282246310006",
            "1234567890123456",
            "4444444444444444",
            "9999999999999999"
        )

        invalidCardNumbers.forEach { cardNumber ->
            assertFalse(
                validationRule.validate(cardNumber).valid,
                "Card number '$cardNumber' should be invalid"
            )
        }
    }

    @Test
    fun testTooShortNumbers() {
        val shortNumbers = listOf(
            "411111111",      // 9 digits
            "41111111111",    // 11 digits
            "123456789012"    // 12 digits but invalid
        )

        shortNumbers.forEach { cardNumber ->
            assertFalse(
                validationRule.validate(cardNumber).valid,
                "Card number '$cardNumber' should be invalid (too short or invalid)"
            )
        }
    }

    @Test
    fun testNonNumericCharacters() {
        val invalidInputs = listOf(
            "411111111111111a", // Contains letter
            "4111-1111-1111-1111", // Contains dashes
            "4111.1111.1111.1111", // Contains dots
            "abc123def456ghi7",     // Mixed letters and numbers
            "4111 1111 1111 111a"   // Space and letter
        )

        invalidInputs.forEach { input ->
            assertFalse(
                validationRule.validate(input).valid,
                "Input '$input' should be invalid"
            )
        }
    }

    @Test
    fun testEmptyValue() {
        assertFalse(
            validationRule.validate("").valid,
            "Empty card number should be invalid"
        )
    }

    @Test
    fun testValidationRuleType() {
        assertEquals(
            ValidationRuleType.LUHN,
            validationRule.type,
            "ValidationRuleLuhn should have correct type"
        )
    }

    @Test
    fun testMessageId() {
        assertEquals(
            "luhn",
            validationRule.messageId,
            "ValidationRuleLuhn should have correct messageId"
        )
    }
}