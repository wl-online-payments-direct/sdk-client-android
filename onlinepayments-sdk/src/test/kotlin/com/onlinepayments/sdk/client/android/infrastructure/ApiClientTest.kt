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

package com.onlinepayments.sdk.client.android.infrastructure

import com.google.gson.GsonBuilder
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.card.Card
import com.onlinepayments.sdk.client.android.domain.card.CardSource
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionRequest
import com.onlinepayments.sdk.client.android.domain.currencyConversion.Transaction
import com.onlinepayments.sdk.client.android.domain.exceptions.CommunicationException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsRequest
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationRequest
import com.onlinepayments.sdk.client.android.infrastructure.http.ApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IGoPayApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApiClientTest {

    private lateinit var server: MockWebServer
    private lateinit var apiClient: IGoPayApi

    @BeforeTest
    fun setUp() {
        server = MockWebServer()
        server.start()

        val gson = GsonBuilder().create()

        apiClient = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(IGoPayApi::class.java)
    }

    @AfterTest
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getBasicPaymentProductsReturnsDto() = runTest {
        enqueueOkJson("basicPaymentProducts.json")

        val dto = apiClient.getBasicPaymentProducts(
            customerId = "customer123",
            params = mapOf("countryCode" to "NL", "locale" to "en_GB")
        )

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path!!.startsWith("/customer123/products?"))
        assertTrue(request.path!!.contains("countryCode=NL"))
        assertTrue(request.path!!.contains("locale=en_GB"))
        assertTrue(request.path!!.contains("hide=fields"))
        assertTrue(request.path!!.contains("cacheBuster="))

        assertNotNull(dto)
        assertEquals(true, dto.paymentProducts?.isNotEmpty())
    }

    @Test
    fun getPaymentProductReturnsDto() = runTest {
        enqueueOkJson("cardPaymentProduct.json")

        val dto = apiClient.getPaymentProduct(
            customerId = "customer123",
            productId = "302",
            params = mapOf("countryCode" to "NL", "locale" to "en_GB")
        )

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path!!.startsWith("/customer123/products/302?"))
        assertTrue(request.path!!.contains("countryCode=NL"))
        assertTrue(request.path!!.contains("locale=en_GB"))
        assertTrue(request.path!!.contains("cacheBuster="))

        assertNotNull(dto)
        assertNotNull(dto.fields)
        assertTrue(dto.fields.isNotEmpty())
    }

    @Test
    fun getPaymentProductNetworksReturnsDto() = runTest {
        enqueueOkJson("paymentProductNetworks.json")

        val dto = apiClient.getPaymentProductNetworks(
            customerId = "customer123",
            productId = "302",
            params = mapOf("countryCode" to "NL", "locale" to "en_GB")
        )

        assertNotNull(dto)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path!!.startsWith("/customer123/products/302/networks?"))
        assertTrue(request.path!!.contains("countryCode=NL"))
        assertTrue(request.path!!.contains("locale=en_GB"))
        assertTrue(request.path!!.contains("cacheBuster="))
    }

    @Test
    fun getIinDetailsReturnsDto() = runTest {
        enqueueOkJson("iinDetailsResponse.json")

        val dto = apiClient.getIinDetails(
            customerId = "customer123",
            request = IinDetailsRequest("4567350000000000")
        )

        assertNotNull(dto)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/customer123/services/getIINdetails", request.path)
    }

    @Test
    fun getPublicKeyReturnsDto() = runTest {
        enqueueOkJson("publicKeyResponse.json")

        val dto = apiClient.getPublicKey("customer123")
        assertNotNull(dto)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/customer123/crypto/publickey", request.path)
    }

    @Test
    fun getCurrencyConversionQuoteReturnsDto() = runTest {
        enqueueOkJson("currencyConversionSuccess.json")

        val card = Card("4567350000000000", 1)

        val cardSource = CardSource(
            card
        )

        val amountOfMoney = AmountOfMoney(
            amount = 1000L,
            currencyCode = "EUR"
        )

        val transaction = Transaction(
            amountOfMoney
        )

        val requestBody = CurrencyConversionRequest(
            cardSource = cardSource,
            transaction = transaction
        )
        val dto = apiClient.getCurrencyConversionQuote(
            customerId = "customer123",
            request = requestBody
        )

        assertNotNull(dto)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/customer123/services/dccrate", request.path)
    }

    @Test
    fun getSurchargeCalculationReturnsDto() = runTest {
        enqueueOkJson("scWithSurcharge.json")

        val amountOfMoney = AmountOfMoney(
            amount = 1000L,
            currencyCode = "EUR"
        )

        val card = Card("4567350000000000", 1)

        val cardSource = CardSource(
            card
        )
        val requestBody = SurchargeCalculationRequest(
            amountOfMoney = amountOfMoney,
            cardSource = cardSource
        )

        val dto = apiClient.getSurchargeCalculation(
            customerId = "customer123",
            request = requestBody
        )
        assertNotNull(dto)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/customer123/services/surchargecalculation", request.path)
    }

    private fun enqueueOkJson(resourceName: String) {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(getJsonResource(resourceName))
        )
    }

    private fun getJsonResource(resourceName: String): String {
        val stream = javaClass.classLoader?.getResourceAsStream(resourceName)
        requireNotNull(stream) { "Missing test resource: $resourceName" }

        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    @Test
    fun getBasicPaymentProductsThrowsCommunicationErrorWhenGetResponseIsNotJson() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/html")
                .setBody("<html><body>Not JSON</body></html>")
        )

        val client = ApiClient(apiClient)

        val exception = assertFailsWith<CommunicationException> {
            client.getBasicPaymentProducts("customer123", emptyMap())
        }

        assertEquals(exception.message?.contains("JSON", ignoreCase = true), true)
    }

    @Test
    fun getIinDetailsThrowsCommunicationErrorWhenPostResponseIsNotJson() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/html")
                .setBody("<html><body>Not JSON</body></html>")
        )

        val client = ApiClient(apiClient)

        val exception = assertFailsWith<CommunicationException> {
            client.getIinDetails("customer123", IinDetailsRequest("411111"))
        }

        assertEquals(exception.message?.contains("JSON", ignoreCase = true), true)
    }

    @Test
    fun getBasicPaymentProductsThrowsResponseExceptionForHttp304() = runTest {
        server.enqueue(MockResponse().setResponseCode(304))

        val client = ApiClient(apiClient)

        val exception = assertFailsWith<ResponseException> {
            client.getBasicPaymentProducts("customer123", emptyMap())
        }

        assertEquals(304, exception.httpStatusCode)
    }
}
