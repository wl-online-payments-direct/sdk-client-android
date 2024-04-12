/*
 * Copyright 2020 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.session

import android.content.Context
import com.onlinepayments.sdk.client.android.configuration.Constants
import com.onlinepayments.sdk.client.android.util.Util
import com.onlinepayments.sdk.client.android.encryption.EncryptData
import com.onlinepayments.sdk.client.android.encryption.Encryptor
import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener
import com.onlinepayments.sdk.client.android.model.PaymentRequest
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField
import java.util.UUID

/**
 * Helper for encrypting the [PaymentRequest].
 *
 * @param context [Context] used for reading device metadata which is sent to the Online Payments gateway
 * @param paymentRequest the [PaymentRequest] that will be encrypted
 * @param clientSessionId the sessionId that is used to communicate with the Online Payments gateway
 * @param metaData the metadata which is sent to the Online Payments gateway
 * @param paymentRequestPreparedListener [PaymentRequestPreparedListener] that is called when encryption is completed
 */
internal class SessionEncryptionHelper(
    private val context: Context,
    private val paymentRequest: PaymentRequest,
    private val clientSessionId: String,
    private val metaData: Map<String, String>,
    private val paymentRequestPreparedListener: PaymentRequestPreparedListener
) {
    private fun notifyListener(encryptedData: String) {
        if (metaData.isNotEmpty()) {
            paymentRequestPreparedListener.onPaymentRequestPrepared(
                PreparedPaymentRequest(encryptedData, Util.getBase64EncodedMetadata(metaData))
            )
        } else {
            paymentRequestPreparedListener.onPaymentRequestPrepared(
                PreparedPaymentRequest(
                    encryptedData,
                    Util.getBase64EncodedMetadata(context, null, Constants.SDK_IDENTIFIER)
                )
            )
        }
    }

    @JvmSynthetic
    fun onPublicKeyReceived(response: PublicKeyResponse) {
        encryptData(response)
    }

    private fun encryptData(publicKeyResponse: PublicKeyResponse) {
        val accountOnFileId = paymentRequest.accountOnFile?.id
        val nonce = UUID.randomUUID().toString()
        val paymentProductId = paymentRequest.paymentProduct.id.toInt()

        val encryptData = EncryptData(
            accountOnFileId,
            clientSessionId,
            nonce,
            paymentProductId,
            paymentRequest.tokenize,
            formattedPaymentValues()
        )

        // Encrypt all the fields in the PaymentProduct
        val encryptor = Encryptor(publicKeyResponse)

        try {
            val encryptedString = encryptor.encrypt(encryptData)
            notifyListener(encryptedString)
        } catch (e: EncryptDataException) {
            paymentRequestPreparedListener.onFailure(e)
        }
    }

    private fun formattedPaymentValues(): Map<String, String> {
        val formattedPaymentValues: MutableMap<String, String> = HashMap()
        for (field in paymentRequest.paymentProduct.paymentProductFields) {
            paymentRequest.getValue(field.id)?.let { value ->
                val unmaskedValue = paymentRequest.getUnmaskedValue(field.id, value)

                // The date and expiry date are already in the correct format.
                // If the masks given by the Online Payments gateway are correct
                if (field.type != null && unmaskedValue != null) {
                    formattedPaymentValues[field.id] =  formattedUnmaskedValue(field.type, unmaskedValue)
                }
            }
        }

        return formattedPaymentValues
    }

    private fun formattedUnmaskedValue(fieldType: PaymentProductField.Type, unmaskedValue: String): String {
        return if (fieldType == PaymentProductField.Type.NUMERICSTRING) {
            unmaskedValue.replace("[^\\d.]".toRegex(), "")
        } else {
            unmaskedValue
        }
    }
}
