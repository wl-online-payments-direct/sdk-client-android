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

import com.onlinepayments.sdk.client.android.integration.BaseMockIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Mock-backed integration tests for public key retrieval.
 */
class PublicKeyMockIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun `GetPublicKey returns cached result for repeated request`() {
        runBlocking {
            val publicKeyJson = MockServerHelper.loadJsonResource("publicKeyResponse.json")
            val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, publicKeyJson)

            try {
                val firstResult = mockSdk.getPublicKey()
                val secondResult = mockSdk.getPublicKey()

                assertNotNull(firstResult, "First public key response should not be null")
                assertNotNull(secondResult, "Second public key response should not be null")
                assertEquals(firstResult.getKeyId(), secondResult.getKeyId())
                assertEquals(
                    1,
                    server.requestCount,
                    "Public key should be requested only once because the second call should use cache"
                )
            } finally {
                server.shutdown()
            }
        }
    }
}