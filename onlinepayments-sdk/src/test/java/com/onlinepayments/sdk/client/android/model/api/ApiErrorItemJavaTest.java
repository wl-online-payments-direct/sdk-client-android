/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.api;

import android.util.Base64;

import com.onlinepayments.sdk.client.android.testUtil.GsonHelperJava;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.Objects;

/**
 * Junit Test class which tests deserialization of [ApiErrorItem]
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiErrorItemJavaTest {
    
    @Test
    public void testDeserializingWithAllProperties() {
        ApiErrorItem apiErrorItem = GsonHelperJava.fromResourceJson(
            "apiErrorItemComplete.json",
            ApiErrorItem.class
        );

        Assert.assertEquals("123456", apiErrorItem.getErrorCode());
        Assert.assertEquals("PAYMENT_PLATFORM_ERROR", apiErrorItem.getCategory());
        Assert.assertEquals(
            404,
            Objects.requireNonNull(apiErrorItem.getHttpStatusCode()).intValue()
        );
        Assert.assertEquals("1", apiErrorItem.getId());
        Assert.assertEquals("The product could not be found", apiErrorItem.getMessage());
        Assert.assertFalse(apiErrorItem.getRetriable());
    }

    @Test
    @PrepareForTest(Base64.class)
    public void testDeserializingWithMissingOptionalProperties() {
        ApiErrorItem apiErrorItem = GsonHelperJava.fromResourceJson(
            "apiErrorItemMissingOptionalProperties.json",
            ApiErrorItem.class
        );

        Assert.assertEquals("123456", apiErrorItem.getErrorCode());
        Assert.assertNull(apiErrorItem.getCategory());
        Assert.assertNull(apiErrorItem.getHttpStatusCode());
        Assert.assertNull(apiErrorItem.getId());
        Assert.assertEquals("This error does not contain a message", apiErrorItem.getMessage());
        Assert.assertTrue(apiErrorItem.getRetriable());
    }
}