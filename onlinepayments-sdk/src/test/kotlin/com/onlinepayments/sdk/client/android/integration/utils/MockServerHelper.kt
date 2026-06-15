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

package com.onlinepayments.sdk.client.android.integration.utils

import android.content.Context
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

/**
 * Helper that spins up a [MockWebServer] and wires an [OnlinePaymentsSdk] to it,
 * allowing integration tests to return controlled JSON responses.
 *
 * Usage (PaymentProductIntegrationTest):
 * ```
 * val json = MockServerHelper.loadJsonResource("paymentProductVisa.json")
 * val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, json)
 * try {
 *     val result = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
 *     assertTrue(result.accountsOnFile.isNotEmpty())
 * } finally {
 *     server.shutdown()
 * }
 * ```
 */
object MockServerHelper {

    /**
     * Starts a [MockWebServer], enqueues a single [responseBody] with [statusCode], and returns
     * an [OnlinePaymentsSdk] configured to talk to that server, paired with the server instance.
     *
     * The caller is responsible for calling [MockWebServer.shutdown] when done.
     */
    fun createMockSdkWithResponse(
        context: Context,
        responseBody: String,
        statusCode: Int = 200
    ): Pair<OnlinePaymentsSdk, MockWebServer> {
        val server = MockWebServer()
        server.start()

        server.enqueue(
            MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(responseBody)
        )

        val baseUrl = server.url("/").toString()

        return buildMockSdkWithServer(context, server, baseUrl)
    }

    /**
     * Starts a [MockWebServer], enqueues multiple [responses] in order, and returns
     * an [OnlinePaymentsSdk] configured to talk to that server, paired with the server instance.
     *
     * The caller is responsible for calling [MockWebServer.shutdown] when done.
     */
    fun createMockSdkWithResponses(
        context: Context,
        vararg responses: MockResponse
    ): Pair<OnlinePaymentsSdk, MockWebServer> {
        val server = MockWebServer()
        server.start()
        responses.forEach { server.enqueue(it) }
        val baseUrl = server.url("/").toString()
        return buildMockSdkWithServer(context, server, baseUrl)
    }

    private fun buildMockSdkWithServer(
        context: Context,
        server: MockWebServer,
        baseUrl: String
    ): Pair<OnlinePaymentsSdk, MockWebServer> {
        val fakeSession = SessionData(
            clientSessionId = "mock-session-id",
            customerId = "mock-customer-id",
            clientApiUrl = baseUrl,
            assetUrl = baseUrl
        )
        val mockSdk = OnlinePaymentsSdk(fakeSession, context, TestConfig.sdkConfiguration)
        return mockSdk to server
    }

    /**
     * Loads a JSON file from `src/test/resources/` by name.
     */
    fun loadJsonResource(resourceName: String): String {
        return MockServerHelper::class.java.classLoader
            ?.getResourceAsStream(resourceName)
            ?.bufferedReader()
            ?.readText()
            ?: error("Test resource not found: $resourceName")
    }
}
