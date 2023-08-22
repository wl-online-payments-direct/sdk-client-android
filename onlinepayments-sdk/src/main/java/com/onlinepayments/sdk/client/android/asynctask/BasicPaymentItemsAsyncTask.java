/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.listener.BasicPaymentItemsResponseListener;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Async task which loads all {@link BasicPaymentItems} from the Online Payments gateway.
 *
 * @deprecated In a future release, this class will become internal to the SDK. Use {@link com.onlinepayments.sdk.client.android.session.Session#getBasicPaymentItems(Context, PaymentContext, boolean, BasicPaymentItemsResponseListener)} to obtain Basic Payment Items.
 */
@Deprecated
public class BasicPaymentItemsAsyncTask extends AsyncTask<String, Void, ApiResponse<BasicPaymentItems>> {

    private static final String TAG = BasicPaymentItemsAsyncTask.class.getName();

    // The listeners that will be called by the AsyncTask when the BasicPaymentItems are loaded
    private List<OnBasicPaymentItemsCallCompleteListener> listeners;

    // The listeners that will be called by the AsyncTask when the BasicPaymentItems are loaded
    private List<BasicPaymentItemsCallListener> callListeners;

    // The listeners that will be called by the AsyncTask when the BasicPaymentItems are loaded
    private List<BasicPaymentItemsResponseListener> responseListeners;

    // Context needed for reading metadata which is sent to the Online Payments gateway
    private Context context;

    // Contains all the information needed to communicate with the Online Payments gateway to get BasicPaymentItems
    private PaymentContext paymentContext;

    // Communicator which does the communication to the Online Payments gateway
    private C2sCommunicator communicator;

    // Defines whether the BasicPaymentItems that will be returned should contain paymentProductGroups
    private boolean groupPaymentItems;

    /**
     * Create a BasicPaymentItemsAsyncTask
     *
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link BasicPaymentItems}
     * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
     * @param groupPaymentItems Boolean that indicates whether the {@link BasicPaymentItems} should be grouped or not
     * @param listeners List of {@link OnBasicPaymentItemsCallCompleteListener} which will be called by the AsyncTask when the {@link BasicPaymentItems} are loaded
     */
    public BasicPaymentItemsAsyncTask(Context context, PaymentContext paymentContext, C2sCommunicator communicator, List<OnBasicPaymentItemsCallCompleteListener> listeners, boolean groupPaymentItems) {
        this(context, paymentContext, communicator, groupPaymentItems);
        if (listeners == null) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, listeners may not be null");
        }
        this.listeners = listeners;
    }

    /**
     * Create a BasicPaymentItemsAsyncTask
     *
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link BasicPaymentItems}
     * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
     * @param groupPaymentItems Boolean that indicates whether the {@link BasicPaymentItems} should be grouped or not
     * @param callListeners List of {@link BasicPaymentItemsCallListener} which will be called by the AsyncTask when the {@link BasicPaymentItems} are loaded
     */
    public BasicPaymentItemsAsyncTask(Context context, PaymentContext paymentContext, C2sCommunicator communicator, boolean groupPaymentItems, List<BasicPaymentItemsCallListener> callListeners) {
        this(context, paymentContext, communicator, groupPaymentItems);
        if (callListeners == null) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, listeners may not be null");
        }
        this.callListeners = callListeners;
    }

    /**
     * Create a BasicPaymentItemsAsyncTask
     *
     * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
     * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link BasicPaymentItems}
     * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
     * @param groupPaymentItems Boolean that indicates whether the {@link BasicPaymentItems} should be grouped or not
     * @param responseListeners List of {@link BasicPaymentItemsResponseListener} which will be called by the AsyncTask when the {@link BasicPaymentItems} are loaded
     */
    public BasicPaymentItemsAsyncTask(Context context, PaymentContext paymentContext, boolean groupPaymentItems, List<BasicPaymentItemsResponseListener> responseListeners, C2sCommunicator communicator) {
        this(context, paymentContext, communicator, groupPaymentItems);
        if (responseListeners == null) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, responseListeners may not be null");
        }
        this.responseListeners = responseListeners;
    }

    /**
     * Internal helper constructor to reduce duplicate code, until the deprecated constructor is removed.
     */
    private BasicPaymentItemsAsyncTask (Context context, PaymentContext paymentContext, C2sCommunicator communicator, boolean groupPaymentItems) {
        if (context == null ) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, context may not be null");
        }
        if (paymentContext == null ) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, paymentContext may not be null");
        }
        if (communicator == null ) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, communicator may not be null");
        }

        this.context = context;
        this.paymentContext = paymentContext;
        this.communicator = communicator;
        this.groupPaymentItems = groupPaymentItems;
    }

    @Override
    protected ApiResponse<BasicPaymentItems> doInBackground(String... params) {

        ApiResponse<BasicPaymentItems> result = new ApiResponse<>();

        // Create the paymentProductsCallable
        Callable<BasicPaymentProducts> paymentProductsCallable = new BasicPaymentProductsAsyncTask(communicator, context, paymentContext, new LinkedList<>());
        try {

            // Retrieve the basicPaymentProducts
            BasicPaymentProducts basicPaymentProducts = paymentProductsCallable.call();

            if (basicPaymentProducts != null) {
                result.data = new BasicPaymentItems(basicPaymentProducts.getPaymentProductsAsItems(), basicPaymentProducts.getAccountsOnFile());
            }
        } catch (Exception e) {
            Log.i(TAG, "Error while getting paymentItems: " + e.getMessage());
            result.error = new ErrorResponse("Error while getting paymentItems: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(ApiResponse<BasicPaymentItems> apiResponse) {

        BasicPaymentItems basicPaymentItems = apiResponse.data;

        // Call listener callbacks
        if (listeners != null) {
            for (OnBasicPaymentItemsCallCompleteListener listener : listeners) {
                listener.onBasicPaymentItemsCallComplete(basicPaymentItems);
            }
        }

        if (callListeners != null ) {
            for (BasicPaymentItemsCallListener listener : callListeners) {
                if (apiResponse.error == null) {
                    if (basicPaymentItems != null) {
                        listener.onBasicPaymentItemsCallComplete(basicPaymentItems);
                    } else {
                        ErrorResponse error = new ErrorResponse("Empty Response without Error");
                        listener.onBasicPaymentItemsCallError(error);
                    }
                } else {
                    listener.onBasicPaymentItemsCallError(apiResponse.error);
                }
            }
        }

        if (responseListeners != null ) {
            for (BasicPaymentItemsResponseListener listener : responseListeners) {
                if (apiResponse.error == null) {
                    if (basicPaymentItems != null) {
                        listener.onSuccess(basicPaymentItems);
                    } else {
                        ErrorResponse error = new ErrorResponse("Empty Response without Error");
                        listener.onApiError(error);
                    }
                } else {
                    if (apiResponse.error.throwable != null) {
                        listener.onException(apiResponse.error.throwable);
                    } else {
                        listener.onApiError(apiResponse.error);
                    }
                }
            }
        }
    }

    /**
     * Callback Interface that is invoked when the Basic Payment Items request completes.
     *
     * @deprecated use {@link BasicPaymentItemsResponseListener} instead to also receive callbacks with error information.
     */
    @Deprecated
    public interface OnBasicPaymentItemsCallCompleteListener {
        void onBasicPaymentItemsCallComplete(BasicPaymentItems basicPaymentItems);
    }

    /**
     * Callback Interface that is invoked when the Basic Payment Items request completes.
     * In case of an error and/or exception, the {@link #onBasicPaymentItemsCallError(ErrorResponse)} callback will be invoked.
     * On success the {@link #onBasicPaymentItemsCallComplete(BasicPaymentItems)} callback will be invoked.
     *
     * @deprecated use {@link BasicPaymentItemsResponseListener} instead.
     */
    @Deprecated
    public interface BasicPaymentItemsCallListener {
        /**
         * Invoked when the request was successful and data is available.
         *
         * @param basicPaymentItems the list of available {@link BasicPaymentItems}
         */
        void onBasicPaymentItemsCallComplete(@NonNull BasicPaymentItems basicPaymentItems);

        /**
         * Invoked when the request failed due to a network error.
         *
         * @param error Error object that contains more information about the error that occurred.
         */
        void onBasicPaymentItemsCallError(ErrorResponse error);
    }
}
