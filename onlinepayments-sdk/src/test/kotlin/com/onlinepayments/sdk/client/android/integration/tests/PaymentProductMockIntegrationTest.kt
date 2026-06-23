/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.integration.tests

import com.onlinepayments.sdk.client.android.integration.BaseMockIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Mock-backed integration tests for payment product retrieval.
 * Tests caching and SDK mapping logic without requiring live credentials.
 */
class PaymentProductMockIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun `GetPaymentProduct returns cached result for repeated request`() = runBlocking {
        val json = MockServerHelper.loadJsonResource("paymentProductVisa.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, json)

        try {
            val firstResult = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
            val secondResult = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)

            assertEquals(1, server.requestCount, "Repeated request with same context should use cache")
            assertEquals(firstResult.id, secondResult.id, "Cached result should have the same product id")
            assertEquals(firstResult.fields.map { it.id }, secondResult.fields.map { it.id })
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `GetPaymentProduct makes new API call for different context`() = runBlocking {
        val json = MockServerHelper.loadJsonResource("paymentProductVisa.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponses(
            context,
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(json),
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(json)
        )

        try {
            mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)

            val differentContext = createPaymentContext(
                amount = 1000,
                currencyCode = "USD",
                countryCode = "NL"
            )

            mockSdk.getPaymentProduct(TestConfig.productIdVisa, differentContext)

            assertEquals(2, server.requestCount, "Different context should trigger a new API call")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `GetPaymentProduct returns product with display hints`() = runBlocking {
        val json = MockServerHelper.loadJsonResource("cardPaymentProduct.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, json)

        try {
            val result = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)

            assertEquals("VISA", result.label)
            assertEquals("test-logo", result.logo)
            assertEquals(0, result.displayOrder)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `GetPaymentProduct returns product with mapped accounts on file`() = runBlocking {
        val json = MockServerHelper.loadJsonResource("paymentProductVisa.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, json)

        try {
            val result = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
            val accountOnFile = result.getAccountOnFile("123")

            assertFalse(result.accountsOnFile.isEmpty(), "Accounts on file should be mapped")
            assertNotNull(accountOnFile, "Expected account on file should be present")
            assertEquals("123", accountOnFile.id)
            assertEquals(TestConfig.productIdVisa, accountOnFile.paymentProductId)
            assertEquals("************1111", accountOnFile.getValue("cardNumber"))
            assertFalse(accountOnFile.isWritable("cardNumber"), "Card number should be read-only")
            assertTrue(accountOnFile.isWritable("expiryDate"), "Expiry date should be writable")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `GetPaymentProduct returns product with valid field structure`() = runBlocking {
        val json = MockServerHelper.loadJsonResource("cardPaymentProduct.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, json)

        try {
            val result = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)

            val cardNumberField = result.getField("cardNumber")
            val expiryDateField = result.getField("expiryDate")
            val cvvField = result.getField("cvv")
            val cardholderNameField = result.getField("cardholderName")

            assertEquals(4, result.fields.size)

            assertNotNull(cardNumberField, "Card number field should exist")
            assertEquals("cardNumber", cardNumberField.id)
            assertEquals("Card number", cardNumberField.label)
            assertTrue(cardNumberField.isRequired)
            assertEquals("4567 3500 0042 7977", cardNumberField.applyMask("4567350000427977"))

            assertNotNull(expiryDateField, "Expiry date field should exist")
            assertEquals("expiryDate", expiryDateField.id)
            assertEquals("Expiry date", expiryDateField.label)
            assertTrue(expiryDateField.isRequired)
            assertEquals("12/30", expiryDateField.applyMask("1230"))

            assertNotNull(cvvField, "CVV field should exist")
            assertEquals("cvv", cvvField.id)
            assertEquals("Card verification code", cvvField.label)
            assertTrue(cvvField.isRequired)

            assertNotNull(cardholderNameField, "Cardholder name field should exist")
            assertEquals("cardholderName", cardholderNameField.id)
            assertEquals("Cardholder's name", cardholderNameField.label)
            assertFalse(cardholderNameField.isRequired)

            assertEquals(
                listOf("cardNumber", "expiryDate", "cvv"),
                result.requiredFields.map { it.id }
            )
        } finally {
            server.shutdown()
        }
    }
}