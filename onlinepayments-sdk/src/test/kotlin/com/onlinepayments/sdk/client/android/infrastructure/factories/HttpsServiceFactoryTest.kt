/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.factories

import android.content.Context
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.infrastructure.encryption.MetadataUtil
import com.onlinepayments.sdk.client.android.mocks.MockContext
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HttpsServiceFactoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var context: Context
    private lateinit var sessionData: SessionData
    private lateinit var sdkConfiguration: SdkConfiguration

    @BeforeTest
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        context = MockContext.setup()

        sessionData = SessionData(
            clientSessionId = "test-session-123",
            customerId = "test-customer-456",
            clientApiUrl = mockWebServer.url("/client/v1/").toString(),
            assetUrl = "https://assets.example.com"
        )

        sdkConfiguration = SdkConfiguration(
            environmentIsProduction = false,
            appIdentifier = "TestApp/1.0.0",
            sdkIdentifier = "AndroidSDK/2.5.0"
        )

        mockkObject(MetadataUtil)
    }

    @AfterTest
    fun tearDown() {
        mockWebServer.shutdown()
        unmockkObject(MetadataUtil)
    }

    @Test
    fun `createApiService without logger`() {
        every { MetadataUtil.getMetadata(any(), any(), any()) } returns mapOf("key" to "value")
        every { MetadataUtil.getBase64EncodedMetadata(any()) } returns "encoded-meta"

        val apiService = HttpServiceFactory.createApiService(
            configuration = sdkConfiguration,
            sessionData = sessionData,
            context = context,
            apiLogger = null
        )

        assertNotNull(apiService)
    }

    @Test
    fun `createApiService adds Authorization header`() = runTest {
        every { MetadataUtil.getMetadata(any(), any(), any()) } returns mapOf("key" to "value")
        every { MetadataUtil.getBase64EncodedMetadata(any()) } returns "encoded-meta"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"paymentProducts": []}""")
        )

        val apiService = HttpServiceFactory.createApiService(
            configuration = sdkConfiguration,
            sessionData = sessionData,
            context = context,
            apiLogger = null
        )

        apiService.getBasicPaymentProducts("test-customer-456", emptyMap())

        val request = mockWebServer.takeRequest()
        val authHeader = request.getHeader("Authorization")

        assertNotNull(authHeader)
        assertTrue(authHeader.startsWith("GCS v1Client:"))
        assertTrue(authHeader.contains("test-session-123"))
    }

    @Test
    fun `createApiService adds X-GCS-ClientMetaInfo header`() = runTest {
        every { MetadataUtil.getMetadata(any(), any(), any()) } returns mapOf("key" to "value")
        every { MetadataUtil.getBase64EncodedMetadata(any()) } returns "encoded-meta"

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"paymentProducts": []}""")
        )

        val apiService = HttpServiceFactory.createApiService(
            configuration = sdkConfiguration,
            sessionData = sessionData,
            context = context,
            apiLogger = null
        )

        apiService.getBasicPaymentProducts("test-customer-456", emptyMap())

        val request = mockWebServer.takeRequest()
        val metadataHeader = request.getHeader("X-GCS-ClientMetaInfo")

        assertNotNull(metadataHeader)
        assertTrue(metadataHeader == "encoded-meta")
    }

    @Test
    fun `createApiService does not add X-GCS-ClientMetaInfo when metadata is empty`() = runTest {
        every { MetadataUtil.getMetadata(any(), any(), any()) } returns emptyMap()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"paymentProducts": []}""")
        )

        val apiService = HttpServiceFactory.createApiService(
            configuration = sdkConfiguration,
            sessionData = sessionData,
            context = context,
            apiLogger = null
        )

        apiService.getBasicPaymentProducts("test-customer-456", emptyMap())

        val request = mockWebServer.takeRequest()
        val metadataHeader = request.getHeader("X-GCS-ClientMetaInfo")

        assertNull(metadataHeader)
    }
}