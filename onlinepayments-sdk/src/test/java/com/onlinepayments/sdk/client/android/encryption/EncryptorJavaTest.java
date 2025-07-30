/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.encryption;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JUnit Test class which tests Encryptor.encrypt function
 */
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(EncryptUtil.class)
public class EncryptorJavaTest {

    private static MockedStatic<Base64> mockedBase64;

    // This is a randomly generated RSA key, it is not used in our sdk
    private static final String DUMMY_PUBLIC_KEY_STRING =
        """
            MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2f4YBFXsT5uxNbT2EwzXos01p
            3g3ZjjmgQL6NEwcUqf4rtHpGvSVCcCmtLoCH/DPazF0masnQPA33zzMHvKT5h832
            UADjaWD8ltajkmdJd+bxNlJD4FFEjFpslZaa7dusPp7CmXUkkJH/nI6N1IpISSDN
            /V48f7hS8uHQsp6XJwIDAQAB""";

    private static final PublicKeyResponse PUBLIC_KEY_RESPONSE = new PublicKeyResponse(
        "keyId",
        DUMMY_PUBLIC_KEY_STRING
    );

    private static final Encryptor encryptor = new Encryptor(PUBLIC_KEY_RESPONSE);

    private static final byte[] DUMMY_BYTE_ARRAY = new byte[]{
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
    };

    @BeforeClass
    public static void setup() {
        // Mock android.util.Base64
        mockedBase64 = mockStatic(Base64.class);

        when(Base64.decode(any(byte[].class), eq(Base64.DEFAULT)))
            .thenAnswer(invocation -> {
                // decode using Java's Base64 to mimic actual behavior
                byte[] input = (byte[]) invocation.getArguments()[0];
                return java.util.Base64.getMimeDecoder().decode(input);
            });

        when(Base64.encode(any(byte[].class), eq(Base64.URL_SAFE)))
            .thenAnswer(invocation -> {
                // encode using Java's Base64
                byte[] input = (byte[]) invocation.getArguments()[0];
                return java.util.Base64.getMimeEncoder().encode(input);
            });

        // Mock EncryptUtil
        EncryptUtil mockEncryptUtil = mock(EncryptUtil.class);
        Whitebox.setInternalState(mockEncryptUtil, "aesAlgorithmType", "AES");
        Whitebox.setInternalState(mockEncryptUtil, "aesAlgorithmMode", "AES/CBC/PKCS5Padding");
        Whitebox.setInternalState(
            mockEncryptUtil,
            "rsaAlgorithmMode",
            "RSA/ECB/OAEPWithSHA-1AndMGF1Padding"
        );
        Whitebox.setInternalState(mockEncryptUtil, "hmacAlgorithmType", "HmacSHA512");

        Whitebox.setInternalState(encryptor, "encryptUtil", mockEncryptUtil);

        // Pass-through or real-method calls for these:
        when(mockEncryptUtil.base64UrlEncode(any(byte[].class)))
            .thenCallRealMethod();

        when(mockEncryptUtil.concatenateByteArrays(
            any(byte[].class),
            any(byte[].class),
            any(byte[].class),
            any(byte[].class)
        ))
            .thenCallRealMethod();

        when(mockEncryptUtil.encryptPayload(
            any(String.class),
            any(byte[].class),
            any(byte[].class)
        ))
            .thenCallRealMethod();

        when(mockEncryptUtil.calculateHmac(any(byte[].class), any(byte[].class)))
            .thenCallRealMethod();

        // These functions are mocked so they return the same 'random' bytes on each execution
        when(mockEncryptUtil.generateSecureRandomBytes(64))
            .thenReturn(Arrays.copyOfRange(DUMMY_BYTE_ARRAY, 0, 64));

        when(mockEncryptUtil.generateSecureRandomBytes(16))
            .thenReturn(Arrays.copyOfRange(DUMMY_BYTE_ARRAY, 0, 16));

        when(mockEncryptUtil.encryptContentEncryptionKey(
            any(byte[].class),
            any(PublicKey.class)
        ))
            .thenReturn(DUMMY_BYTE_ARRAY);
    }

    @AfterClass
    public static void close() {
        // Mocked static needs to be deregistered so it won't affect other tests
        mockedBase64.close();
    }

    @Test
    public void testEncrypt() {
        EncryptData encryptData = getEncryptData();

        // Expected value
        String expectedEncryptedString =
            "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZDQkMtSFM1MTIiLCJraWQiOiJrZXlJZCJ9.NqvW2I8+328" +
                "rMFIBUCqJGjp1RAEr+8YoYmjMVdSzbgypu40wFjrnq+xzdD7nrAB0pGwXsSV/CGs0eCJhFvhSrC/7b4c" +
                "3LGGW+nEZPN/uHxWUB7+cIIeNy7LLm4H4Bo5zPLznz6ANSFvFi5NvKaFlFwugE5wl2JTbd7FBEiHRS/k" +
                ".NqvW2I8+328rMFIBUCqJGg./g4XGB1LlEPt3T76cR6yKutFjhg8UYO4ctRXGWOObjDQcPf44++XiUmv" +
                "l0bfEMCtU3rjBrnb7UoUbIhN2dqQyS+POwWoqpwcQAKXr2lHBBTDMf+1bXecEdCRGaN6ou7/fPbYq5sL" +
                "Lfesh2H6RBhH5CKtmRIvSJ6gUgPg3PVC746ho+veKiqnozr76HCn+1G2M3NY0jwOv1pGOkd+lVQ/K5f/" +
                "jN3ekrHd57VTqm81WkqxSThL1vkf/V5R4lFdcPkRFNa72t0RqWNoIrTRpcHVIFrU6IXkVUiTEa47je4L" +
                "/FjrnqFoMHUVZVMGpzufPFQudL9qySDRh7so3hA1CyluWw.RsCX5oRBm/qcjOAPD/9DxBx6QbGFQSPqF" +
                "0lci1SBF2Y";

        // Perform encryption
        String encryptedString = encryptor.encrypt(encryptData)
            .replaceAll("\n", "")
            .replaceAll("\r", "");

        // Compare
        assertEquals(expectedEncryptedString, encryptedString);
    }

    @NonNull
    private static EncryptData getEncryptData() {
        Map<String, String> paymentValues = new LinkedHashMap<>();
        paymentValues.put("cardNumber", "4012000033330026");
        paymentValues.put("cardholderName", "Test User");
        paymentValues.put("cvv", "123");
        paymentValues.put("expiryDate", "1225");


        return new EncryptData(null, "clientSessionId", "nonce", 1, false, paymentValues);
    }
}
