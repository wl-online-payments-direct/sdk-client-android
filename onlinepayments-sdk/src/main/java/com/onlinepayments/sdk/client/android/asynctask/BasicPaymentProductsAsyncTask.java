/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.asynctask;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.Callable;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.listener.BasicPaymentProductsResponseListener;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts;

/**
 * AsyncTask which loads all {@link BasicPaymentProducts} from the Online Payments Gateway.
 *
 * @deprecated In a future release, this class will become internal to the SDK. Use {@link com.onlinepayments.sdk.client.android.session.Session#getBasicPaymentProducts(Context, PaymentContext, BasicPaymentProductsResponseListener)} to obtain Basic Payment Products.
 */
@Deprecated
public class BasicPaymentProductsAsyncTask extends AsyncTask<String, Void, ApiResponse<BasicPaymentProducts>> implements Callable<BasicPaymentProducts> {

	// The listeners that will be called by the AsyncTask when the BasicPaymentProducts are loaded
	private List<OnBasicPaymentProductsCallCompleteListener> listeners;
	// The listeners that will be called by the AsyncTask when the BasicPaymentProducts are loaded
	private List<BasicPaymentProductsCallListener> callListeners;
	// The listeners that will be called by the AsyncTask when the BasicPaymentProducts are loaded
	private List<BasicPaymentProductsResponseListener> responseListeners;

	// Context needed for reading metadata which is sent to the Online Payments gateway
	private Context context;

	// Contains all the information needed to communicate with the Online Payments gateway to get BasicPaymentProducts
	private PaymentContext paymentContext;

	// Communicator which does the communication to the Online Payments gateway
	private C2sCommunicator communicator;

	/**
	 * Create a BasicPaymentProductsAsyncTask
	 *
	 * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link BasicPaymentProducts}
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param listeners list of {@link OnBasicPaymentProductsCallCompleteListener} which will be called by the AsyncTask when the {@link BasicPaymentProducts} are loaded
	 */
	public BasicPaymentProductsAsyncTask(Context context, PaymentContext paymentContext, C2sCommunicator communicator, List<OnBasicPaymentProductsCallCompleteListener> listeners) {
		this(context, paymentContext, communicator);
		if (listeners == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, listeners may not be null");
		}
		this.listeners = listeners;
	}

	/**
	 * Create a BasicPaymentProductsAsyncTask
	 *
	 * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link BasicPaymentProducts}
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param callListeners list of {@link BasicPaymentProductsCallListener} which will be called by the AsyncTask when the {@link BasicPaymentProducts} are loaded
	 */
	public BasicPaymentProductsAsyncTask(Context context, C2sCommunicator communicator, PaymentContext paymentContext, List<BasicPaymentProductsCallListener> callListeners) {
		this(context, paymentContext, communicator);
		if (callListeners == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, listeners may not be null");
		}
		this.callListeners = callListeners;
	}

	/**
	 * Create a BasicPaymentProductsAsyncTask
	 *
	 * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link BasicPaymentProducts}
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param responseListeners list of {@link BasicPaymentProductsResponseListener} which will be called by the AsyncTask when the {@link BasicPaymentProducts} are loaded
	 */
	public BasicPaymentProductsAsyncTask(C2sCommunicator communicator, Context context, PaymentContext paymentContext, List<BasicPaymentProductsResponseListener> responseListeners) {
		this(context, paymentContext, communicator);
		if (responseListeners == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, responseListeners may not be null");
		}
		this.responseListeners = responseListeners;
	}

	/**
	 * Create a BasicPaymentProductsAsyncTask
	 *
	 * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link BasicPaymentProducts}
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 */
	private BasicPaymentProductsAsyncTask(Context context, PaymentContext paymentContext, C2sCommunicator communicator) {

		if (context == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, context may not be null");
		}
		if (paymentContext == null ) {
			throw new InvalidParameterException("Error creating BasicPaymentProductsAsyncTask, paymentContext may not be null");
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

		if (responseListeners != null ) {
			for (BasicPaymentProductsResponseListener listener : responseListeners) {
				if (apiResponse.error == null) {
					if (basicPaymentProducts != null) {
						listener.onSuccess(basicPaymentProducts);
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

	@Override
	public BasicPaymentProducts call() throws Exception {

		// Load the BasicPaymentProducts from the Online Payments gateway
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
	 * Callback Interface that is invoked when the Basic Payment Products request completes.
	 *
	 * @deprecated use {@link BasicPaymentProductsResponseListener} instead to also receive callbacks with error information.
	 */
	@Deprecated
	public interface OnBasicPaymentProductsCallCompleteListener {
		void onBasicPaymentProductsCallComplete(BasicPaymentProducts basicPaymentProducts);
	}

	/**
	 * Callback Interface that is invoked when the Basic Payment Products request completes.
	 * In case of an error and/or exception, the {@link #onBasicPaymentProductsCallError(ErrorResponse)} callback will be invoked.
	 * On success the {@link #onBasicPaymentProductsCallComplete(BasicPaymentProducts)} callback will be invoked.
	 *
	 * @deprecated use {@link BasicPaymentProductsResponseListener} instead to also receive callbacks with error information.
	 */
	@Deprecated
	public interface BasicPaymentProductsCallListener {
		/**
		 * Invoked when the request was successful and data is available.
		 *
		 * @param basicPaymentProducts the payment products
		 */
		void onBasicPaymentProductsCallComplete(@NonNull BasicPaymentProducts basicPaymentProducts);

		/**
		 * Invoked when the request failed due to a network error.
		 *
		 * @param error Error object that contains more information about the error that occurred.
		 */
		void onBasicPaymentProductsCallError(ErrorResponse error);
	}
}
