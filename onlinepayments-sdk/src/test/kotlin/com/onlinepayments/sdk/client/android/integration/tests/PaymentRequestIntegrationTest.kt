/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.integration.tests

import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.domain.validation.ValidationResult
import com.onlinepayments.sdk.client.android.domain.validation.rules.ValidationRuleType
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

        return@runBlocking
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

        return@runBlocking
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

        return@runBlocking
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

        return@runBlocking
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

        return@runBlocking
    }

    @Test
    fun createToken_withValidData_shouldSucceed() = runBlocking {
        val request = sdk.encryptPaymentRequest(createValidRequest())

        assertNotNull(request, "Encryption should succeed")

        val response = ServerApiHelper.createToken(request.encryptedCustomerInput)

        assertNotNull(response)
        assertNotNull(response.token)
        assertNotNull("CREATED", response.tokenStatus)

        return@runBlocking
    }

    @Test
    fun createPayment_withValidData_shouldSucceed() = runBlocking {
        val request = sdk.encryptPaymentRequest(createValidRequest())

        assertNotNull(request, "Encryption should succeed")

        val response = ServerApiHelper.createPayment(request.encryptedCustomerInput)

        val payment = response.getAsJsonObject("payment")

        assertNotNull(payment, "Payment should not be null")
        assertNotNull(payment.get("id"))

        return@runBlocking
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
