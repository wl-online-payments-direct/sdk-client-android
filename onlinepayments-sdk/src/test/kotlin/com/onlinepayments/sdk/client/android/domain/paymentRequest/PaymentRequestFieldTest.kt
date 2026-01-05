/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentRequest

import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductFieldType
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductFieldDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaymentRequestFieldTest {
    private lateinit var paymentProductField: PaymentProductField
    private lateinit var paymentRequestField: PaymentRequestField

    @BeforeTest
    fun setUp() {
        val response = GsonHelper.fromResourceJson(
            "paymentProductFieldCard.json",
            PaymentProductFieldDto::class.java
        )

        val definition = PaymentProductFactory().createPaymentProductField(response)
        paymentRequestField = PaymentRequestField(definition, false)
    }

    @Test
    fun `setValue and getValue should work correctly`() {
        paymentRequestField.setValue("1234 5678 9012 3456 789")
        val value = paymentRequestField.getValue()

        assertEquals("1234567890123456789", value)
    }

    @Test
    fun `getValue should return null when no value set`() {
        val value = paymentRequestField.getValue()
        assertNull(value)
    }

    @Test
    fun `setValue with empty string should set value to null`() {
        paymentRequestField.setValue("")
        val value = paymentRequestField.getValue()

        assertNull(value)
    }

    @Test
    fun `clearValue should return null`() {
        paymentRequestField.clearValue()
        val value = paymentRequestField.getValue()
        assertNull(value)
    }

    @Test
    fun `getType should return NUMERICSTRING`() {
        assertEquals(PaymentProductFieldType.NUMERICSTRING, paymentRequestField.getType())
    }

    @Test
    fun `shouldObfuscate should return true`() {
        assertTrue(paymentRequestField.shouldObfuscate())
    }

    @Test
    fun `isRequired should return true`() {
        assertTrue(paymentRequestField.isRequired())
    }

    @Test
    fun `getMaskedValue should return masked value`() {
        paymentRequestField.setValue("1234567890123456789")
        assertEquals("1234 5678 9012 3456 789", paymentRequestField.getMaskedValue())
        assertEquals("1234567890123456789", paymentRequestField.getValue())
    }

    @Test
    fun `getId should return cardNumber`() {
        assertEquals("cardNumber", paymentRequestField.getId())
    }

    @Test
    fun `getLabel should return Card number`() {
        assertEquals("Card number", paymentRequestField.getLabel())
    }

    @Test
    fun `getPlaceholder should return test placeholder`() {
        assertEquals("test placeholder", paymentRequestField.getPlaceholder())
    }

    @Test
    fun `validate should return list of errors if field required and value not provided`() {
        val validationResult = paymentRequestField.validate()

        assertFalse(validationResult.isValid)
        assertEquals(1, validationResult.errors.size)
    }

    @Test
    fun `validate should return empty errors and true for isValid when correct card number passed`() {
        paymentRequestField.setValue("7822551678890142249")
        val validationResult = paymentRequestField.validate()

        assertTrue(validationResult.isValid)
        assertEquals(0, validationResult.errors.size)
    }

    @Test
    fun `setValue should throw correct error message for READ_ONLY field`() {
        paymentProductField = GsonHelper.fromResourceJson(
            "paymentProductFieldCard.json",
            PaymentProductField::class.java
        )
        paymentRequestField = PaymentRequestField(paymentProductField, true)

        val exception = assertFailsWith<InvalidArgumentException> {
            paymentRequestField.setValue("4222422242224222")
        }

        assertEquals("Cannot write \"READ_ONLY\" field: cardNumber", exception.message)
    }
}