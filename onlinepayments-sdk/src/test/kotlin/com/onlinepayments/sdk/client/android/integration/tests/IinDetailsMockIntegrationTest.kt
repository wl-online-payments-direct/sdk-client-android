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

package com.onlinepayments.sdk.client.android.integration.tests

import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus
import com.onlinepayments.sdk.client.android.integration.BaseMockIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Mock-backed integration tests for IIN details lookup.
 * Tests SDK mapping logic without requiring live credentials.
 */
class IinDetailsMockIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun `GetIinDetails returns existing but not allowed when card is not allowed in context`() = runBlocking {
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(
            context,
            """{"countryCode":"NL","isAllowedInContext":false,"paymentProductId":"1"}"""
        )

        try {
            val result = mockSdk.getIinDetails(TestConfig.cardNumberWithSurcharge, paymentContext)

            assertNotNull(result, "Result should not be null")
            assertEquals(
                IinDetailStatus.EXISTING_BUT_NOT_ALLOWED,
                result.status,
                "Status should be EXISTING_BUT_NOT_ALLOWED when isAllowedInContext is false"
            )
            assertEquals("1", result.paymentProductId)
            assertEquals("NL", result.countryCode)

            val request = server.takeRequest()
            assertEquals(
                true,
                request.path?.contains("getIINdetails", ignoreCase = true),
                "Request should hit the IIN details endpoint"
            )
            assertEquals("POST", request.method, "IIN endpoint should use POST")
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `Details allowed context status existing_but_not_allowed`() = runBlocking {
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(
            context,
            """{"countryCode":"NL","isAllowedInContext":false,"paymentProductId":"1"}"""
        )

        try {
            val result = mockSdk.getIinDetails("411111", paymentContext)

            assertNotNull(result, "Result should not be null")
            assertEquals(
                IinDetailStatus.EXISTING_BUT_NOT_ALLOWED,
                result.status,
                "Status should be EXISTING_BUT_NOT_ALLOWED"
            )
            assertEquals(false, result.isAllowedInContext)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `GetIinDetails returns unknown status when payment product id is absent`() = runBlocking {
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(
            context,
            """{"countryCode":"NL","isAllowedInContext":true}"""
        )

        try {
            val result = mockSdk.getIinDetails("999999", paymentContext)

            assertNotNull(result, "Result should not be null")
            assertEquals(
                IinDetailStatus.UNKNOWN,
                result.status,
                "Status should be UNKNOWN when paymentProductId is absent"
            )
        } finally {
            server.shutdown()
        }
    }
}
