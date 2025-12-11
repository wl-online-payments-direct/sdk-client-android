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

import android.util.Base64;

import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

/**
 * Junit Test class which tests EncryptUtil's functions
 */
@RunWith(MockitoJUnitRunner.class)
public class EncryptUtilJavaTest {

    private final EncryptUtil encryptUtil = new EncryptUtil();

    private static MockedStatic<Base64> mockedBase64;

    @BeforeClass
    public static void setup() {
        // Mock Base64
        mockedBase64 = Mockito.mockStatic(Base64.class);
        mockedBase64.when(() -> Base64.decode(
            ArgumentMatchers.any(byte[].class),
            ArgumentMatchers.eq(Base64.DEFAULT)
        )).thenAnswer(invocation -> {
            byte[] input = invocation.getArgument(0);
            return java.util.Base64.getMimeDecoder().decode(input);
        });
        mockedBase64.when(() -> Base64.encode(
            ArgumentMatchers.any(byte[].class),
            ArgumentMatchers.eq(Base64.URL_SAFE)
        )).thenAnswer(invocation -> {
            byte[] input = invocation.getArgument(0);
            return java.util.Base64.getMimeEncoder().encode(input);
        });
    }

    @AfterClass
    public static void close() {
        // Mocked static needs to be deregistered otherwise tests will fail when run again
        mockedBase64.close();
    }

    @Test
    public void testBase64UrlEncode() {
        String expectedBase64UrlEncoded = "ZGF0YVRvQmVFbmNvZGVk";

        byte[] dataToBeEncoded = "dataToBeEncoded".getBytes();

        String base64UrlEncoded = encryptUtil.base64UrlEncode(dataToBeEncoded);

        Assert.assertTrue(expectedBase64UrlEncoded.contentEquals(base64UrlEncoded));
    }

    @Test
    public void testGenerateSecureRandomBytes() {
        byte[] generatedSecureRandomBytesOne = encryptUtil.generateSecureRandomBytes(10);
        byte[] generatedSecureRandomBytesTwo = encryptUtil.generateSecureRandomBytes(10);

        // The generated secure random bytes should be different each time it is executed
        Assert.assertNotEquals(generatedSecureRandomBytesOne, generatedSecureRandomBytesTwo);
    }

    @Test
    public void testEncryptContentEncryptionKey() {
        byte[] contentEncryptionKey = "contentEncryptionKey".getBytes();
        // This is a randomly generated RSA key, it is not used in our sdk
        String dummyPublicKeyString = """
            MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2f4YBFXsT5uxNbT2EwzXos01p
            3g3ZjjmgQL6NEwcUqf4rtHpGvSVCcCmtLoCH/DPazF0masnQPA33zzMHvKT5h832
            UADjaWD8ltajkmdJd+bxNlJD4FFEjFpslZaa7dusPp7CmXUkkJH/nI6N1IpISSDN
            /V48f7hS8uHQsp6XJwIDAQAB""";
        PublicKeyResponse publicKeyResponse = new PublicKeyResponse("keyId", dummyPublicKeyString);
        java.security.PublicKey publicKey = publicKeyResponse.getPublicKey();

        assert publicKey != null;
        byte[] encryptedContentEncryptionKeyOne = encryptUtil.encryptContentEncryptionKey(
            contentEncryptionKey,
            publicKey
        );
        byte[] encryptedContentEncryptionKeyTwo = encryptUtil.encryptContentEncryptionKey(
            contentEncryptionKey,
            publicKey
        );

        // The encrypted content encryption key should be different on each execution, because Cipher uses padding
        Assert.assertNotEquals(encryptedContentEncryptionKeyOne, encryptedContentEncryptionKeyTwo);
    }

    @Test
    public void testEncryptPayload() {
        byte[] expectedEncryptedPayload = new byte[]{
            -25, -44, 98, -76, 111, 43, 84, -101, -118, -27, 79,
            -74, -90, -48, 48, -82, -110, -41, -113, 86, -120,
            93, 29, -13, -94, -18, 58, -84, 43, 42, 64, -121
        };

        String payload = "payloadToBeEncrypted";
        // ContentEncryptionKey & InitializationVector need to be either 16, 24 or 32 bytes
        byte[] contentEncryptionKey = "contentEncryptionKey".getBytes();
        byte[] initializationVector = "initializationVector".getBytes();
        contentEncryptionKey = Arrays.copyOfRange(contentEncryptionKey, 0, 16);
        initializationVector = Arrays.copyOfRange(initializationVector, 0, 16);

        byte[] encryptedPayload = encryptUtil.encryptPayload(
            payload,
            contentEncryptionKey,
            initializationVector
        );

        Assert.assertArrayEquals(expectedEncryptedPayload, encryptedPayload);
    }

    @Test
    public void testConcatenateByteArrays() {
        byte[] expectedConcatenatedByteArray = new byte[]{
            98, 121, 116, 101, 65, 114, 114, 97, 121, 79, 110,
            101, 98, 121, 116, 101, 65, 114, 114, 97, 121, 84,
            119, 111, 98, 121, 116, 101, 65, 114, 114, 97, 121,
            84, 104, 114, 101, 101
        };

        byte[] byteArrayOne = "byteArrayOne".getBytes();
        byte[] byteArrayTwo = "byteArrayTwo".getBytes();
        byte[] byteArrayThree = "byteArrayThree".getBytes();

        byte[] concatenatedByteArray = encryptUtil.concatenateByteArrays(
            byteArrayOne,
            byteArrayTwo,
            byteArrayThree
        );

        Assert.assertArrayEquals(expectedConcatenatedByteArray, concatenatedByteArray);
    }

    @Test
    public void testCalculateHmac() {
        String expectedHmac = Arrays.toString(new byte[]{
            -65, -86, -22, 82, -103, 37, -85, 11, 56, -90, 39,
            17, 88, 2, -23, 29, -64, 22, -61, -31, -9, 84, -44,
            -5, 1, 75, -70, 50, 103, 105, 73, 5, 91, -4, 111, -37,
            -56, 117, 60, 119, 29, -51, -109, 71, 10, 32, 2, -48,
            -36, -64, -124, -111, 62, 39, 70, 32, -112, 50, 121,
            106, -1, -13, 83, -114
        });

        byte[] key = "123publicKey546".getBytes();
        byte[] hmacInput = "hmacInput".getBytes();
        String calculatedHmac = Arrays.toString(encryptUtil.calculateHmac(key, hmacInput));

        Assert.assertEquals(expectedHmac, calculatedHmac);
    }
}