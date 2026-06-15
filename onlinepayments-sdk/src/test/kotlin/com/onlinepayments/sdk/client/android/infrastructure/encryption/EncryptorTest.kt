/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.encryption

import com.onlinepayments.sdk.client.android.domain.exceptions.EncryptionException
import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Junit Test class which tests Encryptor.encrypt function
 */
@RunWith(RobolectricTestRunner::class)
class EncryptorTest {
    companion object {
        // This is a randomly generated RSA key, it is not used in our sdk
        private const val DUMMY_PUBLIC_KEY_STRING =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2f4YBFXsT5uxNbT2EwzXos01p\n" +
                "3g3ZjjmgQL6NEwcUqf4rtHpGvSVCcCmtLoCH/DPazF0masnQPA33zzMHvKT5h832\n" +
                "UADjaWD8ltajkmdJd+bxNlJD4FFEjFpslZaa7dusPp7CmXUkkJH/nI6N1IpISSDN\n" +
                "/V48f7hS8uHQsp6XJwIDAQAB"

        private val publicKeyResponse = PublicKeyResponse(
            keyId = "keyId",
            publicKey = DUMMY_PUBLIC_KEY_STRING
        )
        private val encryptor = Encryptor(publicKeyResponse)

    }

    @Test
    fun `encrypt should produce a valid JWE compact serialisation`() {
        val paymentValues = mapOf(
            "cardNumber" to "4012000033330026",
            "cardholderName" to "Test User",
            "cvv" to "123",
            "expiryDate" to "1225"
        )
        val requestEncryptionData = RequestEncryptionData(null, "clientSessionId", "nonce", 1, false, paymentValues)

        val encryptedString = encryptor.encrypt(requestEncryptionData).lines().joinToString("")

        // JWE compact serialisation has exactly 5 dot-separated base64url segments
        val segments = encryptedString.split(".")
        assertEquals(5, segments.size, "JWE must have exactly 5 segments")
        segments.forEach { segment ->
            assertTrue(segment.isNotEmpty(), "Each JWE segment must be non-empty")
        }
        // No standard (non-URL-safe) base64 padding characters in any segment
        assertTrue(!encryptedString.contains('+') && !encryptedString.contains('/'),
            "JWE must use URL-safe base64url encoding (no '+' or '/')")
    }

    @Test
    fun `encrypt with accountOnFile produces different output`() {
        val paymentValues = mapOf("cardNumber" to "4012000033330026")
        val withAccountOnFile = RequestEncryptionData("1234", "clientSessionId", "nonce", 1, false, paymentValues)
        val withoutAccountOnFile = RequestEncryptionData(null, "clientSessionId", "nonce", 1, false, paymentValues)

        val encryptedWith = encryptor.encrypt(withAccountOnFile).lines().joinToString("")
        val encryptedWithout = encryptor.encrypt(withoutAccountOnFile).lines().joinToString("")

        assertNotEquals(encryptedWith, encryptedWithout)
    }

    @Test
    fun `encrypt with tokenize flag produces different output`() {
        val paymentValues = mapOf("cardNumber" to "4012000033330026")
        val withTokenize = RequestEncryptionData(null, "clientSessionId", "nonce", 1, true, paymentValues)
        val withoutTokenize = RequestEncryptionData(null, "clientSessionId", "nonce", 1, false, paymentValues)

        val encryptedWith = encryptor.encrypt(withTokenize).lines().joinToString("")
        val encryptedWithout = encryptor.encrypt(withoutTokenize).lines().joinToString("")

        assertNotEquals(encryptedWith, encryptedWithout)
    }

    @Test
    fun `encryptTokenRequest with missing payment product id should throw EncryptionException`() {
        val paymentValues = mapOf(
            "cardNumber" to "42424242424242"
        )

        val requestEncryptionData = RequestEncryptionData(
            null,
            "clientSessionId",
            "nonce",
            null,
            false,
            paymentValues
        )

        val exception = assertFailsWith<EncryptionException> {
            encryptor.encryptTokenRequest(requestEncryptionData)
        }

        assertEquals(
            "Error encrypting credit card token request: the payment product ID not set.",
            exception.message
        )
    }
}