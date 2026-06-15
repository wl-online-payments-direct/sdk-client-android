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

import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Integration tests for getPaymentProduct.
 * Tests real API calls to the preprod environment.
 */
class PaymentProductIntegrationTest : BaseIntegrationTest() {

    @Test
    fun getPaymentProduct_withSdkUnsupportedProductId_shouldThrowResponseError() = runBlocking {
        assertTrue(
            Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS.isNotEmpty(),
            "UNAVAILABLE_PAYMENT_PRODUCT_IDS must not be empty — otherwise this test is vacuous"
        )
        for (unsupportedId in Constants.UNAVAILABLE_PAYMENT_PRODUCT_IDS) {
            val exception = assertFailsWith<ResponseException> {
                sdk.getPaymentProduct(unsupportedId, paymentContext)
            }

            assertEquals(
                Constants.NOT_FOUND_ERROR,
                exception.httpStatusCode,
                "Should return 404 for SDK-unsupported product ID: $unsupportedId"
            )
        }
    }
}
