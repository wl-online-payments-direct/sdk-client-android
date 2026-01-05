/*
 * Do not remove or alter the notices in this preamble.
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
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider
import com.onlinepayments.sdk.client.android.infrastructure.utils.GooglePayUtil
import com.onlinepayments.sdk.client.android.infrastructure.utils.Logger
import com.onlinepayments.sdk.client.android.mocks.MockContext
import com.onlinepayments.sdk.client.android.mocks.MockEncoding
import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
class OnlinePaymentsSDKTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockWebServer: MockWebServer
    private val mockContext = MockContext.setup()
    private val mockLogger = mockk<Logger>(relaxed = true)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        MockEncoding.setup()

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
        assertEquals(29, products.count())

        val resultIds = products.map { it.id }
        val filteredIds = listOf(117, 5700, 5772, 5784)

        filteredIds.forEach { filteredId ->
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

        assertNotNull(exception)
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
        val tokenRequest = GsonHelperJava.fromResourceJson(
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

        assertEquals(29, paymentProducts.paymentProducts.count())
    }

    // Error Handling Tests

    @Test
    fun testGetPaymentProductsUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentProductsForbidden() = runTest {
        setMockServerResponse("apiError400.json", 403)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentProductsNotFound() = runTest {
        setMockServerResponse("apiError400.json", 404)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentProductsServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentProductsServiceUnavailable() = runTest {
        setMockServerResponse("apiError400.json", 503)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentProductUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getPaymentProduct(1, paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentProductNotFound() = runTest {
        setMockServerResponse("apiError400.json", 404)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getPaymentProduct(1, paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetPaymentProductServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getPaymentProduct(1, paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetNetworksUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_APPLEPAY, paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetNetworksServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")
        val paymentContext = PaymentContext(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_APPLEPAY, paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetIinDetailsUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getIinDetails("411111", paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetIinDetailsServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoneyWithAmount(1298L, "EUR")
        val paymentContext = PaymentContextWithAmount(amountOfMoney, "NL", isRecurring = false)

        val exception = assertFailsWith<ResponseException> {
            getSdk().getIinDetails("411111", paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetCurrencyConversionQuoteUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getCurrencyConversionQuote(amountOfMoney, "411111", "1")
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetCurrencyConversionQuoteServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getCurrencyConversionQuote(amountOfMoney, "411111", "1")
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetSurchargeCalculationUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "411111", "1")
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetSurchargeCalculationServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "411111", "1")
        }

        assertNotNull(exception)
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

        assertNotNull(exception)
    }

    @Test
    fun testEncryptTokenRequestWithPublicKeyError() = runTest {
        val tokenRequest = GsonHelperJava.fromResourceJson(
            "creditCardTokenRequest.json",
            CreditCardTokenRequest::class.java
        )

        setMockServerResponse("apiError400.json", 500)

        val exception = assertFailsWith<ResponseException> {
            getSdk().encryptTokenRequest(tokenRequest)
        }

        assertNotNull(exception)
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

        val exception = assertFailsWith<SdkException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
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

        val exception = assertFailsWith<SdkException> {
            getSdk().getPaymentProduct(1, paymentContext)
        }

        assertNotNull(exception)
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

        val exception = assertFailsWith<SdkException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
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

        val exception = assertFailsWith<SdkException> {
            getSdk().getCurrencyConversionQuote(amountOfMoney, "411111", "1")
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetSurchargeCalculationWithMalformedResponse() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{ invalid json")
                .setResponseCode(200)
        )

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<SdkException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "411111", "1")
        }

        assertNotNull(exception)
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

        val exception = assertFailsWith<SdkException> {
            getSdk().getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_APPLEPAY, paymentContext)
        }

        assertNotNull(exception)
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

        val exception = assertFailsWith<SdkException> {
            getSdk().getBasicPaymentProducts(paymentContext)
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetCurrencyConversionQuoteTokenUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getCurrencyConversionQuote(amountOfMoney, "token-123")
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetSurchargeCalculationTokenUnauthorized() = runTest {
        setMockServerResponse("apiError400.json", 401)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "token-123")
        }

        assertNotNull(exception)
    }

    @Test
    fun testGetSurchargeCalculationTokenServerError() = runTest {
        setMockServerResponse("apiError400.json", 500)

        val amountOfMoney = AmountOfMoney(1298L, "EUR")

        val exception = assertFailsWith<ResponseException> {
            getSdk().getSurchargeCalculation(amountOfMoney, "token-123")
        }

        assertNotNull(exception)
    }

    private fun getSdk(): OnlinePaymentsSdk {
        val sessionData = SessionData(
            "sessionId",
            "clientId",
            mockWebServer.url("/").toString(),
            "https://example.com"
        )

        val config = SdkConfiguration(
            false,
            "SDKTestApp",
            "AndroidSDK",
            true
        )

        return OnlinePaymentsSdk(sessionData, mockContext, config)
    }

    private fun setMockServerResponse(jsonFile: String, responseCode: Int, delay: Long = 0L) {
        val json = GsonHelperJava.fromResourceJson(jsonFile, JsonElement::class.java)

        mockWebServer.enqueue(
            MockResponse()
                .setBody(json.toString())
                .setResponseCode(responseCode)
                .setBodyDelay(delay, TimeUnit.MILLISECONDS)
        )
    }
}