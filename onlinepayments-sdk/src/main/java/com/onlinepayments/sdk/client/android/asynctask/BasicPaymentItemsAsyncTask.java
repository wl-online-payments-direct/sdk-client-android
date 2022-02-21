package com.onlinepayments.sdk.client.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
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
 * Async task which loads all BasicPaymentItems from the GC gateway. If grouping is enabled, this class
 * does two calls to the GC gateway. One that retrieves the BasicPaymentProducts and the other retrieves
 * the BasicPaymentProductGroups. After both calls have been finished both responses are combined to create
 * a single BasicPaymentItems object.
 *
 * Copyright 2017 Global Collect Services B.V
 */
public class BasicPaymentItemsAsyncTask extends AsyncTask<String, Void, ApiResponse<BasicPaymentItems>> {

    private static final String TAG = BasicPaymentItemsAsyncTask.class.getName();

    // The listeners that will be called by the AsyncTask when the PaymentProductSelectables are loaded
    private List<OnBasicPaymentItemsCallCompleteListener> listeners;

    private List<BasicPaymentItemsCallListener> callListeners;

    // Context needed for reading stubbed BasicPaymentProducts
    private Context context;

    // Contains all the information needed to communicate with the GC gateway to get paymentproducts
    private PaymentContext paymentContext;

    // Communicator which does the communication to the GC gateway
    private C2sCommunicator communicator;

    // Defines whether the selectables that will be returned should contain paymentProductGroups
    private boolean groupPaymentItems;

    /**
     * @deprecated use {@link #BasicPaymentItemsAsyncTask(Context, PaymentContext, C2sCommunicator, boolean, List<BasicPaymentItemsCallListener>)}
     */
    public BasicPaymentItemsAsyncTask(Context context, PaymentContext paymentContext, C2sCommunicator communicator, List<OnBasicPaymentItemsCallCompleteListener> listeners, boolean groupPaymentItems) {
        this(context, paymentContext, communicator, groupPaymentItems);
        if (listeners == null) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, listeners may not be null");
        }
        this.listeners = listeners;
    }

    public BasicPaymentItemsAsyncTask(Context context, PaymentContext paymentContext, C2sCommunicator communicator, boolean groupPaymentItems, List<BasicPaymentItemsCallListener> callListeners) {
        this(context, paymentContext, communicator, groupPaymentItems);
        if (callListeners == null) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, listeners may not be null");
        }
        this.callListeners = callListeners;
    }

    /**
     * Internal helper constructor to reduce duplicate code, until the deprecated constructor is removed
     */
    private BasicPaymentItemsAsyncTask (Context context, PaymentContext paymentContext, C2sCommunicator communicator, boolean groupPaymentItems) {
        if (context == null ) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, context may not be null");
        }
        if (paymentContext == null ) {
            throw new InvalidParameterException("Error creating BasicPaymentItemsAsyncTask, c2sContext may not be null");
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
        Callable<BasicPaymentProducts> paymentProductsCallable = new BasicPaymentProductsAsyncTask(context, paymentContext, communicator, new LinkedList<>());
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
    }

    /**
     * Interface for OnPaymentProductsCallComplete listener
     * Is called from the BasicPaymentProductsAsyncTask when it has the BasicPaymentProducts
     *
     * @deprecated use {@link BasicPaymentItemsCallListener} instead to receive error information
     *
     * Copyright 2017 Global Collect Services B.V
     *
     */
    public interface OnBasicPaymentItemsCallCompleteListener {
        void onBasicPaymentItemsCallComplete(BasicPaymentItems basicPaymentItems);
    }

    /**
     * Updated interface for OnPaymentProductsCallComplete
     * Is called from the BasicPaymentProductsAsyncTask when it has the BasicPaymentProducts.
     * When there was an error and/or exception, the error callback will be invoked.
     * On success the complete callback will be invoked.
     *
     * Copyright 2020 Global Collect Services B.V
     *
     */
    public interface BasicPaymentItemsCallListener {
        /**
         * When async task was successful and data available
         * @param basicPaymentItems The list of available payment products
         */
        void onBasicPaymentItemsCallComplete(@NonNull BasicPaymentItems basicPaymentItems);

        /**
         * When async task failed due to an error and/or exception
         * @param error The error that occurred
         */
        void onBasicPaymentItemsCallError(ErrorResponse error);
    }
}
