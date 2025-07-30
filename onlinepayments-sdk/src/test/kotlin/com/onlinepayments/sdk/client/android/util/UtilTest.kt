/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.util

import android.os.Build
import android.util.Base64
import com.onlinepayments.sdk.client.android.mocks.MockContext
import com.onlinepayments.sdk.client.android.mocks.MockEncoding
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.reflect.Whitebox

/**
 * Junit Test class which tests Util functions
 */
@RunWith(MockitoJUnitRunner::class)
class UtilTest {
    companion object {
        private val mockContext = MockContext.setup()

        private const val APP_IDENTIFIER = "APP_IDENTIFIER_UTIL_TEST"
        private const val SDK_IDENTIFIER = "UtilTestSdkIdentifier/v1.0.0"
        private const val EXPECTED_ENCODED_METADATA = "eyJwbGF0Zm9ybUlkZW50aWZpZXIiOiJBbmRyb2lkLz" +
                "AuMC4xIiwiYXBwSWRlbnRpZmllciI6IkFQUF9JREVOVElGSUVSX1VUSUxfVEVTVCIsInNka0lkZW50aW" +
                "ZpZXIiOiJVdGlsVGVzdFNka0lkZW50aWZpZXIvdjEuMC4wIiwic2RrQ3JlYXRvciI6Ik9ubGluZVBheW" +
                "1lbnRzIiwic2NyZWVuU2l6ZSI6IjI0MDB4MTA4MCIsImRldmljZUJyYW5kIjoiR29vZ2xlIiwiZGV2aW" +
                "NlVHlwZSI6IlBpeGVsIn0"
    }

    @Before
    fun setup() {
        MockEncoding.setup()

        Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 30)
        Whitebox.setInternalState(Build.VERSION::class.java, "RELEASE", "0.0.1")
        Whitebox.setInternalState(Build::class.java, "MANUFACTURER", "Google")
        Whitebox.setInternalState(Build::class.java, "MODEL", "Pixel")
    }

    @After
    fun close() {
        // Cleanup MockK mocks
        unmockkAll()
    }

    @Test
    fun testGetMetadata() {
        val metaData = Util.getMetadata(mockContext, APP_IDENTIFIER, SDK_IDENTIFIER)

        assertEquals("Pixel", metaData["deviceType"])
        assertEquals(SDK_IDENTIFIER, metaData["sdkIdentifier"])
        assertEquals("2400x1080", metaData["screenSize"])
        assertEquals(APP_IDENTIFIER, metaData["appIdentifier"])
        assertEquals("OnlinePayments", metaData["sdkCreator"])
        assertEquals("Android/0.0.1", metaData["platformIdentifier"])
        assertEquals("Google", metaData["deviceBrand"])
    }

    @Test
    @PrepareForTest(Base64::class)
    fun testGetBase64EncodedMetadata() {
        val encodedMetadata = Util.getBase64EncodedMetadata(
            mockContext,
            APP_IDENTIFIER,
            SDK_IDENTIFIER
        ).lines().joinToString("")

        assertEquals(EXPECTED_ENCODED_METADATA, encodedMetadata)
    }

    @Test
    fun testGetBase64EncodedMetadataWithMetadata() {
        val metaData = Util.getMetadata(mockContext, APP_IDENTIFIER, SDK_IDENTIFIER)
        val encodedMetadata = Util.getBase64EncodedMetadata(metaData).lines().joinToString("")

        assertEquals(EXPECTED_ENCODED_METADATA, encodedMetadata)
    }
}
