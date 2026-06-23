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

import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.exceptions.CommunicationException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk
import com.onlinepayments.sdk.client.android.integration.BaseMockIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Mock-backed integration tests for basic payment products.
 * Tests caching, filtering and error handling without requiring live credentials.
 */
class BasicPaymentProductsMockIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun `GetBasicPaymentProducts returns cached result for repeated request`() {
        runBlocking {
            val response = MockServerHelper.loadJsonResource("basicPaymentProducts.json")
            val (mockSdk, mockWebServer) = MockServerHelper.createMockSdkWithResponse(context, response)

            try {
                val firstResult = mockSdk.getBasicPaymentProducts(paymentContext)
                val secondResult = mockSdk.getBasicPaymentProducts(paymentContext)

                assertEquals(1, mockWebServer.requestCount, "Repeated request with same context should use cache")
                assertEquals(
                    firstResult.paymentProducts.map { it.id },
                    secondResult.paymentProducts.map { it.id },
                    "Cached result should contain the same product IDs"
                )
            } finally {
                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun `GetBasicPaymentProducts makes new API call for different context`() {
        runBlocking {
            val response = MockServerHelper.loadJsonResource("basicPaymentProducts.json")
            val (mockSdk, mockWebServer) = MockServerHelper.createMockSdkWithResponses(
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
                mockSdk.getBasicPaymentProducts(paymentContext)

                val differentContext = createPaymentContext(
                    amount = 1000,
                    currencyCode = "USD",
                    countryCode = "NL"
                )

                mockSdk.getBasicPaymentProducts(differentContext)

                assertEquals(2, mockWebServer.requestCount, "Different context should trigger a new API call")
            } finally {
                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun `GetBasicPaymentProducts filters products not supported by SDK`() {
        runBlocking {
            val response = MockServerHelper.loadJsonResource("basicPaymentProductsAllFiltered.json")
            val (mockSdk, mockWebServer) = MockServerHelper.createMockSdkWithResponse(context, response)

            try {
                val exception = assertFailsWith<ResponseException> {
                    mockSdk.getBasicPaymentProducts(paymentContext)
                }

                assertEquals(404, exception.httpStatusCode, "Should return 404 when all products are filtered out")
                assertEquals(
                    "No payment products available.",
                    exception.message,
                    "Should return the expected error message"
                )
            } finally {
                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun `GetBasicPaymentProducts throws error when no payment products are available`() {
        runBlocking {
            val response = """{"paymentProducts":[]}"""
            val (mockSdk, mockWebServer) = MockServerHelper.createMockSdkWithResponse(context, response)

            try {
                val exception = assertFailsWith<ResponseException> {
                    mockSdk.getBasicPaymentProducts(paymentContext)
                }

                assertEquals(404, exception.httpStatusCode)
                assertEquals("No payment products available.", exception.message)
            } finally {
                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun `GetBasicPaymentProducts throws response error for 503 response`() {
        runBlocking {
            val (mockSdk, mockWebServer) = MockServerHelper.createMockSdkWithResponse(
                context = context,
                responseBody = """{"errorId":"SERVER_ERROR","errors":[]}""",
                statusCode = 503
            )

            try {
                val exception = assertFailsWith<ResponseException> {
                    mockSdk.getBasicPaymentProducts(paymentContext)
                }

                assertEquals(503, exception.httpStatusCode)
            } finally {
                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun `GetBasicPaymentProducts throws communication error for malformed JSON`() {
        runBlocking {
            val (mockSdk, mockWebServer) = MockServerHelper.createMockSdkWithResponse(
                context = context,
                responseBody = "not-valid-json",
                statusCode = 200
            )

            try {
                assertFailsWith<CommunicationException> {
                    mockSdk.getBasicPaymentProducts(paymentContext)
                }
            } finally {
                mockWebServer.shutdown()
            }
        }
    }

    @Test
    fun `GetBasicPaymentProducts throws communication error for network failure`() {
        runBlocking {
            val mockWebServer = MockWebServer()
            mockWebServer.start()
            mockWebServer.enqueue(
                MockResponse().apply {
                    socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST
                }
            )

            val baseUrl = mockWebServer.url("/").toString()
            val sessionData = SessionData(
                clientSessionId = "mock-session-id",
                customerId = "mock-customer-id",
                clientApiUrl = baseUrl,
                assetUrl = baseUrl
            )
            val mockSdk = OnlinePaymentsSdk(sessionData, context, TestConfig.sdkConfiguration)

            try {
                assertFailsWith<CommunicationException> {
                    mockSdk.getBasicPaymentProducts(paymentContext)
                }
            } finally {
                mockWebServer.shutdown()
            }
        }
    }
}