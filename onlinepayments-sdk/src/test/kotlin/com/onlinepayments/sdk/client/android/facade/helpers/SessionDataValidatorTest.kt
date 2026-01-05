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
import com.onlinepayments.sdk.client.android.domain.exceptions.ConfigurationException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SessionDataValidatorTest {

    @Test
    fun `validateRequiredFields should pass for valid SessionData`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com",
            assetUrl = "https://assets.example.com"
        )

        // Should not throw
        SessionDataValidator.validateRequiredFields(sessionData)
    }

    @Test
    fun `validateRequiredFields should throw for blank customerId`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "",
            clientApiUrl = "https://api.example.com",
            assetUrl = "https://assets.example.com"
        )

        val exception = assertFailsWith<ConfigurationException> {
            SessionDataValidator.validateRequiredFields(sessionData)
        }

        assertEquals("The SessionData parameter 'customerId' is mandatory.", exception.message)
    }

    @Test
    fun `validateRequiredFields should throw for blank clientSessionId`() {
        val sessionData = SessionData(
            clientSessionId = "",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com",
            assetUrl = "https://assets.example.com"
        )

        val exception = assertFailsWith<ConfigurationException> {
            SessionDataValidator.validateRequiredFields(sessionData)
        }

        assertEquals("The SessionData parameter 'clientSessionId' is mandatory.", exception.message)
    }

    @Test
    fun `validateRequiredFields should throw for blank clientApiUrl`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "",
            assetUrl = "https://assets.example.com"
        )

        val exception = assertFailsWith<ConfigurationException> {
            SessionDataValidator.validateRequiredFields(sessionData)
        }

        assertEquals("The SessionData parameter 'clientApiUrl' is mandatory.", exception.message)
    }

    @Test
    fun `validateRequiredFields should throw for blank assetUrl`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com",
            assetUrl = ""
        )

        val exception = assertFailsWith<ConfigurationException> {
            SessionDataValidator.validateRequiredFields(sessionData)
        }

        assertEquals("The SessionData parameter 'assetUrl' is mandatory.", exception.message)
    }

    @Test
    fun `validateRequiredFields should throw for whitespace-only customerId`() {
        val sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "   ",
            clientApiUrl = "https://api.example.com",
            assetUrl = "https://assets.example.com"
        )

        val exception = assertFailsWith<ConfigurationException> {
            SessionDataValidator.validateRequiredFields(sessionData)
        }

        assertEquals("The SessionData parameter 'customerId' is mandatory.", exception.message)
    }
}
