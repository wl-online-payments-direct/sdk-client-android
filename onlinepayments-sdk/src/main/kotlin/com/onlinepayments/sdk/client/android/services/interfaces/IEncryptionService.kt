/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.services.interfaces

import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest

internal interface IEncryptionService {
    suspend fun getPublicKey(): PublicKeyResponse

    suspend fun encryptPaymentRequest(paymentRequest: PaymentRequest): EncryptedRequest

    suspend fun encryptTokenPaymentRequest(tokenRequest: CreditCardTokenRequest): EncryptedRequest
}
