/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.communicate.interceptors

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.onlinepayments.sdk.client.android.exception.ApiException
import com.onlinepayments.sdk.client.android.exception.CommunicationException
import com.onlinepayments.sdk.client.android.model.api.ApiError
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse
import com.onlinepayments.sdk.client.android.providers.LoggerProvider
import okhttp3.Interceptor
import okhttp3.Response

class ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (!response.isSuccessful && response.code != 404) {
            // Use peekBody to avoid consuming the response body completely.
            val errorBody = response.peekBody(Long.MAX_VALUE).string()

            val message = "Request failed with status: ${response.code}"

            val errorResponse = ErrorResponse(message)

            try {
                errorResponse.apiError = Gson().fromJson(errorBody, ApiError::class.java)

                throw ApiException(message, errorResponse)
            } catch (_: JsonSyntaxException) {
                LoggerProvider.logger.i(TAG, "Unable to parse error body: $errorBody")

                throw CommunicationException(message, null, null, errorBody)
            }
        }

        return response
    }

    companion object {
        private const val TAG = "HttpCommunicator"
    }
}