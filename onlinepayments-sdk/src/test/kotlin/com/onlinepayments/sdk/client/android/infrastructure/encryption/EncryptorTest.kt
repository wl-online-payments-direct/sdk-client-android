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

import com.onlinepayments.sdk.client.android.domain.publicKey.PublicKeyResponse
import com.onlinepayments.sdk.client.android.mocks.MockEncoding
import io.mockk.unmockkAll
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.reflect.Whitebox
import java.security.PublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Junit Test class which tests Encryptor.encrypt function
 */
@RunWith(MockitoJUnitRunner::class)
@PrepareForTest(EncryptionUtil::class)
class EncryptorTest {
    companion object {
        private fun <T> any(type: Class<T>): T = Mockito.any(type)

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
        private val dummyByteArray = byteArrayOf(
            54, -85, -42, -40, -113, 62, -33, 111, 43, 48, 82, 1, 80, 42,
            -119, 26, 58, 117, 68, 1, 43, -5, -58, 40, 98, 104, -52, 85,
            -44, -77, 110, 12, -87, -69, -115, 48, 22, 58, -25, -85, -20,
            115, 116, 62, -25, -84, 0, 116, -92, 108, 23, -79, 37, 127, 8,
            107, 52, 120, 34, 97, 22, -8, 82, -84, 47, -5, 111, -121, 55,
            44, 97, -106, -6, 113, 25, 60, -33, -18, 31, 21, -108, 7, -65,
            -100, 32, -121, -115, -53, -78, -53, -101, -127, -8, 6, -114,
            115, 60, -68, -25, -49, -96, 13, 72, 91, -59, -117, -109, 111,
            41, -95, 101, 23, 11, -96, 19, -100, 37, -40, -108, -37, 119,
            -79, 65, 18, 33, -47, 75, -7
        )

        @BeforeClass
        @JvmStatic
        fun setup() {
            // Mock Base64
            MockEncoding.setup()

            // Mock EncryptUtil
            val mockEncryptionUtil = mock(EncryptionUtil::class.java)
            Whitebox.setInternalState(mockEncryptionUtil, "aesAlgorithmType", "AES")
            Whitebox.setInternalState(mockEncryptionUtil, "aesAlgorithmMode", "AES/CBC/PKCS5Padding")
            Whitebox.setInternalState(
                mockEncryptionUtil,
                "rsaAlgorithmMode",
                "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"
            )
            Whitebox.setInternalState(mockEncryptionUtil, "hmacAlgorithmType", "HmacSHA512")
            Whitebox.setInternalState(encryptor, "encryptionUtil", mockEncryptionUtil)

            `when`(mockEncryptionUtil.base64UrlEncode(any(ByteArray::class.java))).thenCallRealMethod()
            `when`(
                mockEncryptionUtil.concatenateByteArrays(
                    any(ByteArray::class.java),
                    any(ByteArray::class.java),
                    any(ByteArray::class.java),
                    any(ByteArray::class.java)
                )
            ).thenCallRealMethod()
            `when`(
                mockEncryptionUtil.encryptPayload(
                    any(String::class.java),
                    any(ByteArray::class.java),
                    any(ByteArray::class.java)
                )
            ).thenCallRealMethod()
            `when`(
                mockEncryptionUtil.calculateHmac(
                    any(ByteArray::class.java),
                    any(ByteArray::class.java)
                )
            ).thenCallRealMethod()

            // These functions are mocked so they return the same 'random' bytes on each execution
            `when`(mockEncryptionUtil.generateSecureRandomBytes(64)).thenReturn(
                dummyByteArray.copyOfRange(0, 64)
            )
            `when`(mockEncryptionUtil.generateSecureRandomBytes(16)).thenReturn(
                dummyByteArray.copyOfRange(0, 16)
            )
            `when`(
                mockEncryptionUtil.encryptContentEncryptionKey(
                    any(ByteArray::class.java), any(
                        PublicKey::class.java
                    )
                )
            ).thenReturn(dummyByteArray)
        }

        @AfterClass
        @JvmStatic
        fun close() {
            // Cleanup MockK mocks
            unmockkAll()
        }
    }

    @Test
    fun testEncrypt() {
        val paymentValues = mapOf(
            "cardNumber" to "4012000033330026",
            "cardholderName" to "Test User",
            "cvv" to "123",
            "expiryDate" to "1225"
        )
        val requestEncryptionData = RequestEncryptionData(null, "clientSessionId", "nonce", 1, false, paymentValues)

        val expectedEncryptedString = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZDQkMtSFM1MTIiLC" +
            "JraWQiOiJrZXlJZCJ9.NqvW2I8+328rMFIBUCqJGjp1RAEr+8YoYmjMVdSzbgypu40wFjrnq" +
            "+xzdD7nrAB0pGwXsSV/CGs0eCJhFvhSrC/7b4c3LGGW+nEZPN/uHxWUB7+cIIeNy7LLm4H4Bo" +
            "5zPLznz6ANSFvFi5NvKaFlFwugE5wl2JTbd7FBEiHRS/k.NqvW2I8+328rMFIBUCqJGg./g4XGB1Ll" +
            "EPt3T76cR6yKutFjhg8UYO4ctRXGWOObjDQcPf44++XiUmvl0bfEMCtU3rjBrnb7UoUbIhN2d" +
            "qQyS+POwWoqpwcQAKXr2lHBBTDMf+1bXecEdCRGaN6ou7/fPbYq5sLLfesh2H6RBhH5CKtmRIv" +
            "SJ6gUgPg3PVC746ho+veKiqnozr76HCn+1G2M3NY0jwOv1pGOkd+lVQ/K5f/jN3ekrHd57VTqm" +
            "81WkqxSThL1vkf/V5R4lFdcPkRFNa72t0RqWNoIrTRpcHVIFrU6IXkVUiTEa47je4L/FjrnqFo" +
            "MHUVZVMGpzufPFQudL9qySDRh7so3hA1CyluWw.RsCX5oRBm/qcjOAPD/9DxBx6QbGFQSPqF0lci1SBF2Y"
        val encryptedString = encryptor.encrypt(requestEncryptionData).lines().joinToString("")

        assertEquals(expectedEncryptedString, encryptedString)
    }
}
