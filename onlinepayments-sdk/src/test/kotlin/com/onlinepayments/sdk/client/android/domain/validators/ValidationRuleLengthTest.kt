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

import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleLength
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationRuleLengthTest {
    @Test
    fun testRuleHasCorrectBoundaries() {
        val validationRule = ValidationRuleLength(3, 10)

        assertEquals(
            3,
            validationRule.getMinLength(),
            "The min length should be set correctly"
        )

        assertEquals(
            10,
            validationRule.getMaxLength(),
            "The max length should be set correctly"
        )
    }

    @Test
    fun testValidLengthWithinRange() {
        val validationRule = ValidationRuleLength(3, 10)

        val validValues = listOf("abc", "test", "12345", "1234567890")

        validValues.forEach { value ->
            assertTrue(
                validationRule.validate(value).valid,
                "Value '$value' (length ${value.length}) should be valid for range 3-10"
            )
        }
    }

    @Test
    fun testInvalidLengthTooShort() {
        val validationRule = ValidationRuleLength(5, 10)

        val shortValues = listOf("", "a", "ab", "abc", "abcd")

        shortValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Value '$value' (length ${value.length}) should be invalid for min length 5"
            )
        }
    }

    @Test
    fun testInvalidLengthTooLong() {
        val validationRule = ValidationRuleLength(3, 8)

        val longValues = listOf("123456789", "1234567890", "12345678901")

        longValues.forEach { value ->
            assertFalse(
                validationRule.validate(value).valid,
                "Value '$value' (length ${value.length}) should be invalid for max length 8"
            )
        }
    }

    @Test
    fun testExactBoundaryValues() {
        val validationRule = ValidationRuleLength(3, 8)

        // Test exact min length
        assertTrue(
            validationRule.validate("abc").valid,
            "Value with exact min length should be valid"
        )

        // Test exact max length
        assertTrue(
            validationRule.validate("12345678").valid,
            "Value with exact max length should be valid"
        )
    }

    @Test
    fun testZeroMinLength() {
        val validationRule = ValidationRuleLength(0, 5)

        assertTrue(
            validationRule.validate("").valid,
            "Empty string should be valid with min length 0"
        )

        assertTrue(
            validationRule.validate("12345").valid,
            "Max length value should be valid"
        )

        assertFalse(
            validationRule.validate("123456").valid,
            "Value exceeding max length should be invalid"
        )
    }

    @Test
    fun testSameLengthMinMax() {
        val validationRule = ValidationRuleLength(5, 5)

        assertTrue(
            validationRule.validate("12345").valid,
            "Value with exact required length should be valid"
        )

        assertFalse(
            validationRule.validate("1234").valid,
            "Value shorter than required should be invalid"
        )

        assertFalse(
            validationRule.validate("123456").valid,
            "Value longer than required should be invalid"
        )
    }

    @Test
    fun testNonExistentField() {
        val validationRule = ValidationRuleLength(3, 10)

        assertFalse(
            validationRule.validate("").valid,
            "Non-existent field should be invalid when min length > 0"
        )
    }

    @Test
    fun testInvalidParameterNegativeMinLength() {
        assertFailsWith<InvalidArgumentException>("Negative min length should throw exception") {
            ValidationRuleLength(-1, 5)
        }
    }

    @Test
    fun testInvalidParameterMaxLessThanMin() {
        assertFailsWith<InvalidArgumentException>("Max length less than min should throw exception") {
            ValidationRuleLength(5, 3)
        }
    }

    @Test
    fun testGetterMethods() {
        val validationRule = ValidationRuleLength(3, 10)

        assertEquals(
            3,
            validationRule.getMinLength(),
            "Min length getter should return correct value"
        )
        assertEquals(
            10,
            validationRule.getMaxLength(),
            "Max length getter should return correct value"
        )
    }

    @Test
    fun testUnicodeCharacters() {
        val validationRule = ValidationRuleLength(3, 10)

        val unicodeValues = listOf("🎉🎊", "café", "naïve", "Šal", "résumé", "中文测试")

        unicodeValues.forEach { value ->
            val isValid = validationRule.validate(value).valid
            assertTrue(
                isValid,
                "Unicode value '$value' (length ${value.length}) validation should match expected"
            )
        }
    }

    @Test
    fun testWhitespaceCharacters() {
        val validationRule = ValidationRuleLength(3, 10)

        assertTrue(
            validationRule.validate("   ").valid,
            "Spaces should count towards length"
        )

        assertTrue(
            validationRule.validate("\t\n\r").valid,
            "Whitespace characters should count towards length"
        )

        assertTrue(
            validationRule.validate("a b c").valid,
            "Mixed content with spaces should be valid"
        )
    }

    @Test
    fun testValidationRuleType() {
        val validationRule = ValidationRuleLength(3, 10)
        assertEquals(
            ValidationRuleType.LENGTH,
            validationRule.type,
            "ValidationRuleLength should have LENGTH type"
        )
    }

    @Test
    fun testMessageId() {
        val validationRule = ValidationRuleLength(3, 10)
        assertEquals(
            "length",
            validationRule.messageId,
            "ValidationRuleLength should have correct messageId"
        )
    }
}