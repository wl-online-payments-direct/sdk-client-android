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
 * Junit Testclass which tests deserialization of [ApiErrorItem]
 *
 * Copyright 2024 Global Collect Services B.V
 *
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
        assertEquals("123456", apiErrorItem.code)
        assertEquals(404, apiErrorItem.httpStatusCode)
        assertEquals("1", apiErrorItem.id)
        assertEquals("The product could not be found", apiErrorItem.message)
        assertFalse(apiErrorItem.isRetriable)
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
        assertEquals("This error does not contain a code", apiErrorItem.code)
        assertNull(apiErrorItem.httpStatusCode)
        assertNull(apiErrorItem.id)
        assertEquals("This error does not contain a message", apiErrorItem.message)
        assertTrue(apiErrorItem.isRetriable)
    }
}
