package com.onlinepayments.sdk.client.android.listener;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;

/**
 * Generic typed interface that serves as the base for the typed Response Listeners used for API requests.
 * {@link T type} The type of the Response Listener. An object of this type represents the response type of the API call.
 * {@link #onSuccess(T) onSuccess} this callback will be invoked when the API call completes successfully.
 * {@link #onApiError(ErrorResponse) onApiError} In case of an error during execution, this callback will be invoked.
 * {@link #onException(Throwable) onException} In case of an exception during execution, this callback will be invoked.
 */
interface GenericResponseListener<T> {

    /**
     * Invoked when the request was successful and data is available.
     *
     * @param response the {@link T type} which contains the typed data
     */
    void onSuccess(@NonNull T response);

    /**
     * Invoked when the request failed due to a network error.
     *
     * @param error Error object that contains more information about the error that occurred.
     */
    void onApiError(ErrorResponse error);

    /**
     * Invoked when the request failed due to an exception.
     *
     * @param t Throwable object that contains the exception that occurred.
     */
    void onException(Throwable t);
}
