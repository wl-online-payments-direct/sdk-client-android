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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.security.InvalidParameterException
import kotlin.test.assertFailsWith

@RunWith(MockitoJUnitRunner::class)
class ValidationRuleFixedListTest {

    private lateinit var paymentRequest: PaymentRequest
    private val fieldId = "Country"

    @Before
    fun setup() {
        val paymentProduct = GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductForValidators.json",
            PaymentProduct::class.java
        )
        paymentRequest = PaymentRequest(paymentProduct)
    }

    @Test
    fun testValidValuesInList() {
        val allowedValues = mutableListOf<String?>("US", "GB", "DE", "FR", "NL")
        val validationRule = ValidationRuleFixedList(allowedValues)

        allowedValues.forEach { value ->
            paymentRequest.setValue(fieldId, value.toString())
            assertTrue("Value '$value' should be valid", validationRule.validate(paymentRequest, fieldId))
        }
    }

    @Test
    fun testInvalidValuesNotInList() {
        val allowedValues = mutableListOf<String?>("US", "GB", "DE", "FR", "NL")
        val validationRule = ValidationRuleFixedList(allowedValues)

        val invalidValues = listOf("CA", "AU", "JP", "ES", "IT", "XX", "123", "")

        invalidValues.forEach { value ->
            paymentRequest.setValue(fieldId, value)
            assertFalse("Value '$value' should be invalid", validationRule.validate(paymentRequest, fieldId))
        }
    }

    @Test
    fun testEmptyStringInList() {
        val allowedValues = mutableListOf<String?>("US", "", "GB")
        val validationRule = ValidationRuleFixedList(allowedValues)

        paymentRequest.setValue(fieldId, "")
        assertTrue(
            "Empty string should be valid when in allowed list",
            validationRule.validate(paymentRequest, fieldId)
        )
    }

    @Test
    fun testSingleValueList() {
        val allowedValues = mutableListOf<String?>("ONLY_VALUE")
        val validationRule = ValidationRuleFixedList(allowedValues)

        paymentRequest.setValue(fieldId, "ONLY_VALUE")
        assertTrue("Single allowed value should be valid", validationRule.validate(paymentRequest, fieldId))

        paymentRequest.setValue(fieldId, "OTHER_VALUE")
        assertFalse("Value not in single-item list should be invalid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testCaseSensitivity() {
        val allowedValues = mutableListOf<String?>("US", "GB", "DE")
        val validationRule = ValidationRuleFixedList(allowedValues)

        paymentRequest.setValue(fieldId, "us")
        assertFalse(
            "Lowercase value should be invalid when uppercase in list",
            validationRule.validate(paymentRequest, fieldId)
        )

        paymentRequest.setValue(fieldId, "Us")
        assertFalse(
            "Mixed case value should be invalid when uppercase in list",
            validationRule.validate(paymentRequest, fieldId)
        )
    }

    @Test
    fun testWhitespaceValues() {
        val allowedValues = mutableListOf<String?>("US", " GB ", "DE")
        val validationRule = ValidationRuleFixedList(allowedValues)

        paymentRequest.setValue(fieldId, " GB ")
        assertTrue(
            "Value with spaces should be valid when exactly matching list item",
            validationRule.validate(paymentRequest, fieldId)
        )

        paymentRequest.setValue(fieldId, "GB")
        assertFalse(
            "Trimmed value should be invalid when list contains spaced version",
            validationRule.validate(paymentRequest, fieldId)
        )
    }

    @Test
    fun testEmptyListThrowsException() {
        val emptyList = mutableListOf<String?>()

        assertFailsWith<InvalidParameterException>("Empty list should throw InvalidParameterException") {
            ValidationRuleFixedList(emptyList)
        }
    }

    @Test
    fun testNonExistentField() {
        val allowedValues = mutableListOf<String?>("US", "GB", "DE")
        val validationRule = ValidationRuleFixedList(allowedValues)

        assertFalse("Non-existent field should be invalid", validationRule.validate(paymentRequest, "nonExistentField"))
    }

    @Test
    fun testValidationRuleType() {
        val allowedValues = mutableListOf<String?>("US", "GB", "DE")
        val validationRule = ValidationRuleFixedList(allowedValues)

        assertTrue(
            "ValidationRuleFixedList should have FIXEDLIST type",
            validationRule.type == ValidationType.FIXEDLIST
        )
    }

    @Test
    fun testMessageId() {
        val allowedValues = mutableListOf<String?>("US", "GB", "DE")
        val validationRule = ValidationRuleFixedList(allowedValues)

        assertTrue(
            "ValidationRuleFixedList should have correct messageId",
            validationRule.messageId == "fixedList"
        )
    }

    @Test
    fun testListValuesImmutability() {
        val allowedValues = mutableListOf<String?>("US", "GB", "DE")
        val validationRule = ValidationRuleFixedList(allowedValues)

        // Modify original list
        allowedValues.add("FR")

        // The validation rule should not be affected
        paymentRequest.setValue(fieldId, "FR")
        assertFalse(
            "Added value should not be valid (list should be immutable)",
            validationRule.validate(paymentRequest, fieldId)
        )

        // Original values should still work
        paymentRequest.setValue(fieldId, "US")
        assertTrue("Original values should still be valid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testListValuesProperty() {
        val allowedValues = mutableListOf("US", "GB", "DE", null)
        val validationRule = ValidationRuleFixedList(allowedValues)

        // Verify the listValues property contains the expected values
        assertTrue("List should contain US", validationRule.listValues.contains("US"))
        assertTrue("List should contain GB", validationRule.listValues.contains("GB"))
        assertTrue("List should contain DE", validationRule.listValues.contains("DE"))
        assertTrue("List should contain null", validationRule.listValues.contains(null))
        assertFalse("List should not contain FR", validationRule.listValues.contains("FR"))

        // Verify list size
        assertTrue("List should have 4 items", validationRule.listValues.size == 4)
    }
}