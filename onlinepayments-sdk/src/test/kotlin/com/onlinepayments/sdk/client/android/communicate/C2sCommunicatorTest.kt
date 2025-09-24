/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.communicate

import android.os.Looper
import android.util.Base64
import com.google.gson.JsonElement
import com.onlinepayments.sdk.client.android.exception.ApiException
import com.onlinepayments.sdk.client.android.exception.CommunicationException
import com.onlinepayments.sdk.client.android.mocks.MockContext
import com.onlinepayments.sdk.client.android.mocks.MockEncoding
import com.onlinepayments.sdk.client.android.model.AmountOfMoney
import com.onlinepayments.sdk.client.android.model.PaymentContext
import com.onlinepayments.sdk.client.android.model.iin.IinStatus
import com.onlinepayments.sdk.client.android.providers.LoggerProvider
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import com.onlinepayments.sdk.client.android.util.GooglePayUtil
import com.onlinepayments.sdk.client.android.util.Logger
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import retrofit2.HttpException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
@PrepareForTest(Base64::class)
class C2sCommunicatorTest {

    companion object {
        private const val CUSTOMER_ID = "452a87d14c004b19b32e1d30958aad06"
    }

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockWebServer: MockWebServer

    private val mockContext = MockContext.setup()
    private val mockLogger = mockk<Logger>(relaxed = true)

    private var communicator: C2sCommunicator? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        LoggerProvider.logger = mockLogger
        MockEncoding.setup()

        mockkObject(GooglePayUtil)
        every { GooglePayUtil.isGooglePayAllowed(any(), any(), any()) } returns false
        coEvery { GooglePayUtil.isGooglePayAllowed(any(), any(), any()) } returns false

        mockkStatic(Looper::class)
        val mockLooper = mockk<Looper>(relaxed = true)
        every { Looper.getMainLooper() } returns mockLooper
        coEvery { Looper.getMainLooper() } returns mockLooper

        communicator = C2sCommunicator(
            C2sCommunicatorConfiguration(
                "87f140991c7a4e4096da8e40a6672544",
                CUSTOMER_ID,
                //"https://payment.preprod.direct.ingenico.com",
                mockWebServer.url("/").toString(),
                "https://assets.test.cdn.v-psp.com/s2s/50cb7a6a83a1d2a51950",
                false,
                "SDKTestApp",
                "SDKTest",
            ),
            true,
            mockContext
        )
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
        LoggerProvider.reset()
        unmockkAll()
    }

    @Test
    fun testSuccessGetRequest() = runTest {
        setMockServerResponse("paymentProducts.json", 200)

        val paymentProducts = communicator?.getBasicPaymentProducts(getPaymentContext())
        assertNotNull(paymentProducts)
        assertEquals(29, paymentProducts.getBasicPaymentProducts().size)

        val recordedRequest = mockWebServer.takeRequest()
        val expectedPath =
            "/client/v1/$CUSTOMER_ID/products?countryCode=NL&amount=1298&isRecurring=false&currencyCode=EUR&hide=fields&cacheBuster="
        assertTrue(recordedRequest.path?.startsWith(expectedPath) == true)
    }

    @Test
    fun testFailGetRequest() = runTest {
        setMockServerResponse("apiError400.json", 400)

        val exception = assertFailsWith<ApiException> {
            communicator?.getBasicPaymentProducts(getPaymentContext())
        }

        assertNotNull(exception.errorResponse?.apiError)
    }

    @Test
    fun testFailGetRequestWith403() = runTest {
        mockWebServer.enqueue(
            MockResponse().setBody("Forbidden").setResponseCode(403)
        )

        val exception = assertFailsWith<CommunicationException> {
            communicator?.getBasicPaymentProducts(getPaymentContext())
        }

        assertNotNull(exception.cause)
        assertEquals(403, (exception.cause as HttpException).code())
    }

    @Test
    fun testSuccessPostRequest() = runTest {
        setMockServerResponse("normalIINResponseVisa.json", 200)

        val iinDetails = communicator!!.getIinDetails("411111", getPaymentContext())
        assertNotNull(iinDetails)
        assertEquals(IinStatus.SUPPORTED, iinDetails.status)
    }

    @Test
    fun testFailPostRequestWith400() = runTest {
        setMockServerResponse("apiError400.json", 400)

        val exception = assertFailsWith<ApiException> {
            communicator!!.getIinDetails("411111", getPaymentContext())
        }

        assertNotNull(exception.errorResponse?.apiError)
    }

    @Test
    fun testFailPostRequestWith500() = runTest {
        mockWebServer.enqueue(
            MockResponse().setBody("Internal Server Error").setResponseCode(500)
        )

        val exception = assertFailsWith<CommunicationException> {
            communicator!!.getIinDetails("411111", getPaymentContext())
        }

        assertNotNull(exception.cause)
        assertEquals(500, (exception.cause as HttpException).code())
    }

    private fun getPaymentContext(): PaymentContext {
        return PaymentContext(AmountOfMoney(1298L, "EUR"), "NL", false)
    }

    private fun setMockServerResponse(jsonFile: String, responseCode: Int) {
        val json = GsonHelper.fromResourceJson<JsonElement>(
            jsonFile,
            JsonElement::class.java
        ).toString()

        mockWebServer.enqueue(
            MockResponse().setBody(json).setResponseCode(responseCode)
        )
    }

    @Test
    fun testFilteredPaymentProductIdsThrowApiException() = runTest {
        val paymentContext = getPaymentContext()

        val filteredIds = listOf("117", "5700", "5772", "5784")

        for (productId in filteredIds) {
            val exception = assertFailsWith<ApiException> {
                communicator!!.getPaymentProduct(productId, paymentContext)
            }

            assertEquals("Product with id $productId not found.", exception.message)
        }
    }
}
