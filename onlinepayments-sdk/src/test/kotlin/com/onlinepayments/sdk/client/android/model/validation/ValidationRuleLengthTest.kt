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
import java.security.InvalidParameterException
import kotlin.test.assertFailsWith

@RunWith(MockitoJUnitRunner::class)
class ValidationRuleLengthTest {

    private lateinit var paymentRequest: PaymentRequest
    private val fieldId = "PostalCode"

    @Before
    fun setup() {
        val paymentProduct = GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductForValidators.json",
            PaymentProduct::class.java
        )
        paymentRequest = PaymentRequest(paymentProduct)
    }

    @Test
    fun testValidLengthWithinRange() {
        val validationRule = ValidationRuleLength(3, 10)

        val validValues = listOf("abc", "test", "12345", "1234567890")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Value '$value' (length ${value.length}) should be valid for range 3-10",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testInvalidLengthTooShort() {
        val validationRule = ValidationRuleLength(5, 10)

        val shortValues = listOf("", "a", "ab", "abc", "abcd")

        shortValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Value '$value' (length ${value.length}) should be invalid for min length 5",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testInvalidLengthTooLong() {
        val validationRule = ValidationRuleLength(3, 8)

        val longValues = listOf("123456789", "1234567890", "12345678901")

        longValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Value '$value' (length ${value.length}) should be invalid for max length 8",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testExactBoundaryValues() {
        val validationRule = ValidationRuleLength(3, 8)

        // Test exact min length
        paymentRequest.setValue(fieldId, "abc")
        assertTrue("Value with exact min length should be valid", validationRule.validate(paymentRequest, fieldId))

        // Test exact max length
        paymentRequest.setValue(fieldId, "12345678")
        assertTrue("Value with exact max length should be valid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testZeroMinLength() {
        val validationRule = ValidationRuleLength(0, 5)

        paymentRequest.setValue(fieldId, "")
        assertTrue("Empty string should be valid with min length 0", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "12345")
        assertTrue("Max length value should be valid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "123456")
        assertFalse("Value exceeding max length should be invalid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testSameLengthMinMax() {
        val validationRule = ValidationRuleLength(5, 5)

        paymentRequest.setValue(fieldId, "12345")
        assertTrue("Value with exact required length should be valid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "1234")
        assertFalse("Value shorter than required should be invalid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "123456")
        assertFalse("Value longer than required should be invalid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testNonExistentField() {
        val validationRule = ValidationRuleLength(3, 10)

        assertFalse(
            "Non-existent field should be invalid when min length > 0",
            validationRule.validate(paymentRequest, "nonExistentField")
        )
    }

    @Test
    fun testNonExistentFieldWithZeroMinLength() {
        val validationRule = ValidationRuleLength(0, 10)

        assertTrue(
            "Non-existent field should be valid when min length is 0",
            validationRule.validate(paymentRequest, "nonExistentField")
        )
    }

    @Test
    fun testInvalidParameterNegativeMinLength() {
        assertFailsWith<InvalidParameterException>("Negative min length should throw exception") {
            ValidationRuleLength(-1, 5)
        }
    }

    @Test
    fun testInvalidParameterMaxLessThanMin() {
        assertFailsWith<InvalidParameterException>("Max length less than min should throw exception") {
            ValidationRuleLength(5, 3)
        }
    }

    @Test
    fun testValidationRuleType() {
        val validationRule = ValidationRuleLength(3, 10)
        assertTrue(
            "ValidationRuleLength should have LENGTH type",
            validationRule.type == ValidationType.LENGTH
        )
    }

    @Test
    fun testMessageId() {
        val validationRule = ValidationRuleLength(3, 10)
        assertTrue(
            "ValidationRuleLength should have correct messageId",
            validationRule.messageId == "length"
        )
    }

    @Test
    fun testGetterMethods() {
        val validationRule = ValidationRuleLength(3, 10)

        assertEquals("Min length getter should return correct value", 3, validationRule.getMinLength())
        assertEquals("Max length getter should return correct value", 10, validationRule.getMaxLength())
    }

    @Test
    fun testUnicodeCharacters() {
        val validationRule = ValidationRuleLength(3, 10)

        val unicodeValues = listOf("🎉🎊", "café", "naïve", "Šal", "résumé", "中文测试")

        unicodeValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            val isValid = validationRule.validate(paymentRequest, fieldId)
            assertTrue("Unicode value '$value' (length ${value.length}) validation should match expected", isValid)
        }
    }

    @Test
    fun testWhitespaceCharacters() {
        val validationRule = ValidationRuleLength(3, 10)

        paymentRequest.setValue(fieldId, "   ")
        assertTrue("Spaces should count towards length", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "\t\n\r")
        assertTrue(
            "Whitespace characters should count towards length",
            validationRule.validate(paymentRequest, fieldId)
        )

        paymentRequest.setValue(fieldId, "a b c")
        assertTrue("Mixed content with spaces should be valid", validationRule.validate(paymentRequest, fieldId))
    }
}