/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.services

import android.content.Context
import com.google.gson.Gson
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount
import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.ICacheManager
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IPaymentProductFactory
import com.onlinepayments.sdk.client.android.infrastructure.utils.CacheManager
import com.onlinepayments.sdk.client.android.services.interfaces.IPaymentProductService
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PaymentProductServiceTest {

    private lateinit var apiClient: IApiClient
    private lateinit var context: Context
    private lateinit var sessionData: SessionData
    private lateinit var sdkConfiguration: SdkConfiguration
    private lateinit var paymentProductService: IPaymentProductService
    private lateinit var paymentContext: PaymentContext
    private lateinit var cacheManager: ICacheManager
    private lateinit var paymentProductFactory: IPaymentProductFactory
    private lateinit var gson: Gson

    @BeforeTest
    fun setUp() {
        clearAllMocks()

        cacheManager = CacheManager()
        apiClient = mockk()
        context = mockk(relaxed = true)
        paymentProductFactory = PaymentProductFactory()
        gson = Gson()

        paymentContext = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 100L,
                currencyCode = "EUR"
            ),
            countryCode = "NL",
            isRecurring = false
        )

        sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/client/v1/",
            assetUrl = "https://assets.example.com"
        )

        sdkConfiguration = SdkConfiguration(
            environmentIsProduction = false,
            appIdentifier = "TestApp",
            sdkIdentifier = "AndroidSDK"
        )

        paymentProductService = PaymentProductService(
            apiClient = apiClient,
            context = context,
            sessionData = sessionData,
            configuration = sdkConfiguration,
            cacheManager = cacheManager,
            paymentProductFactory = paymentProductFactory
        )
    }

    @Test
    fun `getBasicPaymentProducts returns BasicPaymentProducts with products`() = runTest {
        prepareBasicProductsClientResponse()

        val result = paymentProductService.getBasicPaymentProducts(paymentContext)

        assertNotNull(result)
        assertNotNull(result.paymentProducts)
    }

    @Test
    fun `getBasicPaymentProducts filters out Apple Pay product`() = runTest {
        prepareBasicProductsClientResponse()

        val result = paymentProductService.getBasicPaymentProducts(paymentContext)

        assertNotNull(result)
    }

    @Test
    fun `getPaymentProduct throws ResponseException for Apple Pay`() = runTest {
        val exception = assertFailsWith<ResponseException> {
            paymentProductService.getPaymentProduct(
                Constants.PAYMENT_PRODUCT_ID_APPLEPAY,
                paymentContext
            )
        }

        assertEquals("Product with id 302 not found or not available.", exception.message)
    }

    @Test
    fun `getPaymentProduct throws ResponseException for unavailable product`() = runTest {
        val unavailableId = Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS.first()

        val exception = assertFailsWith<ResponseException> {
            paymentProductService.getPaymentProduct(unavailableId, paymentContext)
        }

        assertEquals("Product with id $unavailableId not found or not available.", exception.message)
    }

    @Test
    fun `getPaymentProduct returns PaymentProduct successfully`() = runTest {
        prepareProductClientResponse()

        val result = paymentProductService.getPaymentProduct(1, paymentContext)

        assertNotNull(result)
        assertEquals(1, result.id)
        assertNotNull(result.fields)
    }

    @Test
    fun `getPaymentProduct returns product with fields sorted by displayOrder`() = runTest {
        prepareProductClientResponse()

        val result = paymentProductService.getPaymentProduct(1, paymentContext)

        assertNotNull(result)
        val fields = result.fields
        assertEquals(4, fields.size)
        assertEquals("cardNumber", fields[0].id)
        assertEquals("cardholderName", fields[1].id)
        assertEquals("expiryDate", fields[2].id)
        assertEquals("cvv", fields[3].id)
    }

    @Test
    fun `getPaymentProduct returns product with label and logo`() = runTest {
        prepareProductClientResponse()

        val result = paymentProductService.getPaymentProduct(1, paymentContext)

        assertNotNull(result)
        assertEquals("VISA", result.label)
        assertEquals("test-logo", result.logo)
    }

    @Test
    fun `getPaymentProductNetworks returns networks successfully`() = runTest {
        val networksJson = """
            {
                "networks": ["visa", "mastercard"]
            }
        """.trimIndent()

        val networks: PaymentProductNetworksResponse =
            gson.fromJson(networksJson, PaymentProductNetworksResponse::class.java)

        coEvery {
            apiClient.getPaymentProductNetworks(
                customerId = sessionData.customerId,
                productId = "1",
                params = paymentContext.toMap()
            )
        } returns networks

        val result = paymentProductService.getPaymentProductNetworks(1, paymentContext)

        assertNotNull(result)
    }

    @Test
    fun `getBasicPaymentProducts uses cache on second call`() = runTest {
        prepareBasicProductsClientResponse()

        val result1 = paymentProductService.getBasicPaymentProducts(paymentContext)

        val result2 = paymentProductService.getBasicPaymentProducts(paymentContext)

        assertNotNull(result1)
        assertNotNull(result2)

        coVerify(exactly = 1) {
            apiClient.getBasicPaymentProducts(
                customerId = sessionData.customerId,
                params = paymentContext.toMap()
            )
        }
    }

    @Test
    fun `getPaymentProduct uses cache on second call`() = runTest {
        prepareProductClientResponse()

        val result1 = paymentProductService.getPaymentProduct(1, paymentContext)

        val result2 = paymentProductService.getPaymentProduct(1, paymentContext)

        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(result1.id, result2.id)

        coVerify(exactly = 1) {
            apiClient.getPaymentProduct(
                customerId = sessionData.customerId,
                productId = "1",
                params = paymentContext.toMap()
            )
        }
    }

    @Test
    fun `getBasicPaymentProducts cache invalidates with different payment context`() = runTest {
        val dto = GsonHelper.fromResourceJson(
            "basicPaymentProducts.json",
            BasicPaymentProductsDto::class.java
        )

        val differentContext = PaymentContextWithAmount(
            amountOfMoney = AmountOfMoneyWithAmount(
                amount = 200L,
                currencyCode = "EUR"
            ),
            countryCode = "NL",
            isRecurring = false
        )

        coEvery {
            apiClient.getBasicPaymentProducts(
                customerId = sessionData.customerId,
                params = paymentContext.toMap()
            )
        } returns dto

        coEvery {
            apiClient.getBasicPaymentProducts(
                customerId = sessionData.customerId,
                params = differentContext.toMap()
            )
        } returns dto

        val result1 = paymentProductService.getBasicPaymentProducts(paymentContext)

        val result2 = paymentProductService.getBasicPaymentProducts(differentContext)

        assertNotNull(result1)
        assertNotNull(result2)

        coVerify(exactly = 1) {
            apiClient.getBasicPaymentProducts(
                customerId = sessionData.customerId,
                params = paymentContext.toMap()
            )
        }

        coVerify(exactly = 1) {
            apiClient.getBasicPaymentProducts(
                customerId = sessionData.customerId,
                params = differentContext.toMap()
            )
        }
    }

    @Test
    fun `getPaymentProduct cache invalidates with different payment context`() = runTest {
        val dto = GsonHelper.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto::class.java
        )

        val differentContext = PaymentContext(
            amountOfMoney = paymentContext.amountOfMoney,
            countryCode = "BE"
        )

        coEvery {
            apiClient.getPaymentProduct(
                customerId = sessionData.customerId,
                productId = "1",
                params = paymentContext.toMap()
            )
        } returns dto

        coEvery {
            apiClient.getPaymentProduct(
                customerId = sessionData.customerId,
                productId = "1",
                params = differentContext.toMap()
            )
        } returns dto

        val result1 = paymentProductService.getPaymentProduct(1, paymentContext)

        val result2 = paymentProductService.getPaymentProduct(1, differentContext)

        assertNotNull(result1)
        assertNotNull(result2)

        coVerify(exactly = 1) {
            apiClient.getPaymentProduct(
                customerId = sessionData.customerId,
                productId = "1",
                params = paymentContext.toMap()
            )
        }

        coVerify(exactly = 1) {
            apiClient.getPaymentProduct(
                customerId = sessionData.customerId,
                productId = "1",
                params = differentContext.toMap()
            )
        }
    }

    @Test
    fun `getPaymentProductNetworks uses cache on second call`() = runTest {
        val networksJson = """
        {
            "networks": ["visa", "mastercard"]
        }
    """.trimIndent()

        val networks: PaymentProductNetworksResponse =
            gson.fromJson(networksJson, PaymentProductNetworksResponse::class.java)

        coEvery {
            apiClient.getPaymentProductNetworks(
                customerId = sessionData.customerId,
                productId = "1",
                params = paymentContext.toMap()
            )
        } returns networks

        val result1 = paymentProductService.getPaymentProductNetworks(1, paymentContext)

        val result2 = paymentProductService.getPaymentProductNetworks(1, paymentContext)

        assertNotNull(result1)
        assertNotNull(result2)

        coVerify(exactly = 1) {
            apiClient.getPaymentProductNetworks(
                customerId = sessionData.customerId,
                productId = "1",
                params = paymentContext.toMap()
            )
        }
    }

    private fun prepareBasicProductsClientResponse() {
        val dto = GsonHelper.fromResourceJson(
            "basicPaymentProducts.json",
            BasicPaymentProductsDto::class.java
        )

        coEvery {
            apiClient.getBasicPaymentProducts(
                customerId = sessionData.customerId,
                params = paymentContext.toMap()
            )
        } returns dto
    }

    private fun prepareProductClientResponse() {
        val dto = GsonHelper.fromResourceJson(
            "cardPaymentProduct.json",
            PaymentProductDto::class.java
        )

        coEvery {
            apiClient.getPaymentProduct(
                customerId = sessionData.customerId,
                productId = "1",
                params = paymentContext.toMap()
            )
        } returns dto
    }
}