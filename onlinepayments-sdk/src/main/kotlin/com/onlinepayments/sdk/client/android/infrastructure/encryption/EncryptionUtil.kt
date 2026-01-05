/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.encryption

import android.util.Base64
import com.onlinepayments.sdk.client.android.domain.exceptions.EncryptionException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class EncryptionUtil {
    // AES Encryption setting
    private val aesAlgorithmType = "AES"
    private val aesAlgorithmMode = "AES/CBC/PKCS5Padding"

    // HMAC calculation setting
    private val hmacAlgorithmType = "HmacSHA512"

    // RSA Encryption settings
    private val rsaAlgorithmMode = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"

    /**
     * Encodes a String with base64Url encoding.
     * It also removes characters which must be removed according to the <a href="http://tools.ietf.org/html/draft-ietf-jose-json-web-signature-29#appendix-C">JOSE spec</a>.
     *
     * @param data the data which will be encoded
     *
     * @return encoded data
     */
    fun base64UrlEncode(data: ByteArray): String {
        return String(Base64.encode(data, Base64.URL_SAFE), StandardCharsets.UTF_8)
            .replace("=", "")
            .replace("\n", "")
            .replace("\r", "")
    }

    /**
     * Generates bytearray which is filled with random bytes from SecureRandom
     *
     * @param size the size of the random [ByteArray]
     *
     * @return [ByteArray] of provided [size]
     */
    fun generateSecureRandomBytes(size: Int): ByteArray {
        // Create SecureRandom and ByteArray of provided size
        val secureRandom = SecureRandom()
        val randomBytes = ByteArray(size)

        // Fill the randomContentEncryptionKey with random bytes
        secureRandom.nextBytes(randomBytes)

        return randomBytes
    }

    /**
     * Encrypts a given ContentEncryptionKey with a public key using RSA.
     *
     * @param contentEncryptionKey the byte array to be encrypted
     * @param publicKey the public key
     *
     * @return [ByteArray] of encrypted contentEncryptionKey
     *
     * @throws [EncryptionException] when an error occurs while encrypting data
     */
    fun encryptContentEncryptionKey(
        contentEncryptionKey: ByteArray,
        publicKey: PublicKey
    ): ByteArray {
        try {
            // Create Cipher
            val rsaCipher = Cipher.getInstance(rsaAlgorithmMode)

            // Encrypt the ContentEncryptionKey with the publicKey
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)

            return rsaCipher.doFinal(contentEncryptionKey)
        } catch (e: Exception) {
            throw EncryptionException("Error while encrypting data ", e)
        }
    }

    /**
     * Encrypts a given String with a contentEncryptionKey and initializationVector using AES.
     *
     * @param payload the data which is encrypted
     * @param contentEncryptionKey the secret which is used for encrypting the payload
     * @param initializationVector the initializationVector which is used for encrypting the payload
     *
     * @return [ByteArray] of encrypted payload
     *
     * @throws [EncryptionException] when an error occurs while encrypting data
     */
    fun encryptPayload(
        payload: String,
        contentEncryptionKey: ByteArray,
        initializationVector: ByteArray
    ): ByteArray {
        try {
            // Create AES Cipher for encrypting payload
            val secretKey = SecretKeySpec(contentEncryptionKey, aesAlgorithmType)
            val ivParameter = IvParameterSpec(initializationVector)
            val aesCipher = Cipher.getInstance(aesAlgorithmMode)
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameter)

            return aesCipher.doFinal(payload.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            throw EncryptionException("Error while encrypting data ", e)
        }
    }

    /**
     * Concatenates multiple byteArrays into one [ByteArray].
     *
     * @param byteArrays the data which should be concatenated
     *
     * @return Concatenated [ByteArray]
     *
     * @throws [IOException] when there is an error writing to the ByteArrayOutputStream
     */
    fun concatenateByteArrays(vararg byteArrays: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()

        byteArrays.forEach { array ->
            outputStream.write(array)
        }

        return outputStream.toByteArray()
    }

    /**
     * Calculates the HMAC for the message represented by [ByteArray] hmacInput and the secure random key.
     *
     * @param key secure random key, used for encrypting the data
     * @param hmacInput the data to be encrypted
     *
     * @return encrypted data
     *
     * @throws [EncryptionException] when an error occurs while encrypting data
     */
    fun calculateHmac(key: ByteArray, hmacInput: ByteArray): ByteArray {
        try {
            val secretKey = SecretKeySpec(key, hmacAlgorithmType)
            val mac = Mac.getInstance(hmacAlgorithmType)
            mac.init(secretKey)

            return mac.doFinal(hmacInput)
        } catch (e: Exception) {
            throw EncryptionException("Error while encrypting data ", e)
        }
    }
}

