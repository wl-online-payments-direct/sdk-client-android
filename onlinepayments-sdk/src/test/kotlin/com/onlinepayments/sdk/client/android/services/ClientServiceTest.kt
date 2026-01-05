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

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.card.Card
import com.onlinepayments.sdk.client.android.domain.card.CardSource
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.ICacheManager
import com.onlinepayments.sdk.client.android.infrastructure.utils.CacheManager
import com.onlinepayments.sdk.client.android.services.interfaces.IClientService
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import retrofit2.HttpException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ClientServiceTest {

    private lateinit var apiClient: IApiClient
    private lateinit var sessionData: SessionData
    private lateinit var clientService: IClientService
    private lateinit var paymentContext: PaymentContextWithAmount
    private lateinit var cacheManager: ICacheManager

    @BeforeTest
    fun setUp() {
        clearAllMocks()

        cacheManager = CacheManager()
        apiClient = mockk()
        sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/client/v1/",
            assetUrl = "https://assets.example.com"
        )

        paymentContext = PaymentContextWithAmount(
            amountOfMoney = AmountOfMoneyWithAmount(100L, "EUR"),
            countryCode = "NL",
            isRecurring = false
        )

        clientService = ClientService(apiClient, sessionData, cacheManager)
    }

    @Test
    fun `getIinDetails returns IinDetailsResponse successfully with SUPPORTED status`() = runTest {
        val iinResponseDto = GsonHelper.fromResourceJson(
            "iinDetailsResponse.json",
            IinDetailsResponse::class.java
        )

        coEvery {
            apiClient.getIinDetails(any(), any())
        } returns iinResponseDto

        val result = clientService.getIinDetails("411111", paymentContext)

        assertNotNull(result)
        assertNotNull(result.status)
        assertEquals(IinDetailStatus.SUPPORTED, result.status)
    }

    @Test
    fun `getIinDetails returns EXISTING_BUT_NOT_ALLOWED when not allowed in context`() = runTest {
        val iinResponseDto = mockk<IinDetailsResponse>(relaxed = true)

        every { iinResponseDto.paymentProductId } returns "1"
        every { iinResponseDto.isAllowedInContext } returns false

        coEvery {
            apiClient.getIinDetails(any(), any())
        } returns iinResponseDto

        val result = clientService.getIinDetails("411111", paymentContext)

        assertNotNull(result)
        assertEquals(IinDetailStatus.EXISTING_BUT_NOT_ALLOWED, result.status)
    }

    @Test
    fun `getIinDetails returns UNKNOWN status when paymentProductId is null`() = runTest {
        val iinResponseDto = mockk<IinDetailsResponse>(relaxed = true)

        every { iinResponseDto.paymentProductId } returns null

        coEvery {
            apiClient.getIinDetails(any(), any())
        } returns iinResponseDto

        val result = clientService.getIinDetails("411111", paymentContext)

        assertNotNull(result)
        assertEquals(IinDetailStatus.UNKNOWN, result.status)
    }

    @Test
    fun `getIinDetails returns SUPPORTED when paymentProductId exists and is allowed`() = runTest {
        val iinResponseDto = mockk<IinDetailsResponse>(relaxed = true)

        every { iinResponseDto.paymentProductId } returns "1"
        every { iinResponseDto.isAllowedInContext } returns true

        coEvery {
            apiClient.getIinDetails(any(), any())
        } returns iinResponseDto

        val result = clientService.getIinDetails("411111", paymentContext)

        assertNotNull(result)
        assertEquals(IinDetailStatus.SUPPORTED, result.status)
    }

    @Test
    fun `getIinDetails returns NOT_ENOUGH_DIGITS when input is too short`() = runTest {
        val result = clientService.getIinDetails("123", paymentContext)

        assertEquals(IinDetailStatus.NOT_ENOUGH_DIGITS, result.status)
    }

    @Test
    fun `getIinDetails returns UNKNOWN on 404 error`() = runTest {
        val httpException = mockk<HttpException> {
            every { code() } returns 404
        }

        coEvery {
            apiClient.getIinDetails(any(), any())
        } throws httpException

        val result = clientService.getIinDetails("411111", paymentContext)

        assertEquals(IinDetailStatus.UNKNOWN, result.status)
    }

    @Test
    fun `getCurrencyConversionQuote with PartialCard returns response`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "currencyConversionSuccess.json",
            CurrencyConversionResponse::class.java
        )

        coEvery {
            apiClient.getCurrencyConversionQuote(any(), any())
        } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")
        val card = Card("411111", 1)
        val cardSource = CardSource(card)

        val result = clientService.getCurrencyConversionQuote(amountOfMoney, cardSource)

        assertNotNull(result)
        assertEquals("5cd02469177743fb8a0b2c78937ee25f", result.dccSessionId)
    }

    @Test
    fun `getCurrencyConversionQuote with PartialCard without productId returns response`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "currencyConversionSuccess.json",
            CurrencyConversionResponse::class.java
        )

        coEvery {
            apiClient.getCurrencyConversionQuote(any(), any())
        } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")
        val card = Card("411111", null)
        val cardSource = CardSource(card)

        val result = clientService.getCurrencyConversionQuote(amountOfMoney, cardSource)

        assertNotNull(result)
    }

    @Test
    fun `getCurrencyConversionQuote with Token returns response`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "currencyConversionSuccess.json",
            CurrencyConversionResponse::class.java
        )

        coEvery {
            apiClient.getCurrencyConversionQuote(any(), any())
        } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")
        val cardSource = CardSource("token-123")

        val result = clientService.getCurrencyConversionQuote(amountOfMoney, cardSource)

        assertNotNull(result)
    }

    @Test
    fun `getSurchargeCalculation with PartialCard returns response`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "scWithSurcharge.json",
            SurchargeCalculationResponse::class.java
        )

        coEvery {
            apiClient.getSurchargeCalculation(any(), any())
        } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")
        val card = Card("411111", 1)
        val cardSource = CardSource(card)

        val result = clientService.getSurchargeCalculation(amountOfMoney, cardSource)

        assertNotNull(result)
    }

    @Test
    fun `getSurchargeCalculation with PartialCard without productId returns response`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "scWithSurcharge.json",
            SurchargeCalculationResponse::class.java
        )

        coEvery {
            apiClient.getSurchargeCalculation(any(), any())
        } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")
        val card = Card("411111", null)
        val cardSource = CardSource(card)

        val result = clientService.getSurchargeCalculation(amountOfMoney, cardSource)

        assertNotNull(result)
    }

    @Test
    fun `getSurchargeCalculation with Token returns response`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "scWithSurcharge.json",
            SurchargeCalculationResponse::class.java
        )

        coEvery {
            apiClient.getSurchargeCalculation(any(), any())
        } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")
        val cardSource = CardSource("token-789")

        val result = clientService.getSurchargeCalculation(amountOfMoney, cardSource)

        assertNotNull(result)
    }

    @Test
    fun `getCurrencyConversionQuote uses cache on second call with card`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "currencyConversionSuccess.json",
            CurrencyConversionResponse::class.java
        )

        coEvery { apiClient.getCurrencyConversionQuote(any(), any()) } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")
        val cardSource = CardSource(Card("411111", 1))

        val response1 = clientService.getCurrencyConversionQuote(amountOfMoney, cardSource)
        val response2 = clientService.getCurrencyConversionQuote(amountOfMoney, cardSource)

        assertNotNull(response1)
        assertNotNull(response2)

        coVerify(exactly = 1) {
            apiClient.getCurrencyConversionQuote(
                customerId = sessionData.customerId,
                request = any()
            )
        }
    }

    @Test
    fun `getCurrencyConversionQuote cache invalidates with different token`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "currencyConversionSuccess.json",
            CurrencyConversionResponse::class.java
        )

        coEvery { apiClient.getCurrencyConversionQuote(any(), any()) } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")

        val token1 = CardSource("token-123")
        val token2 = CardSource("token-456")

        clientService.getCurrencyConversionQuote(amountOfMoney, token1)
        clientService.getCurrencyConversionQuote(amountOfMoney, token2)

        coVerify(exactly = 2) {
            apiClient.getCurrencyConversionQuote(sessionData.customerId, any())
        }
    }

    @Test
    fun `getSurchargeCalculation uses cache on second call with token`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "scWithSurcharge.json",
            SurchargeCalculationResponse::class.java
        )

        coEvery { apiClient.getSurchargeCalculation(any(), any()) } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")
        val cardSource = CardSource("token-789")

        val response1 = clientService.getSurchargeCalculation(amountOfMoney, cardSource)
        val response2 = clientService.getSurchargeCalculation(amountOfMoney, cardSource)

        assertNotNull(response1)
        assertNotNull(response2)

        coVerify(exactly = 1) {
            apiClient.getSurchargeCalculation(
                customerId = sessionData.customerId,
                request = any()
            )
        }
    }

    @Test
    fun `getSurchargeCalculation cache invalidates with different card number`() = runTest {
        val responseDto = GsonHelper.fromResourceJson(
            "scWithSurcharge.json",
            SurchargeCalculationResponse::class.java
        )

        coEvery { apiClient.getSurchargeCalculation(any(), any()) } returns responseDto

        val amountOfMoney = AmountOfMoney(1000L, "EUR")

        val cardSource1 = CardSource(Card("411111", 1))
        val cardSource2 = CardSource(Card("555555", 1))

        clientService.getSurchargeCalculation(amountOfMoney, cardSource1)
        clientService.getSurchargeCalculation(amountOfMoney, cardSource2)

        coVerify(exactly = 2) {
            apiClient.getSurchargeCalculation(sessionData.customerId, any())
        }
    }

    @Test
    fun `getIinDetails uses cache on second call`() = runTest {
        val iinResponseDto = GsonHelper.fromResourceJson(
            "iinDetailsResponse.json",
            IinDetailsResponse::class.java
        )

        coEvery {
            apiClient.getIinDetails(any(), any())
        } returns iinResponseDto

        val result1 = clientService.getIinDetails("411111", paymentContext)
        val result2 = clientService.getIinDetails("411111", paymentContext)

        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(result1.status, result2.status)

        coVerify(exactly = 1) {
            apiClient.getIinDetails(sessionData.customerId, any())
        }
    }

    @Test
    fun `getIinDetails cache invalidates with different partial card`() = runTest {
        val iinResponseDto = GsonHelper.fromResourceJson(
            "iinDetailsResponse.json",
            IinDetailsResponse::class.java
        )

        coEvery {
            apiClient.getIinDetails(any(), any())
        } returns iinResponseDto

        clientService.getIinDetails("411111", paymentContext)
        clientService.getIinDetails("555555", paymentContext)

        coVerify(exactly = 2) {
            apiClient.getIinDetails(sessionData.customerId, any())
        }
    }
}