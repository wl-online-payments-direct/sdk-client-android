/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.networking;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.onlinepayments.sdk.client.android.communicate.TLSSocketFactory;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

public class TLSSocketFactoryJavaTest {
    private final TLSSocketFactory tlsSocketFactory = setupTLSSocketFactory();

    private TLSSocketFactory setupTLSSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            return new TLSSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TLSSocketFactory", e);
        }
    }

    // Key cipher suites that should be supported for secure TLS connections
    private final List<String> requiredCipherSuites = Arrays.asList(
        "TLS_AES_256_GCM_SHA384",
        "TLS_AES_128_GCM_SHA256",
        "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
        "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
        "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
    );

    private final String[] expectedEnabledProtocols = {"TLSv1.2", "TLSv1.3"};

    @Test
    public void testDefaultCipherSuites() {
        String[] defaultCipherSuites = tlsSocketFactory.getDefaultCipherSuites();
        assertNotNull("Default cipher suites should not be null", defaultCipherSuites);
        assertTrue("Default cipher suites should not be empty", defaultCipherSuites.length > 0);

        // Verify that key cipher suites are present
        List<String> actualSuites = Arrays.asList(defaultCipherSuites);
        for (String required : requiredCipherSuites) {
            assertTrue("Default cipher suites should include " + required,
                actualSuites.contains(required));
        }
    }

    @Test
    public void testSupportedCipherSuites() {
        String[] supportedCipherSuites = tlsSocketFactory.getSupportedCipherSuites();
        assertNotNull("Supported cipher suites should not be null", supportedCipherSuites);
        assertTrue("Supported cipher suites should not be empty", supportedCipherSuites.length > 0);

        // Verify that key cipher suites are present
        List<String> actualSuites = Arrays.asList(supportedCipherSuites);
        for (String required : requiredCipherSuites) {
            assertTrue("Supported cipher suites should include " + required,
                actualSuites.contains(required));
        }
    }

    @Test
    public void testCreateSocket() throws Exception {
        //noinspection resource Should not throw exception
        SSLSocket socket = (SSLSocket) tlsSocketFactory.createSocket();
        assertArrayEquals(expectedEnabledProtocols, socket.getEnabledProtocols());
    }
}