/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.session

import android.content.Context
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.encryption.EncryptData
import com.onlinepayments.sdk.client.android.encryption.Encryptor
import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener
import com.onlinepayments.sdk.client.android.model.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import com.onlinepayments.sdk.client.android.util.Util

/**
 * Helper for encrypting the [PaymentRequest].
 *
 * @param context [Context] used for reading device metadata which is sent to the Online Payments gateway
 * @param clientSessionId the sessionId that is used to communicate with the Online Payments gateway
 * @param metaData the metadata which is sent to the Online Payments gateway
 * @param paymentRequestPreparedListener [PaymentRequestPreparedListener] [Deprecated] that is called when encryption is completed
 */
internal sealed class SessionEncryptionHelper(
    val context: Context,
    val clientSessionId: String,
    val metaData: Map<String, String>,
    val paymentRequestPreparedListener: PaymentRequestPreparedListener? = null
) {
    protected fun getResponse(encryptedData: String): PreparedPaymentRequest {
        return if (metaData.isNotEmpty()) {
            PreparedPaymentRequest(encryptedData, Util.getBase64EncodedMetadata(metaData))
        } else {
            PreparedPaymentRequest(
                encryptedData,
                Util.getBase64EncodedMetadata(context, null, Constants.SDK_IDENTIFIER)
            )
        }
    }

    fun getPreparedRequest(publicKey: PublicKeyResponse): PreparedPaymentRequest {
        val encrypted = encrypt(publicKey, encryptData(publicKey))
        return getResponse(encrypted)
    }

    fun onPublicKeyReceived(publicKey: PublicKeyResponse) {
        try {
            val encrypted = encrypt(publicKey, encryptData(publicKey))
            paymentRequestPreparedListener?.onPaymentRequestPrepared(getResponse(encrypted))
        } catch (e: EncryptDataException) {
            paymentRequestPreparedListener?.onFailure(e)
        }
    }

    protected abstract fun encryptData(publicKey: PublicKeyResponse): EncryptData

    private fun encrypt(publicKey: PublicKeyResponse, data: EncryptData): String =
        Encryptor(publicKey).encrypt(data)

    protected fun formattedPaymentValues(req: PaymentRequest): Map<String, String> {
        val formatted = mutableMapOf<String, String>()
        for (field in req.paymentProduct!!.getPaymentProductFields()) {
            req.getValue(field.id)?.let { value ->
                val unmasked = req.getUnmaskedValue(field.id, value)
                field.type?.let { type ->
                    formatted[field.id] =
                        if (type == PaymentProductField.Type.NUMERICSTRING)
                            unmasked.replace("[^\\d.]".toRegex(), "")
                        else unmasked
                }
            }
        }
        return formatted
    }

    protected fun filterTokenRequestValues(req: CreditCardTokenRequest): Map<String, String> {
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

    protected fun formattedUnmaskedValue(
        fieldType: PaymentProductField.Type,
        unmaskedValue: String
    ): String {
        return if (fieldType == PaymentProductField.Type.NUMERICSTRING) {
            unmaskedValue.replace("[^\\d.]".toRegex(), "")
        } else {
            unmaskedValue
        }
    }

    class Payment(
        context: Context,
        clientSessionId: String,
        metaData: Map<String, String>,
        paymentRequestPreparedListener: PaymentRequestPreparedListener? = null,
        private val paymentRequest: PaymentRequest
    ) : SessionEncryptionHelper(context, clientSessionId, metaData, paymentRequestPreparedListener) {

        override fun encryptData(publicKey: PublicKeyResponse): EncryptData {
            val nonce = java.util.UUID.randomUUID().toString()
            val accountOnFileId = paymentRequest.accountOnFile?.id
            val productId = paymentRequest.paymentProduct?.getId()?.toInt()
            val values = formattedPaymentValues(paymentRequest)

            return EncryptData(
                accountOnFileId = accountOnFileId,
                clientSessionId = clientSessionId,
                nonce = nonce,
                paymentProductId = productId,
                tokenize = paymentRequest.tokenize,
                paymentValues = values
            )
        }
    }

    class TokenPayment(
        context: Context,
        clientSessionId: String,
        metaData: Map<String, String>,
        paymentRequestPreparedListener: PaymentRequestPreparedListener? = null,
        private val tokenRequest: CreditCardTokenRequest
    ) : SessionEncryptionHelper(context, clientSessionId, metaData, paymentRequestPreparedListener) {

        override fun encryptData(publicKey: PublicKeyResponse): EncryptData {
            val nonce = java.util.UUID.randomUUID().toString()
            val values = filterTokenRequestValues(tokenRequest)
            return EncryptData(
                accountOnFileId = null,
                clientSessionId = clientSessionId,
                nonce = nonce,
                paymentProductId = tokenRequest.paymentProductId,
                tokenize = false,
                paymentValues = values
            )
        }
    }
}
