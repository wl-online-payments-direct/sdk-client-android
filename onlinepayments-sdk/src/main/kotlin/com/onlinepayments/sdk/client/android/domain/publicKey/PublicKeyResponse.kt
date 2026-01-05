/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.publicKey

import android.util.Base64
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

class PublicKeyResponse internal constructor(
    private val keyId: String? = null,
    private val publicKey: String? = null
) {
    private var parsedPublicKey: PublicKey? = null
    private val logger = LoggerProvider.logger

    fun getKeyId(): String? {
        return keyId
    }

    fun getPublicKey(): PublicKey? {
        if (parsedPublicKey != null) {
            return parsedPublicKey
        }

        if (publicKey != null) {
            try {
                val keyBytes: ByteArray? =
                    Base64.decode(publicKey.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
                val spec = X509EncodedKeySpec(keyBytes)
                val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM_TYPE)

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
        private val TAG: String = PublicKeyResponse::class.java.name
        private const val RSA_ALGORITHM_TYPE = "RSA"
    }
}
