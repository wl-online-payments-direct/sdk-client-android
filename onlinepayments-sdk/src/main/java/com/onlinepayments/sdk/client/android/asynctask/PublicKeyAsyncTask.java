/*
 * Copyright 2017 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.asynctask;

import java.security.InvalidParameterException;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.listener.PublicKeyResponseListener;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.model.api.ApiResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;

/**
 * AsyncTask which executes a public key lookup call to the Online Payments gateway.
 *
 * @deprecated In a future release, this class will become internal to the SDK. Use {@link com.onlinepayments.sdk.client.android.session.Session#getPublicKey(Context, PublicKeyResponseListener)} to obtain a public key.
 */
@Deprecated
public class PublicKeyAsyncTask extends AsyncTask<String, Void, ApiResponse<PublicKeyResponse>> {

	// The listener which will be called when a Public Key response is received, or an error occurs.
	private PublicKeyResponseListener listener;

	// The listener which will be called by the AsyncTask when a PublicKeyResponse is received
	private OnPublicKeyLoadedListener onPublicKeyLoadedListener;
	// The listener which will be called by the AsyncTask when a PublicKeyResponse is received
	private PublicKeyListener callListener;

	// Context needed for reading stubbed publickey data
	private Context context;

	// Communicator which does the communication to the Online Payments gateway
	private C2sCommunicator communicator;

	/**
	 * Create PublicKeyAsyncTask
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param listener {@link PublicKeyResponseListener} which will be called by the AsyncTask when a {@link PublicKeyResponse} has been received
	 */
	public PublicKeyAsyncTask(Context context, C2sCommunicator communicator, PublicKeyResponseListener listener) {
		this(context, communicator);
		if (listener == null) {
			throw new InvalidParameterException("Error creating PublicKeyAsyncTask, listener may not be null");
		}
		this.listener = listener;
	}

	/**
	 * Create PublicKeyAsyncTask
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param onPublicKeyLoadedListener {@link OnPublicKeyLoadedListener} which will be called by the AsyncTask when a {@link PublicKeyResponse} has been received
	 */
	public PublicKeyAsyncTask(Context context, C2sCommunicator communicator, OnPublicKeyLoadedListener onPublicKeyLoadedListener) {

		this(context, communicator);
		if (onPublicKeyLoadedListener == null) {
			throw new InvalidParameterException("Error creating PublicKeyAsyncTask, listener may not be null");
		}
		this.onPublicKeyLoadedListener = onPublicKeyLoadedListener;
	}

	/**
	 * Create PublicKeyAsyncTask
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
	 * @param listener {@link PublicKeyListener} which will be called by the AsyncTask when a {@link PublicKeyResponse} has been received
	 */
	public PublicKeyAsyncTask(Context context, C2sCommunicator communicator, PublicKeyListener listener) {

		this(context, communicator);
		if (listener == null) {
			throw new InvalidParameterException("Error creating PublicKeyAsyncTask, listener may not be null");
		}
		this.callListener = listener;
	}

	/**
	 * Create PublicKeyAsyncTask
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param communicator {@link C2sCommunicator} which does the communication to the Online Payments gateway
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
		if (onPublicKeyLoadedListener != null) {
			onPublicKeyLoadedListener.onPublicKeyLoaded(publicKeyResponse);
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

		if (listener != null) {
			if (response.error == null) {
				if (publicKeyResponse != null) {
					listener.onSuccess(publicKeyResponse);
				} else {
					ErrorResponse error = new ErrorResponse("Empty Response without Error");
					listener.onApiError(error);
				}
			} else {
				if (response.error.throwable != null) {
					listener.onException(response.error.throwable);
				} else {
					listener.onApiError(response.error);
				}
			}
		}
	}


	/**
	 * Callback Interface that is invoked when the Public key request completes.
	 *
	 * @deprecated use {@link PublicKeyResponseListener} instead.
	 */
	@Deprecated
	public interface OnPublicKeyLoadedListener {

		/**
		 * Listener that is called when publickey is loaded.
		 *
		 * @param response the {@link PublicKeyResponse} which contains the public key data
		 */
		void onPublicKeyLoaded(PublicKeyResponse response);
	}

	/**
	 * Callback Interface that is invoked when the Public key request completes.
	 * In case of an error and/or exception, the {@link #onPublicKeyError(ErrorResponse)} callback will be invoked.
	 * On success the {@link #onPublicKeyLoaded(PublicKeyResponse)} callback will be invoked.
	 *
	 * @deprecated use {@link PublicKeyResponseListener} instead.
	 */
	@Deprecated
	public interface PublicKeyListener {

		/**
		 * Invoked when the request was successful and data is available.
		 *
		 * @param response the {@link PublicKeyResponse} which contains the public key data
		 */
		void onPublicKeyLoaded(@NonNull PublicKeyResponse response);

		/**
		 * Invoked when the request failed due to a network error.
		 *
		 * @param error Error object that contains more information about the error that occurred.
		 */
		void onPublicKeyError(ErrorResponse error);
	}
}
