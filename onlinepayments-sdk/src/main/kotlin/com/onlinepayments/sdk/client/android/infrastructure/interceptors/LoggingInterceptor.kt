/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.interceptors

import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal class LoggingInterceptor(var loggingEnabled: AtomicBoolean) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!loggingEnabled.get()) {
            return chain.proceed(request)
        }

        // Log the request details
        logRequest(request)

        val startTime = System.nanoTime()
        val response = chain.proceed(request)
        // Log the response details
        logResponse(response, startTime)

        return response
    }

    private fun logRequest(request: Request) {
        var log = "Request URL : ${request.url}\n" +
            "Request method : ${request.method}\n" +
            "Request headers : \n"
        for (header in request.headers) {
            log += "\t\t ${header.first} : ${header.second}\n"
        }

        if (request.method.equals("post", ignoreCase = true)) {
            log += "Body : ${request.getBodyAsString()}\n"
        }

        LoggerProvider.logger.i(TAG, log)
    }

    private fun logResponse(response: Response, startNs: Long) {
        var log = "Response for ${response.request.method} request URL: ${response.request.url}\n" +
            "Response code : ${response.code}\n" +
            "Response headers : \n"
        for (header in response.headers) {
            log += "\t\t ${header.first} : ${header.second}\n"
        }

        log += "Response body : ${response.getBodyAsString()}\n"

        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        log += "Response duration : $duration milliseconds\n"

        LoggerProvider.logger.i(TAG, log)
    }

    private fun Request.getBodyAsString(): String {
        val requestCopy = this.newBuilder().build()
        val buffer = Buffer()
        requestCopy.body?.writeTo(buffer)

        return buffer.readUtf8()
    }

    private fun Response.getBodyAsString(): String {
        return this.peekBody(Long.MAX_VALUE).string()
    }

    companion object {
        private const val TAG = "HttpCommunicator"
    }
}
