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

import com.onlinepayments.sdk.client.android.domain.exceptions.CommunicationException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.integration.BaseMockIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

/**
 * Mock-backed tests covering error responses: 5xx, malformed JSON, and network disconnect.
 */
class ErrorCoverageIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun `getBasicPaymentProducts with 503 response should throw ResponseException`() = runTest {
        val (sdk, server) = MockServerHelper.createMockSdkWithResponse(
            context,
            """{"errorId":"SERVER_ERROR","errors":[]}""",
            statusCode = 503
        )
        try {
            val exception = assertFailsWith<ResponseException> {
                sdk.getBasicPaymentProducts(paymentContext)
            }
            assertEquals(503, exception.httpStatusCode)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `getBasicPaymentProducts with malformed JSON should throw CommunicationException`() = runTest {
        val (sdk, server) = MockServerHelper.createMockSdkWithResponse(
            context,
            "not-valid-json",
            statusCode = 200
        )
        try {
            assertFailsWith<CommunicationException> {
                sdk.getBasicPaymentProducts(paymentContext)
            }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `getBasicPaymentProducts with network disconnect should throw CommunicationException`() = runTest {
        val server = okhttp3.mockwebserver.MockWebServer()
        server.start()
        server.enqueue(MockResponse().apply { socketPolicy = SocketPolicy.DISCONNECT_AFTER_REQUEST })
        val baseUrl = server.url("/").toString()
        val fakeSession = com.onlinepayments.sdk.client.android.domain.configuration.SessionData(
            clientSessionId = "mock-session-id",
            customerId = "mock-customer-id",
            clientApiUrl = baseUrl,
            assetUrl = baseUrl
        )
        val sdk = com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk(
            fakeSession, context, TestConfig.sdkConfiguration
        )
        try {
            assertFailsWith<CommunicationException> {
                sdk.getBasicPaymentProducts(paymentContext)
            }
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `getPaymentProduct with 404 response should throw ResponseException`() = runTest {
        val (sdk, server) = MockServerHelper.createMockSdkWithResponse(
            context,
            """{"errorId":"NOT_FOUND","errors":[]}""",
            statusCode = 404
        )
        try {
            val exception = assertFailsWith<ResponseException> {
                sdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
            }
            assertEquals(404, exception.httpStatusCode)
        } finally {
            server.shutdown()
        }
    }
}