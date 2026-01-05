/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.integration.tests

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

        return@runBlocking
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

        return@runBlocking
    }

    @Test
    fun getBasicPaymentProducts_withInvalidAmount_shouldReturnEmpty() = runBlocking {
        val invalidContext = createPaymentContext(amount = -1)

        val response = sdk.getBasicPaymentProducts(invalidContext)
        assertEquals(0, response.paymentProducts.size)

        return@runBlocking
    }

    @Test
    fun getBasicPaymentProducts_calledTwice_shouldUseCacheOnSecondCall() = runBlocking {
        // First call - should fetch from API
        val firstResult = sdk.getBasicPaymentProducts(paymentContext)
        val firstCallTime = System.currentTimeMillis()

        // Second call - should use cache
        val secondResult = sdk.getBasicPaymentProducts(paymentContext)
        val secondCallTime = System.currentTimeMillis()

        assertNotNull(firstResult, "First result should not be null")
        assertNotNull(secondResult, "Second result should not be null")

        // Cached call should be significantly faster
        assertTrue(
            (secondCallTime - firstCallTime) < 100, // Less than 100ms for cached call
            "Second call should be faster (cached)"
        )

        assertEquals(
            firstResult.paymentProducts.size,
            secondResult.paymentProducts.size,
            "Results should have same number of products"
        )

        return@runBlocking
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

        return@runBlocking
    }

    @Test
    fun getPaymentProduct_shouldHaveDisplayHints() = runBlocking {
        val productId = TestConfig.productIdVisa

        val result = sdk.getPaymentProduct(productId, paymentContext)

        assertNotNull(result, "Result should not be null")
        assertNotNull(result.logo, "Result should have logo")

        return@runBlocking
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

        return@runBlocking
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

        return@runBlocking
    }
}
