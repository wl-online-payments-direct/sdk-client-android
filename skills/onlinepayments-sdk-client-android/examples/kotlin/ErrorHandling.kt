package com.example.checkout

import android.util.Log
import com.onlinepayments.sdk.client.android.domain.exceptions.CommunicationException
import com.onlinepayments.sdk.client.android.domain.exceptions.ConfigurationException
import com.onlinepayments.sdk.client.android.domain.exceptions.EncryptionException
import com.onlinepayments.sdk.client.android.domain.exceptions.IllegalStateSdkException
import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import kotlinx.coroutines.CancellationException

// [START handle-sdk-error]
enum class ErrorAction {
    SHOW_VALIDATION_ERRORS, // the customer can fix the input
    RETRY, // a network problem, or a call that overlapped another: the same call can be tried again
    RESTART_CHECKOUT, // get new session data from your backend and create a new OnlinePaymentsSdk
    INTEGRATION_ERROR, // a bug in the integration: fix the code
}

// Call from the catch block around an SDK call. A CancellationException is rethrown: it is not an error.
fun classifySdkError(error: Throwable): ErrorAction {
    if (error is CancellationException) {
        throw error
    }
    return when (error) {
        // Invalid input: encryptPaymentRequest with a request that does not validate (metadata["data"]
        // is the ValidationResult), setValue on a READ_ONLY field, getField with an unknown id.
        is InvalidArgumentException -> ErrorAction.SHOW_VALIDATION_ERRORS

        // No response, a network error, or a response that is not JSON. error.cause has details.
        is CommunicationException -> ErrorAction.RETRY

        // getIinDetails while another IIN lookup is running.
        is IllegalStateSdkException -> ErrorAction.RETRY

        // The Client API returned an error, for example because the session is no longer valid, or
        // httpStatusCode 404 when no payment product is available.
        is ResponseException -> {
            // Log only the status code, error id and error codes: never payment data.
            Log.w(
                "Checkout",
                "Client API error ${error.httpStatusCode} ${error.apiError.errorId} " +
                    error.apiError.errors?.joinToString(",") { it.errorCode.orEmpty() },
            )
            ErrorAction.RESTART_CHECKOUT
        }

        // Encryption failed, or encryptTokenRequest without a payment product id.
        is EncryptionException -> ErrorAction.RESTART_CHECKOUT

        // A blank session value or an invalid clientApiUrl when the SDK is created.
        is ConfigurationException -> ErrorAction.INTEGRATION_ERROR

        else -> ErrorAction.INTEGRATION_ERROR
    }
}
// [END handle-sdk-error]
