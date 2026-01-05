/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.factories

import org.junit.Assert
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import kotlin.test.Test

class TLSSocketFactoryTest {
    private val tlsSocketFactory = setupTLSSocketFactory()

    private fun setupTLSSocketFactory(): TLSSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, null, null)
        return TLSSocketFactory(sslContext.socketFactory)
    }

    // Key cipher suites that should be supported for secure TLS connections
    private val requiredCipherSuites = listOf(
        "TLS_AES_256_GCM_SHA384",
        "TLS_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
    )

    private val expectedEnabledProtocols = arrayOf("TLSv1.2", "TLSv1.3")

    @Test
    fun testDefaultCipherSuites() {
        val defaultCipherSuites = tlsSocketFactory.defaultCipherSuites
        Assert.assertNotNull("Default cipher suites should not be null", defaultCipherSuites)
        Assert.assertTrue("Default cipher suites should not be empty", defaultCipherSuites.isNotEmpty())

        // Verify that key cipher suites are present
        val actualSuites = defaultCipherSuites.toList()
        for (required in requiredCipherSuites) {
            Assert.assertTrue(
                "Default cipher suites should include $required",
                actualSuites.contains(required)
            )
        }
    }

    @Test
    fun testSupportedCipherSuites() {
        val supportedCipherSuites = tlsSocketFactory.supportedCipherSuites
        Assert.assertNotNull("Supported cipher suites should not be null", supportedCipherSuites)
        Assert.assertTrue("Supported cipher suites should not be empty", supportedCipherSuites.isNotEmpty())

        // Verify that key cipher suites are present
        val actualSuites = supportedCipherSuites.toList()
        for (required in requiredCipherSuites) {
            Assert.assertTrue(
                "Supported cipher suites should include $required",
                actualSuites.contains(required)
            )
        }
    }

    @Test
    fun testCreateSocket() {
        val socket = tlsSocketFactory.createSocket() as SSLSocket

        Assert.assertArrayEquals(expectedEnabledProtocols, socket.enabledProtocols)
    }
}