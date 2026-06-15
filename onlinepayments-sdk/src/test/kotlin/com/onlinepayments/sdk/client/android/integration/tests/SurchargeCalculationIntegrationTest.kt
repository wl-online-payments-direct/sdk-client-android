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
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeResult
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for getSurchargeCalculation.
 * Tests real API calls to the preprod environment.
 *
 * @todo un-skip this test suite, once the merchant has been configured to support surcharge
 */
@Ignore("Surcharge integration tests skipped: merchant is not configured to support surcharge.")
class SurchargeCalculationIntegrationTest : BaseIntegrationTest() {

    private val amountOfMoney = AmountOfMoney(1000L, "EUR")

    @Test
    fun getSurchargeCalculation_withCardAndProductId_shouldReturnSurchargeResponse() = runBlocking {
        val result = sdk.getSurchargeCalculation(
            amountOfMoney,
            TestConfig.cardNumberWithSurcharge,
            TestConfig.productIdWithSurcharge
        )

        assertNotNull(result, "Result should not be null")
        assertTrue(result.surcharges.isNotEmpty(), "Surcharges list should not be empty")
        assertEquals(
            SurchargeResult.OK,
            result.surcharges[0].result,
            "Surcharge result should be OK"
        )
        assertEquals(
            amountOfMoney.amount,
            result.surcharges[0].netAmount.amount,
            "Net amount should match the requested amount"
        )
    }

    @Test
    fun getSurchargeCalculation_withCardAndNoProductId_shouldReturnSurchargeResponse() = runBlocking {
        val result = sdk.getSurchargeCalculation(
            amountOfMoney,
            TestConfig.cardNumberWithSurcharge,
            null
        )

        assertNotNull(result, "Result should not be null")
        assertTrue(result.surcharges.isNotEmpty(), "Surcharges list should not be empty")
        assertEquals(
            SurchargeResult.OK,
            result.surcharges[0].result,
            "Surcharge result should be OK"
        )
    }

    @Test
    fun getSurchargeCalculation_withToken_shouldReturnSurchargeResponse() = runBlocking {
        val result = sdk.getSurchargeCalculation(amountOfMoney, TestConfig.cardTokenWithSurcharge)

        assertNotNull(result, "Result should not be null")
        assertTrue(result.surcharges.isNotEmpty(), "Surcharges list should not be empty")
        assertEquals(
            SurchargeResult.OK,
            result.surcharges[0].result,
            "Surcharge result should be OK"
        )
    }

    @Test
    fun getSurchargeCalculation_withNoSurchargeCardAndProductId_shouldReturnNoSurchargeResponse() = runBlocking {
        val result = sdk.getSurchargeCalculation(
            amountOfMoney,
            TestConfig.cardNumberWithoutSurcharge,
            TestConfig.productIdWithoutSurcharge
        )

        assertNotNull(result, "Result should not be null")
        assertTrue(result.surcharges.isNotEmpty(), "Surcharges list should not be empty")
        assertEquals(
            SurchargeResult.NO_SURCHARGE,
            result.surcharges[0].result,
            "Surcharge result should be NO_SURCHARGE"
        )
    }

    @Test
    fun getSurchargeCalculation_withNoSurchargeCardAndNoProductId_shouldReturnNoSurchargeResponse() = runBlocking {
        val result = sdk.getSurchargeCalculation(
            amountOfMoney,
            TestConfig.cardNumberWithoutSurcharge,
            null
        )

        assertNotNull(result, "Result should not be null")
        assertTrue(result.surcharges.isNotEmpty(), "Surcharges list should not be empty")
        assertEquals(
            SurchargeResult.NO_SURCHARGE,
            result.surcharges[0].result,
            "Surcharge result should be NO_SURCHARGE"
        )
    }

    @Test
    fun getSurchargeCalculation_calledTwice_shouldUseCacheOnSecondCall() = runBlocking {
        val cachedAmount = AmountOfMoney(1100L, "EUR")

        // First call — hits the network
        val firstStartTime = System.currentTimeMillis()
        sdk.getSurchargeCalculation(cachedAmount, TestConfig.cardTokenWithSurcharge)
        val firstCallDuration = System.currentTimeMillis() - firstStartTime

        // Second call — should be served from cache
        val secondStartTime = System.currentTimeMillis()
        val secondResult = sdk.getSurchargeCalculation(cachedAmount, TestConfig.cardTokenWithSurcharge)
        val secondCallDuration = System.currentTimeMillis() - secondStartTime

        assertNotNull(secondResult, "Cached result should not be null")
        assertTrue(firstCallDuration > secondCallDuration, "Cached call should be faster than network call")
    }
}
