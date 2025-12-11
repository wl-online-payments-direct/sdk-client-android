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

@RunWith(MockitoJUnitRunner::class)
class ValidationRuleLuhnTest {

    private lateinit var validationRule: ValidationRuleLuhn
    private lateinit var paymentRequest: PaymentRequest
    private val fieldId = "cardNumber"

    @Before
    fun setup() {
        validationRule = ValidationRuleLuhn()

        val paymentProduct = GsonHelper.fromResourceJson<PaymentProduct>(
            "paymentProductVisa.json",
            PaymentProduct::class.java
        )
        paymentRequest = PaymentRequest(paymentProduct)
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
            paymentRequest.setValue(fieldId, cardNumber)
            assertTrue("Card number '$cardNumber' should be valid", validationRule.validate(paymentRequest, fieldId))
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
            paymentRequest.setValue(fieldId, cardNumber)
            assertTrue("Card number '$cardNumber' should be valid", validationRule.validate(paymentRequest, fieldId))
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
            paymentRequest.setValue(fieldId, cardNumber)
            assertFalse("Card number '$cardNumber' should be invalid", validationRule.validate(paymentRequest, fieldId))
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
            paymentRequest.setValue(fieldId, cardNumber)
            assertFalse(
                "Card number '$cardNumber' should be invalid (too short or invalid)",
                validationRule.validate(paymentRequest, fieldId)
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
            paymentRequest.setValue(fieldId, input)
            assertFalse("Input '$input' should be invalid", validationRule.validate(paymentRequest, fieldId))
        }
    }

    @Test
    fun testEmptyValue() {
        paymentRequest.setValue(fieldId, "")
        assertFalse("Empty card number should be invalid", validationRule.validate(paymentRequest, fieldId))
    }

    @Test
    fun testNonExistentField() {
        assertFalse("Non-existent field should be invalid", validationRule.validate(paymentRequest, "nonExistentField"))
    }

    @Test
    fun testValidationRuleType() {
        assertTrue(
            "ValidationRuleLuhn should have LUHN type",
            validationRule.type == ValidationType.LUHN
        )
    }

    @Test
    fun testMessageId() {
        assertTrue(
            "ValidationRuleLuhn should have correct messageId",
            validationRule.messageId == "luhn"
        )
    }
}