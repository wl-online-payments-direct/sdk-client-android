/*
 * Copyright 2020 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.encryption

import android.util.Log
import com.google.gson.GsonBuilder
import com.onlinepayments.sdk.client.android.exception.EncryptDataException
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

internal class Encryptor(
    // PublicKeyResponse which holds the Online Payments Gateway public key
    private val publicKeyResponse: PublicKeyResponse
) {
    companion object {
        // Tag used for logging
        private val TAG = Encryptor::class.simpleName

        // ContentEncryptionKey byte[] size = 512 bits
        private const val CONTENT_ENCRYPTION_KEY_SIZE = 64

        // Initialization Vector byte[] size = 128 bits
        private const val INITIALIZATION_VECTOR = 16

        // Protected Header settings
        private const val PROTECTED_HEADER_ALG = "RSA-OAEP"
        private const val PROTECTED_HEADER_ENC = "A256CBC-HS512"

        private const val BYTE_BUFFER_CAPACITY = 8
    }

    // Helper class for Encryption
    private val encryptUtil = EncryptUtil()

    /**
     * Encrypts all payment product field values for the given payment request as {@link EncryptData}.
     *
     * @param encryptData contains all field values and variables required for making a payment request
     *
     * @return encrypted String
     */
    @JvmSynthetic
    fun encrypt(encryptData: EncryptData): String {
        // Convert EncryptData to JSON format
        val gsonBuilder = GsonBuilder()
            .registerTypeAdapter(EncryptData::class.java, EncryptDataJsonSerializer())
            .create()
        val payload = gsonBuilder.toJson(encryptData)

        try {
            // Create protected header and encode it with Base64 encoding
            val protectedHeader = createProtectedHeader()
            val encodedProtectedHeader =
                encryptUtil.base64UrlEncode(protectedHeader.toByteArray(StandardCharsets.UTF_8))

            // Create ContentEncryptionKey, is a random ByteArray
            val contentEncryptionKey = encryptUtil.generateSecureRandomBytes(CONTENT_ENCRYPTION_KEY_SIZE)

            // Encrypt the contentEncryptionKey with the Online Payments gateway publickey and encode it with Base64 encoding
            val encryptedContentEncryptionKey =
                encryptUtil.encryptContentEncryptionKey(contentEncryptionKey, publicKeyResponse.publicKey)
            val encodedEncryptedContentEncryptionKey = encryptUtil.base64UrlEncode(encryptedContentEncryptionKey)

            // Split the contentEncryptionKey in ENC_KEY and MAC_KEY for using hmac
            val macKey = contentEncryptionKey.copyOf(CONTENT_ENCRYPTION_KEY_SIZE / 2)
            val encKey = contentEncryptionKey.copyOfRange(CONTENT_ENCRYPTION_KEY_SIZE / 2, CONTENT_ENCRYPTION_KEY_SIZE)

            // Create Initialization Vector
            val initializationVector = encryptUtil.generateSecureRandomBytes(INITIALIZATION_VECTOR)
            val encodedInitializationVector = encryptUtil.base64UrlEncode(initializationVector)

            // Encrypt content with ContentEncryptionKey and Initialization Vector
            val cipherText = encryptUtil.encryptPayload(payload, encKey, initializationVector)
            val encodedCipherText = encryptUtil.base64UrlEncode(cipherText)

            // Create Additional Authenticated Data and Additional Authenticated Data Length
            val additionalAuthenticatedData = encodedProtectedHeader.toByteArray(StandardCharsets.UTF_8)
            val al = calculateAdditionalAuthenticatedDataLength(additionalAuthenticatedData)

            // Calculate HMAC
            val calculatedHMAC = calculateHMAC(
                macKey,
                additionalAuthenticatedData,
                initializationVector,
                cipherText,
                al
            )

            // Truncate HMAC value to Create Authentication Tag
            val authenticationTag = calculatedHMAC.copyOf(calculatedHMAC.size / 2)
            val encodedAuthenticationTag = encryptUtil.base64UrlEncode(authenticationTag)

            return buildCompactRepresentation(
                encodedProtectedHeader,
                encodedEncryptedContentEncryptionKey,
                encodedInitializationVector,
                encodedCipherText,
                encodedAuthenticationTag
            )
        } catch (e: Exception) {
            Log.i(TAG, "Error while encrypting fields ${e.message}")
            throw EncryptDataException("Error while encrypting fields ${e.message}")
        }
    }

    /**
     * Calculates HMAC over the data.
     *
     * @param macKey unique random key
     * @param additionalAuthenticatedData Additional Authenticated Data
     * @param initializationVector Initialization Vector
     * @param cipherText encrypted data
     * @param al Additional Authenticated Data Length
     *
     * @return HMAC value
     */
    private fun calculateHMAC(
        macKey: ByteArray,
        additionalAuthenticatedData: ByteArray,
        initializationVector: ByteArray,
        cipherText: ByteArray,
        al: ByteArray
    ): ByteArray {
        // Create HMAC Computation input
        val hmacInput = encryptUtil.concatenateByteArrays(
            additionalAuthenticatedData,
            initializationVector,
            cipherText,
            al
        )

        // And calculate HMAC over that ByteArray
        return encryptUtil.calculateHmac(macKey, hmacInput)
    }


    /**
     * Creates Protected header string which determines the Algorithm and Encryption with which the payload will be encrypted.
     *
     * @return protected header String
     */
    private fun createProtectedHeader(): String {
        return StringBuilder()
            .append("{\"alg\":\"").append(PROTECTED_HEADER_ALG).append("\",")
            .append("\"enc\":\"").append(PROTECTED_HEADER_ENC).append("\",")
            .append("\"kid\":\"").append(publicKeyResponse.keyId).append("\"}")
            .toString()
    }

    /**
     * Creates the CompactRepresentation of all the encrypted components.
     *
     * @param components list of all components
     *
     * @return CompactRepresentation of all the encrypted components
     */
    private fun buildCompactRepresentation(vararg components: String): String {
        // Loop over all components to add them to the StringBuilder
        val builder = StringBuilder()

        for (componentCount in components.indices) {
            builder.append(components[componentCount])

            // Append . between the different components
            if (componentCount != components.size - 1) {
                builder.append(".")
            }
        }

        return builder.toString()
    }

    /**
     * Calculate Additional Authenticated Data Length.
     *
     * @return byte representation of the Additional Authenticated Data Length
     */
    private fun calculateAdditionalAuthenticatedDataLength(additionalAuthenticatedData: ByteArray): ByteArray {
        val lengthInBits = (additionalAuthenticatedData.size * BYTE_BUFFER_CAPACITY).toLong()

        return ByteBuffer.allocate(BYTE_BUFFER_CAPACITY).putLong(lengthInBits).array()
    }
}
