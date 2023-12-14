/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.listener.PaymentProductNetworkResponseListener;
import com.onlinepayments.sdk.client.android.model.CountryCode;
import com.onlinepayments.sdk.client.android.model.CurrencyCode;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.session.Session;

import java.security.InvalidParameterException;

/**
 * AsyncTask which loads a Payment Product Network from the Online Payments Gateway.
 *
 * @deprecated In a future release, this class will become internal to the SDK. Use {@link com.onlinepayments.sdk.client.android.session.Session#getNetworksForPaymentProduct(String, Context, PaymentContext, PaymentProductNetworkResponseListener)} to obtain the Payment Product's Networks.
 */
@Deprecated
public class PaymentProductNetworkAsyncTask extends AsyncTask<String, Void, ApiResponse<PaymentProductNetworkResponse>> {

    // The listeners which will be called by the AsyncTask when a PaymentProductNetworkResponse is retrieved
    private PaymentProductNetworkListener networkListener;
    // The listeners which will be called by the AsyncTask when a PaymentProductNetworkResponse is retrieved
    private PaymentProductNetworkResponseListener responseListener;
    // Context needed for reading metadata which is sent to the Online Payments gateway
    private Context context;
    // Contains all the information needed to communicate with the Online Payments gateway to get BasicPaymentItems
    private PaymentContext paymentContext;
    // Communicator which does the communication to the Online Payments gateway
    private C2sCommunicator communicator;
    // Product id for which the network must be retrieved
    private String productId;

    /**
     * Create a PaymentProductNetworkAsyncTask
     * 
     * @param productId the id of the product for which the network must be retrieved
     * @param customerId for which customer the network must be retrieved. This argument is no longer used, provide the correct customerId when creating your {@link Session}
     * @param currencyCode for which currencyCode the network must be retrieved. This argument is no longer used, provide the correct currencyCode in the {@link com.onlinepayments.sdk.client.android.model.AmountOfMoney} property of the {@link PaymentContext} provided.
     * @param countryCode for which countryCode the network must be retrieved. This argument is no longer used, provide the correct countryCode in the {@link PaymentContext} provided.
     * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link PaymentProductNetworkResponse}
     * @param listener {@link PaymentProductNetworkListener} which will be called by the AsyncTask when the {@link PaymentProductNetworkResponse} is retrieved
     *
     * @deprecated use {@link #PaymentProductNetworkAsyncTask(String, C2sCommunicator, Context, PaymentContext, PaymentProductNetworkResponseListener)} instead.
     */
    @Deprecated
    public PaymentProductNetworkAsyncTask(String productId, String customerId, CurrencyCode currencyCode, CountryCode countryCode,
                                          C2sCommunicator communicator, Context context, PaymentContext paymentContext, PaymentProductNetworkListener listener) {
        this(productId, customerId, currencyCode.toString(), countryCode.toString(), communicator, context, paymentContext, listener);
    }

    /**
     * Create a PaymentProductNetworkAsyncTask
     *
     * @param productId the product of the id for which the network must be retrieved
     * @param customerId for which customer the network must be retrieved. This argument is no longer used, provide the correct customerId when creating your {@link Session}
     * @param currencyCode for which currencyCode the network must be retrieved. This argument is no longer used, provide the correct currencyCode in the {@link com.onlinepayments.sdk.client.android.model.AmountOfMoney} property of the {@link PaymentContext} provided.
     * @param countryCode for which countryCode the network must be retrieved. This argument is no longer used, provide the correct countryCode in the {@link PaymentContext} provided.
     * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link PaymentProductNetworkResponse}
     * @param listener {@link PaymentProductNetworkListener} which will be called by the AsyncTask when the {@link PaymentProductNetworkResponse} is retrieved
     *
     * @deprecated use {@link #PaymentProductNetworkAsyncTask(String, C2sCommunicator, Context, PaymentContext, PaymentProductNetworkResponseListener)} instead.
     */
    @Deprecated
    public PaymentProductNetworkAsyncTask(String productId, String customerId, String currencyCode, String countryCode,
                                          C2sCommunicator communicator, Context context, PaymentContext paymentContext, PaymentProductNetworkListener listener) {
        if (productId == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, productId may not be null");
        }
        if (context == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, context may not be null");
        }
        if (paymentContext == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, paymentContext may not be null");
        }
        if (communicator == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, communicator may not be null");
        }
        if (listener == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, listener may not be null");
        }

        this.context = context;
        this.paymentContext = paymentContext;
        this.communicator = communicator;
        this.productId = productId;
        this.networkListener = listener;
    }

    /**
     * Create a PaymentProductNetworkAsyncTask
     *
     * @param productId the product of the id for which the network must be retrieved
     * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link PaymentProductNetworkResponse}
     * @param listener {@link PaymentProductNetworkResponseListener} which will be called by the AsyncTask when the {@link PaymentProductNetworkResponse} is retrieved
     */
    public PaymentProductNetworkAsyncTask(String productId, C2sCommunicator communicator, Context context, PaymentContext paymentContext, PaymentProductNetworkResponseListener listener) {
        if (productId == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, productId may not be null");
        }
        if (context == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, context may not be null");
        }
        if (paymentContext == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, paymentContext may not be null");
        }
        if (communicator == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, communicator may not be null");
        }
        if (listener == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, listener may not be null");
        }

        this.context = context;
        this.paymentContext = paymentContext;
        this.communicator = communicator;
        this.productId = productId;
        this.responseListener = listener;
    }

    @Override
    protected ApiResponse<PaymentProductNetworkResponse> doInBackground(String... strings) {
        return communicator.getPaymentProductNetwork(productId, context, paymentContext);
    }

    @Override
    protected void onPostExecute(ApiResponse<PaymentProductNetworkResponse> apiResponse) {
        PaymentProductNetworkResponse networkResponse = apiResponse.data;

        if (networkListener != null) {
            if (apiResponse.error == null) {
                if (networkResponse != null) {
                    networkListener.onPaymentProductNetworkCallComplete(networkResponse);
                } else {
                    ErrorResponse error = new ErrorResponse("Empty Response without Error");
                    networkListener.onPaymentProductNetworkError(error);
                }
            } else {
                networkListener.onPaymentProductNetworkError(apiResponse.error);
            }
        }

        if (responseListener != null) {
            if (apiResponse.error == null) {
                if (networkResponse != null) {
                    responseListener.onSuccess(networkResponse);
                } else {
                    ErrorResponse error = new ErrorResponse("Empty Response without Error");
                    responseListener.onApiError(error);
                }
            } else {
                if (apiResponse.error.throwable != null) {
                    responseListener.onException(apiResponse.error.throwable);
                } else {
                    responseListener.onApiError(apiResponse.error);
                }
            }
        }
    }

    /**
     * Callback Interface that is invoked when the Payment Product Network request completes.
     * In case of an error and/or exception, the {@link #onPaymentProductNetworkError(ErrorResponse)} callback will be invoked.
     * On success the {@link #onPaymentProductNetworkCallComplete(PaymentProductNetworkResponse)} callback will be invoked.
     *
     * @deprecated Use {@link PaymentProductNetworkResponseListener} instead.
     */
    @Deprecated
    public interface PaymentProductNetworkListener {
        /**
         * Invoked when the request was successful and data is available.
         *
         * @param response the {@link PaymentProductNetworkResponse} returned by the Online Payments gateway
         */
        void onPaymentProductNetworkCallComplete(@NonNull PaymentProductNetworkResponse response);

        /**
         * Invoked when the request failed due to a network error.
         *
         * @param error Error object that contains more information about the error that occurred.
         */
        void onPaymentProductNetworkError(ErrorResponse error);
    }
}
