/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreditCardTokenRequestTest {

    @Test
    fun testConstructors() {
        var req = CreditCardTokenRequest()
        assertNull(req.cardNumber)
        assertNull(req.cardHolderName)
        assertNull(req.expiryDate)
        assertNull(req.securityCode)
        assertNull(req.paymentProductId)


        req = CreditCardTokenRequest(
            cardNumber = "4111111111111111",
            cardHolderName = "Test cardholderName",
            expiryDate = "12/28",
            securityCode = "123",
            paymentProductId = 1
        )
        assertEquals("4111111111111111", req.cardNumber)
        assertEquals("Test cardholderName", req.cardHolderName)
        assertEquals("12/28", req.expiryDate)
        assertEquals("123", req.securityCode)
        assertEquals(1, req.paymentProductId)

        req = CreditCardTokenRequest()
        req.cardNumber = "4000000000000002"
        req.paymentProductId = 2
        assertEquals("4000000000000002", req.cardNumber)
        assertEquals(2, req.paymentProductId)

        req = CreditCardTokenRequest(
            cardNumber = "4111111111111111",
            cardHolderName = "Test cardholderName",
            expiryDate = "12/28",
            securityCode = "123",
            paymentProductId = 1
        )

        val map = req.getValues()

        assertEquals(5, map.size)

        assertEquals("4111111111111111", map["cardNumber"])
        assertEquals("Test cardholderName", map["cardHolderName"])
        assertEquals("12/28", map["expiryDate"])
        assertEquals("123", map["securityCode"])
        assertEquals(1, map["paymentProductId"])
    }

    private val gson: Gson = GsonBuilder().create()

    @Test
    fun testDeserialization() {
        val original = CreditCardTokenRequest(
            cardNumber = "4111111111111111",
            cardHolderName = "Test cardholderName",
            expiryDate = "12/28",
            securityCode = "123",
            paymentProductId = 1
        )

        var json = gson.toJson(original)
        val copy = gson.fromJson(json, CreditCardTokenRequest::class.java)

        assertEquals(original.cardNumber, copy.cardNumber)
        assertEquals(original.cardHolderName, copy.cardHolderName)
        assertEquals(original.expiryDate, copy.expiryDate)
        assertEquals(original.securityCode, copy.securityCode)
        assertEquals(original.paymentProductId, copy.paymentProductId)

        var map = copy.getValues()
        assertEquals(1, map["paymentProductId"])

        json = """{ "cardNumber": "4111111111111111" }"""
        val obj = gson.fromJson(json, CreditCardTokenRequest::class.java)

        assertEquals("4111111111111111", obj.cardNumber)
        assertNull(obj.cardHolderName)
        assertNull(obj.expiryDate)
        assertNull(obj.securityCode)
        assertNull(obj.paymentProductId)

        map = obj.getValues()
        assertEquals("4111111111111111", map["cardNumber"])
        assertNull(map["cardHolderName"])
        assertNull(map["expiryDate"])
        assertNull(map["securityCode"])
        assertNull(map["paymentProductId"])
    }
}
