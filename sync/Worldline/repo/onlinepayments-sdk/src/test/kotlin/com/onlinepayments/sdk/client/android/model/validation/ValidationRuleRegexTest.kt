/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.validation

import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ValidationRuleRegexTest {

    private lateinit var paymentRequest: PaymentRequest
    private val fieldId = "CompanyName"

    @Before
    fun setup() {
        val paymentProduct = GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductForValidators.json",
            PaymentProduct::class.java
        )
        paymentRequest = PaymentRequest(paymentProduct)
    }

    @Test
    fun testDigitsOnlyRegex() {
        val validationRule = ValidationRuleRegex("\\d+")

        val validValues = listOf("123", "0", "999999", "1234567890")
        val invalidValues = listOf("abc", "12a", "a123", "12.3", "", "12 34")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Digits-only value '$value' should be valid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse("Non-digits value '$value' should be invalid", validationRule.validate(paymentRequest, fieldId))
        }
    }

    @Test
    fun testLettersOnlyRegex() {
        val validationRule = ValidationRuleRegex("[a-zA-Z]+")

        val validValues = listOf("abc", "ABC", "AbC", "hello", "WORLD")
        val invalidValues = listOf("123", "abc123", "a1b", "", "hello world", "hello-world")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue("Letters-only value '$value' should be valid", validationRule.validate(paymentRequest, fieldId))
        }

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Non-letters value '$value' should be invalid",
                validationRule.validate(paymentRequest, fieldId)
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
            paymentRequest.setValue(fieldId, email)
            assertTrue(
                "Valid email '$email' should match regex",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidEmails.forEach { email ->
            paymentRequest.setValue(fieldId, email)
            assertFalse(
                "Invalid email '$email' should not match regex",
                validationRule.validate(paymentRequest, fieldId)
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
            paymentRequest.setValue(fieldId, phone)
            assertTrue(
                "Valid phone '$phone' should match regex",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidPhones.forEach { phone ->
            paymentRequest.setValue(fieldId, phone)
            assertFalse(
                "Invalid phone '$phone' should not match regex",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testExactLengthRegex() {
        val validationRule = ValidationRuleRegex("^.{5}$")

        val validValues = listOf("12345", "abcde", "1a2b3", "     ", "!@#$%")
        val invalidValues = listOf("1234", "123456", "", "12345678901")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "5-character value '$value' should be valid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Non-5-character value '$value' should be invalid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testOptionalGroupsRegex() {
        val validationRule = ValidationRuleRegex("^\\d{4}(-\\d{4})?$")

        val validValues = listOf("1234", "1234-5678")
        val invalidValues = listOf("123", "12345", "1234-567", "1234-56789", "abcd", "1234-abcd")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Value '$value' should match optional group regex",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Value '$value' should not match optional group regex",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testCaseInsensitiveRegex() {
        val validationRule = ValidationRuleRegex("(?i)^(yes|no)$")

        val validValues = listOf("yes", "YES", "Yes", "YeS", "no", "NO", "No", "nO")
        val invalidValues = listOf("maybe", "y", "n", "true", "false", "")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Case-insensitive value '$value' should be valid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Invalid value '$value' should not match",
                validationRule.validate(paymentRequest, fieldId)
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
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Valid expiry date '$value' should match regex",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Invalid expiry date '$value' should not match regex",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testUnicodeRegex() {
        val validationRule = ValidationRuleRegex("^[\\p{L}]+$") // Unicode letters

        val validValues = listOf("café", "naïve", "résumé", "北京", "москва")
        val invalidValues = listOf("café123", "test@test", "hello world", "123", "")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Unicode letters value '$value' should be valid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Non-letters value '$value' should be invalid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testEmptyStringWithEmptyRegex() {
        val validationRule = ValidationRuleRegex("")

        paymentRequest.setValue(fieldId, "")
        assertTrue("Empty string should match empty regex", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "a")
        assertFalse("Non-empty string should not match empty regex", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testMatchAnyRegex() {
        val validationRule = ValidationRuleRegex(".*")

        val anyValues = listOf("", "abc", "123", "!@#", "hello world", "🎉")

        anyValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Any value '$value' should match .* regex",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testNonExistentField() {
        val validationRule = ValidationRuleRegex("\\d+")

        assertFalse(
            "Non-existent field should be invalid",
            validationRule.validate(paymentRequest, "nonExistentField")
        )
    }

    @Test
    fun testMultilineRegex() {
        val validationRule = ValidationRuleRegex("(?s).*test.*") // DOTALL flag

        paymentRequest.setValue(fieldId, "line1\ntest\nline3")
        assertTrue("Multiline string with test should match", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "line1\nother\nline3")
        assertFalse("Multiline string without test should not match", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testEscapedCharactersRegex() {
        val validationRule = ValidationRuleRegex("^\\$\\d+\\.\\d{2}$") // Price format $XX.XX

        val validValues = listOf("$0.99", "$12.34", "$123.45")
        val invalidValues = listOf("0.99", "$12", "$12.3", "$12.345", "12.34")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Price format '$value' should be valid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Invalid price format '$value' should not match",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testValidationRuleType() {
        val validationRule = ValidationRuleRegex("\\d+")
        assertTrue(
            "ValidationRuleRegex should have REGULAREXPRESSION type",
            validationRule.type == ValidationType.REGULAREXPRESSION
        )
    }

    @Test
    fun testMessageId() {
        val validationRule = ValidationRuleRegex("\\d+")
        assertTrue(
            "ValidationRuleRegex should have correct messageId",
            validationRule.messageId == "regularExpression"
        )
    }

    @Test
    fun testRegexProperty() {
        val regex = "^[a-zA-Z0-9]+$"
        val validationRule = ValidationRuleRegex(regex)

        assertEquals("Regex property should return the original regex", regex, validationRule.pattern)
    }
}