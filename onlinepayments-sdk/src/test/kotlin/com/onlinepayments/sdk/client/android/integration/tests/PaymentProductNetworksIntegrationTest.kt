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

import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Integration tests for getNetworksForPaymentProduct.
 * Tests real API calls to the preprod environment.
 */
class PaymentProductNetworksIntegrationTest : BaseIntegrationTest() {

    @Test
    fun getNetworksForPaymentProduct_withWrongProductId_shouldThrowResponseError() = runBlocking {
        try {
            sdk.getNetworksForPaymentProduct(1, paymentContext)
            fail("Should have thrown a ResponseException for a product that does not support networks")
        } catch (e: ResponseException) {
            assertNotNull(e.httpStatusCode, "Response error should have an HTTP status code")
            assertTrue(
                e.httpStatusCode >= 400,
                "HTTP status should be a 4xx or 5xx error, got: ${e.httpStatusCode}"
            )
        }
    }

    @Test
    fun getNetworksForPaymentProduct_withValidProductId_shouldReturnNetworksList() = runBlocking {
        val result = sdk.getNetworksForPaymentProduct(Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result.networks, "Networks list should not be null")
        assertTrue(
            result.networks.isNotEmpty(),
            "Networks list should be non-empty for Google Pay"
        )
    }

    @Test
    fun getNetworksForPaymentProduct_calledTwice_shouldUseCacheOnSecondCall() = runBlocking {
        val productId = Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY

        val firstStartTime = System.currentTimeMillis()
        val firstResult = sdk.getNetworksForPaymentProduct(productId, paymentContext)
        val firstCallDuration = System.currentTimeMillis() - firstStartTime

        val secondStartTime = System.currentTimeMillis()
        val secondResult = sdk.getNetworksForPaymentProduct(productId, paymentContext)
        val secondCallDuration = System.currentTimeMillis() - secondStartTime

        assertNotNull(firstResult, "First result should not be null")
        assertNotNull(secondResult, "Second result should not be null")
        assertEquals(firstResult.networks, secondResult.networks, "Both results should have the same networks")
        assertTrue(firstCallDuration > secondCallDuration, "Cached call should be faster than network call")
    }
}
