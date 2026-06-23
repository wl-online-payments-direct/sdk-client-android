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

import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.integration.BaseMockIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Mock-backed integration tests for payment request validation and model behavior.
 */
class PaymentRequestMockIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun `ApplyMask produces correctly formatted output`() {
        runBlocking {
            val productJson = MockServerHelper.loadJsonResource("paymentProductVisa.json")
            val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, productJson)

            try {
                val product = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
                val request = PaymentRequest(product, null, false)

                request.setValue("cardNumber", "4567350000427977")
                request.setValue("expiryDate", "1230")
                request.setValue("cvv", "123")

                assertEquals("4567 3500 0042 7977", request.getField("cardNumber").getMaskedValue())
                assertEquals("12/30", request.getField("expiryDate").getMaskedValue())
                assertEquals("123", request.getField("cvv").getMaskedValue())
            } finally {
                server.shutdown()
            }
        }
    }

    @Test
    fun `EncryptPaymentRequest does not call public key API when mandatory field is missing`() {
        runBlocking {
            val productJson = MockServerHelper.loadJsonResource("paymentProductVisa.json")
            val (productSdk, productServer) = MockServerHelper.createMockSdkWithResponse(context, productJson)
            val paymentProduct = productSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
            productServer.shutdown()

            val (noCallSdk, server) = MockServerHelper.createMockSdkWithResponse(context, "{}")

            val request = PaymentRequest(paymentProduct, null, false)
            request.setValue("cardholderName", "Test Cardholder")
            request.setValue("cvv", "123")
            request.setValue("expiryDate", "1230")

            try {
                assertFailsWith<InvalidArgumentException> {
                    noCallSdk.encryptPaymentRequest(request)
                }

                assertEquals(0, server.requestCount, "Public key API should not be called when validation fails")
            } finally {
                server.shutdown()
            }
        }
    }

    @Test
    fun `GetPaymentProduct returns cached result for repeated request`() {
        runBlocking {
            val productJson = MockServerHelper.loadJsonResource("paymentProductVisa.json")
            val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, productJson)

            try {
                val firstResult = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
                val secondResult = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)

                assertEquals(1, server.requestCount, "Repeated getPaymentProduct request should use cache")
                assertEquals(firstResult.id, secondResult.id)
            } finally {
                server.shutdown()
            }
        }
    }

    @Test
    fun `PaymentRequest prevents writing read-only account-on-file card number`() {
        runBlocking {
            val productJson = MockServerHelper.loadJsonResource("paymentProductVisa.json")
            val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, productJson)

            try {
                val product = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
                val accountOnFile = product.accountsOnFile[0]
                val request = PaymentRequest(product, accountOnFile)

                assertFailsWith<InvalidArgumentException> {
                    request.setValue("cardNumber", "4222422242224222")
                }
            } finally {
                server.shutdown()
            }
        }
    }
}