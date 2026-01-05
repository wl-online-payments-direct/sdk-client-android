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

import android.content.Context
import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse
import com.onlinepayments.sdk.client.android.infrastructure.encryption.Encryptor
import com.onlinepayments.sdk.client.android.infrastructure.encryption.MetadataUtil
import com.onlinepayments.sdk.client.android.infrastructure.encryption.RequestEncryptionData
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.INonceProvider
import com.onlinepayments.sdk.client.android.infrastructure.providers.NonceProvider
import com.onlinepayments.sdk.client.android.services.interfaces.IEncryptionService

internal class EncryptionService(
    private val apiClient: IApiClient,
    private val sessionData: SessionData,
    private val context: Context,
    private val configuration: SdkConfiguration?,
    private val nonceProvider: INonceProvider = NonceProvider()
) : IEncryptionService {
    constructor(
        apiClient: IApiClient,
        sessionData: SessionData,
        context: Context
    ) : this(apiClient, sessionData, context, null)

    override suspend fun getPublicKey(): PublicKeyResponse {
        val dto = apiClient.getPublicKey(sessionData.customerId)

        return PublicKeyResponse(dto.keyId, dto.publicKey)
    }

    override suspend fun encryptPaymentRequest(
        paymentRequest: PaymentRequest
    ): EncryptedRequest {
        val validationResult = paymentRequest.validate()

        if (!validationResult.isValid) {
            throw InvalidArgumentException(
                message = "Cannot encrypt invalid request.",
                cause = null,
                metadata = mapOf(
                    "data" to validationResult
                )
            )
        }

        return encryptData(getPaymentRequestEncryptionData(paymentRequest))
    }

    override suspend fun encryptTokenPaymentRequest(
        tokenRequest: CreditCardTokenRequest
    ): EncryptedRequest {
        return encryptData(getTokenRequestEncryptionData(tokenRequest))
    }

    private suspend fun encryptData(preparedData: RequestEncryptionData): EncryptedRequest {
        val publicKey = getPublicKey()

        val encryptedRequest = Encryptor(publicKey).encrypt(preparedData)

        return EncryptedRequest(
            encryptedRequest,
            MetadataUtil.getBase64EncodedMetadata(context, configuration?.appIdentifier, Constants.SDK_IDENTIFIER)
        )
    }

    private fun getPaymentRequestEncryptionData(paymentRequest: PaymentRequest): RequestEncryptionData {
        return RequestEncryptionData(
            accountOnFileId = paymentRequest.getAccountOnFile()?.id,
            clientSessionId = sessionData.clientSessionId,
            nonce = nonceProvider.generateNonce(),
            paymentProductId = paymentRequest.paymentProduct.id,
            tokenize = paymentRequest.getTokenize(),
            paymentValues = paymentRequest.getValues()
        )
    }

    private fun getTokenRequestEncryptionData(tokenRequest: CreditCardTokenRequest): RequestEncryptionData {
        return RequestEncryptionData(
            accountOnFileId = null,
            clientSessionId = sessionData.clientSessionId,
            nonce = nonceProvider.generateNonce(),
            paymentProductId = tokenRequest.paymentProductId,
            tokenize = false,
            paymentValues = filterTokenRequestValues(tokenRequest)
        )
    }

    private fun filterTokenRequestValues(req: CreditCardTokenRequest): Map<String, String> {
        return req.getValues()
            .filterValues { value ->
                when (value) {
                    null -> false
                    is String -> value.isNotBlank()
                    else -> true
                }
            }
            .mapValues { (_, value) -> value.toString() }
    }
}
