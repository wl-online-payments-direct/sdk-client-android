package com.onlinepayments.sdk.client.android.encryption

import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Junit Testclass which tests EncryptDataJsonSerializer
 *
 * Copyright 2024 Global Collect Services B.V
 *
 */
class EncryptDataJsonSerializerTest {
    @Test
    fun testSerialization() {
        val expectedPayload = "{\"paymentProductId\":1,\"clientSessionId\":\"clientSessionId\",\"nonce\":\"nonce\"," +
                "\"paymentValues\":[{\"key\":\"cardNumber\",\"value\":\"4012000033330026\"}," +
                "{\"key\":\"cardholderName\",\"value\":\"Test User\"},{\"key\":\"cvv\",\"value\":\"123\"}," +
                "{\"key\":\"expiryDate\",\"value\":\"1225\"}]}"

        // Convert EncryptData to JSON format
        val paymentValues = mapOf(
            "cardNumber" to "4012000033330026",
            "cardholderName" to "Test User",
            "cvv" to "123",
            "expiryDate" to "1225"
        )
        val encryptData = EncryptData(null, "clientSessionId", "nonce", 1, false, paymentValues)
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(EncryptData::class.java, EncryptDataJsonSerializer())
        val gson = gsonBuilder.create()
        val payload = gson.toJson(encryptData)

        assertEquals(expectedPayload, payload)
    }
}
