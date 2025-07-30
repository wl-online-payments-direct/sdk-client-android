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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JUnit Test class which tests EncryptDataJsonSerializer
 */
public class EncryptDataJsonSerializerJavaTest {

    @Test
    public void testSerialization() {
        String expectedPayload = "{\"paymentProductId\":1,\"clientSessionId\":\"clientSessionId\",\"nonce\":\"nonce\"," + "\"paymentValues\":[{\"key\":\"cardNumber\",\"value\":\"4012000033330026\"}," + "{\"key\":\"cardholderName\",\"value\":\"Test User\"},{\"key\":\"cvv\",\"value\":\"123\"}," + "{\"key\":\"expiryDate\",\"value\":\"1225\"}]}";

        String payload = getPayload();

        JsonObject expectedObj = JsonParser.parseString(expectedPayload).getAsJsonObject();
        JsonObject actualObj = JsonParser.parseString(payload).getAsJsonObject();

        // Extract the paymentValues arrays
        JsonArray expectedArray = expectedObj.getAsJsonArray("paymentValues");
        JsonArray actualArray = actualObj.getAsJsonArray("paymentValues");

        // Remove paymentValues from each object so we can compare the rest
        expectedObj.remove("paymentValues");
        actualObj.remove("paymentValues");

        // 1) Compare all other fields (paymentProductId, clientSessionId, nonce, etc.)
        Assert.assertEquals("Non-array fields differ", expectedObj, actualObj);

        // 2) Compare paymentValues ignoring order
        // Convert each array element to a String and store in a set
        Set<String> expectedSet = new HashSet<>();
        for (JsonElement element : expectedArray) {
            expectedSet.add(element.toString());
        }

        Set<String> actualSet = new HashSet<>();
        for (JsonElement element : actualArray) {
            actualSet.add(element.toString());
        }

        Assert.assertEquals(expectedSet, actualSet);
    }

    private static String getPayload() {
        Map<String, String> paymentValues = new HashMap<>();
        paymentValues.put("cardNumber", "4012000033330026");
        paymentValues.put("expiryDate", "1225");
        paymentValues.put("cvv", "123");
        paymentValues.put("cardholderName", "Test User");

        EncryptData encryptData = new EncryptData(null,
            "clientSessionId",
            "nonce",
            1,
            false,
            paymentValues
        );

        // Build Gson with our custom serializer
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EncryptData.class, new EncryptDataJsonSerializer());
        Gson gson = gsonBuilder.create();

        // Convert EncryptData to JSON
        return gson.toJson(encryptData);
    }
}
