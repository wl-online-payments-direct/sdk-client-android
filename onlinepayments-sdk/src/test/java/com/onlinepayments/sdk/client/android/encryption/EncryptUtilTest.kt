package com.onlinepayments.sdk.client.android.encryption

import android.util.Base64
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import org.junit.AfterClass
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

/**
 * Junit Testclass which tests EncryptUtil's functions
 *
 * Copyright 2024 Global Collect Services B.V
 *
 */
@RunWith(MockitoJUnitRunner::class)
class EncryptUtilTest {
    private val encryptUtil = EncryptUtil()

    companion object {
        private lateinit var mockedBase64: MockedStatic<Base64>

        @BeforeClass
        @JvmStatic
        fun setup() {
            // Mock Base64
            mockedBase64 = mockStatic(Base64::class.java)
            `when`(
                Base64.decode(
                    ArgumentMatchers.any(ByteArray::class.java),
                    ArgumentMatchers.eq(Base64.DEFAULT)
                )
            ).thenAnswer { invocation ->
                java.util.Base64.getMimeDecoder().decode(invocation.arguments[0] as ByteArray)
            }
            `when`(
                Base64.encode(
                    ArgumentMatchers.any(),
                    ArgumentMatchers.eq(Base64.URL_SAFE)
                )
            ).thenAnswer { invocation ->
                java.util.Base64.getMimeEncoder().encode(invocation.arguments[0] as ByteArray)
            }
        }

        @AfterClass
        @JvmStatic
        fun close() {
            // Mocked static needs to be deregistered otherwise tests will fail when run again
            mockedBase64.close()
        }
    }

    @Test
    fun testBase64UrlEncode() {
        val expectedBase64UrlEncoded = "ZGF0YVRvQmVFbmNvZGVk"

        val dataToBeEncoded = "dataToBeEncoded".toByteArray()

        val base64UrlEncoded = encryptUtil.base64UrlEncode(dataToBeEncoded)

        assertTrue(expectedBase64UrlEncoded.contentEquals(base64UrlEncoded))
    }

    @Test
    fun testGenerateSecureRandomBytes() {
        val generatedSecureRandomBytesOne = encryptUtil.generateSecureRandomBytes(10)
        val generatedSecureRandomBytesTwo = encryptUtil.generateSecureRandomBytes(10)

        // The generated secure random bytes should be different each time it is executed
        assertFalse(generatedSecureRandomBytesOne.contentEquals(generatedSecureRandomBytesTwo))
    }

    @Test
    fun testEncryptContentEncryptionKey() {
        val contentEncryptionKey = "contentEncryptionKey".toByteArray()
        // This is a randomly generated RSA key, it is not used in our sdk
        val dummyPublicKeyString = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2f4YBFXsT5uxNbT2EwzXos01p\n" +
                "3g3ZjjmgQL6NEwcUqf4rtHpGvSVCcCmtLoCH/DPazF0masnQPA33zzMHvKT5h832\n" +
                "UADjaWD8ltajkmdJd+bxNlJD4FFEjFpslZaa7dusPp7CmXUkkJH/nI6N1IpISSDN\n" +
                "/V48f7hS8uHQsp6XJwIDAQAB"
        val publicKey = PublicKeyResponse("keyId", dummyPublicKeyString).publicKey

        val encryptedContentEncryptionKeyOne = encryptUtil.encryptContentEncryptionKey(contentEncryptionKey, publicKey)
        val encryptedContentEncryptionKeyTwo = encryptUtil.encryptContentEncryptionKey(contentEncryptionKey, publicKey)

        // The encrypted content encryption key should be different on each execution, because Cipher uses padding
        assertFalse(encryptedContentEncryptionKeyOne.contentEquals(encryptedContentEncryptionKeyTwo))
    }

    @Test
    fun testEncryptPayload() {
        val expectedEncryptedPayload = byteArrayOf(
            -25, -44, 98, -76, 111, 43, 84, -101, -118, -27, 79,
            -74, -90, -48, 48, -82, -110, -41, -113, 86, -120,
            93, 29, -13, -94, -18, 58, -84, 43, 42, 64, -121
        )

        val payload = "payloadToBeEncrypted"
        // ContentEncryptionKey & InitializationVector need to be either 16, 24 or 32 bytes
        val contentEncryptionKey = "contentEncryptionKey".toByteArray().copyOfRange(0, 16)
        val initializationVector = "initializationVector".toByteArray().copyOfRange(0, 16)

        val encryptedPayload = encryptUtil.encryptPayload(payload, contentEncryptionKey, initializationVector)

        assertTrue(expectedEncryptedPayload.contentEquals(encryptedPayload))
    }

    @Test
    fun testConcatenateByteArrays() {
        val expectedConcatenatedByteArray = byteArrayOf(
            98, 121, 116, 101, 65, 114, 114, 97, 121, 79, 110,
            101, 98, 121, 116, 101, 65, 114, 114, 97, 121, 84,
            119, 111, 98, 121, 116, 101, 65, 114, 114, 97, 121,
            84, 104, 114, 101, 101
        )

        val byteArrayOne = "byteArrayOne".toByteArray()
        val byteArrayTwo = "byteArrayTwo".toByteArray()
        val byteArrayThree = "byteArrayThree".toByteArray()

        val concatenatedByteArray = encryptUtil.concatenateByteArrays(byteArrayOne, byteArrayTwo, byteArrayThree)

        assertTrue(expectedConcatenatedByteArray.contentEquals(concatenatedByteArray))
    }

    @Test
    fun testCalculateHmac() {
        val expectedHmac = byteArrayOf(
            -65, -86, -22, 82, -103, 37, -85, 11, 56, -90, 39,
            17, 88, 2, -23, 29, -64, 22, -61, -31, -9, 84, -44,
            -5, 1, 75, -70, 50, 103, 105, 73, 5, 91, -4, 111, -37,
            -56, 117, 60, 119, 29, -51, -109, 71, 10, 32, 2, -48,
            -36, -64, -124, -111, 62, 39, 70, 32, -112, 50, 121,
            106, -1, -13, 83, -114
        )

        val key = "123publicKey546".toByteArray()
        val hmacInput = "hmacInput".toByteArray()
        val calculatedHmac = encryptUtil.calculateHmac(key,hmacInput)

        assertTrue(expectedHmac.contentEquals(calculatedHmac))
    }
}
