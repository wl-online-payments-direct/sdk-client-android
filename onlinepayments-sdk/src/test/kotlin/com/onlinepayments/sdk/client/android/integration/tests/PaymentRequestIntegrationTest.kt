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
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.domain.validation.ValidationResult
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk
import com.onlinepayments.sdk.client.android.integration.BaseIntegrationTest
import com.onlinepayments.sdk.client.android.integration.utils.MockServerHelper
import com.onlinepayments.sdk.client.android.integration.utils.ServerApiHelper
import com.onlinepayments.sdk.client.android.integration.utils.TestConfig
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Integration tests for payment request encryption.
 * Tests real encryption with actual public keys from the preprod environment.
 */
class PaymentRequestIntegrationTest : BaseIntegrationTest() {

    @Test
    fun encryptPaymentRequest_withValidData_shouldReturnEncryptedData() = runBlocking {
        // Get a payment product
        val productId = TestConfig.productIdVisa
        val paymentProduct = sdk.getPaymentProduct(productId, paymentContext)

        // Create payment request
        val paymentRequest = PaymentRequest(paymentProduct, null, false)
        paymentRequest.setValue("cardNumber", TestConfig.cardNumberWithSurcharge)
        paymentRequest.setValue("cardholderName", "Test Cardholder")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("expiryDate", "1226")

        // Encrypt the request
        val result = sdk.encryptPaymentRequest(paymentRequest)

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

    @Test
    fun encryptPaymentRequest_withMissingMandatoryField_shouldThrowException() = runBlocking {
        // Get a payment product
        val productId = TestConfig.productIdVisa
        val paymentProduct = sdk.getPaymentProduct(productId, paymentContext)

        // Create payment request without mandatory field (cardNumber)
        val paymentRequest = PaymentRequest(paymentProduct, null, false)
        paymentRequest.setValue("cardholderName", "Test Cardholder")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("expiryDate", "1226")
        // Missing cardNumber

        try {
            sdk.encryptPaymentRequest(paymentRequest)
            fail("Should have thrown an exception for missing mandatory field")
        } catch (e: InvalidArgumentException) {
            // Expected
            assertEquals("Cannot encrypt invalid request.", e.message)
            val validationResult = (e.metadata as Map<*, *>)["data"] as ValidationResult

            assertEquals(1, validationResult.errors.size)
            assertEquals(ValidationRuleType.REQUIRED.toString(), validationResult.errors[0].type)
            assertEquals("cardNumber", validationResult.errors[0].paymentProductFieldId)
        }
    }

    @Test
    fun encryptPaymentRequest_withInvalidCardNumber_shouldThrowException() = runBlocking {
        // Get a payment product
        val productId = TestConfig.productIdVisa
        val paymentProduct = sdk.getPaymentProduct(productId, paymentContext)

        // Create payment request with invalid card number (fails Luhn check)
        val paymentRequest = PaymentRequest(paymentProduct, null, false)
        paymentRequest.setValue("cardNumber", "4222422242224222")
        paymentRequest.setValue("cardholderName", "Test Cardholder")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("expiryDate", "1226")

        try {
            sdk.encryptPaymentRequest(paymentRequest)
            fail("Should have thrown an exception for invalid card number")
        } catch (e: InvalidArgumentException) {
            // Expected - should fail Luhn validation
            assertEquals("Cannot encrypt invalid request.", e.message)
            val validationResult = (e.metadata as Map<*, *>)["data"] as ValidationResult

            assertEquals(1, validationResult.errors.size)
            assertEquals(ValidationRuleType.LUHN.toString(), validationResult.errors[0].type)
        }
    }

    @Test
    fun encryptPaymentRequest_validation_shouldCheckAllFields() = runBlocking {
        // Get a payment product
        val productId = TestConfig.productIdVisa
        val paymentProduct = sdk.getPaymentProduct(productId, paymentContext)

        // Create payment request
        val paymentRequest = PaymentRequest(paymentProduct, null, false)
        paymentRequest.setValue("cardNumber", TestConfig.cardNumberWithSurcharge)
        paymentRequest.setValue("cardholderName", "Test Cardholder")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("expiryDate", "1226")

        // Validate the request
        val validationResult = paymentRequest.validate()

        assertTrue(validationResult.isValid, "Payment request should be valid")
        assertTrue(validationResult.errors.isEmpty(), "Should have no validation errors")
    }

    @Test
    fun encryptPaymentRequest_multipleRequests_shouldGenerateDifferentEncryptedData() = runBlocking {
        // Get a payment product
        val productId = TestConfig.productIdVisa
        val paymentProduct = sdk.getPaymentProduct(productId, paymentContext)

        // Create first payment request
        val paymentRequest1 = PaymentRequest(paymentProduct, null, false)
        paymentRequest1.setValue("cardNumber", TestConfig.cardNumberWithSurcharge)
        paymentRequest1.setValue("cardholderName", "Test Cardholder 1")
        paymentRequest1.setValue("cvv", "123")
        paymentRequest1.setValue("expiryDate", "1226")

        // Create second payment request with same data
        val paymentRequest2 = PaymentRequest(paymentProduct, null, false)
        paymentRequest2.setValue("cardNumber", TestConfig.cardNumberWithSurcharge)
        paymentRequest2.setValue("cardholderName", "Test Cardholder 1")
        paymentRequest2.setValue("cvv", "123")
        paymentRequest2.setValue("expiryDate", "1226")

        // Encrypt both requests
        val result1 = sdk.encryptPaymentRequest(paymentRequest1)
        val result2 = sdk.encryptPaymentRequest(paymentRequest2)

        // Results should be different due to random nonce in encryption
        assertNotNull(result1, "First result should not be null")
        assertNotNull(result2, "Second result should not be null")

        // The encrypted outputs should be different even with same input
        // (due to random nonce in JWE encryption)
        assertTrue(
            result1.encryptedCustomerInput != result2.encryptedCustomerInput,
            "Encrypted data should be different even with same input (random nonce)"
        )
    }

    @Test
    fun createToken_withValidData_shouldSucceed() = runBlocking {
        val request = sdk.encryptPaymentRequest(createValidRequest())

        assertNotNull(request, "Encryption should succeed")

        val response = ServerApiHelper.createToken(request.encryptedCustomerInput)

        assertNotNull(response)
        assertNotNull(response.token)
        assertEquals("CREATED", response.tokenStatus)
    }

    @Test
    fun createPayment_withValidData_shouldSucceed() = runBlocking {
        val request = sdk.encryptPaymentRequest(createValidRequest())

        assertNotNull(request, "Encryption should succeed")

        val response = ServerApiHelper.createPayment(request.encryptedCustomerInput)

        val payment = response.getAsJsonObject("payment")

        assertNotNull(payment, "Payment should not be null")
        assertNotNull(payment.get("id"))
        Unit
    }

    @Test
    fun encryptPaymentRequest_withAofEndToEnd_createsPaymentSuccessfully() = runBlocking {
        val tokenRequest = CreditCardTokenRequest()
        tokenRequest.cardNumber = TestConfig.cardNumberWithSurcharge
        tokenRequest.cardholderName = "Darwin Núñez"
        tokenRequest.expiryDate = "1230"
        tokenRequest.securityCode = "123"
        tokenRequest.paymentProductId = TestConfig.productIdVisa

        val encryptedToken = sdk.encryptTokenRequest(tokenRequest)
        assertNotNull(encryptedToken, "Token encryption should succeed")

        val tokenResponse = ServerApiHelper.createToken(encryptedToken.encryptedCustomerInput)
        assertNotNull(tokenResponse.token, "Token should be created")

        val sessionWithToken = ServerApiHelper.createSessionWithTokens(listOf(tokenResponse.token!!))
        val sdkWithToken = OnlinePaymentsSdk(
            sessionWithToken,
            context,
            TestConfig.sdkConfiguration
        )

        val product = sdkWithToken.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
        assertNotNull(product)
        assertTrue(product.accountsOnFile.isNotEmpty(), "Product should contain accounts on file")

        val aof = product.accountsOnFile[0]
        val request = PaymentRequest(product, aof)
        request.setValue("cvv", "123")

        val encryptedData = sdkWithToken.encryptPaymentRequest(request)
        assertNotNull(encryptedData, "Encrypted payment request should not be null")

        val paymentResult = ServerApiHelper.createPayment(encryptedData.encryptedCustomerInput)
        val payment = paymentResult.getAsJsonObject("payment")
        assertNotNull(payment, "Payment should be created")
        assertNotNull(payment.get("id"), "Payment should have an ID")
        Unit
    }

    @Test
    fun encryptPaymentRequest_withAofAndShortCvv_shouldFailValidation() = runBlocking {
        val json = MockServerHelper.loadJsonResource("paymentProductVisa.json")
        val (mockSdk, server) = MockServerHelper.createMockSdkWithResponse(context, json)

        try {
            val product = mockSdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)
            val aof = product.accountsOnFile[0]
            val request = PaymentRequest(product, aof)
            request.setValue("cvv", "1") // too short: minLength is 3 for Visa CVV

            val exception = assertFailsWith<InvalidArgumentException> {
                mockSdk.encryptPaymentRequest(request)
            }

            val validationResult = exception.metadata!!["data"] as ValidationResult

            assertFalse(validationResult.isValid)
            assertEquals(1, validationResult.errors.size)
            assertEquals("cvv", validationResult.errors.first().paymentProductFieldId)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun encryptPaymentRequest_withTokenizeFlag_shouldSucceed() = runBlocking {
        val product = sdk.getPaymentProduct(TestConfig.productIdVisa, paymentContext)

        val maskedValue = product.getField("expiryDate")!!.applyMask("122030")
        val validExpiry = if (maskedValue?.length == 5) "1230" else "122030"

        val request = PaymentRequest(product, null, false)
        request.setValue("cardNumber", TestConfig.cardNumberWithoutSurcharge)
        request.setValue("cardholderName", "Test Cardholder")
        request.setValue("cvv", "123")
        request.setValue("expiryDate", validExpiry)
        request.setTokenize(true)

        assertTrue(request.getTokenize(), "Tokenize flag should be true before encryption")

        val result = sdk.encryptPaymentRequest(request)

        assertNotNull(result, "Encrypted result should not be null")
        assertTrue(result.encryptedCustomerInput.isNotEmpty(), "Encrypted customer input should not be empty")
        assertTrue(result.encodedClientMetaInfo.isNotEmpty(), "Encoded client meta info should not be empty")
    }

    private suspend fun createValidRequest(): PaymentRequest {
        val productId = TestConfig.productIdVisa
        val paymentProduct = sdk.getPaymentProduct(productId, paymentContext)

        // we have to determine the correct expiry date format (4 or 6 digits)
        val maskedValue = paymentProduct.getField("expiryDate")!!.applyMask("122030")
        val validValue: String = if (maskedValue?.length == 5) "1230" else "122030"

        val paymentRequest = PaymentRequest(paymentProduct, null, false)
        paymentRequest.setValue("cardNumber", TestConfig.cardNumberWithoutSurcharge)
        paymentRequest.setValue("cardholderName", "Test Cardholder")
        paymentRequest.setValue("cvv", "123")
        paymentRequest.setValue("expiryDate", validValue)

        return paymentRequest
    }
}
