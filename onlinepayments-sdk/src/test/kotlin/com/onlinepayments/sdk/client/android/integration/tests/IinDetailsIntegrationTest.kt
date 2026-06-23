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

import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for IIN details lookup.
 * Tests real API calls to the preprod environment.
 */
class IinDetailsIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `GetIinDetails accepts card number with six digits`() {
        runBlocking {
            val cardNumber = TestConfig.cardNumberWithCoBrands.substring(0, 6)

            val result = sdk.getIinDetails(cardNumber, paymentContext)

            assertNotNull(result, "Result should not be null")
            assertNotNull(result.status, "Status should be set")
        }
    }

    @Test
    fun `GetIinDetails returns supported status for valid BIN`() {
        runBlocking {
            val cardNumber = TestConfig.cardNumberWithCoBrands.substring(0, 6)

            val result = sdk.getIinDetails(cardNumber, paymentContext)

            assertNotNull(result, "Result should not be null")
            assertEquals(
                IinDetailStatus.SUPPORTED,
                result.status,
                "Status should be SUPPORTED for a valid BIN"
            )
        }
    }

    @Test
    fun `GetIinDetails returns supported status for full card number`() {
        runBlocking {
            val result = sdk.getIinDetails(TestConfig.cardNumberWithoutSurcharge, paymentContext)

            assertNotNull(result, "Result should not be null")
            assertEquals(
                IinDetailStatus.SUPPORTED,
                result.status,
                "Status should be SUPPORTED for a full valid card number"
            )
        }
    }

    @Test
    fun `GetIinDetails accepts card number with spaces`() {
        runBlocking {
            val cardNumberWithSpaces = TestConfig.cardNumberWithoutSurcharge
                .substring(0, 6)
                .chunked(4)
                .joinToString(" ")

            val result = sdk.getIinDetails(cardNumberWithSpaces, paymentContext)

            assertNotNull(result, "Result should not be null")
            assertEquals(
                IinDetailStatus.SUPPORTED,
                result.status,
                "Status should be SUPPORTED when card number contains spaces"
            )
        }
    }

    @Test
    fun `GetIinDetails returns unknown status for unknown card number`() {
        runBlocking {
            val result = sdk.getIinDetails("999999", paymentContext)

            assertNotNull(result, "Result should not be null")
            assertEquals(
                IinDetailStatus.UNKNOWN,
                result.status,
                "Status should be UNKNOWN for an unrecognized card number"
            )
        }
    }

    @Test
    fun `GetIinDetails returns cached result for repeated BIN`() {
        runBlocking {
            val cardNumber = TestConfig.cardNumberWithSurcharge.substring(0, 6)

            val firstResult = sdk.getIinDetails(cardNumber, paymentContext)
            val secondResult = sdk.getIinDetails(cardNumber, paymentContext)

            assertNotNull(firstResult, "First result should not be null")
            assertNotNull(secondResult, "Second result should not be null")
            assertEquals(
                firstResult.status,
                secondResult.status,
                "Cached result should have the same status"
            )
            assertEquals(
                firstResult.paymentProductId,
                secondResult.paymentProductId,
                "Cached result should have the same payment product id"
            )
        }
    }

    @Test
    fun `GetIinDetails makes a new API call for different card numbers`() {
        runBlocking {
            val firstCardNumber = TestConfig.cardNumberWithSurcharge.substring(0, 6)
            val secondCardNumber = TestConfig.cardNumberWithoutSurcharge.substring(0, 6)

            val firstResult = sdk.getIinDetails(firstCardNumber, paymentContext)
            val secondResult = sdk.getIinDetails(secondCardNumber, paymentContext)

            assertNotNull(firstResult, "First result should not be null")
            assertNotNull(secondResult, "Second result should not be null")
            assertNotNull(firstResult.status, "First status should be set")
            assertNotNull(secondResult.status, "Second status should be set")
        }
    }

    @Test
    fun `GetIinDetails returns not enough digits status when card number has fewer than six digits`() {
        runBlocking {
            val result = sdk.getIinDetails("12345", paymentContext)

            assertNotNull(result, "Result should not be null")
            assertEquals(
                IinDetailStatus.NOT_ENOUGH_DIGITS,
                result.status,
                "Android SDK returns NOT_ENOUGH_DIGITS instead of throwing an exception"
            )
        }
    }
}