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

package com.onlinepayments.sdk.client.android.facade

import com.google.gson.JsonElement
import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount
import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.currencyConversion.ConversionResultType
import com.onlinepayments.sdk.client.android.domain.exceptions.IllegalStateSdkException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse
import com.onlinepayments.sdk.client.android.facade.helpers.SessionDataNormalizer
import com.onlinepayments.sdk.client.android.facade.listeners.BasicPaymentProductsResponseListener
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentProductResponseListener
import com.onlinepayments.sdk.client.android.facade.listeners.PublicKeyResponseListener
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IServiceFactory
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider
import com.onlinepayments.sdk.client.android.infrastructure.utils.GooglePayUtil
import com.onlinepayments.sdk.client.android.infrastructure.utils.Logger
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class OnlinePaymentsSDKTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockWebServer: MockWebServer
    private val mockContext = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val mockLogger = mockk<Logger>(relaxed = true)
    private lateinit var originalMainDispatcher: CoroutineDispatcher

    companion object {
        private val FILTERED_PRODUCT_IDS = listOf(117, 5700, 5772, 5784)
    }

    @BeforeTest
    fun setup() {
        originalMainDispatcher = OnlinePaymentsSdk.mainDispatcher
        Dispatchers.setMain(testDispatcher)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockkObject(GooglePayUtil)
        every { GooglePayUtil.isGooglePayAllowed(any(), any(), any()) } returns false
        coEvery { GooglePayUtil.isGooglePayAllowed(any(), any(), any()) } returns false

        LoggerProvider.logger = mockLogger
        OnlinePaymentsSdk.mainDispatcher = testDispatcher
    }

    @AfterTest
    fun tearDown() {
        try {
            mockWebServer.shutdown()
            mockWebServer.close()
        } catch (_: Exception) {
            // Ignore shutdown errors
        }
        Dispatchers.resetMain()
        OnlinePaymentsSdk.mainDispatcher = originalMainDispatcher
        LoggerProvider.reset()
        unmockkAll()
    }

    @Test
    fun testGetPaymentProducts() = runTest {
        setMockServerResponse("paymentProducts.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)
        val paymentProducts = getSdk().getBasicPaymentProducts(paymentContext)

        val products = paymentProducts.paymentProducts
        assertEquals(30, products.count())

        val resultIds = products.map { it.id }

        FILTERED_PRODUCT_IDS.forEach { filteredId ->
            assertTrue(
                !resultIds.contains(filteredId),
                "Expected product with id=$filteredId to be filtered out, but it was found."
            )
        }
    }

    @Test
    fun testGetPaymentProductsWithError() = runTest {
        setMockServerResponse("apiError400.json", 400)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertEquals(400, exception.httpStatusCode)
    }

    @Test
    fun testGetPaymentProduct() = runTest {
        setMockServerResponse("paymentProductVisa.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val paymentProduct = getSdk().getPaymentProduct(1, paymentContext)

        assertNotNull(paymentProduct)
    }

    @Test
    fun testGetPaymentProductFromCache() = runTest {
        setMockServerResponse("paymentProductVisa.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val sdk = getSdk()
        val paymentProduct = sdk.getPaymentProduct(1, paymentContext)

        setMockServerResponse("apiError400.json", 404)

        val cachedProduct = sdk.getPaymentProduct(1, paymentContext)
        assertEquals(paymentProduct, cachedProduct)

        assertFailsWith<ResponseException> {
            sdk.getPaymentProduct(2, paymentContext)
        }
    }

    @Test
    fun testGetPaymentProductNetworks() = runTest {
        setMockServerResponse("paymentProductNetworks.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val networks = getSdk().getNetworksForPaymentProduct(
            Constants.PAYMENT_PRODUCT_ID_APPLEPAY,
            paymentContext
        )

        assertEquals(3, networks.networks?.count())
    }

    @Test
    fun testGetIinDetails() = runTest {
        setMockServerResponse("normalIINResponseVisa.json", 200)

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val sdk = getSdk()

        var iinDetails = sdk.getIinDetails("4141", paymentContext)
        assertEquals(IinDetailStatus.NOT_ENOUGH_DIGITS, iinDetails.status)
        assertNull(iinDetails.paymentProductId)

        iinDetails = sdk.getIinDetails("414141", paymentContext)
        assertEquals(IinDetailStatus.SUPPORTED, iinDetails.status)
        assertEquals("1", iinDetails.paymentProductId)
    }

    @Test
    fun testGetIinDetailsNotFound() = runTest {
        setMockServerResponse("iinDetailsNotFound.json", 404)

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val details = getSdk().getIinDetails("411111", paymentContext)

        assertEquals(IinDetailStatus.UNKNOWN, details.status)
    }

    @Test
    fun testGetIinDetailsAlreadyInProgress() = runTest {
        setMockServerResponse("normalIINResponseVisa.json", 200, 1000)

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val sdk = getSdk()

        launch {
            sdk.getIinDetails("411111", paymentContext)
        }

        advanceUntilIdle()

        val exception = assertFailsWith<IllegalStateSdkException> {
            sdk.getIinDetails("411111", paymentContext)
        }

        assertEquals("IIN lookup is already in progress", exception.message)
    }

    @Test
    fun testGetPublicKey() = runTest {
        setMockServerResponse("publicKeyResponse.json", 200)

        val publicKey = getSdk().getPublicKey()

        assertNotNull(publicKey)
        assertNotNull(publicKey.getPublicKey())
        assertEquals("X.509", publicKey.getPublicKey()?.format)
        assertEquals("12345678-aaaa-bbbb-cccc-876543218765", publicKey.getKeyId())
    }

    @Test
    fun testGetPublicKeyBadRequest() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("{}").setResponseCode(200))

        val publicKey = getSdk().getPublicKey()

        assertNull(publicKey.getPublicKey())
    }

    @Test
    fun testCreatePaymentRequest() = runTest {
        setMockServerResponse("cardPaymentProduct.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val paymentProduct = getSdk().getPaymentProduct(1, paymentContext)
        assertNotNull(paymentProduct)

        val paymentRequest = PaymentRequest(paymentProduct, null, false)
        paymentRequest.setValue("cardNumber", "7822551678890142249")
        paymentRequest.setValue("expiryDate", "122030")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("cardholderName", "John Doe")

        setMockServerResponse("publicKeyResponse.json", 200)

        val preparedRequest = getSdk().encryptPaymentRequest(paymentRequest)

        assertNotNull(preparedRequest.encryptedCustomerInput)
        assertNotNull(preparedRequest.encodedClientMetaInfo)
    }

    @Test
    fun testCreateTokenPaymentRequest() = runTest {
        val tokenRequest = GsonHelper.fromResourceJson(
            "creditCardTokenRequest.json",
            CreditCardTokenRequest::class.java
        )

        setMockServerResponse("publicKeyResponse.json", 200)

        val preparedTokenRequest = getSdk().encryptTokenRequest(tokenRequest)

        assertNotNull(preparedTokenRequest.encryptedCustomerInput)
        assertNotNull(preparedTokenRequest.encodedClientMetaInfo)
    }

    @Test
    fun testGetCurrencyConversionQuoteForCard() = runTest {
        setMockServerResponse("currencyConversionSuccess.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote = getSdk().getCurrencyConversionQuote(amountOfMoney, "411111", "1")

        assertEquals(ConversionResultType.ALLOWED, quote.result.result)
        assertNotNull(quote.proposal.rate)
    }

    @Test
    fun testGetCurrencyConversionQuoteForCardNoRate() = runTest {
        setMockServerResponse("currencyConversionNoRate.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote = getSdk().getCurrencyConversionQuote(amountOfMoney, "411111", "1")

        assertEquals(ConversionResultType.NO_RATE, quote.result.result)
        assertNull(quote.proposal.rate)
    }

    @Test
    fun testGetSurchargeCalculationForCard() = runTest {
        setMockServerResponse("scWithSurcharge.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote = getSdk().getSurchargeCalculation(amountOfMoney, "411111", "1")

        assertEquals(1, quote.surcharges.count())
    }

    @Test
    fun testGetSurchargeCalculationForToken() = runTest {
        setMockServerResponse("scWithSurcharge.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val quote = getSdk().getSurchargeCalculation(amountOfMoney, "token-789")

        assertEquals(1, quote.surcharges.count())
    }

    @Test
    fun testSyncMethods() {
        setMockServerResponse("paymentProducts.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val paymentProducts = getSdk().getBasicPaymentProductsSync(paymentContext)

        assertEquals(30, paymentProducts.paymentProducts.count())
    }

    @Test
    fun testGetPaymentProductsUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertEquals(401, exception.httpStatusCode)
    }

    @Test
    fun testGetPaymentProductsForbidden() = runTest {
        setMockServerResponse("apiError400.json", 403)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertEquals(403, exception.httpStatusCode)
    }

    @Test
    fun testGetPaymentProductsNotFound() = runTest {
        setMockServerResponse("apiError400.json", 404)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertEquals(404, exception.httpStatusCode)
    }

    @Test
    fun testGetPaymentProductsServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertEquals(500, exception.httpStatusCode)
    }

    @Test
    fun testGetPaymentProductsServiceUnavailable() = runTest {
        setMockServerResponse("apiError400.json", 503)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertEquals(503, exception.httpStatusCode)
    }

    @Test
    fun testGetPaymentProductUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getPaymentProduct(1, paymentContext)
        }

        assertEquals(401, exception.httpStatusCode)
    }

    @Test
    fun testGetPaymentProductNotFound() = runTest {
        setMockServerResponse("apiError400.json", 404)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getPaymentProduct(1, paymentContext)
        }

        assertEquals(404, exception.httpStatusCode)
    }

    @Test
    fun testGetPaymentProductServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getPaymentProduct(1, paymentContext)
        }

        assertEquals(500, exception.httpStatusCode)
    }

    @Test
    fun testGetNetworksUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_APPLEPAY, paymentContext)
        }

        assertEquals(401, exception.httpStatusCode)
    }

    @Test
    fun testGetNetworksServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_APPLEPAY, paymentContext)
        }

        assertEquals(500, exception.httpStatusCode)
    }

    @Test
    fun testGetIinDetailsUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getIinDetails("411111", paymentContext)
        }

        assertEquals(401, exception.httpStatusCode)
    }

    @Test
    fun testGetIinDetailsServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getIinDetails("411111", paymentContext)
        }

        assertEquals(500, exception.httpStatusCode)
    }

    @Test
    fun testGetCurrencyConversionQuoteUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getCurrencyConversionQuote(amountOfMoney, "411111", "1")
        }

        assertEquals(401, exception.httpStatusCode)
    }

    @Test
    fun testGetCurrencyConversionQuoteServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getCurrencyConversionQuote(amountOfMoney, "411111", "1")
        }

        assertEquals(500, exception.httpStatusCode)
    }

    @Test
    fun testGetSurchargeCalculationUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "411111", "1")
        }

        assertEquals(401, exception.httpStatusCode)
    }

    @Test
    fun testGetSurchargeCalculationServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "411111", "1")
        }

        assertEquals(500, exception.httpStatusCode)
    }

    @Test
    fun testEncryptPaymentRequestWithPublicKeyError() = runTest {
        setMockServerResponse("cardPaymentProduct.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val paymentProduct = getSdk().getPaymentProduct(1, paymentContext)
        val paymentRequest = PaymentRequest(paymentProduct, null, false)
        paymentRequest.setValue("cardNumber", "7822551678890142249")
        paymentRequest.setValue("expiryDate", "122030")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("cardholderName", "John Doe")

        setMockServerResponse("apiError400.json", 500)

        val exception = assertFailsWith<ResponseException> {
            getSdk().encryptPaymentRequest(paymentRequest)
        }

        assertEquals(500, exception.httpStatusCode)
    }

    @Test
    fun testEncryptTokenRequestWithPublicKeyError() = runTest {
        val tokenRequest = GsonHelper.fromResourceJson(
            "creditCardTokenRequest.json",
            CreditCardTokenRequest::class.java
        )

        setMockServerResponse("apiError400.json", 500)

        val exception = assertFailsWith<ResponseException> {
            getSdk().encryptTokenRequest(tokenRequest)
        }

        assertEquals(500, exception.httpStatusCode)
    }

    // Network Failure Tests

    @Test
    fun testGetPaymentProductsWithMalformedJson() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{ invalid json")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        assertFailsWith<SdkException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }
    }

    @Test
    fun testGetPaymentProductWithMalformedJson() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{ invalid json")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        assertFailsWith<SdkException> {
            getSdk().getPaymentProduct(1, paymentContext)
        }
    }

    @Test
    fun testGetPaymentProductsWithEmptyResponse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        assertFailsWith<SdkException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }
    }

    @Test
    fun testGetIinDetailsWithMalformedResponse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{}")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val details = getSdk().getIinDetails("411111", paymentContext)

        assertEquals(IinDetailStatus.UNKNOWN, details.status)
    }

    @Test
    fun testGetIinDetailsWith404Response() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{}")
                .setResponseCode(404)
        )

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val details = getSdk().getIinDetails("411111", paymentContext)

        assertEquals(IinDetailStatus.UNKNOWN, details.status)
    }

    @Test
    fun testGetCurrencyConversionQuoteWithMalformedResponse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{ invalid json")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        assertFailsWith<SdkException> {
            getSdk().getCurrencyConversionQuote(amountOfMoney, "411111", "1")
        }
    }

    @Test
    fun testGetSurchargeCalculationWithMalformedResponse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{ invalid json")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        assertFailsWith<SdkException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "411111", "1")
        }
    }

    @Test
    fun testGetNetworksWithEmptyResponse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        assertFailsWith<SdkException> {
            getSdk().getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_APPLEPAY, paymentContext)
        }
    }

    // Additional Edge Case Tests

    @Test
    fun testGetPaymentProductsWithPartiallyCorruptedData() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("""{"paymentProducts": [{"id": "invalid"}]}""")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        assertFailsWith<SdkException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }
    }

    @Test
    fun testGetCurrencyConversionQuoteTokenUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getCurrencyConversionQuote(amountOfMoney, "token-123")
        }

        assertEquals(401, exception.httpStatusCode)
    }

    @Test
    fun testGetSurchargeCalculationTokenUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "token-123")
        }

        assertEquals(401, exception.httpStatusCode)
    }

    @Test
    fun testGetSurchargeCalculationTokenServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "token-123")
        }

        assertEquals(500, exception.httpStatusCode)
    }

    @Test
    fun testConstructorCreatesInstanceWithSessionData() {
        val sdk = OnlinePaymentsSdk(getSessionData(), mockContext, null)

        assertNotNull(sdk)
    }

    @Test
    fun testConstructorCreatesInstanceWhenFactoryIsProvided() {
        val mockFactory = mockk<IServiceFactory>(relaxed = true)

        val sdk = OnlinePaymentsSdk(
            getSessionData(),
            mockContext,
            null,
            CoroutineScope(SupervisorJob() + Dispatchers.IO),
            mockFactory
        )

        assertNotNull(sdk)
    }

    @Test
    fun testConstructorCreatesInstanceWithConfiguration() {
        val config = SdkConfiguration(
            environmentIsProduction = false,
            appIdentifier = "TestApp/1.0",
            sdkIdentifier = "AndroidSDK/2.5"
        )

        val sdk = OnlinePaymentsSdk(getSessionData(), mockContext, config)

        assertNotNull(sdk)
    }

    @Test
    fun testConstructorCreatesServicesWithDefaultFactory() = runTest {
        setMockServerResponse("publicKeyResponse.json", 200)

        val sdk = OnlinePaymentsSdk(
            getSessionData(),
            mockContext,
            SdkConfiguration(false, "TestApp", "AndroidSDK"),
            CoroutineScope(SupervisorJob() + Dispatchers.IO),
            null
        )

        val publicKey = sdk.getPublicKey()

        assertNotNull(publicKey)
    }

    @Test
    fun testConstructorNormalizesSessionData() {
        mockkObject(SessionDataNormalizer)

        var capturedSessionData: SessionData? = null

        every { SessionDataNormalizer.normalize(any()) } answers {
            capturedSessionData = firstArg()
            callOriginal()
        }

        val sessionData = getSessionData()

        OnlinePaymentsSdk(sessionData, mockContext, null)

        assertEquals(sessionData, capturedSessionData)
    }

    // Listener API Tests (p4d): verify callback-based API methods deliver results via onSuccess.
    // These tests use Dispatchers.Unconfined so the ServiceCallWrapper delivers callbacks eagerly
    // on whichever thread the HTTP response arrives on (matching OnlinePaymentsSdkListenersJavaTest).

    @Test
    fun testGetBasicPaymentProductsWithListener_invokesOnSuccess() {
        OnlinePaymentsSdk.mainDispatcher = Dispatchers.Unconfined
        setMockServerResponse("paymentProducts.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val latch = java.util.concurrent.CountDownLatch(1)
        var result: BasicPaymentProducts? = null
        var error: SdkException? = null

        getSdk().getBasicPaymentProducts(paymentContext, object : BasicPaymentProductsResponseListener {
            override fun onSuccess(response: BasicPaymentProducts) {
                result = response
                latch.countDown()
            }
            override fun onFailure(exception: SdkException) {
                error = exception
                latch.countDown()
            }
        })

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Listener should be called within 5 seconds")
        assertNull(error, "Expected no error but got: $error")

        val actualResult = assertNotNull(result)
        assertTrue(actualResult.paymentProducts.isNotEmpty())
    }

    @Test
    fun testGetPaymentProductWithListener_invokesOnSuccess() {
        OnlinePaymentsSdk.mainDispatcher = Dispatchers.Unconfined
        setMockServerResponse("cardPaymentProduct.json", 200)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val latch = java.util.concurrent.CountDownLatch(1)
        var result: PaymentProduct? = null
        var error: SdkException? = null

        getSdk().getPaymentProduct(1, paymentContext, object : PaymentProductResponseListener {
            override fun onSuccess(response: PaymentProduct?) {
                result = response
                latch.countDown()
            }
            override fun onFailure(exception: SdkException) {
                error = exception
                latch.countDown()
            }
        })

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Listener should be called within 5 seconds")
        assertNull(error, "Expected no error but got: $error")

        val actualResult = assertNotNull(result)
        assertEquals(1, actualResult.id)
    }

    @Test
    fun testGetPublicKeyWithListener_invokesOnSuccess() {
        OnlinePaymentsSdk.mainDispatcher = Dispatchers.Unconfined
        setMockServerResponse("publicKeyResponse.json", 200)

        val latch = java.util.concurrent.CountDownLatch(1)
        var result: PublicKeyResponse? = null
        var error: SdkException? = null

        getSdk().getPublicKey(object : PublicKeyResponseListener {
            override fun onSuccess(response: PublicKeyResponse) {
                result = response
                latch.countDown()
            }
            override fun onFailure(exception: SdkException) {
                error = exception
                latch.countDown()
            }
        })

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Listener should be called within 5 seconds")
        assertNull(error, "Expected no error but got: $error")

        val actualResult = assertNotNull(result)
        assertEquals("12345678-aaaa-bbbb-cccc-876543218765", actualResult.getKeyId())
    }

    private fun getSessionData(): SessionData {
        return SessionData(
            clientSessionId = "sessionId",
            customerId = "clientId",
            clientApiUrl = mockWebServer.url("/").toString(),
            assetUrl = "https://example.com"
        )
    }

    private fun getSdk(): OnlinePaymentsSdk {
        val config = SdkConfiguration(
            false,
            "SDKTestApp",
            "AndroidSDK",
            true
        )

        return OnlinePaymentsSdk(getSessionData(), mockContext, config)
    }

    private fun setMockServerResponse(jsonFile: String, responseCode: Int, delay: Long = 0L) {
        val json = GsonHelper.fromResourceJson(jsonFile, JsonElement::class.java)

        mockWebServer.enqueue(
            MockResponse()
                .setBody(json.toString())
                .setResponseCode(responseCode)
                .setBodyDelay(delay, TimeUnit.MILLISECONDS)
        )
    }
}