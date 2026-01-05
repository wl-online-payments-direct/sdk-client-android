/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentProduct

import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductFieldDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaymentProductFieldTest {
    private lateinit var paymentProductField: PaymentProductField

    @BeforeTest
    fun setUp() {
        val dto = GsonHelper.fromResourceJson(
            "paymentProductFieldCard.json",
            PaymentProductFieldDto::class.java
        )

        paymentProductField = PaymentProductFactory().createPaymentProductField(dto)
    }

    @Test
    fun `getLabel should return Card number`() {
        val label = paymentProductField.label
        assertEquals("Card number", label)
    }

    @Test
    fun `getType should return Card number`() {
        val type = paymentProductField.type
        assertEquals("numericstring", type.toString().lowercase())
    }

    @Test
    fun `getPlaceholder should return test placeholder`() {
        val placeholder = paymentProductField.placeholder
        assertEquals("test placeholder", placeholder)
    }

    @Test
    fun `shouldObfuscate should return true`() {
        val shouldObfuscate = paymentProductField.shouldObfuscate
        assertTrue(shouldObfuscate)
    }

    @Test
    fun `isRequired should return true`() {
        val required = paymentProductField.isRequired
        assertTrue(required)
    }

    @Test
    fun `applyMask should return masked value`() {
        val maskedValue = paymentProductField.applyMask("12345678901234567890")
        assertEquals("1234 5678 9012 3456 789", maskedValue)
    }

    @Test
    fun `applyMask should return unmasked value if no mask`() {
        val fieldWithoutMask = GsonHelper.fromResourceJson(
            "paymentProductFieldWithoutMask.json",
            PaymentProductField::class.java
        )

        val maskedValue = fieldWithoutMask.applyMask("12345678901234567890")
        assertEquals("12345678901234567890", maskedValue)
    }

    @Test
    fun `removeMask should return unmasked value`() {
        val mask = paymentProductField.applyMask("1234567890123456789")
        assertEquals("1234 5678 9012 3456 789", mask)

        val rawValue = paymentProductField.removeMask(mask)
        assertEquals("1234567890123456789", rawValue)
    }

    @Test
    fun `validate should return empty list of error messages for valid input`() {
        val errorMessages = paymentProductField.validate("4242424242424242")
        assertEquals(0, errorMessages.size)
    }

    @Test
    fun `validate should return list with error messages for invalid input`() {
        val errorMessages = paymentProductField.validate("424")
        assertEquals(2, errorMessages.size)

        assertEquals("Card number is in invalid format.", errorMessages[0].errorMessage)
        assertEquals("Provided value does not have an allowed length.", errorMessages[1].errorMessage)
    }

    @Test
    fun `validate should return required error when value is null and field is required`() {
        val errorMessages = paymentProductField.validate(null)
        assertEquals(1, errorMessages.size)
        assertEquals("Field required.", errorMessages[0].errorMessage)
        assertEquals(ValidationRuleType.REQUIRED.toString(), errorMessages[0].type)
    }

    @Test
    fun `validate should return required error when value is empty and field is required`() {
        val errorMessages = paymentProductField.validate("")
        assertEquals(1, errorMessages.size)
        assertEquals("Field required.", errorMessages[0].errorMessage)
    }
}
