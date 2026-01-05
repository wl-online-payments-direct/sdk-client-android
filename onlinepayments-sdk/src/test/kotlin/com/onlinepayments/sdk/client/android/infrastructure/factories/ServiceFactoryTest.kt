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
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class ServiceFactoryTest {
    private lateinit var context: Context
    private lateinit var sessionData: SessionData
    private lateinit var configuration: SdkConfiguration
    private lateinit var apiClient: IApiClient

    @BeforeTest
    fun setUp() {
        context = mockk(relaxed = true)
        apiClient = mockk(relaxed = true)

        sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/client/v1/",
            assetUrl = "https://assets.example.com"
        )

        configuration = SdkConfiguration(
            environmentIsProduction = false,
            appIdentifier = "TestApp/1.0.0",
            sdkIdentifier = "AndroidSDK/2.5.0"
        )
    }

    @Test
    fun `factory creates successfully`() {
        val factory = ServiceFactory(
            ServiceFactoryConfiguration(
                sessionData = sessionData,
                configuration = configuration,
                context = context,
                apiClient = apiClient
            )
        )

        assertNotNull(factory)
    }

    @Test
    fun `apiClient returns provided client`() {
        val factory = ServiceFactory(
            ServiceFactoryConfiguration(
                sessionData = sessionData,
                configuration = configuration,
                context = context,
                apiClient = apiClient
            )
        )

        val result = factory.apiClient

        assertNotNull(result)
        assertSame(apiClient, result)
    }

    @Test
    fun `encryptionService is created`() {
        val factory = ServiceFactory(
            ServiceFactoryConfiguration(
                sessionData = sessionData,
                configuration = configuration,
                context = context,
                apiClient = apiClient
            )
        )

        val result = factory.encryptionService

        assertNotNull(result)
    }

    @Test
    fun `clientService is created`() {
        val factory = ServiceFactory(
            ServiceFactoryConfiguration(
                sessionData = sessionData,
                configuration = configuration,
                context = context,
                apiClient = apiClient
            )
        )

        val result = factory.clientService

        assertNotNull(result)
    }

    @Test
    fun `paymentProductService is created`() {
        val factory = ServiceFactory(
            ServiceFactoryConfiguration(
                sessionData = sessionData,
                configuration = configuration,
                context = context,
                apiClient = apiClient
            )
        )

        val result = factory.paymentProductService

        assertNotNull(result)
    }

    @Test
    fun `cacheManager is created`() {
        val factory = ServiceFactory(
            ServiceFactoryConfiguration(
                sessionData = sessionData,
                configuration = configuration,
                context = context,
                apiClient = apiClient
            )
        )

        val result = factory.cacheManager

        assertNotNull(result)
    }

    @Test
    fun `services are lazily initialized only once`() {
        val factory = ServiceFactory(
            ServiceFactoryConfiguration(
                sessionData = sessionData,
                configuration = configuration,
                context = context,
                apiClient = apiClient
            )
        )

        val first = factory.encryptionService
        val second = factory.encryptionService

        assertSame(first, second, "Same instance should be returned")
    }
}