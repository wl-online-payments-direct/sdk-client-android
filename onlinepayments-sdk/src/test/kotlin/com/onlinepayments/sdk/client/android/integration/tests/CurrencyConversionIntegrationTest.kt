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

package com.onlinepayments.sdk.client.android.integration.tests

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Integration tests for getCurrencyConversionQuote.
 * Tests real API calls to the preprod environment.
 *
 * @todo un-skip this test suite, once the merchant has been configured to support currency conversion
 */
@Ignore("Currency conversion integration tests skipped: merchant is not configured to support currency conversion.")
class CurrencyConversionIntegrationTest : BaseIntegrationTest() {

    private val amountOfMoney = AmountOfMoney(1000L, "AUD")

    @Test
    fun getCurrencyConversionQuote_withCardAndProductId_shouldReturnBaseAmount() = runBlocking {
        val result = sdk.getCurrencyConversionQuote(
            amountOfMoney,
            TestConfig.cardNumberWithSurcharge,
            TestConfig.productIdWithCurrencyConversion
        )

        assertNotNull(result, "Result should not be null")
        assertEquals(
            amountOfMoney.amount,
            result.proposal.baseAmount.amount,
            "Base amount should match the requested amount"
        )
        assertEquals(
            amountOfMoney.currencyCode,
            result.proposal.baseAmount.currencyCode,
            "Base currency should match the requested currency"
        )
    }

    @Test
    fun getCurrencyConversionQuote_withCardAndNoProductId_shouldReturnBaseAmount() = runBlocking {
        val result = sdk.getCurrencyConversionQuote(
            amountOfMoney,
            TestConfig.cardNumberWithSurcharge,
            null
        )

        assertNotNull(result, "Result should not be null")
        assertEquals(
            amountOfMoney.amount,
            result.proposal.baseAmount.amount,
            "Base amount should match the requested amount"
        )
        assertEquals(
            amountOfMoney.currencyCode,
            result.proposal.baseAmount.currencyCode,
            "Base currency should match the requested currency"
        )
    }

    @Test
    fun getCurrencyConversionQuote_withToken_shouldReturnBaseAmount() = runBlocking {
        val result = sdk.getCurrencyConversionQuote(amountOfMoney, TestConfig.cardTokenWithCurrencyConversion)

        assertNotNull(result, "Result should not be null")
        assertEquals(
            amountOfMoney.amount,
            result.proposal.baseAmount.amount,
            "Base amount should match the requested amount"
        )
        assertEquals(
            amountOfMoney.currencyCode,
            result.proposal.baseAmount.currencyCode,
            "Base currency should match the requested currency"
        )
    }

    @Test
    fun getCurrencyConversionQuote_withNoConversionCardAndProductId_shouldThrowResponseError() = runBlocking {
        try {
            sdk.getCurrencyConversionQuote(
                amountOfMoney,
                TestConfig.cardNumberWithoutSurcharge,
                TestConfig.productIdWithoutCurrencyConversion
            )
            fail("Should have thrown a ResponseException for a card that does not support currency conversion")
        } catch (e: ResponseException) {
            assertNotNull(e.httpStatusCode, "Response error should have an HTTP status code")
            assertTrue(e.httpStatusCode >= 400, "Response error should have an error HTTP status code")
        }
    }

    @Test
    fun getCurrencyConversionQuote_withNoConversionCardAndNoProductId_shouldThrowResponseError() = runBlocking {
        try {
            sdk.getCurrencyConversionQuote(
                amountOfMoney,
                TestConfig.cardNumberWithoutSurcharge,
                null
            )
            fail("Should have thrown a ResponseException for a card that does not support currency conversion")
        } catch (e: ResponseException) {
            assertNotNull(e.httpStatusCode, "Response error should have an HTTP status code")
            assertTrue(e.httpStatusCode >= 400, "Response error should have an error HTTP status code")
        }
    }

    @Test
    fun getCurrencyConversionQuote_calledTwice_shouldUseCacheOnSecondCall() = runBlocking {
        val cachedAmount = AmountOfMoney(1100L, "AUD")

        val firstStartTime = System.currentTimeMillis()
        sdk.getCurrencyConversionQuote(cachedAmount, TestConfig.cardTokenWithCurrencyConversion)
        val firstCallDuration = System.currentTimeMillis() - firstStartTime

        val secondStartTime = System.currentTimeMillis()
        val secondResult = sdk.getCurrencyConversionQuote(cachedAmount, TestConfig.cardTokenWithCurrencyConversion)
        val secondCallDuration = System.currentTimeMillis() - secondStartTime

        assertNotNull(secondResult, "Cached result should not be null")
        assertEquals(
            cachedAmount.amount,
            secondResult.proposal.baseAmount.amount,
            "Cached result base amount should match the requested amount"
        )
        assertEquals(
            cachedAmount.currencyCode,
            secondResult.proposal.baseAmount.currencyCode,
            "Cached result base currency should match the requested currency"
        )
        assertTrue(firstCallDuration > secondCallDuration, "Cached call should be faster than network call")
    }
}
