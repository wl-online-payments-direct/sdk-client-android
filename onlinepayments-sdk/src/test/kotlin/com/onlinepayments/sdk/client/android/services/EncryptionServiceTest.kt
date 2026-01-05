/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.services

import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.publicKey.PublicKeyResponseDto
import com.onlinepayments.sdk.client.android.infrastructure.factories.PaymentProductFactory
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.mocks.MockContext
import com.onlinepayments.sdk.client.android.mocks.MockEncoding
import com.onlinepayments.sdk.client.android.testUtil.GsonHelper
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EncryptionServiceTest {

    private lateinit var apiClient: IApiClient
    private lateinit var sessionData: SessionData
    private lateinit var encryptionService: EncryptionService
    private lateinit var paymentRequest: PaymentRequest
    private lateinit var tokenRequest: CreditCardTokenRequest

    @BeforeTest
    fun setUp() {
        clearAllMocks()
        MockEncoding.setup()

        apiClient = mockk()

        sessionData = SessionData(
            clientSessionId = "session123",
            customerId = "customer456",
            clientApiUrl = "https://api.example.com/client/v1/",
            assetUrl = "https://assets.example.com"
        )

        paymentRequest = mockk()
        tokenRequest = mockk()

        encryptionService = EncryptionService(
            apiClient = apiClient,
            sessionData = sessionData,
            context = MockContext.setup(),
        )
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getPublicKey returns PublicKeyResponse successfully`() = runTest {
        val publicKeyResponseDto = GsonHelper.fromResourceJson(
            "publicKeyResponse.json",
            PublicKeyResponseDto::class.java
        )

        coEvery {
            apiClient.getPublicKey(any())
        } returns publicKeyResponseDto

        val result = encryptionService.getPublicKey()

        assertNotNull(result)
        assertNotNull(result.getKeyId())
        assertNotNull(result.getPublicKey())
        coVerify { apiClient.getPublicKey(sessionData.customerId) }
    }

    @Test
    fun `encryptPaymentRequest returns PreparedPaymentRequest successfully`() = runTest {
        val publicKeyResponseDto = GsonHelper.fromResourceJson(
            "publicKeyResponse.json",
            PublicKeyResponseDto::class.java
        )

        coEvery {
            apiClient.getPublicKey(any())
        } returns publicKeyResponseDto

        every {
            paymentRequest.validate()
        } returns mockk {
            every { isValid } returns true
        }

        every { paymentRequest.getAccountOnFile() } returns null
        every { paymentRequest.paymentProduct.id } returns 1
        every { paymentRequest.getValues() } returns mapOf("cardNumber" to "4567350000427977")
        every { paymentRequest.getTokenize() } returns false

        val result = encryptionService.encryptPaymentRequest(paymentRequest)

        assertNotNull(result)
        assertNotNull(result.encryptedCustomerInput)
        assertNotNull(result.encodedClientMetaInfo)

        verify { paymentRequest.validate() }
        verify { paymentRequest.getAccountOnFile() }
        verify { paymentRequest.getValues() }
    }

    @Test
    fun `encryptPaymentRequest throws InvalidArgumentException when paymentRequest is invalid`() = runTest {
        val publicKeyResponseDto = GsonHelper.fromResourceJson(
            "publicKeyResponse.json",
            PublicKeyResponseDto::class.java
        )

        coEvery {
            apiClient.getPublicKey(any())
        } returns publicKeyResponseDto

        every {
            paymentRequest.validate()
        } returns mockk {
            every { isValid } returns false
        }

        val exception = assertFailsWith<InvalidArgumentException> {
            encryptionService.encryptPaymentRequest(paymentRequest)
        }

        assertEquals(
            "Cannot encrypt invalid request.",
            exception.message
        )
    }

    @Test
    fun `encryptTokenPaymentRequest returns PreparedPaymentRequest successfully`() = runTest {
        val publicKeyResponseDto = GsonHelper.fromResourceJson(
            "publicKeyResponse.json",
            PublicKeyResponseDto::class.java
        )

        coEvery {
            apiClient.getPublicKey(any())
        } returns publicKeyResponseDto

        every { tokenRequest.paymentProductId } returns 1
        every { tokenRequest.getValues() } returns mapOf(
            "cardNumber" to "4567350000427977",
            "cvv" to "123"
        )

        val result = encryptionService.encryptTokenPaymentRequest(tokenRequest)

        assertNotNull(result)
        assertNotNull(result.encryptedCustomerInput)
        assertNotNull(result.encodedClientMetaInfo)
    }

    @Test
    fun `encryptPaymentRequest returns encryptedCustomerInput and encodedClientMetaInfo`() = runTest {
        val publicKeyResponseDto = GsonHelper.fromResourceJson(
            "publicKeyResponse.json",
            PublicKeyResponseDto::class.java
        )
        val paymentProductDto = GsonHelper.fromResourceJson(
            "paymentProductForPaymentRequest.json",
            PaymentProductDto::class.java
        )

        val realEncryptionService = EncryptionService(apiClient, sessionData, MockContext.setup())

        val paymentProduct = PaymentProductFactory().createPaymentProduct(paymentProductDto)
        val request = PaymentRequest(paymentProduct)

        request.setValue("cvv", "123")
        request.setValue("expiryDate", "1226")
        request.setValue("cardNumber", "4567350000427977")
        request.getField("cardholderName").setValue("Test cardholder")

        coEvery {
            apiClient.getPublicKey(any())
        } returns publicKeyResponseDto

        val result = realEncryptionService.encryptPaymentRequest(request)

        assertNotNull(result)
        assertNotNull(result.encryptedCustomerInput)
        assertNotNull(result.encodedClientMetaInfo)

        assertTrue(result.encryptedCustomerInput.isNotEmpty())
        assertTrue(result.encodedClientMetaInfo.isNotEmpty())
    }

    @Test
    fun `encryptPaymentRequest with valid card number returns encryptedCustomerInput and encodedClientMetaInfo`() =
        runTest {
            val publicKeyResponseDto = GsonHelper.fromResourceJson(
                "publicKeyResponse.json",
                PublicKeyResponseDto::class.java
            )
            val paymentProductDto = GsonHelper.fromResourceJson(
                "paymentProductForPaymentRequest.json",
                PaymentProductDto::class.java
            )

            val realEncryptionService = EncryptionService(apiClient, sessionData, MockContext.setup())

            val paymentProduct = PaymentProductFactory().createPaymentProduct(paymentProductDto)
            val paymentRequest = PaymentRequest(paymentProduct)

            paymentRequest.getField("cardNumber").setValue("7822551678890142249")
            paymentRequest.getField("cardholderName").setValue("Test cardholder name")
            paymentRequest.getField("cvv").setValue("123")
            paymentRequest.getField("expiryDate").setValue("1226")

            assertTrue(paymentRequest.validate().errors.isEmpty())

            coEvery {
                apiClient.getPublicKey(any())
            } returns publicKeyResponseDto

            val result = realEncryptionService.encryptPaymentRequest(paymentRequest)

            assertNotNull(result)
            assertNotNull(result.encryptedCustomerInput)
            assertNotNull(result.encodedClientMetaInfo)
            
            assertTrue(result.encryptedCustomerInput.isNotEmpty())
        }

    @Test
    fun `encryptPaymentRequest throws error with invalid card number`() =
        runTest {
            val publicKeyResponseDto = GsonHelper.fromResourceJson(
                "publicKeyResponse.json",
                PublicKeyResponseDto::class.java
            )
            val paymentProductDto = GsonHelper.fromResourceJson(
                "paymentProductForPaymentRequest.json",
                PaymentProductDto::class.java
            )

            val realEncryptionService = EncryptionService(apiClient, sessionData, MockContext.setup())

            val paymentProduct = PaymentProductFactory().createPaymentProduct(paymentProductDto)
            val paymentRequest = PaymentRequest(paymentProduct)

            paymentRequest.getField("cardNumber").setValue("4222422242224222")
            paymentRequest.getField("cardholderName").setValue("Test cardholder name")
            paymentRequest.getField("cvv").setValue("123")
            paymentRequest.getField("expiryDate").setValue("1226")

            coEvery {
                apiClient.getPublicKey(any())
            } returns publicKeyResponseDto

            val validationMessages = paymentRequest.validate()

            val exception = assertFailsWith<InvalidArgumentException> {
                realEncryptionService.encryptPaymentRequest(paymentRequest)
            }

            assertNotNull(validationMessages.errors)
            assertEquals(
                "Cannot encrypt invalid request.",
                exception.message
            )
            assertEquals("LUHN", validationMessages.errors[0].type)
        }

    @Test
    fun `encryptTokenRequest returns encryptedCustomerInput and encodedClientMetaInfo`() = runTest {
        val publicKeyResponseDto = GsonHelper.fromResourceJson(
            "publicKeyResponse.json",
            PublicKeyResponseDto::class.java
        )

        val realEncryptionService = EncryptionService(apiClient, sessionData, MockContext.setup())

        val token = CreditCardTokenRequest()
        token.securityCode = "123"
        token.cardNumber = "424242424242"
        token.paymentProductId = 1

        coEvery {
            apiClient.getPublicKey(any())
        } returns publicKeyResponseDto

        val result = realEncryptionService.encryptTokenPaymentRequest(token)

        assertNotNull(result)
        assertNotNull(result.encryptedCustomerInput)
        assertNotNull(result.encodedClientMetaInfo)

        assertTrue(result.encryptedCustomerInput.isNotEmpty())
        assertTrue(result.encodedClientMetaInfo.isNotEmpty())
    }
}