package com.onlinepayments.sdk.client.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.model.CountryCode;
import com.onlinepayments.sdk.client.android.model.CurrencyCode;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;

import java.security.InvalidParameterException;

public class PaymentProductNetworkAsyncTask extends AsyncTask<String, Void, ApiResponse<PaymentProductNetworkResponse>> {

    private PaymentProductNetworkListener networkListener;
    
    private Context context;
    
    private PaymentContext paymentContext;
    
    private C2sCommunicator communicator;
    
    private String productId;
    private String customerId;
    private String currencyCode;
    private String countryCode;

    /**
     * @deprecated use {@link #PaymentProductNetworkAsyncTask(String, String, String, String, C2sCommunicator, Context, PaymentContext, PaymentProductNetworkListener)} instead
     */
    @Deprecated
    public PaymentProductNetworkAsyncTask(String productId, String customerId, CurrencyCode currencyCode, CountryCode countryCode,
                                          C2sCommunicator communicator, Context context, PaymentContext paymentContext, PaymentProductNetworkListener listener) {
        this(productId, customerId, currencyCode.toString(), countryCode.toString(), communicator, context, paymentContext, listener);
    }
    /**
     * Constructor
     *
     * @param productId, for which product must the lookup be done
     * @param customerId, for which customer must the lookup be done
     * @param currencyCode, for which currencyCode must the lookup be done
     * @param countryCode, for which countryCode must the lookup be done
     * @param context,      needed for reading metadata
     * @param communicator, Communicator which does the communication to the GC gateway
     * @param listener,     listener which will be called by the AsyncTask
     */
    public PaymentProductNetworkAsyncTask(String productId, String customerId, String currencyCode, String countryCode,
                                          C2sCommunicator communicator, Context context, PaymentContext paymentContext, PaymentProductNetworkListener listener) {
        if (productId == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, productId may not be null");
        }
        if (customerId == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, customerId may not be null");
        }
        if (currencyCode == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, currencyCode may not be null");
        }
        if (countryCode == null) {
            throw new InvalidParameterException("Error creating PaymentProductNetworkAsyncTask, countryCode may not be null");
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
        this.customerId = customerId;
        this.networkListener = listener;
        this.currencyCode = currencyCode;
        this.countryCode = countryCode;
    }

    @Override
    protected ApiResponse<PaymentProductNetworkResponse> doInBackground(String... strings) {
        return communicator.getPaymentProductNetwork(customerId, productId, currencyCode, countryCode, context, paymentContext);
    }

    @Override
    protected void onPostExecute(ApiResponse<PaymentProductNetworkResponse> apiResponse) {
        PaymentProductNetworkResponse networkResponse = apiResponse.data;

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

    public interface PaymentProductNetworkListener {

        void onPaymentProductNetworkCallComplete(@NonNull  PaymentProductNetworkResponse response);

        void onPaymentProductNetworkError(ErrorResponse error);
    }
}
