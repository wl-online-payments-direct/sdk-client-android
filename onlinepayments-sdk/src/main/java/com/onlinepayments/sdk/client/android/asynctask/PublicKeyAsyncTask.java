package com.onlinepayments.sdk.client.android.asynctask;

import java.security.InvalidParameterException;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;

/**
 * AsyncTask which executes an publickey lookup call to the Online Payments platform
 *
 * Copyright 2017 Global Collect Services B.V
 *
 */
public class PublicKeyAsyncTask extends AsyncTask<String, Void, ApiResponse<PublicKeyResponse>> {

	// The listener which will be called by the AsyncTask
	private OnPublicKeyLoadedListener listener;

	private PublicKeyListener callListener;

	// Context needed for reading stubbed publickey data
	private Context context;

	// Communicator which does the communication to the GC gateway
	private C2sCommunicator communicator;

	/**
	 * Constructor
	 * @deprecated use {@link #PublicKeyAsyncTask(Context, C2sCommunicator, PublicKeyListener)}
	 *
	 * @param context, needed for reading stubbed publickey data
	 * @param communicator, Communicator which does the communication to the GC gateway
	 * @param listener, listener which will be called by the AsyncTask
	 */
	public PublicKeyAsyncTask(Context context, C2sCommunicator communicator, OnPublicKeyLoadedListener listener) {

		this(context, communicator);
		if (listener == null) {
			throw new InvalidParameterException("Error creating PublicKeyAsyncTask, listener may not be null");
		}
		this.listener = listener;
	}

	/**
	 * Constructor
	 * @param context, needed for reading stubbed publickey data
	 * @param communicator, Communicator which does the communication to the GC gateway
	 * @param listener, listener which will be called by the AsyncTask
	 */
	public PublicKeyAsyncTask(Context context, C2sCommunicator communicator, PublicKeyListener listener) {

		this(context, communicator);
		if (listener == null) {
			throw new InvalidParameterException("Error creating PublicKeyAsyncTask, listener may not be null");
		}
		this.callListener = listener;
	}

	/**
	 * Constructor
	 * @param context, needed for reading stubbed publickey data
	 * @param communicator, Communicator which does the communication to the GC gateway
	 */
	public PublicKeyAsyncTask(Context context, C2sCommunicator communicator) {

		if (context == null) {
			throw new InvalidParameterException("Error creating PublicKeyAsyncTask, context may not be null");
		}
		if (communicator == null) {
			throw new InvalidParameterException("Error creating PublicKeyAsyncTask, communicator may not be null");
		}
		this.context = context;
		this.communicator = communicator;
	}


	@Override
	protected ApiResponse<PublicKeyResponse> doInBackground(String... params) {

		// Do the call to the Online Payments platform
		return communicator.getPublicKey(context);
	}


	@Override
	protected void onPostExecute(ApiResponse<PublicKeyResponse> response) {

		PublicKeyResponse publicKeyResponse = response.data;

		// Call listener callback
		if (listener != null) {
			listener.onPublicKeyLoaded(publicKeyResponse);
		}

		if (callListener != null) {
			if (response.error == null) {
				if (publicKeyResponse != null) {
					callListener.onPublicKeyLoaded(publicKeyResponse);
				} else {
					ErrorResponse error = new ErrorResponse("Empty Response without Error");
					callListener.onPublicKeyError(error);
				}
			} else {
				callListener.onPublicKeyError(response.error);
			}
		}
	}


	/**
	 * Interface for OnPublicKeyLoaded listener
	 * Is called from the PublicKeyAsyncTask when it has the publickey
	 *
	 * @deprecated use {@link PublicKeyListener} instead
	 *
	 * Copyright 2017 Global Collect Services B.V
	 *
	 */
	public interface OnPublicKeyLoadedListener {

		/**
		 * Listener that is called when publickey is loaded
		 * @param response, the PublicKeyResponse which contains the public key data
		 */
		void onPublicKeyLoaded(PublicKeyResponse response);
	}

	/**
	 * Updated interface for OnPublicKeyLoaded listener
	 * Is called from the PublicKeyAsyncTask when it has retrieved
	 * a BasicPaymentProductGroup with fields, by invoking the complete callback.
	 * When there was an error and/or exception, the error callback will be invoked.
	 *
	 * Copyright 2020 Global Collect Services B.V
	 *
	 */
	public interface PublicKeyListener {

		/**
		 * Listener that is called when publickey is loaded
		 * @param response, the PublicKeyResponse which contains the public key data
		 */
		void onPublicKeyLoaded(@NonNull PublicKeyResponse response);

		/**
		 * When async task failed due to an error and/or exception
		 * @param error The error why retrieving the public key failed
		 */
		void onPublicKeyError(ErrorResponse error);
	}
}
