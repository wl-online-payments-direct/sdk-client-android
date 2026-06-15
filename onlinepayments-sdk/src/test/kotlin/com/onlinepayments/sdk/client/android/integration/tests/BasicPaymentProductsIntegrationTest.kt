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
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
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
    fun getBasicPaymentProducts_shouldReturnBasicPaymentProducts() = runBlocking {
        val result = sdk.getBasicPaymentProducts(paymentContext)

        assertNotNull(result, "Result should not be null")
        assertFalse(
            result.paymentProducts.isEmpty(),
            "Should have at least one payment product"
        )
    }

    @Test
    fun getPaymentProduct_shouldReturnPaymentProduct() = runBlocking {
        val productId = TestConfig.productIdVisa

        val result = sdk.getPaymentProduct(productId, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertEquals(
            productId,
            result.id,
            "Product ID should match requested ID"
        )
        assertFalse(
            result.fields.isEmpty(),
            "Payment product should have fields"
        )
    }

    @Test
    fun getBasicPaymentProducts_withInvalidAmount_shouldThrowResponseException() = runBlocking {
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
    fun getBasicPaymentProducts_calledTwice_shouldUseCacheOnSecondCall() = runBlocking {
        // First call - should fetch from API
        val firstResult = sdk.getBasicPaymentProducts(paymentContext)

        // Second call - should hit cache and return same data
        val secondResult = sdk.getBasicPaymentProducts(paymentContext)

        assertNotNull(firstResult, "First result should not be null")
        assertNotNull(secondResult, "Second result should not be null")

        assertEquals(
            firstResult.paymentProducts.size,
            secondResult.paymentProducts.size,
            "Cached result should have the same number of products"
        )
        assertEquals(
            firstResult.paymentProducts.map { it.id },
            secondResult.paymentProducts.map { it.id },
            "Cached result should contain the same product IDs"
        )
    }

    @Test
    fun getBasicPaymentProducts_withDifferentContext_shouldInvalidateCache() = runBlocking {
        // First call with EUR
        val firstResult = sdk.getBasicPaymentProducts(paymentContext)

        // Second call with USD - different context should invalidate cache
        val usdContext = createPaymentContext(amount = 1000, currencyCode = "USD")
        val secondResult = sdk.getBasicPaymentProducts(usdContext)

        assertNotNull(firstResult, "First result should not be null")
        assertNotNull(secondResult, "Second result should not be null")
        Unit
    }

    @Test
    fun getPaymentProduct_shouldHaveDisplayHints() = runBlocking {
        val productId = TestConfig.productIdVisa

        val result = sdk.getPaymentProduct(productId, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result.logo, "Result should have logo")
        Unit
    }

    @Test
    fun getPaymentProduct_shouldHavePaymentProductFields() = runBlocking {
        val productId = TestConfig.productIdVisa

        val result = sdk.getPaymentProduct(productId, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertFalse(result.fields.isEmpty(), "Should have payment product fields")

        // Verify card products have expected fields
        val fieldIds = result.fields.map { it.id }
        assertTrue(
            fieldIds.contains("cardNumber"),
            "Card product should have cardNumber field"
        )
    }

    @Test
    fun getPaymentProduct_nonExistentProduct_shouldThrowException() = runBlocking {
        val nonExistentProductId = 99999

        try {
            sdk.getPaymentProduct(nonExistentProductId, paymentContext)
            fail("Should have thrown an exception for non-existent product")
        } catch (e: ResponseException) {
            // Expected - server returns 500 for non-existent products
            assertEquals(500, e.httpStatusCode, "Should return error for non-existent product")
        }
    }

    @Test
    fun getBasicPaymentProducts_whenSdkUnsupportedProductsReturned_shouldFilterThemOut() = runBlocking {
        val result = sdk.getBasicPaymentProducts(paymentContext)

        assertNotNull(result, "Result should not be null")

        val returnedIds = result.paymentProducts.mapNotNull { it.id }.toSet()
        val unavailableIds = Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS.toSet()
        val intersection = returnedIds.intersect(unavailableIds)

        assertTrue(
            intersection.isEmpty(),
            "SDK-unsupported products should be filtered out. Found: $intersection"
        )
    }
}
