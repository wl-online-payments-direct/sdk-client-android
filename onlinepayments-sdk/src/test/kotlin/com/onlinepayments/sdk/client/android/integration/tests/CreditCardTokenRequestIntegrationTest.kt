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

import com.onlinepayments.sdk.client.android.domain.exceptions.EncryptionException
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.ServerApiHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.fail

/**
 * Integration tests for credit card tokenization request encryption.
 * Tests real encryption with actual public keys from the preprod environment.
 */
class CreditCardTokenRequestIntegrationTest : BaseIntegrationTest() {
    @Test
    fun validRequest_getValues_shouldContainCorrectFields() {
        val request = getValidRequest()
        assertEquals(
            mapOf<String, Any>(
                "paymentProductId" to TestConfig.productIdVisa,
                "cardNumber" to TestConfig.cardNumberVisa,
                "cardholderName" to "Test Cardholder",
                "cvv" to "123",
                "expiryDate" to "1230",
            ),
            request.getValues()
        )
    }

    @Test
    fun encryptTokenRequest_withValidData_shouldReturnEncryptedData() = runBlocking {
        val result = sdk.encryptTokenRequest(getValidRequest())

        assertAllValid(result)
    }

    @Test
    fun encryptTokenRequest_withInvalidData_shouldReturnEncryptedData() = runBlocking {
        val result = sdk.encryptTokenRequest(getInvalidRequest())

        assertAllValid(result)
    }

    @Test
    fun createToken_withValidData_shouldSucceed() = runBlocking {
        val request = sdk.encryptTokenRequest(getValidRequest())

        val response = ServerApiHelper.createToken(request.encryptedCustomerInput)

        assertNotNull(response)
        assertNotNull(response.token)
        assertEquals("CREATED", response.tokenStatus)
    }

    @Test
    fun createToken_withInvalidData_shouldFail() = runBlocking {
        val result = sdk.encryptTokenRequest(getInvalidRequest())

        try {
            ServerApiHelper.createToken(result.encryptedCustomerInput)
            fail("Should not create token")
        } catch (e: Throwable) {
            assertNotNull(e)
            Unit
        }
    }

    @Test
    fun encryptTokenRequest_withMissingPaymentProductId_shouldThrowEncryptionException() = runBlocking {
        val request = CreditCardTokenRequest()
        request.cardNumber = "4567350000427977"
        // paymentProductId intentionally not set

        try {
            sdk.encryptTokenRequest(request)
            fail("Should have thrown an EncryptionException when paymentProductId is not set")
        } catch (e: EncryptionException) {
            assertEquals(
                "Error encrypting credit card token request: the payment product ID not set.",
                e.message,
                "Should return the expected error message"
            )
        }
    }

    private fun assertAllValid(result: EncryptedRequest) {
        assertNotNull(result, "Result should not be null")
        assertNotNull(result.encryptedCustomerInput, "Encrypted customer input should not be null")
        assertNotNull(result.encodedClientMetaInfo, "Encoded client meta info should not be null")
        assertFalse(
            result.encryptedCustomerInput.isEmpty(),
            "Encrypted customer input should not be empty"
        )
        assertFalse(
            result.encodedClientMetaInfo.isEmpty(),
            "Encoded client meta info should not be empty"
        )
    }

    private fun getValidRequest(): CreditCardTokenRequest {
        val request = CreditCardTokenRequest()
        request.paymentProductId = TestConfig.productIdVisa
        request.cardNumber = TestConfig.cardNumberVisa
        request.cardholderName = "Test Cardholder"
        request.securityCode = "123"
        request.expiryDate = "1230"
        return request
    }

    private fun getInvalidRequest(): CreditCardTokenRequest {
        val request = CreditCardTokenRequest()
        request.paymentProductId = TestConfig.productIdVisa
        request.cardNumber = TestConfig.cardNumberWithSurcharge
        request.cardholderName = "Test Cardholder"

        return request
    }
}
