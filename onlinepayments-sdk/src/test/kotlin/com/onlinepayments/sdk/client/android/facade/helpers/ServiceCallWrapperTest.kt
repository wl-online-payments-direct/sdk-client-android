/*
 * Do not remove or alter the notices in this preamble.
 *
 * This software is owned by Worldline and may not be be altered, copied, reproduced, republished, uploaded, posted, transmitted or distributed in any way, without the prior written consent of Worldline.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.facade.helpers

import com.onlinepayments.sdk.client.android.domain.exceptions.ApiError
import com.onlinepayments.sdk.client.android.domain.exceptions.ApiErrorItem
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException
import com.onlinepayments.sdk.client.android.facade.listeners.GenericResponseListener
import com.onlinepayments.sdk.client.android.infrastructure.providers.LoggerProvider
import com.onlinepayments.sdk.client.android.infrastructure.utils.ApiLogger
import com.onlinepayments.sdk.client.android.infrastructure.utils.Logger
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceCallWrapperTest {

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var testScope: CoroutineScope
    private lateinit var mockLogger: Logger
    private lateinit var wrapper: ServiceCallWrapper

    @BeforeTest
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        testScope = CoroutineScope(SupervisorJob() + testDispatcher)
        mockLogger = mockk(relaxed = true)
        LoggerProvider.logger = mockLogger
        ApiLogger.setLoggingEnabled(true)

        wrapper = ServiceCallWrapper(
            sessionScope = testScope,
            mainDispatcher = testDispatcher,
            logger = mockLogger
        )
    }

    @AfterTest
    fun tearDown() {
        testScope.cancel()
        LoggerProvider.reset()
        ApiLogger.setLoggingEnabled(false)
    }

    // Suspend wrap tests
    @Test
    fun `suspend wrap should return result on success`() = runTest(testDispatcher) {
        val expectedResult = "Success"

        val result = wrapper.wrap {
            expectedResult
        }

        assertEquals(expectedResult, result)
    }

    @Test
    fun `suspend wrap should propagate ResponseException`() = runTest(testDispatcher) {
        val apiError = ApiError(
            errorId = "ERR123",
            errors = listOf(
                ApiErrorItem(
                    errorCode = "400",
                    category = "VALIDATION",
                    message = "Invalid input"
                )
            )
        )
        val responseException = ResponseException(
            httpStatusCode = 400,
            apiError = apiError,
            message = "Bad Request"
        )

        val exception = assertFailsWith<ResponseException> {
            wrapper.wrap {
                throw responseException
            }
        }

        assertEquals(400, exception.httpStatusCode)
        assertEquals("Bad Request", exception.message)
        assertEquals(apiError, exception.apiError)
    }

    @Test
    fun `suspend wrap should propagate generic Exception`() = runTest(testDispatcher) {
        val genericException = IllegalStateException("Something went wrong")

        val exception = assertFailsWith<IllegalStateException> {
            wrapper.wrap {
                throw genericException
            }
        }

        assertEquals("Something went wrong", exception.message)
    }

    // Listener-based wrap tests
    @Test
    fun `listener wrap should call onSuccess with result`() = runTest(testDispatcher) {
        val expectedResult = "Success"
        var successCalled = false
        var failureCalled = false

        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) {
                successCalled = true
                assertEquals(expectedResult, response)
            }

            override fun onFailure(exception: SdkException) {
                failureCalled = true
            }
        }

        wrapper.wrap(listener) {
            expectedResult
        }

        advanceUntilIdle()

        assertTrue(successCalled, "onSuccess should be called")
        assertTrue(!failureCalled, "onFailure should not be called")
    }

    @Test
    fun `listener wrap should call onFailure with ResponseException`() = runTest(testDispatcher) {
        val apiError = ApiError(
            errorId = "ERR456",
            errors = listOf(
                ApiErrorItem(
                    errorCode = "404",
                    category = "NOT_FOUND",
                    message = "Resource not found"
                )
            )
        )
        val responseException = ResponseException(
            httpStatusCode = 404,
            apiError = apiError,
            message = "Not Found"
        )

        var successCalled = false
        var failureCalled = false
        var receivedException: SdkException? = null

        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) {
                successCalled = true
            }

            override fun onFailure(exception: SdkException) {
                failureCalled = true
                receivedException = exception
            }
        }

        wrapper.wrap(listener) {
            throw responseException
        }

        advanceUntilIdle()

        assertTrue(!successCalled, "onSuccess should not be called")
        assertTrue(failureCalled, "onFailure should be called")
        assertTrue(receivedException is ResponseException)
        assertEquals(404, (receivedException as ResponseException).httpStatusCode)
    }

    @Test
    fun `listener wrap should wrap generic Exception in SdkException`() = runTest(testDispatcher) {
        val genericException = IllegalArgumentException("Invalid argument")

        var successCalled = false
        var failureCalled = false
        var receivedException: SdkException? = null

        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) {
                successCalled = true
            }

            override fun onFailure(exception: SdkException) {
                failureCalled = true
                receivedException = exception
            }
        }

        wrapper.wrap(listener) {
            throw genericException
        }

        advanceUntilIdle()

        assertTrue(!successCalled, "onSuccess should not be called")
        assertTrue(failureCalled, "onFailure should be called")
        assertTrue(receivedException is SdkException)
        assertEquals("Invalid argument", receivedException.message)
        assertEquals(genericException, receivedException.cause)
    }

    @Test
    fun `listener wrap should not double-wrap SdkException`() = runTest(testDispatcher) {
        val sdkException = SdkException("SDK error")

        var receivedException: SdkException? = null

        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) {}

            override fun onFailure(exception: SdkException) {
                receivedException = exception
            }
        }

        wrapper.wrap(listener) {
            throw sdkException
        }

        advanceUntilIdle()

        assertEquals(sdkException, receivedException)
    }

    @Test
    fun `suspend wrap should log ResponseException with API error details`() = runTest(testDispatcher) {
        val apiError = ApiError(
            errorId = "ERR789",
            errors = listOf(
                ApiErrorItem(
                    errorCode = "500",
                    category = "SERVER_ERROR",
                    message = "Internal server error"
                )
            )
        )
        val responseException = ResponseException(
            httpStatusCode = 500,
            apiError = apiError,
            message = "Server Error"
        )

        assertFailsWith<ResponseException> {
            wrapper.wrap(logTag = "TestOperation") {
                throw responseException
            }
        }

        verify {
            mockLogger.e(
                "LocalResponseListener",
                match { message ->
                    message.contains("Error while performing the service call for `TestOperation`") &&
                        message.contains("HTTP Status: 500") &&
                        message.contains("ErrorResponse message: Server Error") &&
                        message.contains("apiError id: ERR789")
                }
            )
        }
    }

    @Test
    fun `suspend wrap should log generic Exception`() = runTest(testDispatcher) {
        val exception = RuntimeException("Unexpected error")

        assertFailsWith<RuntimeException> {
            wrapper.wrap(logTag = "TestOperation") {
                throw exception
            }
        }

        verify {
            mockLogger.e(
                "LocalResponseListener",
                match { message ->
                    message.contains("Exception while performing service call for `TestOperation`") &&
                        message.contains("Unexpected error")
                },
                exception
            )
        }
    }

    @Test
    fun `listener wrap should add ListenerBased suffix to log tag`() = runTest(testDispatcher) {
        val apiError = ApiError(
            errorId = "ERR001",
            errors = emptyList()
        )
        val responseException = ResponseException(
            httpStatusCode = 400,
            apiError = apiError,
            message = "Error"
        )

        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) {}
            override fun onFailure(exception: SdkException) {}
        }

        wrapper.wrap(listener, logTag = "TestOp") {
            throw responseException
        }

        advanceUntilIdle()

        verify {
            mockLogger.e(
                "LocalResponseListener",
                match { message ->
                    message.contains("Error while performing the service call for `TestOp[ListenerBased]`")
                }
            )
        }
    }

    @Test
    fun `suspend wrap should not log when logging is disabled`() = runTest(testDispatcher) {
        ApiLogger.setLoggingEnabled(false)

        val responseException = ResponseException(
            httpStatusCode = 500,
            apiError = ApiError(errorId = "ERR", errors = emptyList()),
            message = "Error"
        )

        assertFailsWith<ResponseException> {
            wrapper.wrap(logTag = "TestOp") {
                throw responseException
            }
        }

        verify(exactly = 0) {
            mockLogger.e(any(), any())
        }
    }

    @Test
    fun `listener wrap should not log when logging is disabled`() = runTest(testDispatcher) {
        ApiLogger.setLoggingEnabled(false)

        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) {}
            override fun onFailure(exception: SdkException) {}
        }

        wrapper.wrap(listener, logTag = "TestOp") {
            throw RuntimeException("Test error")
        }

        advanceUntilIdle()

        verify(exactly = 0) {
            mockLogger.e(any(), any())
        }
        verify(exactly = 0) {
            mockLogger.e(any(), any(), any())
        }
    }

    @Test
    fun `suspend wrap should handle null apiError errors list`() = runTest(testDispatcher) {
        val apiError = ApiError(
            errorId = "ERR123",
            errors = null
        )
        val responseException = ResponseException(
            httpStatusCode = 500,
            apiError = apiError,
            message = "Error with null errors"
        )

        assertFailsWith<ResponseException> {
            wrapper.wrap(logTag = "TestOp") {
                throw responseException
            }
        }

        verify {
            mockLogger.e(
                "LocalResponseListener",
                match { message ->
                    message.contains("Error while performing the service call for `TestOp`") &&
                        message.contains("apiError id: ERR123")
                }
            )
        }
    }

    @Test
    fun `suspend wrap should log multiple API error items`() = runTest(testDispatcher) {
        val apiError = ApiError(
            errorId = "ERR999",
            errors = listOf(
                ApiErrorItem(
                    errorCode = "E001",
                    category = "VALIDATION",
                    message = "First error"
                ),
                ApiErrorItem(
                    errorCode = "E002",
                    category = "BUSINESS",
                    message = "Second error"
                ),
                ApiErrorItem(
                    errorCode = "E003",
                    category = "TECHNICAL",
                    message = "Third error"
                )
            )
        )
        val responseException = ResponseException(
            httpStatusCode = 400,
            apiError = apiError,
            message = "Multiple errors"
        )

        assertFailsWith<ResponseException> {
            wrapper.wrap(logTag = "TestOp") {
                throw responseException
            }
        }

        verify {
            mockLogger.e(
                "LocalResponseListener",
                match { message ->
                    message.contains("apiError id: ERR999") &&
                        message.contains("ApiErrorItem errorCode: E001") &&
                        message.contains("message: First error") &&
                        message.contains("ApiErrorItem errorCode: E002") &&
                        message.contains("message: Second error") &&
                        message.contains("ApiErrorItem errorCode: E003") &&
                        message.contains("message: Third error")
                }
            )
        }
    }

    @Test
    fun `listener wrap should handle exception in listener-based call when logging disabled`() = runTest(testDispatcher) {
        ApiLogger.setLoggingEnabled(false)

        var receivedException: SdkException? = null

        val listener = object : GenericResponseListener<String> {
            override fun onSuccess(response: String) {}
            override fun onFailure(exception: SdkException) {
                receivedException = exception
            }
        }

        wrapper.wrap(listener) {
            throw IllegalStateException("Test error")
        }

        advanceUntilIdle()

        assertTrue(receivedException is SdkException)
        assertEquals("Test error", receivedException.message)
    }
}
