package com.example.checkoutjava;

import android.util.Log;

import com.onlinepayments.sdk.client.android.domain.exceptions.ApiErrorItem;
import com.onlinepayments.sdk.client.android.domain.exceptions.CommunicationException;
import com.onlinepayments.sdk.client.android.domain.exceptions.ConfigurationException;
import com.onlinepayments.sdk.client.android.domain.exceptions.EncryptionException;
import com.onlinepayments.sdk.client.android.domain.exceptions.IllegalStateSdkException;
import com.onlinepayments.sdk.client.android.domain.exceptions.InvalidArgumentException;
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException;

import java.util.List;

public final class ErrorHandling {
    private ErrorHandling() {
    }

    // [START handle-sdk-error-java]
    public enum ErrorAction {
        SHOW_VALIDATION_ERRORS, // the customer can fix the input
        RETRY, // a network problem, or a call that overlapped another: the same call can be tried again
        RESTART_CHECKOUT, // get new session data from your backend and create a new OnlinePaymentsSdk
        INTEGRATION_ERROR // a bug in the integration: fix the code
    }

    // Call with the SdkException that a listener's onFailure receives. Kotlin does not declare the
    // exceptions, so around the ...Sync methods you can only catch Exception and test it with instanceof.
    public static ErrorAction classifySdkError(Throwable error) {
        // Invalid input: encryptPaymentRequest with a request that does not validate (the metadata
        // entry "data" is the ValidationResult), setValue on a READ_ONLY field, getField with an unknown id.
        if (error instanceof InvalidArgumentException) {
            return ErrorAction.SHOW_VALIDATION_ERRORS;
        }

        // No response, a network error, or a response that is not JSON. error.getCause() has details.
        if (error instanceof CommunicationException) {
            return ErrorAction.RETRY;
        }

        // getIinDetails while another IIN lookup is running.
        if (error instanceof IllegalStateSdkException) {
            return ErrorAction.RETRY;
        }

        // The Client API returned an error, for example because the session is no longer valid, or
        // getHttpStatusCode() 404 when no payment product is available.
        if (error instanceof ResponseException) {
            ResponseException response = (ResponseException) error;
            // Log only the status code, error id and error codes: never payment data.
            StringBuilder codes = new StringBuilder();
            List<ApiErrorItem> items = response.getApiError().getErrors();
            if (items != null) {
                for (ApiErrorItem item : items) {
                    codes.append(item.getErrorCode()).append(',');
                }
            }
            Log.w("Checkout", "Client API error " + response.getHttpStatusCode() + " "
                    + response.getApiError().getErrorId() + " " + codes);
            return ErrorAction.RESTART_CHECKOUT;
        }

        // Encryption failed, or encryptTokenRequest without a payment product id.
        if (error instanceof EncryptionException) {
            return ErrorAction.RESTART_CHECKOUT;
        }

        // A blank session value or an invalid clientApiUrl when the SDK is created.
        if (error instanceof ConfigurationException) {
            return ErrorAction.INTEGRATION_ERROR;
        }

        return ErrorAction.INTEGRATION_ERROR;
    }
    // [END handle-sdk-error-java]
}
