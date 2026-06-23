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

import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.ServerApiHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for server API helper functionality.
 * Tests session creation and signed server API requests.
 */
class ServerApiIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `Server Api session`() {
        val sessionData = ServerApiHelper.getCachedSession(forceRefresh = true)

        assertFalse(sessionData.clientSessionId.isEmpty(), "Client session id should not be empty")
        assertFalse(sessionData.customerId.isEmpty(), "Customer id should not be empty")
        assertFalse(sessionData.clientApiUrl.isEmpty(), "Client API URL should not be empty")
        assertFalse(sessionData.assetUrl.isEmpty(), "Asset URL should not be empty")
    }

    @Test
    fun `Server Api signature generation`() = runBlocking {
        val paymentProduct = sdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
        val paymentRequest = PaymentRequest(paymentProduct, null, false)

        paymentRequest.setValue("cardNumber", TestConfig.cardNumberWithoutSurcharge)
        paymentRequest.setValue("cardholderName", "Test Cardholder")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("expiryDate", getValidExpiryDate(paymentProduct))

        val encryptedRequest = sdk.encryptPaymentRequest(paymentRequest)

        assertNotNull(encryptedRequest.encryptedCustomerInput, "Encrypted customer input should not be null")
        assertFalse(
            encryptedRequest.encryptedCustomerInput.isEmpty(),
            "Encrypted customer input should not be empty"
        )

        val paymentResponse = ServerApiHelper.createPayment(encryptedRequest.encryptedCustomerInput)

        assertNotNull(paymentResponse, "Payment response should not be null")
        assertTrue(
            paymentResponse.has("payment"),
            "Signed server API request should create a payment response"
        )
    }

    private fun getValidExpiryDate(
        paymentProduct: com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
    ): String {
        val maskedValue = paymentProduct.getField("expiryDate")!!.applyMask("122030")
        return if (maskedValue?.length == 5) "1230" else "122030"
    }
}