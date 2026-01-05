/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.facade.helpers

import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException
import com.onlinepayments.sdk.client.android.facade.listeners.GenericResponseListener
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider
import com.onlinepayments.sdk.client.android.infrastructure.utils.ApiLogger
import com.onlinepayments.sdk.client.android.infrastructure.utils.Logger
import com.onlinepayments.sdk.client.android.domain.exceptions.ApiError
import com.onlinepayments.sdk.client.android.domain.exceptions.ApiErrorItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ServiceCallWrapper(
    private val sessionScope: CoroutineScope,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val logger: Logger = LoggerProvider.logger
) {

    suspend inline fun <T> wrap(
        logTag: String = "",
        crossinline block: suspend () -> T
    ): T {
        try {
            return block()
        } catch (e: ResponseException) {
            logSdkError(logTag, e.httpStatusCode, e.apiError, e.message)
            throw e
        } catch (e: Exception) {
            logException(logTag, e)
            throw e
        }
    }

    inline fun <T> wrap(
        listener: GenericResponseListener<T>,
        logTag: String = "",
        crossinline block: suspend () -> T
    ) {
        sessionScope.launch {
            try {
                val result = block()
                withContext(mainDispatcher) {
                    listener.onSuccess(result)
                }
            } catch (e: ResponseException) {
                logSdkError("$logTag[ListenerBased]", e.httpStatusCode, e.apiError, e.message)
                withContext(mainDispatcher) {
                    listener.onFailure(e)
                }
            } catch (e: Exception) {
                logException(logTag, e)
                withContext(mainDispatcher) {
                    // Wrap non-SDK exceptions in SdkException
                    val sdkException = e as? SdkException
                        ?: SdkException(
                            message = e.message ?: "Unknown error",
                            cause = e
                        )
                    listener.onFailure(sdkException)
                }
            }
        }
    }

    private fun logSdkError(logTag: String, httpStatusCode: Int?, apiError: ApiError?, message: String?) {
        if (ApiLogger.getLoggingEnabled()) {
            val apiErrorId = apiError?.errorId ?: ""
            val apiErrorList = getApiErrorItemListLogs(apiError?.errors)
            logger.e(
                "LocalResponseListener",
                "Error while performing the service call for `$logTag` \n" +
                    "HTTP Status: $httpStatusCode \n" + // ← Added
                    "ErrorResponse message: $message \n" +
                    "apiError id: $apiErrorId \n" +
                    "errorList: $apiErrorList"
            )
        }
    }

    private fun logException(logTag: String, t: Throwable) {
        if (ApiLogger.getLoggingEnabled()) {
            logger.e(
                "LocalResponseListener",
                "Exception while performing service call for `$logTag` \n" +
                    "Exception ${t.message}",
                t
            )
        }
    }

    private fun getApiErrorItemListLogs(apiErrorItems: List<ApiErrorItem>?): String {
        val errorList: MutableList<String> = mutableListOf()

        apiErrorItems?.let {
            for (apiErrorItem in apiErrorItems) {
                errorList.addAll(
                    arrayOf(
                        "",
                        "ApiErrorItem errorCode: ${apiErrorItem.errorCode}",
                        "message: ${apiErrorItem.message}"
                    )
                )
            }
        }

        return errorList.joinToString("\n")
    }
}
