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

import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PaymentProductTest {
    private lateinit var paymentProduct: PaymentProduct

    @BeforeTest
    fun setUp() {
        val dto = GsonHelper.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto::class.java
        )

        paymentProduct = PaymentProductFactory().createPaymentProduct(dto)
    }

    @Test
    fun `fields should return correct length`() {
        val fields = paymentProduct.fields
        assertEquals(4, fields.size)
    }

    @Test
    fun `return label, logo and displayOrder`() {
        val logo = paymentProduct.logo
        val label = paymentProduct.label
        val displayOrder = paymentProduct.displayOrder

        assertNotNull(logo)
        assertNotNull(label)
        assertNotNull(displayOrder)
    }

    @Test
    fun `fields should return correct elements in ascending order`() {
        val fields = paymentProduct.fields
        val actualIds = fields.map { it.id }
        assertEquals(listOf("cardNumber", "cardholderName", "expiryDate", "cvv"), actualIds)
    }

    @Test
    fun `requiredFields should return correct length`() {
        val fields = paymentProduct.requiredFields
        assertEquals(3, fields.size)
    }

    @Test
    fun `requiredFields should return correct fields`() {
        val fields = paymentProduct.requiredFields
        val actualIds = fields.map { it.id }
        assertEquals(listOf("cardNumber", "expiryDate", "cvv"), actualIds)
    }

    @Test
    fun `field should return cardNumber field`() {
        val field = paymentProduct.getField("cardNumber")
        assertNotNull(field)
        assertEquals("cardNumber", field.id)
    }

    @Test
    fun `field should return cvv field`() {
        val field = paymentProduct.getField("cvv")
        assertNotNull(field)
        assertEquals("cvv", field.id)
    }

    @Test
    fun `field should return cardholderName field`() {
        val field = paymentProduct.getField("cardholderName")
        assertNotNull(field)
        assertEquals("cardholderName", field.id)
    }

    @Test
    fun `field should return expiryDate field`() {
        val field = paymentProduct.getField(Constants.EXPIRY_DATE)
        assertNotNull(field)
        assertEquals(Constants.EXPIRY_DATE, field.id)
    }

    @Test
    fun `field should return null if wrong id`() {
        val field = paymentProduct.getField("123")
        assertNull(field)
    }

    @Test
    fun `applyMask on field should return masked string`() {
        val maskedString = paymentProduct.getField("cardNumber")?.applyMask("12345678901234567890")
        assertEquals("1234 5678 9012 3456 789", maskedString)
    }

    @Test
    fun `validate field should return validation messages`() {
        val validationMessages = paymentProduct.getField("cardNumber")?.validate("12345678901234567890")
        val errorMessages = validationMessages?.map { it.errorMessage }
        assertEquals(
            listOf("Card number is in invalid format.", "Provided value does not have an allowed length."),
            errorMessages
        )
    }

    @Test
    fun `isRequired field should return true for required field`() {
        val isRequired = paymentProduct.getField("cardNumber")?.isRequired
        assertEquals(true, isRequired)
    }

    @Test
    fun `isRequired field should return false for optional field`() {
        val isRequired = paymentProduct.getField("cardholderName")?.isRequired
        assertEquals(false, isRequired)
    }
}