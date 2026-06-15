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

import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.integration.BaseMockIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Mock-backed integration tests for basic payment products filtering.
 * Tests SDK filtering logic without requiring live credentials.
 */
class BasicPaymentProductsMockIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun getBasicPaymentProducts_whenAllProductsAreFiltered_shouldThrowResponseException() = runBlocking {
        val filteredJson = MockServerHelper.loadJsonResource("basicPaymentProductsAllFiltered.json")
        val (mockSdk, mockWebServer) = MockServerHelper.createMockSdkWithResponse(context, filteredJson)

        try {
            try {
                mockSdk.getBasicPaymentProducts(paymentContext)
                fail("Should have thrown a ResponseException when all products are filtered out")
            } catch (e: ResponseException) {
                assertEquals(404, e.httpStatusCode, "Should return 404 when all products are filtered out")
                assertEquals(
                    "No payment products available.",
                    e.message,
                    "Should return the expected error message"
                )
            }
        } finally {
            mockWebServer.shutdown()
        }
    }
}
