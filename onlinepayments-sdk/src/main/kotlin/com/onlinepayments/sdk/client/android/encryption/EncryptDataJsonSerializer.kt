/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.encryption

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

internal class EncryptDataJsonSerializer : JsonSerializer<EncryptData> {
    override fun serialize(
        encryptData: EncryptData,
        type: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        val jsonObject = JsonObject()

        if (encryptData.tokenize) {
            jsonObject.addProperty("tokenize", true)
        }

        encryptData.paymentProductId?.let {
            jsonObject.addProperty("paymentProductId", it)
        }

        encryptData.accountOnFileId?.let {
            jsonObject.addProperty("accountOnFileId", it)
        }

        if (encryptData.clientSessionId.isNotEmpty()) {
            jsonObject.addProperty("clientSessionId", encryptData.clientSessionId)
        }

        if (encryptData.nonce.isNotEmpty()) {
            jsonObject.addProperty("nonce", encryptData.nonce)
        }

        val paymentValues = JsonArray()
        for (entry in encryptData.paymentValues.entries) {
            val paymentValue = JsonObject()
            paymentValue.addProperty("key", entry.key)
            paymentValue.addProperty("value", entry.value)
            paymentValues.add(paymentValue)
        }

        jsonObject.add("paymentValues", paymentValues)

        return jsonObject
    }
}
