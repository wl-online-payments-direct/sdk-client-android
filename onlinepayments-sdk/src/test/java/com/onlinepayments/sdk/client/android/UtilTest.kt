package com.onlinepayments.sdk.client.android

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.Base64
import android.view.WindowManager
import android.view.WindowMetrics
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.util.Util
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.reflect.Whitebox

/**
 * Junit Testclass which tests Util functions
 *
 * Copyright 2024 Global Collect Services B.V
 *
 */
@RunWith(MockitoJUnitRunner::class)
class UtilTest {
    companion object {
        private lateinit var mockedBase64: MockedStatic<Base64>
        private val mockContext = mock(Context::class.java)
        private val mockWindowManager = mock(WindowManager::class.java)
        private val mockWindowMetrics = mock(WindowMetrics::class.java)
        private val mockRect = mock(Rect::class.java)
        private const val appIdentifier = "APP_IDENTIFIER_UTIL_TEST"
        private const val sdkIdentifier = "UtilTestSdkIdentifier/v1.0.0"

        private const val expectedEncodedMetadata = "eyJwbGF0Zm9ybUlkZW50aWZpZXIiOiJBbmRyb2lkLzAuMC4xIiwiY" +
                "XBwSWRlbnRpZmllciI6IkFQUF9JREVOVElGSUVSX1VUSUxfVEVTVCIsInNka0lkZW50aWZpZXIiOiJVdGlsVGVzdF" +
                "Nka0lkZW50aWZpZXIvdjEuMC4wIiwic2RrQ3JlYXRvciI6Ik9ubGluZVBheW1lbnRzIiwic2NyZWVuU2l6ZSI6IjI" +
                "0MDB4MTA4MCIsImRldmljZUJyYW5kIjoiR29vZ2xlIiwiZGV2aWNlVHlwZSI6IlBpeGVsIn0"

        @BeforeClass
        @JvmStatic
        fun setup() {
            // Mock functions + properties
            `when`(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(
                mockWindowManager)
            `when`(mockWindowManager.currentWindowMetrics).thenReturn(mockWindowMetrics)
            `when`(mockWindowMetrics.bounds).thenReturn(mockRect)
            `when`(mockRect.width()).thenReturn(1080)
            `when`(mockRect.height()).thenReturn(2400)

            mockedBase64 = mockStatic(Base64::class.java)
            `when`(Base64.encode(any(), eq(Base64.URL_SAFE))).thenAnswer { invocation ->
                java.util.Base64.getMimeEncoder().encode(invocation.arguments[0] as ByteArray)
            }

            Whitebox.setInternalState(Build.VERSION::class.java, "SDK_INT", 30)
            Whitebox.setInternalState(Build.VERSION::class.java, "RELEASE", "0.0.1")
            Whitebox.setInternalState(Build::class.java, "MANUFACTURER", "Google")
            Whitebox.setInternalState(Build::class.java, "MODEL", "Pixel")
        }

        @AfterClass
        @JvmStatic
        fun close() {
            // Mocked static needs to be deregistered otherwise tests will fail when run again
            mockedBase64.close()
        }
    }

    @Test
    fun testGetMetadata() {
        val metaData = Util.getMetadata(mockContext, appIdentifier, sdkIdentifier)

        assertEquals("Pixel", metaData["deviceType"])
        assertEquals(sdkIdentifier, metaData["sdkIdentifier"])
        assertEquals("2400x1080", metaData["screenSize"])
        assertEquals(appIdentifier, metaData["appIdentifier"])
        assertEquals("OnlinePayments", metaData["sdkCreator"])
        assertEquals("Android/0.0.1", metaData["platformIdentifier"])
        assertEquals("Google", metaData["deviceBrand"])
    }

    @Test
    @PrepareForTest(Base64::class)
    fun testGetBase64EncodedMetadata() {
        val encodedMetadata = Util.getBase64EncodedMetadata(mockContext, appIdentifier, sdkIdentifier).lines().joinToString("")

        assertEquals(expectedEncodedMetadata, encodedMetadata)
    }

    @Test
    fun testGetBase64EncodedMetadataWithMetadata() {
        val metaData = Util.getMetadata(mockContext, appIdentifier, sdkIdentifier)
        val encodedMetadata = Util.getBase64EncodedMetadata(metaData).lines().joinToString("")

        assertEquals(expectedEncodedMetadata, encodedMetadata)
    }
}
