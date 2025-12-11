/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * Junit Test class which tests overridden PaymentProductCacheKey functions
 */
public class PaymentProductCacheKeyJavaTest {

    private final PaymentProductCacheKey paymentItemCacheKeyOne =
        new PaymentProductCacheKey(100L, "NL", "EUR", false, "1");
    private final PaymentProductCacheKey paymentItemCacheKeyOneDuplicate =
        new PaymentProductCacheKey(100L, "NL", "EUR", false, "1");
    private final PaymentProductCacheKey paymentItemCacheKeyTwo =
        new PaymentProductCacheKey(100L, "NL", "EUR", false, "3");
    private final PaymentProductCacheKey paymentItemCacheKeyThree =
        new PaymentProductCacheKey(200L, "NL", "EUR", false, "1");

    @Test
    public void testEquals() {
        Assert.assertEquals(paymentItemCacheKeyOne, paymentItemCacheKeyOneDuplicate);
        Assert.assertNotEquals(paymentItemCacheKeyOne, paymentItemCacheKeyTwo);
        Assert.assertNotEquals(paymentItemCacheKeyOne, paymentItemCacheKeyThree);
        Assert.assertNotEquals(paymentItemCacheKeyTwo, paymentItemCacheKeyThree);
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(233023236, paymentItemCacheKeyOne.hashCode());
        Assert.assertEquals(233023238, paymentItemCacheKeyTwo.hashCode());
        Assert.assertEquals(325375336, paymentItemCacheKeyThree.hashCode());
    }
}