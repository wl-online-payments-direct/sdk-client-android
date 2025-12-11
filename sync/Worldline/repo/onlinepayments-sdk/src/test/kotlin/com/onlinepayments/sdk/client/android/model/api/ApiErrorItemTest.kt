/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.api

import android.util.Base64
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.core.classloader.annotations.PrepareForTest

/**
 * Junit Test class which tests deserialization of [ApiErrorItem]
 */
@RunWith(MockitoJUnitRunner::class)
class ApiErrorItemTest {
    @Test
    fun testDeserializingWithAllProperties() {
        val apiErrorItem = GsonHelper.fromResourceJson(
            "apiErrorItemComplete.json",
            ApiErrorItem::class.java
        )

        assertEquals("123456", apiErrorItem.errorCode)
        assertEquals("PAYMENT_PLATFORM_ERROR", apiErrorItem.category)
        assertEquals(404, apiErrorItem.httpStatusCode)
        assertEquals("1", apiErrorItem.id)
        assertEquals("The product could not be found", apiErrorItem.message)
        assertFalse(apiErrorItem.isRetriable())
    }

    @Test
    @PrepareForTest(Base64::class)
    fun testDeserializingWithMissingOptionalProperties() {
        val apiErrorItem = GsonHelper.fromResourceJson(
            "apiErrorItemMissingOptionalProperties.json",
            ApiErrorItem::class.java
        )

        assertEquals("123456", apiErrorItem.errorCode)
        assertNull(apiErrorItem.category)
        assertNull(apiErrorItem.httpStatusCode)
        assertNull(apiErrorItem.id)
        assertEquals("This error does not contain a message", apiErrorItem.message)
        assertTrue(apiErrorItem.isRetriable())
    }
}
