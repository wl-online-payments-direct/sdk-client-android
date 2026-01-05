/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.integration.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.onlinepayments.authentication.V1HmacAuthenticator
import com.onlinepayments.communication.RequestHeader
import com.onlinepayments.domain.CreatedTokenResponse
import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties
import kotlin.io.encoding.Base64

/**
 * Helper for creating sessions using the Java SDK authenticator.
 * Uses the Java SDK's V1HmacAuthenticator for signing requests.
 */
object ServerApiHelper {

    private val client = OkHttpClient()
    private val gson = Gson()

    const val CONTENT_TYPE = "application/json; charset=utf-8"

    private fun getPlatformIdentifier(): String {
        val properties: Properties = System.getProperties()
        val sb = StringBuilder()
        sb.append(properties.get("os.name"))
        sb.append("/")
        sb.append(properties.get("os.version"))
        sb.append(" ")
        sb.append("Java")
        sb.append("/")
        sb.append(properties.get("java.vm.specification.version"))
        sb.append(" ")
        sb.append("(")
        sb.append(properties.get("java.vm.vendor"))
        sb.append("; ")
        sb.append(properties.get("java.vm.name"))
        sb.append("; ")
        sb.append(properties.get("java.version"))
        sb.append(")")

        return sb.toString()
    }

    private fun prepareRequest(path: String, body: RequestBody): Request {
        val authenticator = V1HmacAuthenticator(TestConfig.apiKeyId, TestConfig.apiSecret)

        // Prepare request
        val url = "https://${TestConfig.host}/v2/${TestConfig.merchantId}${path}"
        val uri = URI("/v2/${TestConfig.merchantId}${path}")

        // Build headers for authentication
        val timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)

        // Prepare metadata header
        val metadataHeader = "{" +
            "\"platformIdentifier\": \"${getPlatformIdentifier()}\"," +
            "\"sdkIdentifier\": \"${Constants.SDK_IDENTIFIER}\"," +
            "\"sdkCreator\": \"OnlinePayments\"," +
            "\"integrator\": \"AndroidSDK\"," +
            "\"shoppingCartExtension\": {}" +
            "}"
        val encodedMetadata = Base64.encode(metadataHeader.toByteArray())

        // Build headers list for signature calculation (must include X-GCS headers)
        val requestHeaders = listOf(
            RequestHeader("Content-Type", CONTENT_TYPE),
            RequestHeader("Date", timestamp),
            RequestHeader("X-GCS-ServerMetaInfo", encodedMetadata)
        )

        // Get authorization header from Java SDK authenticator
        val authorization = authenticator.getAuthorization("POST", uri, requestHeaders)

        // Build OkHttp request
        return Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", CONTENT_TYPE)
            .header("Date", timestamp)
            .header("Authorization", authorization)
            .header("X-GCS-ServerMetaInfo", encodedMetadata)
            .build()
    }

    /**
     * Create a session using direct HTTP with Java SDK authentication.
     */
    private fun createSession(): SessionData {
        val jsonBody = "{}"
        val body = jsonBody.toRequestBody(CONTENT_TYPE.toMediaType())
        val request = prepareRequest("/sessions", body)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "Failed to create session: ${response.code} ${response.message}\n" +
                        "Body: ${response.body.string()}"
                )
            }

            val responseBody = response.body.string()

            return gson.fromJson(responseBody, SessionData::class.java)
        }
    }

    fun createToken(encryptedCustomerInput: String): CreatedTokenResponse {
        val jsonBody = """{
                "encryptedCustomerInput": "$encryptedCustomerInput"
            }"""
        val body = jsonBody.toRequestBody(CONTENT_TYPE.toMediaType())
        val request = prepareRequest("/tokens", body)

        // Execute request
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "Failed to create token: ${response.code} ${response.message}\n" +
                        "Body: ${response.body.string()}"
                )
            }

            val responseBody = response.body.string()

            return gson.fromJson(responseBody, CreatedTokenResponse::class.java)
        }
    }

    fun createPayment(
        encryptedCustomerInput: String,
        amount: Long = 1000,
        currencyCode: String = "EUR",
        countryCode: String = "BE"
    ): JsonObject {
        val jsonBody = getPaymentBody(encryptedCustomerInput, amount, currencyCode, countryCode)
        val body = jsonBody.toRequestBody(CONTENT_TYPE.toMediaType())
        val request = prepareRequest("/payments", body)

        // Execute request
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException(
                    "Failed to create payment: ${response.code} ${response.message}\n" +
                        "Body: ${response.body.string()}"
                )
            }

            val responseBody = response.body.string()

            return gson.fromJson(responseBody, JsonObject::class.java)
        }
    }

    private fun getPaymentBody(
        encryptedCustomerInput: String,
        amount: Long,
        currencyCode: String,
        countryCode: String
    ): String {
        return """{
            "encryptedCustomerInput": "$encryptedCustomerInput",
            "order": {
                "amountOfMoney": {
                    "amount": $amount,
                    "currencyCode": "$currencyCode"
                },
                "customer": {
                    "billingAddress": {
                        "countryCode": "$countryCode"
                    },
                    "contactDetails": {
                        "emailAddress": "wile.e.coyote@acmelabs.com",
                        "phoneNumber": "+321234567890"
                    },
                    "device": {
                        "acceptHeader": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                        "browserData": {
                            "colorDepth": 99,
                            "javaEnabled": true,
                            "javaScriptEnabled": true,
                            "screenHeight": "768",
                            "screenWidth": "1024"
                        },
                        "ipAddress": "123.123.123.123",
                        "locale": "en_GB",
                        "userAgent": "Mozilla/5.0(WindowsNT10.0;Win64;x64)AppleWebKit/537.36(KHTML,likeGecko)Chrome/75.0.3770.142Safari/537.36",
                        "timezoneOffsetUtcMinutes": "-180"
                    }
                },
                "shipping": {
                    "addressIndicator": "same-as-billing",
                    "emailAddress": "wile.e.coyote@acmelabs.com",
                    "firstUsageDate": "20100101",
                    "isFirstUsage": false,
                    "method": {
                        "details": "quickshipment",
                        "name": "fast-delivery",
                        "speed": 24,
                        "type": "carrier-low-cost"
                    },
                    "type": "overnight",
                    "shippingCost": 0,
                    "shippingCostTax": 0
                }
            }
        }"""
    }

    /**
     * Cache for session data to avoid excessive API calls during test runs.
     */
    private var cachedSession: SessionData? = null
    private var cacheTimestamp: Long = 0
    private const val CACHE_DURATION_MS = 30 * 60 * 1000 // 30 minutes

    /**
     * Get a session, using cache if available and not expired.
     */
    fun getCachedSession(forceRefresh: Boolean = false): SessionData {
        val now = System.currentTimeMillis()
        val isExpired = (now - cacheTimestamp) > CACHE_DURATION_MS

        if (forceRefresh || cachedSession == null || isExpired) {
            cachedSession = createSession()
            cacheTimestamp = now
        }

        return cachedSession!!
    }
}
