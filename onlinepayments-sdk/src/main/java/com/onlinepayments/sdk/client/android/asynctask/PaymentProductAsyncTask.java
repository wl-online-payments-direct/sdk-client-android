/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.FormElement;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * AsyncTask which loads a {@link PaymentProduct} from the Online Payments Gateway.
 *
 * @deprecated In a future release, this class will become internal to the SDK. Use {@link com.onlinepayments.sdk.client.android.session.Session#getPaymentProduct(Context, String, PaymentContext, PaymentProductResponseListener)} to obtain a specific Payment Product.
 */
@Deprecated
public class PaymentProductAsyncTask extends AsyncTask<String, Void, ApiResponse<PaymentProduct>> {

	// The listeners which will be called by the AsyncTask when the PaymentProduct is retrieved
	private List<OnPaymentProductCallCompleteListener> listeners;

	// The listeners which will be called by the AsyncTask when the PaymentProduct is retrieved
	private List<PaymentProductCallListener> callListeners;

	// The listeners which will be called by the AsyncTask when the PaymentProduct is retrieved
	private List<PaymentProductResponseListener> responseListeners;

	// Context needed for reading metadata which is sent to the Online Payments gateway
	private Context context;

	// The productId for the product which need to be retrieved
	private String productId;

	// Communicator which does the communication to the Online Payments gateway
	private C2sCommunicator communicator;

	// PaymentContext which contains all necessary data for doing call to the Online Payments gateway to retrieve the PaymentProduct
	private PaymentContext paymentContext;

	/**
	 * Create PaymentProductAsyncTask
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param productId the productId of the product which need to be retrieved
	 * @param paymentContext {@link PaymentContext} which contains all necessary data for doing call to the Online Payments gateway to retrieve the {@link PaymentProduct}
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param listeners list of {@link OnPaymentProductCallCompleteListener} which will be called by the AsyncTask when the {@link PaymentProduct} is loaded
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
	 * Create PaymentProductAsyncTask
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param productId the productId of the product which need to be retrieved
	 * @param paymentContext {@link PaymentContext} which contains all necessary data for doing call to the Online Payments gateway to retrieve the {@link PaymentProduct}
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param callListeners list of {@link PaymentProductCallListener} which will be called by the AsyncTask when the {@link PaymentProduct} is loaded
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
	 * Create PaymentProductAsyncTask
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param productId the productId of the product which need to be retrieved
	 * @param paymentContext {@link PaymentContext} which contains all necessary data for doing call to the Online Payments gateway to retrieve the {@link PaymentProduct}
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param responseListeners list of {@link com.onlinepayments.sdk.client.android.listener.PaymentProductResponseListener} which will be called by the AsyncTask when the {@link PaymentProduct} is loaded
	 */
	public PaymentProductAsyncTask(Context context, C2sCommunicator communicator, String productId, PaymentContext paymentContext,
								   List<PaymentProductResponseListener> responseListeners) {

		this(context, productId, paymentContext, communicator);
		if (responseListeners == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, responseListeners may not be null");
		}
		this.responseListeners = responseListeners;
	}

	/**
	 * Create PaymentProductAsyncTask
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param productId the productId of the product which need to be retrieved
	 * @param paymentContext {@link PaymentContext} which contains all necessary data for doing call to the Online Payments gateway to retrieve the {@link PaymentProduct}
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
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

		if (responseListeners != null) {
			for (PaymentProductResponseListener listener : responseListeners) {
				if (apiResponse.error == null) {
					if (paymentProduct != null) {
						listener.onSuccess(paymentProduct);
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
				if (field.getDisplayHints().getFormElement().getFormElementType() == FormElement.Type.LIST) {
					field.getDisplayHints().getFormElement().setType(FormElement.Type.TEXT);
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
	 * Interface for the Async task that retrieves a {@link PaymentProduct}.
	 * Is called from the {@link PaymentProductAsyncTask} when it has retrieved a {@link PaymentProduct}.
	 *
	 * @deprecated use {@link PaymentProductResponseListener} instead to also receive callbacks with error information.
	 */
	@Deprecated
	public interface OnPaymentProductCallCompleteListener {
		void onPaymentProductCallComplete(PaymentProduct paymentProduct);
	}

	/**
	 * Callback Interface that is invoked when the Payment Product request completes.
	 * In case of an error and/or exception, the {@link #onPaymentProductCallError(ErrorResponse)} callback will be invoked.
	 * On success the {@link #onPaymentProductCallComplete(PaymentProduct)} callback will be invoked.
	 *
	 * @deprecated use {@link PaymentProductResponseListener} instead to also receive callbacks with error information.
	 */
	@Deprecated
	public interface PaymentProductCallListener {

		/**
		 * Invoked when the request was successful and data is available.
		 *
		 * @param paymentProduct the retrieved {@link PaymentProduct}
		 */
		void onPaymentProductCallComplete(@NonNull PaymentProduct paymentProduct);

		/**
		 * Invoked when the request failed due to a network error.
		 *
		 * @param error Error object that contains more information about the error that occurred.
		 */
		void onPaymentProductCallError(ErrorResponse error);
	}
}
