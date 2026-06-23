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

import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.domain.validation.ValidationResult
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.ServerApiHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Integration tests for payment request encryption.
 * Tests real encryption with public keys from the preprod environment.
 */
class PaymentRequestIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `EncryptPaymentRequest encrypts valid payment request`() = runBlocking {
        val result = sdk.encryptPaymentRequest(createValidRequest())

        assertValidEncryptedRequest(result)
    }

    @Test
    fun `EncryptPaymentRequest returns encoded client meta information`() = runBlocking {
        val result = sdk.encryptPaymentRequest(createValidRequest())

        assertNotNull(result.encodedClientMetaInfo, "Encoded client meta info should not be null")
        assertFalse(result.encodedClientMetaInfo.isEmpty(), "Encoded client meta info should not be empty")
    }

    @Test
    fun `EncryptPaymentRequest produces different encrypted output for multiple requests`() = runBlocking {
        val firstResult = sdk.encryptPaymentRequest(createValidRequest())
        val secondResult = sdk.encryptPaymentRequest(createValidRequest())

        assertNotNull(firstResult.encryptedCustomerInput, "First encrypted customer input should not be null")
        assertNotNull(secondResult.encryptedCustomerInput, "Second encrypted customer input should not be null")
        assertTrue(
            firstResult.encryptedCustomerInput != secondResult.encryptedCustomerInput,
            "Encrypted output should be different for multiple requests"
        )
    }

    @Test
    fun `EncryptPaymentRequest includes tokenize flag when tokenize is enabled`() = runBlocking {
        val request = createValidRequest()
        request.setTokenize(true)

        assertTrue(request.getTokenize(), "Tokenize flag should be true before encryption")

        val result = sdk.encryptPaymentRequest(request)

        assertValidEncryptedRequest(result)
    }

    @Test
    fun `EncryptPaymentRequest throws error when mandatory field is missing`() = runBlocking {
        val product = sdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
        val request = PaymentRequest(product, null, false)

        request.setValue("cardholderName", "Test Cardholder")
        request.setValue("cvv", "123")
        request.setValue("expiryDate", getValidExpiryDate(product))

        try {
            sdk.encryptPaymentRequest(request)
            fail("Should have thrown InvalidArgumentException for missing mandatory field")
        } catch (e: InvalidArgumentException) {
            assertEquals("Cannot encrypt invalid request.", e.message)

            val validationResult = e.metadata!!["data"] as ValidationResult

            assertEquals(1, validationResult.errors.size)
            assertEquals(ValidationRuleType.REQUIRED.toString(), validationResult.errors[0].type)
            assertEquals("cardNumber", validationResult.errors[0].paymentProductFieldId)
        }
    }

    @Test
    fun `EncryptPaymentRequest throws validation error for invalid card number`() = runBlocking {
        val product = sdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
        val request = PaymentRequest(product, null, false)

        request.setValue("cardNumber", "4222422242224222")
        request.setValue("cardholderName", "Test Cardholder")
        request.setValue("cvv", "123")
        request.setValue("expiryDate", getValidExpiryDate(product))

        try {
            sdk.encryptPaymentRequest(request)
            fail("Should have thrown InvalidArgumentException for invalid card number")
        } catch (e: InvalidArgumentException) {
            assertEquals("Cannot encrypt invalid request.", e.message)

            val validationResult = e.metadata!!["data"] as ValidationResult

            assertEquals(1, validationResult.errors.size)
            assertEquals(ValidationRuleType.LUHN.toString(), validationResult.errors[0].type)
        }
    }

    @Test
    fun `EncryptPaymentRequest succeeds for account-on-file with required fields`() = runBlocking {
        val tokenRequest = CreditCardTokenRequest()
        tokenRequest.paymentProductId = TestConfig.productIdVisa
        tokenRequest.cardNumber = TestConfig.cardNumberWithSurcharge
        tokenRequest.cardholderName = "Test Cardholder"
        tokenRequest.securityCode = "123"
        tokenRequest.expiryDate = "1230"

        val encryptedTokenRequest = sdk.encryptTokenRequest(tokenRequest)
        val tokenResponse = ServerApiHelper.createToken(encryptedTokenRequest.encryptedCustomerInput)
        val sessionWithToken = ServerApiHelper.createSessionWithTokens(listOf(tokenResponse.token!!))
        val sdkWithToken = OnlinePaymentsSdk(sessionWithToken, context, TestConfig.sdkConfiguration)

        val product = sdkWithToken.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
        val accountOnFile = product.accountsOnFile[0]
        val request = PaymentRequest(product, accountOnFile)

        request.setValue("cvv", "123")

        val result = sdkWithToken.encryptPaymentRequest(request)

        assertValidEncryptedRequest(result)
    }

    private suspend fun createValidRequest(): PaymentRequest {
        val product = sdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
        val request = PaymentRequest(product, null, false)

        request.setValue("cardNumber", TestConfig.cardNumberWithoutSurcharge)
        request.setValue("cardholderName", "Test Cardholder")
        request.setValue("cvv", "123")
        request.setValue("expiryDate", getValidExpiryDate(product))

        return request
    }

    private fun getValidExpiryDate(product: com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct): String {
        val maskedValue = product.getField("expiryDate")!!.applyMask("122030")
        return if (maskedValue?.length == 5) "1230" else "122030"
    }

    private fun assertValidEncryptedRequest(result: EncryptedRequest) {
        assertNotNull(result, "Encrypted result should not be null")
        assertNotNull(result.encryptedCustomerInput, "Encrypted customer input should not be null")
        assertNotNull(result.encodedClientMetaInfo, "Encoded client meta info should not be null")
        assertFalse(result.encryptedCustomerInput.isEmpty(), "Encrypted customer input should not be empty")
        assertFalse(result.encodedClientMetaInfo.isEmpty(), "Encoded client meta info should not be empty")
    }
}