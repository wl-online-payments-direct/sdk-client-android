@file:Suppress("OverridingDeprecatedMember", "DEPRECATION")

package com.onlinepayments.sdk.client.android.facade.listeners

import com.onlinepayments.sdk.client.android.domain.exceptions.ApiError
import com.onlinepayments.sdk.client.android.domain.exceptions.ErrorResponse
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GenericResponseListenerTest {
    private val sdkException = SdkException("failed")
    private val responseException = ResponseException(400, "bad request", ApiError("error-id", emptyList()))

    @Test
    fun `a listener that overrides no failure callback ignores a failure instead of overflowing the stack`() {
        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) = Unit
        }

        listener.onFailure(sdkException)
        listener.onFailure(responseException)
    }

    @Test
    fun `the guard is released after each failure`() {
        var apiErrors = 0
        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) = Unit
            override fun onApiError(error: ErrorResponse) {
                apiErrors++
            }
        }

        listener.onFailure(responseException)
        listener.onFailure(responseException)

        assertEquals(2, apiErrors)
    }

    @Test
    fun `a listener that overrides only onApiError still receives API errors from onFailure`() {
        var received: ErrorResponse? = null
        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) = Unit
            override fun onApiError(error: ErrorResponse) {
                received = error
            }
        }

        listener.onFailure(responseException)

        assertEquals("error-id", received?.apiError?.errorId)
    }

    @Test
    fun `a listener that overrides only onException still receives other failures from onFailure`() {
        var received: Throwable? = null
        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) = Unit
            override fun onException(cause: Throwable) {
                received = cause
            }
        }

        listener.onFailure(sdkException)

        assertSame(sdkException, received)
    }

    @Test
    fun `a listener that overrides onFailure receives the failure and no deprecated callback is called`() {
        var failure: SdkException? = null
        var deprecatedCalled = false
        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) = Unit
            override fun onFailure(exception: SdkException) {
                failure = exception
            }

            override fun onApiError(error: ErrorResponse) {
                deprecatedCalled = true
            }

            override fun onException(cause: Throwable) {
                deprecatedCalled = true
            }
        }

        listener.onFailure(responseException)

        assertSame(responseException, failure)
        assertEquals(false, deprecatedCalled)
    }

    @Test
    fun `the guard of one listener does not suppress the failure of another listener`() {
        var innerReceived: ErrorResponse? = null
        val inner = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) = Unit
            override fun onApiError(error: ErrorResponse) {
                innerReceived = error
            }
        }
        val outer = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) = Unit
            override fun onApiError(error: ErrorResponse) {
                inner.onFailure(responseException)
            }
        }

        outer.onFailure(responseException)

        assertEquals("error-id", innerReceived?.apiError?.errorId)
    }
}
