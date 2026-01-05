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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

internal class EncryptDataJsonSerializer : JsonSerializer<RequestEncryptionData> {
    override fun serialize(
        requestEncryptionData: RequestEncryptionData,
        type: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()

        if (requestEncryptionData.tokenize) {
            jsonObject.addProperty("tokenize", true)
        }

        requestEncryptionData.paymentProductId?.let {
            jsonObject.addProperty("paymentProductId", it)
        }

        requestEncryptionData.accountOnFileId?.let {
            jsonObject.addProperty("accountOnFileId", it)
        }

        if (requestEncryptionData.clientSessionId.isNotEmpty()) {
            jsonObject.addProperty("clientSessionId", requestEncryptionData.clientSessionId)
        }

        if (requestEncryptionData.nonce.isNotEmpty()) {
            jsonObject.addProperty("nonce", requestEncryptionData.nonce)
        }

        val paymentValues = JsonArray()
        for (entry in requestEncryptionData.paymentValues.entries) {
            val paymentValue = JsonObject()
            paymentValue.addProperty("key", entry.key)
            paymentValue.addProperty("value", entry.value)
            paymentValues.add(paymentValue)
        }

        jsonObject.add("paymentValues", paymentValues)

        return jsonObject
    }
}

