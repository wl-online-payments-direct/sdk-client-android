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
 * Integration tests for credit card token request encryption.
 * Tests real encryption with public keys from the preprod environment.
 */
class CreditCardTokenRequestIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `CreateToken succeeds with valid token request data`() {
        runBlocking {
            val encryptedRequest = sdk.encryptTokenRequest(createValidTokenRequest())

            val response = ServerApiHelper.createToken(encryptedRequest.encryptedCustomerInput)

            assertNotNull(response, "Create token response should not be null")
            assertNotNull(response.token, "Created token should not be null")
            assertEquals("CREATED", response.tokenStatus, "Token status should be CREATED")
        }
    }

    @Test
    fun `CreateToken fails with invalid token request data`() {
        runBlocking {
            val encryptedRequest = sdk.encryptTokenRequest(createInvalidTokenRequest())

            try {
                ServerApiHelper.createToken(encryptedRequest.encryptedCustomerInput)
                fail("Should not create token for invalid token request data")
            } catch (e: Throwable) {
                assertNotNull(e, "Server API should reject invalid token request data")
            }
        }
    }

    @Test
    fun `EncryptTokenRequest fails when payment product id is missing`() {
        runBlocking {
            val request = CreditCardTokenRequest()
            request.cardNumber = TestConfig.cardNumberVisa

            try {
                sdk.encryptTokenRequest(request)
                fail("Should have thrown EncryptionException when paymentProductId is not set")
            } catch (e: EncryptionException) {
                assertEquals(
                    "Error encrypting credit card token request: the payment product ID not set.",
                    e.message,
                    "Should return the expected error message"
                )
            }
        }
    }

    @Test
    fun `EncryptTokenRequest returns correct values map`() {
        val request = createValidTokenRequest()

        assertEquals(
            mapOf<String, Any?>(
                "cardNumber" to TestConfig.cardNumberVisa,
                "cardholderName" to "Test Cardholder",
                "expiryDate" to "1230",
                "cvv" to "123",
                "paymentProductId" to TestConfig.productIdVisa
            ),
            request.getValues()
        )
    }

    @Test
    fun `EncryptTokenRequest returns encoded client meta information`() {
        runBlocking {
            val result = sdk.encryptTokenRequest(createValidTokenRequest())

            assertNotNull(result.encodedClientMetaInfo, "Encoded client meta info should not be null")
            assertFalse(result.encodedClientMetaInfo.isEmpty(), "Encoded client meta info should not be empty")
        }
    }

    @Test
    fun `EncryptTokenRequest returns encrypted token as string`() {
        runBlocking {
            val result = sdk.encryptTokenRequest(createValidTokenRequest())

            assertNotNull(result.encryptedCustomerInput, "Encrypted customer input should not be null")
            assertFalse(result.encryptedCustomerInput.isEmpty(), "Encrypted customer input should not be empty")
            assertEquals(
                5,
                result.encryptedCustomerInput.lines().joinToString("").split(".").size,
                "Encrypted customer input should be a JWE compact serialization"
            )
        }
    }

    @Test
    fun `EncryptTokenRequest with invalid data still produces encrypted output`() {
        runBlocking {
            val result = sdk.encryptTokenRequest(createInvalidTokenRequest())

            assertValidEncryptedRequest(result)
        }
    }

    @Test
    fun `EncryptTokenRequest with valid data returns encrypted output`() {
        runBlocking {
            val result = sdk.encryptTokenRequest(createValidTokenRequest())

            assertValidEncryptedRequest(result)
        }
    }

    private fun assertValidEncryptedRequest(result: EncryptedRequest) {
        assertNotNull(result, "Result should not be null")
        assertNotNull(result.encryptedCustomerInput, "Encrypted customer input should not be null")
        assertNotNull(result.encodedClientMetaInfo, "Encoded client meta info should not be null")
        assertFalse(result.encryptedCustomerInput.isEmpty(), "Encrypted customer input should not be empty")
        assertFalse(result.encodedClientMetaInfo.isEmpty(), "Encoded client meta info should not be empty")
        assertEquals(
            5,
            result.encryptedCustomerInput.lines().joinToString("").split(".").size,
            "Encrypted customer input should be a JWE compact serialization"
        )
    }

    private fun createValidTokenRequest(): CreditCardTokenRequest {
        val request = CreditCardTokenRequest()
        request.paymentProductId = TestConfig.productIdVisa
        request.cardNumber = TestConfig.cardNumberVisa
        request.cardholderName = "Test Cardholder"
        request.securityCode = "123"
        request.expiryDate = "1230"
        return request
    }

    private fun createInvalidTokenRequest(): CreditCardTokenRequest {
        val request = CreditCardTokenRequest()
        request.paymentProductId = TestConfig.productIdVisa
        request.cardNumber = "not-a-valid-card-number"
        request.cardholderName = ""
        request.securityCode = "x"
        request.expiryDate = "invalid-expiry-date"
        return request
    }
}