/*
 * Copyright 2024 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.communicate

import android.util.Log
import com.google.gson.Gson
import com.onlinepayments.sdk.client.android.exception.CommunicationException
import com.onlinepayments.sdk.client.android.extensions.addMetaData
import com.onlinepayments.sdk.client.android.extensions.addSessionId
import com.onlinepayments.sdk.client.android.extensions.close
import com.onlinepayments.sdk.client.android.extensions.is200Result
import com.onlinepayments.sdk.client.android.extensions.isHttps
import com.onlinepayments.sdk.client.android.extensions.logResponse
import com.onlinepayments.sdk.client.android.extensions.logRequest
import com.onlinepayments.sdk.client.android.extensions.openHttpsConnection
import com.onlinepayments.sdk.client.android.extensions.writePostBody
import com.onlinepayments.sdk.client.android.model.api.ApiError
import com.onlinepayments.sdk.client.android.model.api.ApiResponse
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.Scanner

/**
 * Handles all communication with the Online Payments Client API.
 */
@Suppress("ConstructorParameterNaming")
internal class HttpClient(
    private var _loggingEnabled: Boolean
) {
    companion object {
        @JvmSynthetic
        const val httpHeaderSessionId = "Authorization"
        @JvmSynthetic
        const val httpHeaderMetadata = "X-GCS-ClientMetaInfo"
        @JvmSynthetic
        const val httpHeaderRequestSent = "X-Android-Sent-Millis"
        @JvmSynthetic
        const val httpHeaderRequestReceived = "X-Android-Received-Millis"
    }

    private val tag = "HTTPClient"
    private var gson = Gson()

    @JvmSynthetic
    var loggingEnabled: Boolean = _loggingEnabled
        private set

    @JvmSynthetic
    fun setLoggingEnabled(loggingEnabled: Boolean) {
        this.loggingEnabled = loggingEnabled
    }

    // HTTPGet request
    @JvmSynthetic
    @Throws(CommunicationException::class)
    suspend inline fun <reified T> doHTTPGetRequest(
        location: String,
        clientSessionId: String,
        metaData: Map<String, String>
    ) : ApiResponse<T> = withContext(
        Dispatchers.IO) {
        var connection : HttpURLConnection? = null
        val response = ApiResponse<T>()

        try {
            val url = URL(location)
            connection = openConnection(url)

            // Add session id & metadata headers
            connection.addSessionId(clientSessionId)
            connection.addMetaData(metaData)

            // Log request, if enabled
            connection.logRequest("", loggingEnabled)

            // If result is not 200 (success), create ErrorResponse
            if (!connection.is200Result()) {
                response.error = createErrorResponseFromConnection(connection)
                return@withContext response
            }

            // Get response body
            val responseBody = readInputStreamToString(connection.inputStream)

            // Log response, if enabled
            connection.logResponse(responseBody, loggingEnabled)

            // Deserialize response into object & assign to ApiResponse
            val responseObject = gson.fromJson(responseBody, T::class.java)
            response.data = responseObject

            response
        } catch (e: Exception) {
            // Add ErrorResponse object to response
            response.error = createErrorResponseFromException(e, location)

            response
        } finally {
            connection?.close()
        }
    }

    // HTTPPost request
    @JvmSynthetic
    @Throws(CommunicationException::class)
    suspend inline fun <R, reified T> doHTTPPostRequest(
        location: String,
        clientSessionId: String,
        metaData: Map<String, String>,
        body: R
    ) : ApiResponse<T> = withContext(
        Dispatchers.IO) {
        var connection : HttpURLConnection? = null
        val response = ApiResponse<T>()

        try {
            val url = URL(location)
            connection = openConnection(url)

            // Set request method to POST
            connection.requestMethod = "POST"

            // Add json header
            connection.addRequestProperty("Content-Type", "application/json")

            // Add session id & metadata headers
            connection.addSessionId(clientSessionId)
            connection.addMetaData(metaData)

            // Turn request body into a String
            val postBody = gson.toJson(body)

            // Log request, if enabled
            connection.logRequest(postBody, loggingEnabled)

            // Add post body
            connection.writePostBody(postBody)

            // If result is not 200 (success), create ErrorResponse
            if (!connection.is200Result()) {
                response.error = createErrorResponseFromConnection(connection)
                return@withContext response
            }

            // Get response body
            val responseBody = readInputStreamToString(connection.inputStream)

            // Log response, if enabled
            connection.logResponse(responseBody, loggingEnabled)

            // Deserialize response into object & assign to ApiResponse
            val responseObject = gson.fromJson(responseBody, T::class.java)
            response.data = responseObject

            response
        } catch (e: Exception) {
            // Add ErrorResponse object to response
            response.error = createErrorResponseFromException(e, location)

            response
        } finally {
            connection?.close()
        }
    }

    // Read the input stream to a string
    private fun readInputStreamToString(stream: InputStream) : String {
        return Scanner(stream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next()
    }

    private fun createErrorResponseFromConnection(connection: HttpURLConnection) : ErrorResponse {
        val errorResponse = ErrorResponse("No status 200 received, status is : ${connection.responseCode}")
        try {
            val errorBody = readInputStreamToString(connection.errorStream)
            connection.logResponse(errorBody, loggingEnabled)
            errorResponse.apiError = gson.fromJson(errorBody, ApiError::class.java)
        } catch (e: Exception) {
            Log.i(tag, "doHTTPRequest: Unable to parse ApiError from response")
        }
        return errorResponse
    }

    private fun createErrorResponseFromException(exception: Exception, location: String) : ErrorResponse {
        val errorResponseMessage = when (exception) {
            is MalformedURLException -> "Unable to parse url : $location"
            is IOException -> "IOException while opening connection : ${exception.message}"
            is KeyManagementException -> "KeyManagementException while opening connection : ${exception.message}"
            is NoSuchAlgorithmException -> "NoSuchAlgorithmException while opening connection : ${exception.message}"
            else -> "Unknown exception occurred while opening connection : ${exception.message}"
        }

        return ErrorResponse(errorResponseMessage, exception)
    }

    private fun openConnection(url: URL) : HttpURLConnection {
        return if (url.isHttps()) {
            url.openHttpsConnection()
        } else {
            return url.openConnection() as HttpURLConnection
        }
    }
}
