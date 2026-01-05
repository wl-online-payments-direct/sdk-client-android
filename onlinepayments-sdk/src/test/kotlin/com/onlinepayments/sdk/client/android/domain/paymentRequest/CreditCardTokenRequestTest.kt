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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for CreditCardTokenRequest.
 */
class CreditCardTokenRequestTest {

    @Test
    fun constructor_withAllParameters_shouldSetAllProperties() {
        val request = CreditCardTokenRequest(
            cardNumber = "4567350000427977",
            cardholderName = "John Doe",
            expiryDate = "1226",
            securityCode = "123",
            paymentProductId = 1
        )

        assertEquals("4567350000427977", request.cardNumber)
        assertEquals("John Doe", request.cardholderName)
        assertEquals("1226", request.expiryDate)
        assertEquals("123", request.securityCode)
        assertEquals(1, request.paymentProductId)
    }

    @Test
    fun constructor_withNoParameters_shouldSetNullDefaults() {
        val request = CreditCardTokenRequest()

        assertNull(request.cardNumber)
        assertNull(request.cardholderName)
        assertNull(request.expiryDate)
        assertNull(request.securityCode)
        assertNull(request.paymentProductId)
    }

    @Test
    fun constructor_withPartialParameters_shouldSetProvidedValues() {
        val request = CreditCardTokenRequest(
            cardNumber = "4567350000427977",
            expiryDate = "1226"
        )

        assertEquals("4567350000427977", request.cardNumber)
        assertEquals("1226", request.expiryDate)
        assertNull(request.cardholderName)
        assertNull(request.securityCode)
        assertNull(request.paymentProductId)
    }

    @Test
    fun getValues_withAllValues_shouldReturnMapWithAllFields() {
        val request = CreditCardTokenRequest(
            cardNumber = "4567350000427977",
            cardholderName = "John Doe",
            expiryDate = "1226",
            securityCode = "123",
            paymentProductId = 1
        )

        val values = request.getValues()

        assertNotNull(values)
        assertEquals(5, values.size)
        assertEquals("4567350000427977", values["cardNumber"])
        assertEquals("John Doe", values["cardholderName"])
        assertEquals("1226", values["expiryDate"])
        assertEquals("123", values["cvv"]) // Note: securityCode maps to "cvv"
        assertEquals(1, values["paymentProductId"])
    }

    @Test
    fun getValues_withNullValues_shouldReturnMapWithNullValues() {
        val request = CreditCardTokenRequest()

        val values = request.getValues()

        assertNotNull(values)
        assertEquals(5, values.size)
        assertNull(values["cardNumber"])
        assertNull(values["cardholderName"])
        assertNull(values["expiryDate"])
        assertNull(values["cvv"])
        assertNull(values["paymentProductId"])
    }

    @Test
    fun getValues_securityCodeShouldMapToCvv() {
        val request = CreditCardTokenRequest(
            securityCode = "789"
        )

        val values = request.getValues()

        assertEquals("789", values["cvv"])
        assertNull(values["securityCode"]) // Should not have securityCode key
    }

    @Test
    fun getValues_withPartialValues_shouldReturnMapWithMixedNullAndValues() {
        val request = CreditCardTokenRequest(
            cardNumber = "4567350000427977",
            securityCode = "123"
        )

        val values = request.getValues()

        assertNotNull(values)
        assertEquals("4567350000427977", values["cardNumber"])
        assertEquals("123", values["cvv"])
        assertNull(values["cardholderName"])
        assertNull(values["expiryDate"])
        assertNull(values["paymentProductId"])
    }

    @Test
    fun mutability_shouldAllowPropertyChanges() {
        val request = CreditCardTokenRequest()

        request.cardNumber = "4567350000427977"
        request.cardholderName = "Jane Doe"
        request.expiryDate = "1230"
        request.securityCode = "456"
        request.paymentProductId = 2

        assertEquals("4567350000427977", request.cardNumber)
        assertEquals("Jane Doe", request.cardholderName)
        assertEquals("1230", request.expiryDate)
        assertEquals("456", request.securityCode)
        assertEquals(2, request.paymentProductId)
    }

    @Test
    fun getValues_afterMutation_shouldReturnUpdatedValues() {
        val request = CreditCardTokenRequest(
            cardNumber = "1111222233334444"
        )

        request.cardNumber = "4567350000427977"
        request.securityCode = "999"

        val values = request.getValues()

        assertEquals("4567350000427977", values["cardNumber"])
        assertEquals("999", values["cvv"])
    }

    @Test
    fun serialization_shouldBeSerializable() {
        val request = CreditCardTokenRequest(
            cardNumber = "4567350000427977",
            cardholderName = "Test User",
            expiryDate = "1226",
            securityCode = "123",
            paymentProductId = 1
        )

        // Verify it's Serializable (compilation check)
        val serializable: java.io.Serializable = request
        assertNotNull(serializable)
    }
}
