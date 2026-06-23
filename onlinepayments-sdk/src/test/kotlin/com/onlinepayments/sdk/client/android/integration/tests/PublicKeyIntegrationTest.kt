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

import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Integration tests for getPublicKey.
 * Tests real API calls to the preprod environment.
 */
class PublicKeyIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `GetPublicKey returns public key with non-empty fields`() {
        runBlocking {
            val result = sdk.getPublicKey()

            assertNotNull(result, "Public key response should not be null")
            assertNotNull(result.getKeyId(), "Key id should not be null")
            assertFalse(result.getKeyId()!!.isEmpty(), "Key id should not be empty")
            assertNotNull(result.getPublicKey(), "Public key should be parseable and not null")
        }
    }
}