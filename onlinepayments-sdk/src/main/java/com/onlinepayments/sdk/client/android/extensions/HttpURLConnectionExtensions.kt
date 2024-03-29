/*
 * Copyright 2024 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.extensions

import android.util.Log
import com.onlinepayments.sdk.client.android.util.Util
import com.onlinepayments.sdk.client.android.communicate.HttpClient
import com.onlinepayments.sdk.client.android.configuration.Constants
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

@JvmSynthetic
internal fun HttpURLConnection.close() {
    try {
        inputStream.close()
    } catch (e: Exception) {
        Log.d("HttpURLConnection", "Error while closing InputStream: ${e.message}")
    }

    try {
        outputStream.close()
    } catch (e: Exception) {
        Log.d("HttpURLConnection", "Error while closing OutputStream: ${e.message}")
    }

    try {
        errorStream.close()
    } catch (e: Exception) {
        Log.d("HttpURLConnection", "Error while closing ErrorStream: ${e.message}")
    }

    disconnect()
}

@JvmSynthetic
internal fun HttpURLConnection.writePostBody(postBody: String) {
    doOutput = true
    val writer = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
    writer.use { w ->
        w.write(postBody)
        w.flush()
    }
}

@JvmSynthetic
internal fun HttpURLConnection.is200Result() : Boolean {
    return responseCode == Constants.HTTP_SUCCESS
}

@JvmSynthetic
internal fun HttpURLConnection.logRequest(requestBody: String, loggingEnabled: Boolean) {
    if (loggingEnabled) {
        var log = "Request URL : $url\n" +
                "Request Method : $requestMethod\n" +
                "Request Headers : \n"
        for (header in requestProperties.entries) {
            log += "\t\t ${header.key} : ${header.value}\n"
        }

        if (requestMethod.equals("post", ignoreCase = true)) {
            log += "Body : $requestBody\n"
        }
        Log.i("HttpUrlConnection", log)
    }
}

@JvmSynthetic
internal fun HttpURLConnection.logResponse(responseBody: String, loggingEnabled: Boolean) {
    if (loggingEnabled) {
        var log = "Response URL : $url\n" +
                "Response Code : $responseCode\n" +
                "Response Headers : \n"
        for (header in headerFields.entries) {
            log += "\t\t ${header.key} : ${header.value}\n"
        }

        log += "Response Body : $responseBody\n"
        Log.i("HttpUrlConnection", log)

        logTiming()
    }
}

@JvmSynthetic
internal fun HttpURLConnection.logTiming() {
    if (getHeaderField(HttpClient.httpHeaderRequestSent) != null
        && getHeaderField(HttpClient.httpHeaderRequestReceived) != null) {
        val sentMillis = getMillisFromHeader(HttpClient.httpHeaderRequestSent)
        val receivedMillis = getMillisFromHeader(HttpClient.httpHeaderRequestReceived)
        Log.i("HttpUrlConnection",  "Request duration : ${receivedMillis - sentMillis} milliseconds\n")
    }
}

@JvmSynthetic
internal fun HttpURLConnection.getMillisFromHeader(headerField: String) : Long {
    return getHeaderField(headerField).toLongOrNull() ?: 0L
}

@JvmSynthetic
internal fun HttpURLConnection.addMetaData(metaData: Map<String, String>?) {
    if (!metaData.isNullOrEmpty()) {
        addRequestProperty(HttpClient.httpHeaderMetadata, Util.getBase64EncodedMetadata(metaData))
    }
}

@JvmSynthetic
internal fun HttpURLConnection.addSessionId(clientSessionId: String?) {
    if(!clientSessionId.isNullOrEmpty()) {
        addRequestProperty(HttpClient.httpHeaderSessionId, "GCS v1Client:$clientSessionId")
    }
}
