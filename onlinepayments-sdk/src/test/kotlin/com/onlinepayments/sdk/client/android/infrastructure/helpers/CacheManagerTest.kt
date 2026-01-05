/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.helpers

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.infrastructure.utils.CacheManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CacheManagerTest {

    private lateinit var cacheManager: CacheManager

    @BeforeTest
    fun setUp() {
        cacheManager = CacheManager()
    }

    @Test
    fun `createCacheKeyFromContext creates key with all context fields`() {
        val context = PaymentContext(
            amountOfMoney = AmountOfMoney(1000L, "USD"),
            countryCode = "US",
        )

        val cacheKey = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context,
            suffix = "visa"
        )

        assertEquals("paymentProduct-1000_US_false_USD_visa", cacheKey)
    }

    @Test
    fun `createCacheKeyFromContext creates key without suffix`() {

        val context = PaymentContext(
            amountOfMoney = AmountOfMoney(2500L, "EUR"),
            countryCode = "NL",
            isRecurring = true
        )

        val cacheKey = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context,
            suffix = null
        )

        assertEquals("paymentProduct-2500_NL_true_EUR", cacheKey)
    }

    @Test
    fun `createCacheKeyFromContext handles null amount`() {
        val context = PaymentContext(
            amountOfMoney = AmountOfMoney(currencyCode = "EUR"),
            countryCode = "US",
        )

        val cacheKey = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context,
            suffix = "test"
        )

        assertEquals("paymentProduct-US_false_EUR_test", cacheKey)
    }

    @Test
    fun `hasCache returns false for non-existent key`() {
        val result = cacheManager.hasCache("non-existent-key")

        assertFalse(result)
    }

    @Test
    fun `hasCache returns true for existing key`() {
        cacheManager.set("test-key", "test-value")

        val result = cacheManager.hasCache("test-key")

        assertTrue(result)
    }

    @Test
    fun `set and get stores and retrieves string value`() {
        val key = "string-key"
        val value = "test-string"

        cacheManager.set(key, value)
        val result = cacheManager.get<String>(key)

        assertEquals(value, result)
    }

    @Test
    fun `set and get stores and retrieves complex object`() {
        data class TestObject(val id: String, val name: String)

        val key = "object-key"
        val value = TestObject("123", "Test")

        cacheManager.set(key, value)
        val result = cacheManager.get<TestObject>(key)

        assertNotNull(result)
        assertEquals("123", result.id)
        assertEquals("Test", result.name)
    }

    @Test
    fun `get returns null for non-existent key`() {
        val result = cacheManager.get<String>("non-existent")

        assertNull(result)
    }

    @Test
    fun `set overwrites existing value`() {
        val key = "overwrite-key"
        cacheManager.set(key, "original-value")

        cacheManager.set(key, "new-value")
        val result = cacheManager.get<String>(key)

        assertEquals("new-value", result)
    }

    @Test
    fun `clear removes all cached items`() {
        cacheManager.set("key1", "value1")
        cacheManager.set("key2", "value2")
        cacheManager.set("key3", "value3")

        cacheManager.clear()

        assertFalse(cacheManager.hasCache("key1"))
        assertFalse(cacheManager.hasCache("key2"))
        assertFalse(cacheManager.hasCache("key3"))
        assertNull(cacheManager.get<String>("key1"))
    }

    @Test
    fun `createCacheKeyFromContext returns same key for identical payment contexts`() {
        val context1 = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 1000L,
                currencyCode = "USD"
            ),
            countryCode = "US",
            isRecurring = false
        )

        val context2 = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 1000L,
                currencyCode = "USD"
            ),
            countryCode = "US",
            isRecurring = false
        )

        val cacheKey1 = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context1,
            suffix = "visa"
        )

        val cacheKey2 = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context2,
            suffix = "visa"
        )


        assertEquals(cacheKey1, cacheKey2)
        assertEquals("paymentProduct-1000_US_false_USD_visa", cacheKey1)
    }

    @Test
    fun `identical payment contexts use same cache entry`() {

        val context1 = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 1000L,
                currencyCode = "USD"
            ),
            countryCode = "US",
            isRecurring = false
        )

        val context2 = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 1000L,
                currencyCode = "USD"
            ),
            countryCode = "US",
            isRecurring = false
        )

        val testValue = "cached-product-data"

        val cacheKey1 = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context1
        )
        cacheManager.set(cacheKey1, testValue)

        val cacheKey2 = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context2
        )
        val retrievedValue = cacheManager.get<String>(cacheKey2)

        assertEquals(testValue, retrievedValue)
        assertEquals(cacheKey1, cacheKey2)
    }

    @Test
    fun `different payment contexts create different cache keys`() {
        val context1 = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 1000L,
                currencyCode = "USD"
            ),
            countryCode = "US",
            isRecurring = false
        )

        val context2 = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 2000L, // Different amount
                currencyCode = "USD"
            ),
            countryCode = "US",
            isRecurring = false
        )

        val cacheKey1 = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context1
        )

        val cacheKey2 = cacheManager.createCacheKeyFromContext(
            prefix = "paymentProduct",
            context = context2
        )

        assertNotEquals(cacheKey1, cacheKey2)
    }

    @Test
    fun `setting value with one context retrieves same value with identical context`() {
        data class PaymentProduct(val id: String, val name: String)

        val context1 = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 5000L,
                currencyCode = "EUR"
            ),
            countryCode = "NL",
            isRecurring = true
        )

        val context2 = PaymentContext(
            amountOfMoney = AmountOfMoney(
                amount = 5000L,
                currencyCode = "EUR"
            ),
            countryCode = "NL",
            isRecurring = true
        )

        val product = PaymentProduct("123", "Visa")

        val key1 = cacheManager.createCacheKeyFromContext(
            prefix = "product",
            context = context1,
            suffix = "visa"
        )
        cacheManager.set(key1, product)

        val key2 = cacheManager.createCacheKeyFromContext(
            prefix = "product",
            context = context2,
            suffix = "visa"
        )
        val retrievedProduct = cacheManager.get<PaymentProduct>(key2)

        assertNotNull(retrievedProduct)
        assertEquals(product.id, retrievedProduct.id)
        assertEquals(product.name, retrievedProduct.name)
        assertEquals(key1, key2)
        assertTrue(cacheManager.hasCache(key2))
    }
}