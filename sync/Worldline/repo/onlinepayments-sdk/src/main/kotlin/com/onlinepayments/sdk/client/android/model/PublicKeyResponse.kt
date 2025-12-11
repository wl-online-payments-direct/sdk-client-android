/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model

import android.util.Base64
import com.onlinepayments.sdk.client.android.providers.LoggerProvider
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

/**
 * POJO that holds the PublicKey call response from the Online Payments gateway.
 */
class PublicKeyResponse internal constructor(
    private val keyId: String?,
    private val publicKey: String?
) {
    private var parsedPublicKey: PublicKey? = null

    private val logger = LoggerProvider.logger

    /**
     * Gets the keyId.
     *
     * @return String keyId
     */
    fun getKeyId(): String? {
        return keyId
    }

    /**
     * Gets the PublicKey.
     *
     * @return PublicKey
     */
    fun getPublicKey(): PublicKey? {
        // If parsedPublicKey is already parsed from the publicKey string return it.
        if (parsedPublicKey != null) {
            return parsedPublicKey
        }

        // Else parse the publicKey string to a PublicKey object
        if (publicKey != null) {
            try {
                // Decode base64 and convert the String to a PublicKey instance

                val keyBytes: ByteArray? =
                    Base64.decode(publicKey.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
                val spec = X509EncodedKeySpec(keyBytes)
                val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM_TYPE)

                // Store the result in parsedPublicKey and return it
                parsedPublicKey = keyFactory.generatePublic(spec)

                return parsedPublicKey
            } catch (e: NoSuchAlgorithmException) {
                logger.i(TAG, "Error parsing publicKey response to public key, ${e.message}")
            } catch (e: InvalidKeySpecException) {
                logger.i(TAG, "Error parsing publicKey response to public key, ${e.message}")
            }
        }

        return null
    }

    companion object {
        // Tag used for logging
        private val TAG: String = PublicKeyResponse::class.java.getName()

        // Algorithm type for converting publicKey string to publicKey object
        private const val RSA_ALGORITHM_TYPE = "RSA"
    }
}
