/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.facade.helpers

import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import org.junit.Test
import kotlin.test.assertEquals

class SessionDataNormalizerTest {

    @Test
    fun `normalize should add trailing slash and client prefix when missing`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com",
            assetUrl = "https://assets.example.com"
        )

        val normalized = SessionDataNormalizer.normalize(sessionData)

        assertEquals("https://api.example.com/client/", normalized.clientApiUrl)
        assertEquals("session123", normalized.clientSessionId)
        assertEquals("customer456", normalized.customerId)
        assertEquals("https://assets.example.com", normalized.assetUrl)
    }

    @Test
    fun `normalize should add client prefix when URL has trailing slash but no prefix`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/",
            assetUrl = "https://assets.example.com"
        )

        val normalized = SessionDataNormalizer.normalize(sessionData)

        assertEquals("https://api.example.com/client/", normalized.clientApiUrl)
    }

    @Test
    fun `normalize should not modify URL that already has trailing slash and client prefix`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/client/",
            assetUrl = "https://assets.example.com"
        )

        val normalized = SessionDataNormalizer.normalize(sessionData)

        assertEquals("https://api.example.com/client/", normalized.clientApiUrl)
    }

    @Test
    fun `normalize should add trailing slash when URL has client prefix but no trailing slash`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/client",
            assetUrl = "https://assets.example.com"
        )

        val normalized = SessionDataNormalizer.normalize(sessionData)

        assertEquals("https://api.example.com/client/", normalized.clientApiUrl)
    }

    @Test
    fun `normalize should handle client prefix case-insensitively`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/CLIENT/",
            assetUrl = "https://assets.example.com"
        )

        val normalized = SessionDataNormalizer.normalize(sessionData)

        // Should not add duplicate prefix due to case-insensitive check
        assertEquals("https://api.example.com/CLIENT/", normalized.clientApiUrl)
    }

    @Test
    fun `normalize should handle URL with path segments`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/v1/payments",
            assetUrl = "https://assets.example.com"
        )

        val normalized = SessionDataNormalizer.normalize(sessionData)

        assertEquals("https://api.example.com/v1/payments/client/", normalized.clientApiUrl)
    }

    @Test
    fun `normalize should handle URL with query parameters`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com?test=value",
            assetUrl = "https://assets.example.com"
        )

        val normalized = SessionDataNormalizer.normalize(sessionData)

        assertEquals("https://api.example.com?test=value/client/", normalized.clientApiUrl)
    }

    @Test
    fun `normalize should preserve other SessionData fields`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com",
            assetUrl = "https://assets.example.com"
        )

        val normalized = SessionDataNormalizer.normalize(sessionData)

        assertEquals("session123", normalized.clientSessionId)
        assertEquals("customer456", normalized.customerId)
        assertEquals("https://assets.example.com", normalized.assetUrl)
    }
}
