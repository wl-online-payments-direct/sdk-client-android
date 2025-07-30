/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.Base64;
import android.view.WindowManager;
import android.view.WindowMetrics;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.util.Map;

/**
 * Junit Test class which tests Util functions
 */
@RunWith(MockitoJUnitRunner.class)
public class UtilJavaTest {
    private static MockedStatic<Base64> mockedBase64;
    private static final Context mockContext = Mockito.mock(Context.class);
    private static final WindowManager mockWindowManager = Mockito.mock(WindowManager.class);
    private static final WindowMetrics mockWindowMetrics = Mockito.mock(WindowMetrics.class);
    private static final Rect mockRect = Mockito.mock(Rect.class);
    private static final String appIdentifier = "APP_IDENTIFIER_UTIL_TEST";
    private static final String sdkIdentifier = "UtilTestSdkIdentifier/v1.0.0";

    private static final String expectedEncodedMetadata = "eyJwbGF0Zm9ybUlkZW50aWZpZXIiOiJBbmRyb2lkLzAuMC4xIiwiY" +
            "XBwSWRlbnRpZmllciI6IkFQUF9JREVOVElGSUVSX1VUSUxfVEVTVCIsInNka0lkZW50aWZpZXIiOiJVdGlsVGVzdF" +
            "Nka0lkZW50aWZpZXIvdjEuMC4wIiwic2RrQ3JlYXRvciI6Ik9ubGluZVBheW1lbnRzIiwic2NyZWVuU2l6ZSI6IjI" +
            "0MDB4MTA4MCIsImRldmljZUJyYW5kIjoiR29vZ2xlIiwiZGV2aWNlVHlwZSI6IlBpeGVsIn0";

    @BeforeClass
    public static void setup() {
        // Mock functions + properties
        Mockito.when(mockContext.getSystemService(Context.WINDOW_SERVICE)).thenReturn(
                mockWindowManager);
        Mockito.when(mockWindowManager.getCurrentWindowMetrics())
                .thenReturn(mockWindowMetrics); // Note the change here.
        Mockito.when(mockWindowMetrics.getBounds()).thenReturn(mockRect); // Note the change here.
        Mockito.when(mockRect.width()).thenReturn(1080);
        Mockito.when(mockRect.height()).thenReturn(2400);

        mockedBase64 = Mockito.mockStatic(Base64.class);
        Mockito.when(Base64.encode(
                ArgumentMatchers.any(byte[].class),
                ArgumentMatchers.eq(Base64.URL_SAFE)
        )).thenAnswer(invocation ->
                java.util.Base64.getMimeEncoder().encode((byte[]) invocation.getArgument(0))
        );

        Whitebox.setInternalState(Build.VERSION.class, "SDK_INT", 30);
        Whitebox.setInternalState(Build.VERSION.class, "RELEASE", "0.0.1");
        Whitebox.setInternalState(Build.class, "MANUFACTURER", "Google");
        Whitebox.setInternalState(Build.class, "MODEL", "Pixel");
    }

    @AfterClass
    public static void close() {
        // Mocked static needs to be deregistered otherwise tests will fail when run again
        mockedBase64.close();
    }

    @Test
    public void testGetMetadata() {
        Map<String, String> metaData = Util.INSTANCE.getMetadata(
                mockContext,
                appIdentifier,
                sdkIdentifier
        );

        Assert.assertEquals("Pixel", metaData.get("deviceType"));
        Assert.assertEquals(sdkIdentifier, metaData.get("sdkIdentifier"));
        Assert.assertEquals("2400x1080", metaData.get("screenSize"));
        Assert.assertEquals(appIdentifier, metaData.get("appIdentifier"));
        Assert.assertEquals("OnlinePayments", metaData.get("sdkCreator"));
        Assert.assertEquals("Android/0.0.1", metaData.get("platformIdentifier"));
        Assert.assertEquals("Google", metaData.get("deviceBrand"));
    }

    @Test
    @PrepareForTest(Base64.class)
    public void testGetBase64EncodedMetadata() {
        String encodedMetadata = Util.INSTANCE.getBase64EncodedMetadata(
                mockContext,
                appIdentifier,
                sdkIdentifier
        ).lines().collect(java.util.stream.Collectors.joining("")); // Java 8+ way to join lines

        Assert.assertEquals(expectedEncodedMetadata, encodedMetadata);
    }

    @Test
    public void testGetBase64EncodedMetadataWithMetadata() {
        Map<String, String> metaData = Util.INSTANCE.getMetadata(
                mockContext,
                appIdentifier,
                sdkIdentifier
        );
        String encodedMetadata = Util.INSTANCE.getBase64EncodedMetadata(metaData)
                .lines()
                .collect(java.util.stream.Collectors.joining(""));

        Assert.assertEquals(expectedEncodedMetadata, encodedMetadata);
    }
}
