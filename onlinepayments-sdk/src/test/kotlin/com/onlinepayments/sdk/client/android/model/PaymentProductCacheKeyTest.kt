/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Junit Test class which tests overridden PaymentProductCacheKey functions
 */
class PaymentProductCacheKeyTest {
    private val paymentItemCacheKeyOne = PaymentProductCacheKey(100L, "NL", "EUR", false, "1")
    private val paymentItemCacheKeyOneDuplicate = PaymentProductCacheKey(100L, "NL", "EUR", false, "1")
    private val paymentItemCacheKeyTwo = PaymentProductCacheKey(100L, "NL", "EUR", false, "3")
    private val paymentItemCacheKeyThree = PaymentProductCacheKey(200L, "NL", "EUR", false, "1")

    @Test
    fun testEquals() {
        assertTrue(paymentItemCacheKeyOne == paymentItemCacheKeyOneDuplicate)
        assertFalse(paymentItemCacheKeyOne == paymentItemCacheKeyTwo)
        assertFalse(paymentItemCacheKeyOne == paymentItemCacheKeyThree)
        assertFalse(paymentItemCacheKeyTwo == paymentItemCacheKeyThree)
    }

    @Test
    fun testHashCode() {
        assertEquals(233023236, paymentItemCacheKeyOne.hashCode())
        assertEquals(233023238, paymentItemCacheKeyTwo.hashCode())
        assertEquals(325375336, paymentItemCacheKeyThree.hashCode())
    }
}
