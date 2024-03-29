package com.onlinepayments.sdk.client.android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Junit Testclass which tests overridden PaymentProductCacheKey functions
 *
 * Copyright 2024 Global Collect Services B.V
 *
 */
class PaymentProductCacheKeyTest {
    private val paymentItemCacheKeyOne =
        PaymentProductCacheKey(100L, "NL", "EUR", false, "1")
    private val paymentItemCacheKeyOneDuplicate =
        PaymentProductCacheKey(100L, "NL", "EUR", false, "1")
    private val paymentItemCacheKeyTwo =
        PaymentProductCacheKey(100L, "NL", "EUR", false, "3")
    private val paymentItemCacheKeyThree =
        PaymentProductCacheKey(200L, "NL", "EUR", false, "1")


    @Test
    fun testEquals() {
        assertTrue(paymentItemCacheKeyOne == paymentItemCacheKeyOneDuplicate)
        assertFalse(paymentItemCacheKeyOne == paymentItemCacheKeyTwo)
        assertFalse(paymentItemCacheKeyOne == paymentItemCacheKeyThree)
        assertFalse(paymentItemCacheKeyTwo == paymentItemCacheKeyThree)
    }
    @Test
    fun testHashCode() {
        assertEquals(719683163, paymentItemCacheKeyOne.hashCode())
        assertEquals(719683225, paymentItemCacheKeyTwo.hashCode())
        assertEquals(812035263, paymentItemCacheKeyThree.hashCode())
    }
}
