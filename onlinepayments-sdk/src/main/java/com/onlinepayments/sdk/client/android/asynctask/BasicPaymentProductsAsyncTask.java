package com.onlinepayments.sdk.client.android.asynctask;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts;

/**
 * AsyncTask which loads all BasicPaymentProducts from the GC Gateway
 *
 * Copyright 2017 Global Collect Services B.V
 *
 */
public class BasicPaymentProductsAsyncTask extends AsyncTask<String, Void, ApiResponse<BasicPaymentProducts>> implements Callable<BasicPaymentProducts> {

	// The listener which will be called by the AsyncTask when the BasicPaymentProducts are loaded
	private List<OnBasicPaymentProductsCallCompleteListener> listeners;

	private List<BasicPaymentProductsCallListener> callListeners;

	// Context needed for reading stubbed BasicPaymentProducts
	private Context context;

	// Contains all the information needed to communicate with the GC gateway to get paymentproducts
	private PaymentContext paymentContext;

	// Communicator which does the communication to the GC gateway
	private C2sCommunicator communicator;

	/**
	 * Constructor
	 * @deprecated use {@link #BasicPaymentProductsAsyncTask(Context, C2sCommunicator, PaymentContext, List)}
	 *
	 * @param context, used for reading device metada which is send to the GC gateway
	 * @param paymentContext, request which contains all necessary data for doing call to the GC gateway to get paymentproducts
	 * @param communicator, Communicator which does the communication to the GC gateway
	 * @param listeners, list of listeners which will be called by the AsyncTask when the BasicPaymentProducts are loaded
	 */
	public BasicPaymentProductsAsyncTask(Context context, PaymentContext paymentContext, C2sCommunicator communicator, List<OnBasicPaymentProductsCallCompleteListener> listeners) {
		this(context, paymentContext, communicator);
		if (listeners == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, listeners may not be null");
		}
		this.listeners = listeners;
	}

	/**
	 * Constructor
	 * @param context, used for reading device metada which is send to the GC gateway
	 * @param paymentContext, request which contains all necessary data for doing call to the GC gateway to get paymentproducts
	 * @param communicator, Communicator which does the communication to the GC gateway
	 * @param callListeners, list of listeners which will be called by the AsyncTask when the BasicPaymentProducts are loaded
	 */
	public BasicPaymentProductsAsyncTask(Context context, C2sCommunicator communicator, PaymentContext paymentContext, List<BasicPaymentProductsCallListener> callListeners) {
		this(context, paymentContext, communicator);
		if (callListeners == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, listeners may not be null");
		}
		this.callListeners = callListeners;
	}

	/**
	 * Constructor
	 * @param context, used for reading device metada which is send to the GC gateway
	 * @param paymentContext, request which contains all necessary data for doing call to the GC gateway to get paymentproducts
	 * @param communicator, Communicator which does the communication to the GC gateway
	 */
	private BasicPaymentProductsAsyncTask(Context context, PaymentContext paymentContext, C2sCommunicator communicator) {

		if (context == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, context may not be null");
		}
		if (paymentContext == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, c2sContext may not be null");
		}
		if (communicator == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, communicator may not be null");
		}

		this.context = context;
		this.paymentContext = paymentContext;
		this.communicator = communicator;
	}

	@Override
	protected ApiResponse<BasicPaymentProducts> doInBackground(String... params) {

		return getBasicPaymentProductsInBackground();
	}

	@Override
	protected void onPostExecute(ApiResponse<BasicPaymentProducts> apiResponse) {

		BasicPaymentProducts basicPaymentProducts = apiResponse.data;

		if (listeners != null) {
			// Call listener callbacks
			for (OnBasicPaymentProductsCallCompleteListener listener : listeners) {
				listener.onBasicPaymentProductsCallComplete(basicPaymentProducts);
			}
		}

		if (callListeners != null ) {
			for (BasicPaymentProductsCallListener listener : callListeners) {
				if (apiResponse.error == null) {
					if (basicPaymentProducts != null) {
						listener.onBasicPaymentProductsCallComplete(basicPaymentProducts);
					} else {
						ErrorResponse error = new ErrorResponse("Empty Response without Error");
						listener.onBasicPaymentProductsCallError(error);
					}
				} else {
					listener.onBasicPaymentProductsCallError(apiResponse.error);
				}
			}
		}
	}

	@Override
	public BasicPaymentProducts call() throws Exception {

		// Load the BasicPaymentProducts from the GC gateway
		return getBasicPaymentProductsInBackground().data;
	}

	private ApiResponse<BasicPaymentProducts> getBasicPaymentProductsInBackground() {
		ApiResponse<BasicPaymentProducts> basicPaymentProducts = communicator.getBasicPaymentProducts(paymentContext, context);

		if (basicPaymentProducts.data != null && basicPaymentProducts.data.getBasicPaymentProducts() != null) {

			// Filter Apple pay
			removePaymentProduct(basicPaymentProducts.data, Constants.PAYMENTPRODUCTID_APPLEPAY);

			if (containsPaymentProduct(basicPaymentProducts.data, Constants.PAYMENTPRODUCTID_GOOGLEPAY)) {

				BasicPaymentProduct googlePayPaymentProduct = getPaymentProduct(basicPaymentProducts.data, Constants.PAYMENTPRODUCTID_GOOGLEPAY);

				if (!GooglePayUtil.isGooglePayAllowed(context, communicator, googlePayPaymentProduct)) {
					removePaymentProduct(basicPaymentProducts.data, Constants.PAYMENTPRODUCTID_GOOGLEPAY);
				}
			}

		}
		return basicPaymentProducts;
	}

	private void removePaymentProduct(BasicPaymentProducts basicPaymentProducts, String paymentProductId) {
		for (BasicPaymentProduct paymentProduct: basicPaymentProducts.getBasicPaymentProducts()) {
			if (paymentProduct.getId().equals(paymentProductId)) {
				basicPaymentProducts.getBasicPaymentProducts().remove(paymentProduct);
				break;
			}
		}
	}

	private boolean containsPaymentProduct(BasicPaymentProducts basicPaymentProducts, String paymentProductId) {
		for (BasicPaymentProduct paymentProduct: basicPaymentProducts.getBasicPaymentProducts()) {
			if (paymentProduct.getId().equals(paymentProductId)) {
				return true;
			}
		}
		return false;
	}

	private BasicPaymentProduct getPaymentProduct(BasicPaymentProducts basicPaymentProducts, String paymentProductId) {

		BasicPaymentProduct returnedPaymentProduct = null;

		for (BasicPaymentProduct paymentProduct : basicPaymentProducts.getBasicPaymentProducts()) {
			if (paymentProduct.getId().equals(paymentProductId)) {
				returnedPaymentProduct =  paymentProduct;
			}
		}

		if (returnedPaymentProduct == null) {
			throw new IllegalStateException("Payment product not found");
		}

		return returnedPaymentProduct;
	}


	/**
	 * Interface for OnBasicPaymentProductsCallComplete listener
	 * Is called from the BasicPaymentProductsAsyncTask when it has the BasicPaymentProducts
	 *
	 * @deprecated use {@link BasicPaymentProductsCallListener} instead to receive callbacks with
	 * error information.
	 *
	 * Copyright 2017 Global Collect Services B.V
	 *
	 */
	public interface OnBasicPaymentProductsCallCompleteListener {
		void onBasicPaymentProductsCallComplete(BasicPaymentProducts basicPaymentProducts);
	}

	/**
	 * Updated interface for OnBasicPaymentProductsCallComplete listener
	 * Is called from the BasicPaymentProductsAsyncTask when it has the BasicPaymentProducts
	 * When there was an error and/or exception, the error callback will be
	 * invoked. On success the complete callback will be invoked.
	 *
	 * Copyright 2020 Global Collect Services B.V
	 *
	 */
	public interface BasicPaymentProductsCallListener {
		/**
		 * When async task was successful and data available
		 * @param basicPaymentProducts The payment products
		 */
		void onBasicPaymentProductsCallComplete(@NonNull BasicPaymentProducts basicPaymentProducts);

		/**
		 * When async task failed due to an error and/or exception
		 * @param error The error why payment products could not be retrieved
		 */
		void onBasicPaymentProductsCallError(ErrorResponse error);
	}
}
