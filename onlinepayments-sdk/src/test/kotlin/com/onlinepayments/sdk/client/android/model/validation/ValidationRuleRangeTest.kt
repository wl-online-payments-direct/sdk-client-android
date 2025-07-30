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
class ValidationRuleRangeTest {

    private lateinit var paymentRequest: PaymentRequest
    private val fieldId = "Range"

    @Before
    fun setup() {
        val paymentProduct = GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductForValidators.json",
            PaymentProduct::class.java
        )
        paymentRequest = PaymentRequest(paymentProduct)
    }

    @Test
    fun testValidValuesWithinRange() {
        val validationRule = ValidationRuleRange(10, 100)

        val validValues = listOf("11", "50", "99", "25", "75")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Value '$value' should be valid for range 10-100",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testInvalidValuesBelowRange() {
        val validationRule = ValidationRuleRange(10, 100)

        val belowRangeValues = listOf("5", "0", "-5", "10") // Note: 10 is exclusive minimum

        belowRangeValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Value '$value' should be invalid (below range)",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testInvalidValuesAboveRange() {
        val validationRule = ValidationRuleRange(10, 100)

        val aboveRangeValues = listOf("100", "101", "150", "1000") // Note: 100 is exclusive maximum

        aboveRangeValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Value '$value' should be invalid (above range)",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testBoundaryValues() {
        val validationRule = ValidationRuleRange(0, 10)

        // Test exclusive boundaries
        paymentRequest.setValue(fieldId, "0")
        assertFalse(
            "Min boundary value should be invalid (exclusive)",
            validationRule.validate(paymentRequest, fieldId)
        )

        paymentRequest.setValue(fieldId, "10")
        assertFalse(
            "Max boundary value should be invalid (exclusive)",
            validationRule.validate(paymentRequest, fieldId)
        )

        // Test values just inside boundaries
        paymentRequest.setValue(fieldId, "1")
        assertTrue("Value just above min should be valid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "9")
        assertTrue("Value just below max should be valid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testNegativeRange() {
        val validationRule = ValidationRuleRange(-100, -10)

        paymentRequest.setValue(fieldId, "-50")
        assertTrue("Negative value within range should be valid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "-100")
        assertFalse("Min boundary should be invalid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "-10")
        assertFalse("Max boundary should be invalid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "0")
        assertFalse(
            "Positive value should be invalid in negative range",
            validationRule.validate(paymentRequest, fieldId)
        )
    }

    @Test
    fun testCrossZeroRange() {
        val validationRule = ValidationRuleRange(-10, 10)

        val validValues = listOf("-5", "0", "5", "1", "-1")

        validValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertTrue(
                "Value '$value' should be valid for range -10 to 10",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testNonNumericValues() {
        val validationRule = ValidationRuleRange(10, 100)

        val nonNumericValues = listOf("abc", "12.5", "1a2", "", "fifty", "12 34", " 50 ")

        nonNumericValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Non-numeric value '$value' should be invalid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testDecimalValues() {
        val validationRule = ValidationRuleRange(10, 100)

        val decimalValues = listOf("50.5", "12.34", "99.99", "10.1")

        decimalValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Decimal value '$value' should be invalid (not integer)",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testLeadingZeros() {
        val validationRule = ValidationRuleRange(10, 100)

        paymentRequest.setValue(fieldId, "050")
        assertTrue(
            "Value with leading zeros should be valid if numeric value is in range",
            validationRule.validate(paymentRequest, fieldId)
        )

        paymentRequest.setValue(fieldId, "005")
        assertFalse(
            "Value with leading zeros should be invalid if numeric value is out of range",
            validationRule.validate(paymentRequest, fieldId)
        )
    }

    @Test
    fun testVeryLargeNumbers() {
        val validationRule = ValidationRuleRange(1000000, 2000000)

        paymentRequest.setValue(fieldId, "1500000")
        assertTrue("Large number within range should be valid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "2147483647") // Max int value
        assertFalse(
            "Max int value should be invalid if outside range",
            validationRule.validate(paymentRequest, fieldId)
        )
    }

    @Test
    fun testIntegerOverflow() {
        val validationRule = ValidationRuleRange(10, 100)

        val overflowValues = listOf("2147483648", "9999999999999999999", "-2147483649")

        overflowValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "Integer overflow value '$value' should be invalid",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }

    @Test
    fun testEmptyString() {
        val validationRule = ValidationRuleRange(10, 100)

        paymentRequest.setValue(fieldId, "")
        assertFalse("Empty string should be invalid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testNonExistentField() {
        val validationRule = ValidationRuleRange(10, 100)

        assertFalse(
            "Non-existent field should be invalid",
            validationRule.validate(paymentRequest, "nonExistentField")
        )
    }

    @Test
    fun testValidationRuleType() {
        val validationRule = ValidationRuleRange(10, 100)
        assertTrue(
            "ValidationRuleRange should have RANGE type",
            validationRule.type == ValidationType.RANGE
        )
    }

    @Test
    fun testMessageId() {
        val validationRule = ValidationRuleRange(10, 100)
        assertTrue(
            "ValidationRuleRange should have correct messageId",
            validationRule.messageId == "range"
        )
    }

    @Test
    fun testGetterMethods() {
        val validationRule = ValidationRuleRange(25, 75)

        assertEquals("Min value getter should return correct value", 25, validationRule.getMinValue())
        assertEquals("Max value getter should return correct value", 75, validationRule.getMaxValue())
    }

    @Test
    fun testSingleValueRange() {
        // Edge case: range where min and max are consecutive integers
        val validationRule = ValidationRuleRange(10, 12)

        paymentRequest.setValue(fieldId, "11")
        assertTrue(
            "Only valid value in narrow range should be accepted",
            validationRule.validate(paymentRequest, fieldId)
        )

        paymentRequest.setValue(fieldId, "10")
        assertFalse("Min boundary should be invalid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "12")
        assertFalse("Max boundary should be invalid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testImpossibleRange() {
        // Edge case: range where min and max are equal (no valid values)
        val validationRule = ValidationRuleRange(50, 50)

        val testValues = listOf("49", "50", "51")

        testValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse(
                "No value should be valid in impossible range",
                validationRule.validate(paymentRequest, fieldId)
            )
        }
    }
}