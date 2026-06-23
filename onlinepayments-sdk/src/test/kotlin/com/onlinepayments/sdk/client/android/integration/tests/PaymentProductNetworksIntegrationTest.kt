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

import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for getNetworksForPaymentProduct.
 * Tests real API calls and cache behavior for payment product networks.
 */
class PaymentProductNetworksIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `GetPaymentProductNetworks returns networks for supported payment product`() = runBlocking {
        val result = sdk.getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result.networks, "Networks list should not be null")
        assertTrue(result.networks.isNotEmpty(), "Networks list should be non-empty for Google Pay")
    }

    @Test
    fun `GetPaymentProductNetworks throws error for unsupported payment product`() = runBlocking {
        val exception = assertFailsWith<ResponseException> {
            sdk.getNetworksForPaymentProduct(1, paymentContext)
        }

        assertNotNull(exception.httpStatusCode, "Response error should have an HTTP status code")
        assertTrue(
            exception.httpStatusCode >= 400,
            "HTTP status should be a 4xx or 5xx error, got: ${exception.httpStatusCode}"
        )
    }

    @Test
    fun `GetPaymentProductNetworks returns cached result for repeated request`() = runBlocking {
        val response = MockServerHelper.loadJsonResource("paymentProductNetworks.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, response)

        try {
            val firstResult = mockSdk.getNetworksForPaymentProduct(
                Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY,
                paymentContext
            )
            val secondResult = mockSdk.getNetworksForPaymentProduct(
                Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY,
                paymentContext
            )

            assertEquals(1, server.requestCount, "Repeated request with same context should use cache")
            assertEquals(firstResult.networks, secondResult.networks, "Cached result should contain the same networks")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `GetPaymentProductNetworks makes new API call for different context`() = runBlocking {
        val response = MockServerHelper.loadJsonResource("paymentProductNetworks.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponses(
            context,
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(response),
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(response)
        )

        try {
            mockSdk.getNetworksForPaymentProduct(
                Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY,
                paymentContext
            )

            val differentContext = createPaymentContext(
                amount = 1000,
                currencyCode = "USD",
                countryCode = "NL"
            )

            mockSdk.getNetworksForPaymentProduct(
                Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY,
                differentContext
            )

            assertEquals(2, server.requestCount, "Different context should trigger a new API call")
        } finally {
            server.shutdown()
        }
    }
}