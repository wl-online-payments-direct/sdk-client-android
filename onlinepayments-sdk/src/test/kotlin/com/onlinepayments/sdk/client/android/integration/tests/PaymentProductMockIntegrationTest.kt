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
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Mock-backed integration tests for payment product retrieval.
 * Tests SDK mapping logic without requiring live credentials.
 */
class PaymentProductMockIntegrationTest : BaseMockIntegrationTest() {

    @Test
    fun getPaymentProduct_withValidProductId_shouldContainAccountsOnFile() = runBlocking {
        val json = MockServerHelper.loadJsonResource("paymentProductVisa.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, json)

        try {
            val result = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)

            assertNotNull(result, "Result should not be null")
            assertTrue(
                result.accountsOnFile.isNotEmpty(),
                "Accounts on file should be mapped from the API response"
            )
        } finally {
            server.shutdown()
        }
    }
}
