/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.model.iin.IinStatus;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * AsyncTask which executes an IIN lookup call to the Online Payments gateway.
 *
 * @deprecated In a future release, this class will become internal to the SDK. Use {@link com.onlinepayments.sdk.client.android.session.Session#getIinDetails(Context, String, IinLookupResponseListener, PaymentContext)} to obtain the Iin Details.
 */
@Deprecated
public class IinLookupAsyncTask extends AsyncTask<String, Void, ApiResponse<IinDetailsResponse>> {

	// Minimal nr of chars before doing a iin lookup
	private final Integer IIN_LOOKUP_MIN_NR_OF_CHARS = 6;

	// The listeners which will be called by the AsyncTask when the IinDetailsResponse is received
	private List<OnIinLookupCompleteListener> listeners;

	// The listeners which will be called by the AsyncTask when the IinDetailsResponse is received
	private List<IinLookupCompleteListener> callListeners;

	// The listeners that will be called by the AsyncTask when the IinDetailsResponse is received
	private List<IinLookupResponseListener> responseListeners;

	// Context needed for reading stubbed IinLookup
	private Context context;

	// Entered partial creditcardnumber
	private String partialCreditCardNumber;

	// Communicator which does the communication to the Online Payments gateway
	private C2sCommunicator communicator;

	// Payment context that is sent in the request
	private PaymentContext paymentContext;


	/**
	 * Create IinLookupAsyncTask
	 *
	 * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
	 * @param partialCreditCardNumber partial credit card number that was entered by the user
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param listeners list of {@link OnIinLookupCompleteListener} which will be called by the AsyncTask when the {@link IinDetailsResponse} is loaded
	 * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link IinDetailsResponse}; May be null, but this will yield a limited response from the gateway
	 */
    public IinLookupAsyncTask(Context context, String partialCreditCardNumber, C2sCommunicator communicator,
    						  List<OnIinLookupCompleteListener> listeners, PaymentContext paymentContext) {
		if (context == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, context may not be null");
		}
		if (partialCreditCardNumber == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, partialCreditCardNumber may not be null");
		}
		if (communicator == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, communicator may not be null");
		}
		if (listeners == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, listeners may not be null");
		}

		this.context = context;
		this.listeners = listeners;
		this.communicator = communicator;
		this.partialCreditCardNumber = partialCreditCardNumber;
		this.paymentContext = paymentContext;
    }

	/**
	 * Create IinLookupAsyncTask
	 *
	 * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
	 * @param partialCreditCardNumber partial credit card number that was entered by the user
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link IinDetailsResponse}; May be null, but this will yield a limited response from the gateway
	 * @param callListeners list of {@link IinLookupCompleteListener} which will be called by the AsyncTask when the {@link IinDetailsResponse} is loaded
	 */
	public IinLookupAsyncTask(Context context, String partialCreditCardNumber, C2sCommunicator communicator,
							  PaymentContext paymentContext, List<IinLookupCompleteListener> callListeners) {
		if (context == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, context may not be null");
		}
		if (partialCreditCardNumber == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, partialCreditCardNumber may not be null");
		}
		if (communicator == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, communicator may not be null");
		}
		if (callListeners == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, callListeners may not be null");
		}

		this.context = context;
		this.callListeners = callListeners;
		this.communicator = communicator;
		this.partialCreditCardNumber = partialCreditCardNumber;
		this.paymentContext = paymentContext;
	}

	/**
	 * Create IinLookupAsyncTask
	 *
	 * @param context {@link Context} used for reading device metadata which is sent to the Online Payments gateway
	 * @param partialCreditCardNumber partial credit card number that was entered by the user
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param paymentContext {@link PaymentContext} which contains all necessary payment data for doing a call to the Online Payments gateway to get the {@link IinDetailsResponse}; May be null, but this will yield a limited response from the gateway
	 * @param responseListeners list of {@link IinLookupCompleteListener} which will be called by the AsyncTask when the {@link IinDetailsResponse} is loaded
	 *
	 * @deprecated Use {@link #IinLookupAsyncTask(Context, String, C2sCommunicator, PaymentContext, List)} instead
	 */
	public IinLookupAsyncTask(Context context, C2sCommunicator communicator, String partialCreditCardNumber,
							  PaymentContext paymentContext, List<IinLookupResponseListener> responseListeners) {
		if (context == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, context may not be null");
		}
		if (partialCreditCardNumber == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, partialCreditCardNumber may not be null");
		}
		if (communicator == null ) {
			throw new InvalidParameterException("Error creating PaymentProductAsyncTask, communicator may not be null");
		}
		if (responseListeners == null) {
			throw new InvalidParameterException("Error creating IinLookupAsyncTask, responseListeners may not be null");
		}

		this.context = context;
		this.responseListeners = responseListeners;
		this.communicator = communicator;
		this.partialCreditCardNumber = partialCreditCardNumber;
		this.paymentContext = paymentContext;
	}

	@Override
    protected ApiResponse<IinDetailsResponse> doInBackground(String... params) {

    	// Check if partialCreditCardNumber >= IIN_LOOKUP_MIN_NR_OF_CHARS
    	// If not return IinStatus.NOT_ENOUGH_DIGITS
    	if (partialCreditCardNumber.length() < IIN_LOOKUP_MIN_NR_OF_CHARS) {
    		return defaultIinResponse(IinStatus.NOT_ENOUGH_DIGITS);
    	}

		ApiResponse<IinDetailsResponse> iinResponse = communicator.getPaymentProductIdByCreditCardNumber(partialCreditCardNumber, context, paymentContext);

		// Determine the result of the lookup
		if (iinResponse.error != null || iinResponse.data.getPaymentProductId() == null) {
			// If the iinResponse is null or the paymentProductId is null, then return IinStatus.UNKNOWN
			return defaultIinResponse(IinStatus.UNKNOWN);

		} else if (!iinResponse.data.isAllowedInContext()) {
			// If the paymentproduct is currently not allowed, then return IinStatus.SUPPORTED_BUT_NOT_ALLOWED
			return defaultIinResponse(IinStatus.EXISTING_BUT_NOT_ALLOWED);

		} else {
			// This is a correct result, store this result in the cache and return IinStatus.SUPPORTED
			iinResponse.data.setStatus(IinStatus.SUPPORTED);
			return iinResponse;
		}
	}

	private ApiResponse<IinDetailsResponse> defaultIinResponse(IinStatus status) {
		ApiResponse<IinDetailsResponse> defaultIinDetailsResponse = new ApiResponse<>();
		defaultIinDetailsResponse.data = new IinDetailsResponse(status);

		return defaultIinDetailsResponse;
	}

    @Override
    protected void onPostExecute(ApiResponse<IinDetailsResponse> apiResponse) {

		IinDetailsResponse iinDetailsResponse = apiResponse.data;

    	// Call listener callbacks
		if (listeners != null) {
			for (OnIinLookupCompleteListener listener : listeners) {
				listener.onIinLookupComplete(iinDetailsResponse);
			}
		}

		if (callListeners != null ) {
			for (IinLookupCompleteListener listener : callListeners) {
				if (apiResponse.error == null) {
					if (iinDetailsResponse != null) {
						listener.onIinLookupComplete(iinDetailsResponse);
					} else {
						ErrorResponse error = new ErrorResponse("Empty Response without Error");
						listener.onIinLookupError(error);
					}
				} else {
					listener.onIinLookupError(apiResponse.error);
				}
			}
		}

		if (responseListeners != null ) {
			for (IinLookupResponseListener listener : responseListeners) {
				if (apiResponse.error == null) {
					if (iinDetailsResponse != null) {
						listener.onSuccess(iinDetailsResponse);
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
     * Callback Interface that is invoked when the IIN lookup completes.
	 *
	 * @deprecated use {@link com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener} instead to also receive callbacks containing error information.
     */
	@Deprecated
    public interface OnIinLookupCompleteListener {

    	/**
    	 * Listener that is called when IIN lookup is done.
		 *
    	 * @param response the {@link IinDetailsResponse} returned by the Online Payments gateway
    	 */
        void onIinLookupComplete(IinDetailsResponse response);
    }

	/**
	 * Callback Interface that is invoked when the IIN lookup completes.
	 * In case of an error and/or exception, the {@link #onIinLookupError(ErrorResponse)} callback will be invoked.
	 * On success the {@link #onIinLookupComplete(IinDetailsResponse)} callback will be invoked.
	 *
	 * @deprecated use {@link com.onlinepayments.sdk.client.android.listener.IinLookupResponseListener} instead to also receive callbacks containing error information.
	 */
	@Deprecated
	public interface IinLookupCompleteListener {
		/**
		 * Invoked when the request was successful and data is available.
		 *
		 * @param response the {@link IinDetailsResponse} returned by the Online Payments gateway
		 */
		void onIinLookupComplete(IinDetailsResponse response);

		/**
		 * Invoked when the request failed due to a network error.
		 *
		 * @param error Error object that contains more information about the error that occurred.
		 */
		void onIinLookupError(ErrorResponse error);
	}
}
