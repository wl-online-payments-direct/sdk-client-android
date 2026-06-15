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

package com.onlinepayments.sdk.client.android.util

import android.os.Build
import com.onlinepayments.sdk.client.android.infrastructure.encryption.MetadataUtil
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Junit Test class which tests Util functions
 */
@RunWith(RobolectricTestRunner::class)
class UtilTest {
    companion object {
        private const val APP_IDENTIFIER = "APP_IDENTIFIER_UTIL_TEST"
        private const val SDK_IDENTIFIER = "UtilTestSdkIdentifier/v1.0.0"
    }

    private lateinit var mockContext: android.content.Context

    @BeforeTest
    fun setup() {
        mockContext = ApplicationProvider.getApplicationContext()

        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", 30)
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "RELEASE", "0.0.1")
        ReflectionHelpers.setStaticField(Build::class.java, "MANUFACTURER", "Google")
        ReflectionHelpers.setStaticField(Build::class.java, "MODEL", "Pixel")
    }

    @Test
    fun testGetMetadata() {
        val metaData = MetadataUtil.getMetadata(mockContext, APP_IDENTIFIER, SDK_IDENTIFIER)

        assertEquals("Pixel", metaData["deviceType"])
        assertEquals(SDK_IDENTIFIER, metaData["sdkIdentifier"])
        assertEquals(APP_IDENTIFIER, metaData["appIdentifier"])
        assertEquals("OnlinePayments", metaData["sdkCreator"])
        assertEquals("Android/0.0.1", metaData["platformIdentifier"])
        assertEquals("Google", metaData["deviceBrand"])
        // Screen size is device-config-dependent; verify it is in "HxW" format
        assertTrue(metaData["screenSize"]!!.matches(Regex("\\d+x\\d+")), "screenSize must be in HxW format")
    }

    @Test
    fun testGetBase64EncodedMetadata() {
        val encodedMetadata = MetadataUtil.getBase64EncodedMetadata(
            mockContext,
            APP_IDENTIFIER,
            SDK_IDENTIFIER
        ).lines().joinToString("")

        // Must be valid base64url: non-empty, no standard base64 characters, no padding
        assertTrue(encodedMetadata.isNotEmpty())
        assertFalse(encodedMetadata.contains('+'), "base64url must not contain '+'")
        assertFalse(encodedMetadata.contains('/'), "base64url must not contain '/'")
        assertFalse(encodedMetadata.contains('='), "base64url must not contain padding '='")

        // Decoded JSON must contain the expected metadata fields
        val decoded = String(java.util.Base64.getUrlDecoder().decode(encodedMetadata))
        assertTrue(decoded.contains(APP_IDENTIFIER))
        assertTrue(decoded.contains(SDK_IDENTIFIER))
        assertTrue(decoded.contains("OnlinePayments"))
        assertTrue(decoded.contains("Android/0.0.1"))
        assertTrue(decoded.contains("Google"))
        assertTrue(decoded.contains("Pixel"))
    }

    @Test
    fun testGetBase64EncodedMetadataWithMetadata() {
        val metaData = MetadataUtil.getMetadata(mockContext, APP_IDENTIFIER, SDK_IDENTIFIER)
        val encodedViaMap = MetadataUtil.getBase64EncodedMetadata(metaData).lines().joinToString("")
        val encodedViaContext = MetadataUtil.getBase64EncodedMetadata(mockContext, APP_IDENTIFIER, SDK_IDENTIFIER).lines().joinToString("")

        // Both overloads must produce identical output for the same metadata
        assertEquals(encodedViaContext, encodedViaMap)
    }
}