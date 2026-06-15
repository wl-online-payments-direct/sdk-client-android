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
 * Mock-backed integration tests for payment request validation and encryption.
 * Tests SDK validation and encryption logic without requiring live credentials.
 */
class PaymentRequestMockIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun encryptPaymentRequest_missingMandatoryField_shouldNotCallPublicKeyApi() = runBlocking {
        // First get a real product from the mock server
        val productJson = MockServerHelper.loadJsonResource("paymentProductVisa.json")
        val (productSdk, productServer) = MockServerHelper.createMockSdkWithResponse(context, productJson)
        val paymentProduct = productSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
        productServer.shutdown()

        // Now create a new mock SDK that should never be called
        val (noCallSdk, server) = MockServerHelper.createMockSdkWithResponse(context, "{}")

        val request = PaymentRequest(paymentProduct, null, false)
        request.setValue("cardholderName", "Test Cardholder")
        request.setValue("cvv", "123")
        request.setValue("expiryDate", "1226")

        try {
            assertFailsWith<InvalidArgumentException> { noCallSdk.encryptPaymentRequest(request) }
        } finally {
            assertEquals(0, server.requestCount, "Public key API should not be called when validation fails")
            server.shutdown()
        }
        Unit
    }

    @Test
    fun encryptPaymentRequest_withAofReadOnlyField_shouldThrowOnSetValue() = runBlocking {
        val json = MockServerHelper.loadJsonResource("paymentProductVisa.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, json)

        try {
            val product = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
            val aof = product.accountsOnFile[0]
            val request = PaymentRequest(product, aof)

            assertFailsWith<InvalidArgumentException> {
                request.setValue("cardNumber", "4222422242224222")
            }
            Unit
        } finally {
            server.shutdown()
        }
    }

}
