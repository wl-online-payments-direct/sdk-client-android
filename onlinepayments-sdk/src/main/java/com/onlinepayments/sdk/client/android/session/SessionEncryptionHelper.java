package com.onlinepayments.sdk.client.android.session;

import java.security.InvalidParameterException;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.onlinepayments.sdk.client.android.asynctask.EncryptDataAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PublicKeyAsyncTask;
import com.onlinepayments.sdk.client.android.Util;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;

/**
 * Session contains all methods needed for making a payment
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class SessionEncryptionHelper implements EncryptDataAsyncTask.OnEncryptDataCompleteListener, PublicKeyAsyncTask.OnPublicKeyLoadedListener, PublicKeyAsyncTask.PublicKeyListener {


	private Context context;
	private OnPaymentRequestPreparedListener listener;
	private PaymentRequest paymentRequest;
	private String clientSessionId;
	private Map<String, String> metaData;

	/**
	 * Helper for encrypting the payment request
	 * @param context used for reading device metada which is send to the GC gateway
	 * @param paymentRequest the payment that will be encrypted
	 * @param clientSessionId the sessionId that is used to communicate with the GC gateway
	 * @param listener the listener that waits for the callback of the encryption
	 * @deprecated use {@link #SessionEncryptionHelper(PaymentRequest, String, Map, OnPaymentRequestPreparedListener)} instead.
     */
	@Deprecated
	public SessionEncryptionHelper(Context context, PaymentRequest paymentRequest, String clientSessionId, OnPaymentRequestPreparedListener listener) {

		if (paymentRequest == null ) {
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, paymentRequest may not be null");
		}
		if (context == null ) {
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, context may not be null");
		}
		if (clientSessionId == null ) {
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, clientSessionId may not be null");
		}
		if (listener == null ) {
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, listener may not be null");
		}

		this.context = context;
		this.clientSessionId = clientSessionId;
		this.listener = listener;
		this.paymentRequest = paymentRequest;
	}

	/**
	 * Helper for encrypting the payment request
	 * @param paymentRequest the payment that will be encrypted
	 * @param clientSessionId the sessionId that is used to communicate with the GC gateway
	 * @param metaData the metadata which is send to the GC gateway
	 * @param listener the listener that waits for the callback of the encryption
	 */
	public SessionEncryptionHelper(PaymentRequest paymentRequest, String clientSessionId, Map<String, String> metaData, OnPaymentRequestPreparedListener listener) {

		if (paymentRequest == null ) {
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, paymentRequest may not be null");
		}
		if (clientSessionId == null ) {
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, clientSessionId may not be null");
		}
		if (listener == null ) {
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, listener may not be null");
		}
		if (metaData == null) {
			throw new InvalidParameterException("Error cretaing SessionEncryptionHelper, metaData may not be null");
		}

		this.clientSessionId = clientSessionId;
		this.listener = listener;
		this.paymentRequest = paymentRequest;
		this.metaData = metaData;
	}


	/**
	 * Listener for loaded publickey from the GC gateway
	 */
	@Override
	public void onPublicKeyLoaded(PublicKeyResponse response) {
	     EncryptDataAsyncTask task = new EncryptDataAsyncTask(response, paymentRequest, clientSessionId, this);
	     task.execute();
	}

	@Override
	public void onPublicKeyError(ErrorResponse error) {
		Log.w("PublicKey", error.message + error.apiError);
	}


	/**
	 * Listener for encrypting data
	 */
	@Override
	public void onEncryptDataComplete(String encryptedData) {

		// Call the OnPaymentRequestPrepared listener with the new PreparedPaymentRequest()
		if (metaData != null && !metaData.isEmpty()) {
			listener.onPaymentRequestPrepared(new PreparedPaymentRequest(encryptedData, Util.getBase64EncodedMetadata(metaData)));
		} else {
			listener.onPaymentRequestPrepared(new PreparedPaymentRequest(encryptedData, Util.getBase64EncodedMetadata(context)));
		}
	}


	 /**
     * Interface for OnPaymentRequestPrepared listener
     * Is called from the Session when it has encrypted the given paymentproductfields and composed the PreparedPaymentRequest object with them
     *
     * Copyright 2020 Global Collect Services B.V
     *
     */
    public interface OnPaymentRequestPreparedListener {
        void onPaymentRequestPrepared(PreparedPaymentRequest preparedPaymentRequest);
    }
}
