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

import com.google.gson.GsonBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Junit Test class which tests EncryptDataJsonSerializer
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
        val requestEncryptionData = RequestEncryptionData(null, "clientSessionId", "nonce", 1, false, paymentValues)
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(RequestEncryptionData::class.java, EncryptDataJsonSerializer())
        val gson = gsonBuilder.create()
        val payload = gson.toJson(requestEncryptionData)

        assertEquals(expectedPayload, payload)
    }
}
