/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.utils

import com.google.gson.Gson
import com.onlinepayments.sdk.client.android.domain.exceptions.ApiError
import com.onlinepayments.sdk.client.android.domain.exceptions.CommunicationException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.MalformedURLException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException

/**
 * Utility for executing API calls with comprehensive error handling.
 */
internal object ApiCallExecutor {

    /**
     * Executes an API call with error handling.
     *
     * @param apiCall The suspend API call to execute
     * @return The result of the API call
     * @throws ResponseException if the API returns an error
     * @throws CommunicationException if there's a communication error
     */
    suspend inline fun <T> callApi(crossinline apiCall: suspend () -> T): T {
        return try {
            withContext(Dispatchers.IO) {
                apiCall()
            }
        } catch (e: SdkException) {
            throw e
        } catch (e: HttpException) {
            throw handleHttpException(e)
        } catch (t: Throwable) {
            // For any other unexpected exception, wrap it as well.
            throw CommunicationException(getErrorResponseMessage(t), t)
        }
    }

    /**
     * Handles HTTP exceptions from Retrofit.
     */
    fun handleHttpException(e: HttpException): SdkException {
        // If there is already an SdkException underneath, reuse it.
        findSdkException(e)?.let { return it }

        val errorMessage = "Request failed with status: ${e.code()}"

        val apiError = parseErrorResponse(e)
            ?: ApiError(
                errorId = "HTTP_${e.code()}_JSON_ERROR",
                errors = emptyList()
            )

        return ResponseException(e.code(), errorMessage, apiError, e)
    }

    /**
     * Creates an error response message from a generic exception.
     */
    fun getErrorResponseMessage(exception: Throwable): String {
        return when (exception) {
            is MalformedURLException -> "Unable to parse the request URL"
            is IOException -> "IOException while opening connection: ${exception.message}"
            is KeyManagementException -> "KeyManagementException while opening connection: ${exception.message}"
            is NoSuchAlgorithmException -> "NoSuchAlgorithmException while opening connection: ${exception.message}"
            else -> "Unknown exception occurred while opening connection: ${exception.message}"
        }
    }

    private fun findSdkException(throwable: Throwable): SdkException? {
        return when {
            throwable is SdkException -> throwable
            throwable.cause is SdkException -> throwable.cause as SdkException
            throwable.suppressed.any { it is SdkException } ->
                throwable.suppressed.first { it is SdkException } as SdkException

            else -> null
        }
    }

    private fun parseErrorResponse(e: HttpException): ApiError? {
        return try {
            val body = e.response()?.errorBody()?.string()
            body?.let { Gson().fromJson(it, ApiError::class.java) }
        } catch (_: Exception) {
            null
        }
    }
}
