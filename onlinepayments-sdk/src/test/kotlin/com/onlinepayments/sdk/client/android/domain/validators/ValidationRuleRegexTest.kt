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

import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleRegex
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationRuleRegexTest {
    @Test
    fun testDigitsOnlyRegex() {
        val validationRule = ValidationRuleRegex("\\d+")

        val validValues = listOf("123", "0", "999999", "1234567890")
        val invalidValues = listOf("abc", "12a", "a123", "12.3", "", "12 34")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Digits-only value '$value' should be valid"
            )
        }

        invalidValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Non-digits value '$value' should be invalid"
            )
        }
    }

    @Test
    fun testLettersOnlyRegex() {
        val validationRule = ValidationRuleRegex("[a-zA-Z]+")

        val validValues = listOf("abc", "ABC", "AbC", "hello", "WORLD")
        val invalidValues = listOf("123", "abc123", "a1b", "", "hello world", "hello-world")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Letters-only value '$value' should be valid"
            )
        }

        invalidValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Non-letters value '$value' should be invalid"
            )
        }
    }

    @Test
    fun testEmailRegex() {
        val validationRule = ValidationRuleRegex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")

        val validEmails = listOf(
            "test@example.com",
            "user.name@domain.co.uk",
            "test123@test-domain.org"
        )

        val invalidEmails = listOf(
            "invalid-email",
            "@example.com",
            "test@",
            "test.example.com",
            "test@example"
        )

        validEmails.forEach { email ->
            assertTrue(
                validationRule.validate(email).valid,
                "Valid email '$email' should match regex"
            )
        }

        invalidEmails.forEach { email ->
            assertFalse(
                validationRule.validate(email).valid,
                "Invalid email '$email' should not match regex"
            )
        }
    }

    @Test
    fun testPhoneNumberRegex() {
        val validationRule = ValidationRuleRegex("^\\+?[1-9]\\d{4,14}$")

        val validPhones = listOf(
            "+1234567890",
            "1234567890",
            "+123456789012345"
        )

        val invalidPhones = listOf(
            "0123456789", // starts with 0
            "+0123456789", // starts with +0
            "123", // too short
            "+12345678901234567", // too long
            "123-456-7890", // contains dashes
            "+1 234 567 890" // contains spaces
        )

        validPhones.forEach { phone ->
            assertTrue(
                validationRule.validate(phone).valid,
                "Valid phone '$phone' should match regex"
            )
        }

        invalidPhones.forEach { phone ->
            assertFalse(
                validationRule.validate(phone).valid,
                "Invalid phone '$phone' should not match regex"
            )
        }
    }

    @Test
    fun testExactLengthRegex() {
        val validationRule = ValidationRuleRegex("^.{5}$")

        val validValues = listOf("12345", "abcde", "1a2b3", "     ", "!@#$%")
        val invalidValues = listOf("1234", "123456", "", "12345678901")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "5-character value '$value' should be valid"
            )
        }

        invalidValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Non-5-character value '$value' should be invalid",
            )
        }
    }

    @Test
    fun testOptionalGroupsRegex() {
        val validationRule = ValidationRuleRegex("^\\d{4}(-\\d{4})?$")

        val validValues = listOf("1234", "1234-5678")
        val invalidValues = listOf("123", "12345", "1234-567", "1234-56789", "abcd", "1234-abcd")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Value '$value' should match optional group regex"
            )
        }

        invalidValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Value '$value' should not match optional group regex"
            )
        }
    }

    @Test
    fun testCaseInsensitiveRegex() {
        val validationRule = ValidationRuleRegex("(?i)^(yes|no)$")

        val validValues = listOf("yes", "YES", "Yes", "YeS", "no", "NO", "No", "nO")
        val invalidValues = listOf("maybe", "y", "n", "true", "false", "")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Case-insensitive value '$value' should be valid"
            )
        }

        invalidValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Invalid value '$value' should not match"
            )
        }
    }

    @Test
    fun testComplexRegex() {
        // Credit card expiry date MM/YY format
        val validationRule = ValidationRuleRegex("^(0[1-9]|1[0-2])/([0-9]{2})$")

        val validValues = listOf("01/23", "12/25", "06/30")
        val invalidValues = listOf("00/23", "13/25", "1/23", "01/2025", "01-23", "01/ab")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Valid expiry date '$value' should match regex"
            )
        }

        invalidValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Invalid expiry date '$value' should not match regex"
            )
        }
    }

    @Test
    fun testUnicodeRegex() {
        val validationRule = ValidationRuleRegex("^[\\p{L}]+$") // Unicode letters

        val validValues = listOf("café", "naïve", "résumé", "北京", "москва")
        val invalidValues = listOf("café123", "test@test", "hello world", "123", "")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Unicode letters value '$value' should be valid"
            )
        }

        invalidValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Non-letters value '$value' should be invalid"
            )
        }
    }

    @Test
    fun testEmptyStringWithEmptyRegex() {
        val validationRule = ValidationRuleRegex("")

        assertTrue(
            validationRule.validate("").valid,
            "Empty string should match empty regex"
        )

        assertFalse(
            validationRule.validate("a").valid,
            "Non-empty string should not match empty regex"
        )
    }

    @Test
    fun testMatchAnyRegex() {
        val validationRule = ValidationRuleRegex(".*")

        val anyValues = listOf("", "abc", "123", "!@#", "hello world", "🎉")

        anyValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Any value '$value' should match .* regex"
            )
        }
    }

    @Test
    fun testMultilineRegex() {
        val validationRule = ValidationRuleRegex("(?s).*test.*") // DOTALL flag

        assertTrue(
            validationRule.validate("line1\ntest\nline3").valid,
            "Multiline string with test should match"
        )

        assertFalse(
            validationRule.validate("line1\nother\nline3").valid,
            "Multiline string without test should not match"
        )
    }

    @Test
    fun testEscapedCharactersRegex() {
        val validationRule = ValidationRuleRegex("^\\$\\d+\\.\\d{2}$") // Price format $XX.XX

        val validValues = listOf("$0.99", "$12.34", "$123.45")
        val invalidValues = listOf("0.99", "$12", "$12.3", "$12.345", "12.34")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Price format '$value' should be valid",
            )
        }

        invalidValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Invalid price format '$value' should not match"
            )
        }
    }

    @Test
    fun testRegexProperty() {
        val regex = "^[a-zA-Z0-9]+$"
        val validationRule = ValidationRuleRegex(regex)

        assertEquals(
            validationRule.pattern,
            regex,
            "Regex property should return the original regex"
        )
    }

    @Test
    fun testValidationRuleType() {
        val validationRule = ValidationRuleRegex("\\d+")
        assertEquals(
            ValidationRuleType.REGULAREXPRESSION,
            validationRule.type,
            "ValidationRuleRegex should have correct type"
        )
    }

    @Test
    fun testMessageId() {
        val validationRule = ValidationRuleRegex("\\d+")
        assertEquals(
            "regularExpression",
            validationRule.messageId,
            "ValidationRuleRegex should have correct messageId"
        )
    }
}