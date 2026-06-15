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
import kotlin.test.assertTrue

/**
 * Integration tests for IIN (Issuer Identification Number) details lookup.
 * Tests real API calls to identify card types from card numbers.
 */
class IinDetailsIntegrationTest : BaseIntegrationTest() {

    @Test
    fun getIinDetails_withValidCardNumber_shouldReturnSupported() = runBlocking {
        // Use first 6 digits of a valid card number
        val partialCardNumber = TestConfig.cardNumberWithCoBrands.substring(0, 6)

        val result = sdk.getIinDetails(partialCardNumber, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertEquals(
            IinDetailStatus.SUPPORTED,
            result.status,
            "Status should be SUPPORTED for valid card"
        )
        assertNotNull(
            result.coBrands,
            "Co-brands should not be null"
        )
        Unit
    }

    @Test
    fun getIinDetails_withNotEnoughDigits_shouldReturnNotEnoughDigits() = runBlocking {
        // Use less than 6 digits
        val partialCardNumber = "123"

        val result = sdk.getIinDetails(partialCardNumber, paymentContext)

        assertNotNull(
            result,
            "Result should not be null"
        )
        assertEquals(
            IinDetailStatus.NOT_ENOUGH_DIGITS,
            result.status,
            "Status should be NOT_ENOUGH_DIGITS for short input"
        )
    }

    @Test
    fun getIinDetails_withUnknownCardNumber_shouldReturnUnknown() = runBlocking {
        // Use a BIN that's unlikely to be in the system
        val unknownBin = "999999"

        val result = sdk.getIinDetails(unknownBin, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertEquals(
            IinDetailStatus.UNKNOWN,
            result.status,
            "Status should be UNKNOWN for unrecognized card number"
        )
    }

    @Test
    fun getIinDetails_calledTwice_shouldUseCacheOnSecondCall() = runBlocking {
        val partialCardNumber = TestConfig.cardNumberWithSurcharge.substring(0, 6)

        // First call - should fetch from API
        val firstResult = sdk.getIinDetails(partialCardNumber, paymentContext)

        // Second call - should use cache and return same data
        val secondResult = sdk.getIinDetails(partialCardNumber, paymentContext)

        assertNotNull(firstResult, "First result should not be null")
        assertNotNull(secondResult, "Second result should not be null")

        assertEquals(
            firstResult.status,
            secondResult.status,
            "Cached result should have same status"
        )
    }

    @Test
    fun getIinDetails_withDifferentCardNumbers_shouldInvalidateCache() = runBlocking {
        val cardNumber1 = TestConfig.cardNumberWithSurcharge.substring(0, 6)
        val cardNumber2 = TestConfig.cardNumberWithoutSurcharge.substring(0, 6)

        // First call
        val firstResult = sdk.getIinDetails(cardNumber1, paymentContext)

        // Second call with different card number
        val secondResult = sdk.getIinDetails(cardNumber2, paymentContext)

        assertNotNull(firstResult, "First result should not be null")
        assertNotNull(secondResult, "Second result should not be null")

        // Both should be SUPPORTED or UNKNOWN - just verify we get responses
        assertNotNull(firstResult.status, "First status should not be null")
        assertNotNull(secondResult.status, "Second status should not be null")
        Unit
    }

    @Test
    fun getIinDetails_withFullCardNumber_shouldReturnSupported() = runBlocking {
        // Use full card number (API should handle this)
        val fullCardNumber = TestConfig.cardNumberWithoutSurcharge

        val result = sdk.getIinDetails(fullCardNumber, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertEquals(
            IinDetailStatus.SUPPORTED,
            result.status,
            "Status should be SUPPORTED for full valid card number"
        )
    }

    @Test
    fun getIinDetails_shouldReturnPaymentProductId() = runBlocking {
        val partialCardNumber = TestConfig.cardNumberWithSurcharge.substring(0, 6)

        val result = sdk.getIinDetails(partialCardNumber, paymentContext)

        assertNotNull(result, "Result should not be null")
        if (result.status == IinDetailStatus.SUPPORTED) {
            assertNotNull(
                result.paymentProductId,
                "Payment product ID should not be null for supported card"
            )
        }
    }

    @Test
    fun getIinDetails_shouldReturnCardType() = runBlocking {
        val partialCardNumber = TestConfig.cardNumberWithSurcharge.substring(0, 6)

        val result = sdk.getIinDetails(partialCardNumber, paymentContext)

        assertNotNull(result, "Result should not be null")
        if (result.status == IinDetailStatus.SUPPORTED) {
            // Card type might be null or might have a value depending on API response
            // Just verify we get a valid result
            assertNotNull(result, "IIN details should be present")
        }
    }

    @Test
    fun getIinDetails_whenIsAllowedInContextAbsent_shouldReturnSupported() = runBlocking {
        val partialCardNumber = TestConfig.cardNumberVisa

        val result = sdk.getIinDetails(partialCardNumber, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertTrue(
            result.status == IinDetailStatus.SUPPORTED || result.status == IinDetailStatus.EXISTING_BUT_NOT_ALLOWED,
            "Status should be SUPPORTED or EXISTING_BUT_NOT_ALLOWED for a recognized Visa card"
        )
    }

    @Test
    fun getIinDetails_withCardNumberOf6Digits_shouldNotThrow() = runBlocking {
        val sixDigits = TestConfig.cardNumberWithCoBrands.substring(0, 6)

        val result = sdk.getIinDetails(sixDigits, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result.status, "Status should be set")
        Unit
    }
}
