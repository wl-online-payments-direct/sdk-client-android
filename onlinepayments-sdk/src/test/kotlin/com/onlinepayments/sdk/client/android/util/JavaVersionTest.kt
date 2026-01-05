/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.util

import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertNotNull

class JavaVersionTest {
    @Test
    fun testCorrectJavaVersion() {
        val javaVersion = System.getProperty("java.version")
        assertNotNull(javaVersion)

        println("Running tests with Java version: $javaVersion")
        val majorVersion = getJavaMajorVersion(javaVersion)
        assertTrue(
            "Expected Java 17 or higher, but got: $javaVersion",
            majorVersion >= 17
        )
    }

    private fun getJavaMajorVersion(version: String): Int {
        return if (version.startsWith("1.")) {
            // For Java 8 and earlier: "1.8.0_xxx" → 8
            version.split(".")[1].toIntOrNull() ?: 0
        } else {
            // For Java 9 and newer: "17.0.1", "21", etc. → 17 or 21
            version.split(".")[0].toIntOrNull() ?: 0
        }
    }
}