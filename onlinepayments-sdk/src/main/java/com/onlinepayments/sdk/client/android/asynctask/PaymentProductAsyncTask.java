package com.onlinepayments.sdk.client.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.FormElement;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * AsyncTask which loads a PaymentProduct with fields from the GC Gateway
 *
 * Copyright 2017 Global Collect Services B.V
 *
 */
public class PaymentProductAsyncTask extends AsyncTask<String, Void, ApiResponse<PaymentProduct>> {

	// The listener which will be called by the AsyncTask when PaymentProduct with fields is retrieved
	private List<OnPaymentProductCallCompleteListener> listeners;

	private List<PaymentProductCallListener> callListeners;

	// Context needed for reading stubbed PaymentProduct
	private Context context;

	// The productId for the product which need to be retrieved
	private String productId;

	// Communicator which does the communication to the GC gateway
	private C2sCommunicator communicator;

	// PaymentContext which contains all neccesary data for doing call to the GC gateway to retrieve paymentproducts
	private PaymentContext paymentContext;

	/**
	 * Constructor
	 * @deprecated use {@link #PaymentProductAsyncTask(Context, String, C2sCommunicator, PaymentContext, List)} instead
	 *
	 * @param context, used for reading stubbing data
	 * @param productId, the productId for the product which need to be retrieved
	 * @param paymentContext, PaymentContext which contains all neccesary data for doing call to the GC gateway to retrieve paymentproducts
	 * @param communicator, communicator which does the communication to the GC gateway
	 * @param listeners, listener which will be called by the AsyncTask when the PaymentProduct with fields is retrieved
	 */
	public PaymentProductAsyncTask(Context context, String productId, PaymentContext paymentContext, C2sCommunicator communicator,
								   List<OnPaymentProductCallCompleteListener> listeners) {

		this(context, productId, paymentContext, communicator);
		if (listeners == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, listener may not be null");
		}
		this.listeners = listeners;
	}

	/**
	 * Constructor
	 * @param context, used for reading stubbing data
	 * @param productId, the productId for the product which need to be retrieved
	 * @param paymentContext, PaymentContext which contains all neccesary data for doing call to the GC gateway to retrieve paymentproducts
	 * @param communicator, communicator which does the communication to the GC gateway
	 * @param callListeners, listeners which will be called by the AsyncTask when the PaymentProduct with fields is retrieved
	 */
	public PaymentProductAsyncTask(Context context, String productId, C2sCommunicator communicator, PaymentContext paymentContext,
								   List<PaymentProductCallListener> callListeners) {

		this(context, productId, paymentContext, communicator);
		if (callListeners == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, listener may not be null");
		}
		this.callListeners = callListeners;
	}

	/**
	 * Constructor
	 * @param context, used for reading stubbing data
	 * @param productId, the productId for the product which need to be retrieved
	 * @param paymentContext, PaymentContext which contains all neccesary data for doing call to the GC gateway to retrieve paymentproducts
	 * @param communicator, communicator which does the communication to the GC gateway
	 */
	private PaymentProductAsyncTask(Context context, String productId, PaymentContext paymentContext, C2sCommunicator communicator) {

		if (context == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, context may not be null");
		}
		if (productId == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, productId may not be null");
		}
		if (paymentContext == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, paymentContext may not be null");
		}
		if (communicator == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, communicator may not be null");
		}

		this.context = context;
		this.productId = productId;
		this.paymentContext = paymentContext;
		this.communicator = communicator;
	}

	@Override
	protected ApiResponse<PaymentProduct> doInBackground(String... params) {

		if (productId.equals(Constants.PAYMENTPRODUCTID_APPLEPAY)) {

			// Apple pay is not supported for Android devices
			ApiResponse<PaymentProduct> errorResponse = new ApiResponse();
			errorResponse.error = new ErrorResponse("Apple Pay not supported on Android devices");
			return errorResponse;
		} else {
			// Don't return Google Pay if it is not supported for the current payment.
			ApiResponse<PaymentProduct> paymentProduct = communicator.getPaymentProduct(productId, context, paymentContext);
			if (paymentProduct.data != null && (productId.equals(Constants.PAYMENTPRODUCTID_GOOGLEPAY) && !GooglePayUtil.isGooglePayAllowed(context, communicator, paymentProduct.data))) {
				paymentProduct.data = null;
				paymentProduct.error = new ErrorResponse("Product is GooglePay and not supported for the current payment");
			}
			return paymentProduct;
		}
	}

	@Override
	protected void onPostExecute(ApiResponse<PaymentProduct> apiResponse) {

		PaymentProduct paymentProduct = apiResponse.data;

		paymentProduct = fixProductParametersIfRequired(paymentProduct);

		// Call listener callback
		if (listeners != null) {
			for (OnPaymentProductCallCompleteListener listener : listeners) {
				listener.onPaymentProductCallComplete(paymentProduct);
			}
		}

		if (callListeners != null ) {
			for (PaymentProductCallListener listener : callListeners) {
				if (apiResponse.error == null) {
					if (paymentProduct != null) {
						listener.onPaymentProductCallComplete(paymentProduct);
					} else {
						ErrorResponse error = new ErrorResponse("Empty Response without Error");
						listener.onPaymentProductCallError(error);
					}
				} else {
					listener.onPaymentProductCallError(apiResponse.error);
				}
			}
		}
	}

	private PaymentProduct fixProductParametersIfRequired(PaymentProduct paymentProduct) {
		final String EXPIRY_DATE_MASK = "{{99}}/{{99}}";
		final String BASIC_CARD_NUMBER_MASK = "{{9999}} {{9999}} {{9999}} {{9999}}";
		final String AMEX_CARD_NUMBER_MASK = "{{9999}} {{999999}} {{99999}}";
		final String AMEX_PRODUCT_ID = "2";
		final String EXPIRY_DATE = "expiryDate";
		final String CARD_NUMBER = "cardNumber";

		if (paymentProduct == null) {
			return null;
		}

		for (PaymentProductField field : paymentProduct.getPaymentProductFields()) {
			String fieldId = field.getId();
			if (!EXPIRY_DATE.equals(fieldId) && !CARD_NUMBER.equals(fieldId)) {
				continue;
			}

			// If this is the expiry date field, change the mask and possibly the type
			if (EXPIRY_DATE.equals(field.getId())) {
				// Change the type if it is LIST
				if (field.getDisplayHints().getFormElement().getType() == FormElement.ListType.LIST) {
					field.getDisplayHints().getFormElement().setType(FormElement.ListType.TEXT);
				}
				// Add the mask, if it's null or empty
				if (field.getDisplayHints().getMask() == null || field.getDisplayHints().getMask().isEmpty()) {
					field.getDisplayHints().setMask(EXPIRY_DATE_MASK);
				}
			}

			// If this is the card number field, change the mask if it is null or empty
			if (CARD_NUMBER.equals(fieldId) &&
					(field.getDisplayHints().getMask() == null || field.getDisplayHints().getMask().isEmpty())) {

				if (AMEX_PRODUCT_ID.equals(paymentProduct.getId())) {
					// Set American Express card number mask
					field.getDisplayHints().setMask(AMEX_CARD_NUMBER_MASK);
				} else {
					// Set the basic card number mask
					field.getDisplayHints().setMask(BASIC_CARD_NUMBER_MASK);
				}
			}
		}
		return paymentProduct;
	}

	/**
	 * Interface for OnPaymentProductCallComplete listener
	 * Is called from the PaymentProductAsyncTask when it has retrieved a PaymentProduct with fields
	 *
	 * @deprecated use {@link PaymentProductCallListener} instead to receive callbacks with
	 * error information.
	 *
	 * Copyright 2017 Global Collect Services B.V
	 *
	 */
	public interface OnPaymentProductCallCompleteListener {
		void onPaymentProductCallComplete(PaymentProduct paymentProduct);
	}

	/**
	 * Updated interface for OnPaymentProductCallComplete listener
	 * Is called from the PaymentProductAsyncTask when it has retrieved a PaymentProduct with fields
	 * When there was an error and/or exception, the error callback will be invoked.
	 * On success the complete callback will be invoked.
	 *
	 * Copyright 2020 Global Collect Services B.V
	 *
	 */
	public interface PaymentProductCallListener {

		/**
		 * Listener that is called when the PaymentProduct response has completed
		 *
		 * @param paymentProduct The PaymentProduct
		 */
		void onPaymentProductCallComplete(@NonNull PaymentProduct paymentProduct);

		/**
		 * When async task failed due to an error and/or exception
		 * @param error The error why PaymentProduct failed
		 */
		void onPaymentProductCallError(ErrorResponse error);
	}
}
