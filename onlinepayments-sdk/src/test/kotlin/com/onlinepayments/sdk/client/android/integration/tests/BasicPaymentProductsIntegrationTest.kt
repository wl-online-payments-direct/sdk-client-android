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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Integration tests for getting basic payment products.
 * Tests real API calls to the preprod environment.
 */
class BasicPaymentProductsIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `GetBasicPaymentProducts returns basic payment products for valid context`() = runBlocking {
        val result = sdk.getBasicPaymentProducts(paymentContext)

        assertNotNull(result, "Result should not be null")
        assertFalse(result.paymentProducts.isEmpty(), "Should have at least one payment product")
    }

    @Test
    fun `GetBasicPaymentProducts throws error for invalid amount`() = runBlocking {
        val invalidContext = createPaymentContext(amount = -1)

        try {
            sdk.getBasicPaymentProducts(invalidContext)
            fail("Should have thrown an exception when no payment products are available")
        } catch (e: ResponseException) {
            assertEquals(404, e.httpStatusCode, "Should return 404 when no payment products are available")
            assertEquals(
                "No payment products available.",
                e.message,
                "Should return the expected error message"
            )
        }
    }

    @Test
    fun `GetBasicPaymentProducts filters products not supported in this browser`() = runBlocking {
        val result = sdk.getBasicPaymentProducts(paymentContext)

        assertNotNull(result, "Result should not be null")

        val returnedIds = result.paymentProducts.mapNotNull { it.id }.toSet()
        val unavailableIds = Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS.toSet()
        val unsupportedReturnedIds = returnedIds.intersect(unavailableIds)

        assertTrue(
            unsupportedReturnedIds.isEmpty(),
            "Unsupported products should be filtered out. Found: $unsupportedReturnedIds"
        )
    }
}