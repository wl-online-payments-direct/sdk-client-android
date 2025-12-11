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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JavaVersionJavaTest {

    @Test
    public void testCorrectJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        assertNotNull(javaVersion);

        System.out.println("Running with Java version: " + javaVersion);

        // Extract the major version number
        int majorVersion = parseJavaMajorVersion(javaVersion);

        // Fail the test if version is less than 17
        assertTrue("Expected Java 17 or higher, but got: " + javaVersion, majorVersion >= 17);
    }

    private int parseJavaMajorVersion(String version) {
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2, 3)); // e.g., 1.8 → 8
        } else {
            int dotIndex = version.indexOf(".");
            return Integer.parseInt(dotIndex > 0 ? version.substring(0, dotIndex) : version);
        }
    }
}
