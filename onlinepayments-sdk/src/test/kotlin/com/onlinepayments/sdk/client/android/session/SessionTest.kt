/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.session

import android.os.Looper
import android.util.Base64
import com.google.gson.JsonElement
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.exception.ApiException
import com.onlinepayments.sdk.client.android.mocks.MockContext
import com.onlinepayments.sdk.client.android.mocks.MockEncoding
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.currencyconversion.ConversionResultType
import com.onlinepayments.sdk.client.android.model.iin.IinStatus
import com.onlinepayments.sdk.client.android.providers.LoggerProvider
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import com.onlinepayments.sdk.client.android.util.GooglePayUtil
import com.onlinepayments.sdk.client.android.util.Logger
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
@PrepareForTest(Base64::class)
class SessionTest {
    companion object {
        private val testDispatcher = StandardTestDispatcher()

        private lateinit var mockWebServer: MockWebServer

        private val mockContext = MockContext.setup()

        private val mockLogger = mockk<Logger>(relaxed = true)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        MockEncoding.setup()
        LoggerProvider.logger = mockLogger

        mockkObject(GooglePayUtil)
        every { GooglePayUtil.isGooglePayAllowed(any(), any(), any()) } returns false
        coEvery { GooglePayUtil.isGooglePayAllowed(any(), any(), any()) } returns false

        mockkStatic(Looper::class)
        val mockLooper = mockk<Looper>(relaxed = true)
        every { Looper.getMainLooper() } returns mockLooper
        coEvery { Looper.getMainLooper() } returns mockLooper
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
        LoggerProvider.reset()
        // Cleanup MockK mocks
        unmockkAll()
    }

    @Test
    fun testGetPaymentProducts() = runTest {
        setMockServerResponse("paymentProducts.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)
        val paymentProducts = getSession().getBasicPaymentProducts(paymentContext)

        val products = paymentProducts.getBasicPaymentProducts()
        // After filtering we get 29 products
        assertTrue(products.count() == 29)

        val resultIds = products.map {it.getId() }
        val filteredIds = listOf("117", "5700", "5772", "5784")

        filteredIds.forEach { filteredId ->
            assertFalse(
                "Expected product with id=$filteredId to be filtered out, but it was found.",
                resultIds.contains(filteredId)
            )
        }
    }

    @Test
    fun testGetPaymentProductsWithError() = runTest {
        setMockServerResponse("apiError400.json", 400)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ApiException> {
            getSession().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentItems() = runTest {
        setMockServerResponse("paymentProducts.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)
        val paymentItems = getSession().getBasicPaymentItems(paymentContext)

        // After filtering we get 29 products
        assertTrue(paymentItems.basicPaymentItems.count() == 29)
    }

    @Test
    fun testGetPaymentItemsWithError() = runTest {
        setMockServerResponse("apiError400.json", 400)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ApiException> {
            getSession().getBasicPaymentItems(paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentProduct() = runTest {
        setMockServerResponse("paymentProductVisa.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val paymentProduct = getSession().getPaymentProduct("1", paymentContext)

        assertNotNull(paymentProduct)
        assertTrue(paymentProduct!!.getDisplayHintsList().count() > 0)
    }

    @Test
    fun testGetPaymentProductFromCache() = runTest {
        setMockServerResponse("paymentProductVisa.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val session = getSession()
        val paymentProduct = session.getPaymentProduct("1", paymentContext)

        // set bad request response, which should not be called
        setMockServerResponse("apiErrorItemComplete.json", 404)

        val cachedProduct = session.getPaymentProduct("1", paymentContext)
        assertEquals(paymentProduct, cachedProduct)

        // different product should invoke API call
        assertFailsWith<ApiException> {
            session.getPaymentProduct("2", paymentContext)
        }
    }

    @Test
    fun testGetPaymentProductNetworks() = runTest {
        setMockServerResponse("paymentProductNetworks.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val session = getSession()
        val networks = session.getNetworksForPaymentProduct(
            Constants.PAYMENT_PRODUCT_ID_APPLEPAY,
            paymentContext
        )

        assertEquals(networks.networks?.count(), 3)
    }

    @Test
    fun testGetIinDetails() = runTest {
        setMockServerResponse("normalIINResponseVisa.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val session = getSession()

        // test min digits validation
        var iinDetails = session.getIinDetails("4141", paymentContext)

        assertEquals(iinDetails.status, IinStatus.NOT_ENOUGH_DIGITS)
        assertNull(iinDetails.paymentProductId)

        // test valid response
        iinDetails = session.getIinDetails("414141", paymentContext)

        assertEquals(iinDetails.status, IinStatus.SUPPORTED)
        assertEquals(iinDetails.paymentProductId, "1")
    }

    @Test
    fun testGetIinDetailsNotFound() = runTest {
        setMockServerResponse("iinDetailsNotFound.json", 404)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val session = getSession()

        val details = session.getIinDetails("411111", paymentContext)

        assertEquals(details.status, IinStatus.UNKNOWN)
    }

    @Test
    fun testGetIinDetailsAlreadyInProgress() = runTest {
        setMockServerResponse("normalIINResponseVisa.json", 200, 1000)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val session = getSession()

        // Start the first call (suspended for 1000ms)
        launch {
            session.getIinDetails("411111", paymentContext)
        }

        // Ensure the first call sets `iinLookupPending = true`
        advanceUntilIdle()

        // Second call should throw the exception
        val exception = assertFailsWith<ApiException> {
            session.getIinDetails("411111", paymentContext)
        }

        assertEquals("IIN lookup is already in progress", exception.message)
    }

    @Test
    fun testGetPublicKey() = runTest {
        setMockServerResponse("publicKeyResponse.json", 200)

        val publicKey = getSession().getPublicKey()

        assertNotNull(publicKey)
        assertNotNull(publicKey.getPublicKey())
        assertEquals(publicKey.getPublicKey()?.format, "X.509")
        assertEquals(publicKey.getKeyId(), "12345678-aaaa-bbbb-cccc-876543218765")
    }

    @Test
    fun testGetPublicKeyBadRequest() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("{}").setResponseCode(200))

        val publicKey = getSession().getPublicKey()

        assertNull(publicKey.getPublicKey())
    }

    @Test
    fun testPreparePayment() = runTest {
        // for detailed test regarding payment request check `PaymentRequestTest`
        val paymentRequest = GsonHelper.fromResourceJson<PaymentRequest>(
            "paymentRequest.json",
            PaymentRequest::class.java
        )

        setMockServerResponse("publicKeyResponse.json", 200)

        val preparedRequest = getSession().preparePaymentRequest(paymentRequest)

        assertNotNull(preparedRequest.encryptedFields)
        assertNotNull(preparedRequest.encodedClientMetaInfo)
    }

    @Test
    fun testGetCurrencyConversionQuoteForCard() = runTest {
        setMockServerResponse("currencyConversionSuccess.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote =
            getSession().getCurrencyConversionQuote(amountOfMoney, "411111", "1")

        assertEquals(quote.result.result, ConversionResultType.ALLOWED)
        assertNotNull(quote.proposal.rate)
    }

    @Test
    fun testGetCurrencyConversionQuoteForCardNoRate() = runTest {
        setMockServerResponse("currencyConversionNoRate.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote =
            getSession().getCurrencyConversionQuote(amountOfMoney, "411111", "1")

        assertEquals(quote.result.result, ConversionResultType.NO_RATE)
        assertNull(quote.proposal.rate)
    }

    @Test
    fun testGetCurrencyConversionQuoteForCardNotFound() = runTest {
        setMockServerResponse("currencyConversionNotFound.json", 400)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ApiException> {
            getSession().getCurrencyConversionQuote(amountOfMoney, "411111", "1")
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetCurrencyConversionQuoteForToken() = runTest {
        setMockServerResponse("currencyConversionSuccess.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote = getSession().getCurrencyConversionQuote(amountOfMoney, "411111")

        assertEquals(quote.result.result, ConversionResultType.ALLOWED)
        assertNotNull(quote.proposal.rate)
    }

    @Test
    fun testGetCurrencyConversionQuoteForTokenNoRate() = runTest {
        setMockServerResponse("currencyConversionNoRate.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote = getSession().getCurrencyConversionQuote(amountOfMoney, "411111")

        assertEquals(quote.result.result, ConversionResultType.NO_RATE)
        assertNull(quote.proposal.rate)
    }

    @Test
    fun testGetSurchargeCalculationForCard() = runTest {
        setMockServerResponse("scWithSurcharge.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote = getSession().getSurchargeCalculation(amountOfMoney, "411111", "1")

        assertEquals(quote.surcharges.count(), 1)
    }

    @Test
    fun testGetSurchargeCalculationForToken() = runTest {
        setMockServerResponse("scWithSurcharge.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote = getSession().getSurchargeCalculation(amountOfMoney, "411111")

        assertEquals(quote.surcharges.count(), 1)
    }

    @Test
    fun testLoggingApiException() = runTest {
        clearMocks(mockLogger)
        setMockServerResponse("apiError400.json", 400)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val tagCaptor = mutableListOf<String>()
        val messageCaptor = mutableListOf<String>()

        // default session is created with error logging enabled.
        val session = getSession()
        val exception = assertFailsWith<ApiException> {
            session.getBasicPaymentItems(paymentContext)
        }

        verify { mockLogger.e(capture(tagCaptor), capture(messageCaptor)) }

        assertNotNull(exception)
        assertTrue(session.getLoggingEnabled())
        assertEquals("LocalResponseListener", tagCaptor[0])
        assertTrue(messageCaptor[0].contains(exception.message ?: "dummy text"))
        assertTrue(messageCaptor[0].contains(exception.errorResponse?.message ?: "dummy text"))
        assertTrue(
            "When logging is enabled, API Error should be logged",
            messageCaptor[0].contains("apiError id")
        )
        assertTrue(messageCaptor[0].contains("errorList:"))
    }

    @Test
    fun testLoggingApiExceptionOff() = runTest {
        clearMocks(mockLogger)
        setMockServerResponse("apiError400.json", 400)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val tagCaptor = mutableListOf<String>()
        val messageCaptor = mutableListOf<String>()

        // default session is created with error logging enabled.
        val session = getSession()
        // so we turn it off after the initialization
        session.setLoggingEnabled(false)

        val exception = assertFailsWith<ApiException> {
            session.getBasicPaymentItems(paymentContext)
        }

        verify { mockLogger.e(capture(tagCaptor), capture(messageCaptor)) }

        assertNotNull(exception)
        assertFalse(session.getLoggingEnabled())
        // when the logging is turned off, the api errors should not be logged.
        // the message should be there...
        assertTrue(messageCaptor[0].contains(exception.message ?: "dummy text"))
        // ...but api error details should not
        assertFalse(messageCaptor[0].contains("apiError id"))
        assertFalse(messageCaptor[0].contains("errorList:"))
    }

    private fun getSession(): Session {
        return Session(
            "sessionId",
            "clientId",
            mockWebServer.url("/").toString(),
            "https://example.com",
            false,
            "SDKTestApp",
            true,
            mockContext
        )
    }

    private fun setMockServerResponse(jsonFile: String, responseCode: Int, delay: Long = 0L) {
        val json = GsonHelper.fromResourceJson<JsonElement>(
            jsonFile,
            JsonElement::class.java
        ).toString()

        mockWebServer.enqueue(
            MockResponse().setBody(json).setResponseCode(responseCode)
                .setBodyDelay(delay, TimeUnit.MILLISECONDS)
        )
    }
}