/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.session;

import java.security.InvalidParameterException;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.onlinepayments.sdk.client.android.asynctask.EncryptDataAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PublicKeyAsyncTask;
import com.onlinepayments.sdk.client.android.Util;
import com.onlinepayments.sdk.client.android.exception.EncryptDataException;
import com.onlinepayments.sdk.client.android.listener.PaymentRequestPreparedListener;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;

/**
 * Session contains all methods needed for making a payment.
 *
 * @deprecated In a future release, this class will become internal to the SDK. Use {@link Session#preparePaymentRequest(PaymentRequest, Context, PaymentRequestPreparedListener)} to encrypt your Payment Requests.
 */
@Deprecated
public class SessionEncryptionHelper implements EncryptDataAsyncTask.OnEncryptDataCompleteListener, PublicKeyAsyncTask.OnPublicKeyLoadedListener, PublicKeyAsyncTask.PublicKeyListener {

	private Context context;
	private OnPaymentRequestPreparedListener listener;
	private PaymentRequestPreparedListener paymentRequestPreparedListener;
	private PaymentRequest paymentRequest;
	private String clientSessionId;
	private Map<String, String> metaData;

	/**
	 * Helper for encrypting the payment request.
	 *
	 * @param context used for reading device metadata which is sent to the Online Payments gateway
	 * @param paymentRequest the {@link PaymentRequest} that will be encrypted
	 * @param clientSessionId the sessionId that is used to communicate with the Online Payments gateway
	 * @param listener {@link OnPaymentRequestPreparedListener} that will be called when encryption is completed
	 *
	 *
	 * @deprecated use {@link #SessionEncryptionHelper(PaymentRequest, String, Map, PaymentRequestPreparedListener)} instead.
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
	 * Helper for encrypting the {@link PaymentRequest}.
	 *
	 * @param paymentRequest the {@link PaymentRequest} that will be encrypted
	 * @param clientSessionId the sessionId that is used to communicate with the Online Payments gateway
	 * @param metaData the metadata which is sent to the Online Payments gateway
	 * @param listener {@link OnPaymentRequestPreparedListener} that will be called when encryption is completed
	 *
	 * @deprecated Use {@link SessionEncryptionHelper#SessionEncryptionHelper(PaymentRequest, String, Map, PaymentRequestPreparedListener)} instead
	 */
	@Deprecated
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
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, metaData may not be null");
		}

		this.clientSessionId = clientSessionId;
		this.listener = listener;
		this.paymentRequest = paymentRequest;
		this.metaData = metaData;
	}

	/**
	 * Helper for encrypting the {@link PaymentRequest}.
	 *
	 * @param paymentRequest the {@link PaymentRequest} that will be encrypted
	 * @param clientSessionId the sessionId that is used to communicate with the Online Payments gateway
	 * @param metaData the metadata which is sent to the Online Payments gateway
	 * @param listener {@link PaymentRequestPreparedListener} that will be called when encryption is completed
	 */
	public SessionEncryptionHelper(PaymentRequest paymentRequest, String clientSessionId, Map<String, String> metaData, PaymentRequestPreparedListener listener) {

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
			throw new InvalidParameterException("Error creating SessionEncryptionHelper, metaData may not be null");
		}

		this.clientSessionId = clientSessionId;
		this.paymentRequestPreparedListener = listener;
		this.paymentRequest = paymentRequest;
		this.metaData = metaData;
	}


	/**
	 * Listener for loaded public key as {@link PublicKeyResponse} from the Online Payments gateway.
	 */
	@Override
	public void onPublicKeyLoaded(PublicKeyResponse response) {
	     onPublicKeyReceived(response);
	}

	@Override
	public void onPublicKeyError(ErrorResponse error) {
		Log.w("PublicKey", error.message + error.apiError);
		if (paymentRequestPreparedListener != null) {
			paymentRequestPreparedListener.onFailure(new EncryptDataException("Error while retrieving public key: " + error.message));
		}
	}

	/**
	 * Notify listeners for encryption result.
	 */
	@Override
	public void onEncryptDataComplete(String encryptedData) {
		notifyOldListener(encryptedData);
		notifyListener(encryptedData);
	}

	private void notifyListener(@Nullable String encryptedData) {
		if (paymentRequestPreparedListener != null) {
			if (encryptedData == null) {
				paymentRequestPreparedListener.onFailure(new EncryptDataException("Encryption result is null"));
				return;
			}

			// Call the OnPaymentRequestPrepared listener with the new PreparedPaymentRequest()
			if (metaData != null && !metaData.isEmpty()) {
				paymentRequestPreparedListener.onPaymentRequestPrepared(new PreparedPaymentRequest(encryptedData, Util.getBase64EncodedMetadata(metaData)));
			} else {
				paymentRequestPreparedListener.onPaymentRequestPrepared(new PreparedPaymentRequest(encryptedData, Util.getBase64EncodedMetadata(context)));
			}
		}
	}

	/**
	 * Temporary function to handle deprecated listener
	 * @param encryptedData String representing the encrypted fields for the payment request
	 */
	@Deprecated
	private void notifyOldListener(String encryptedData) {
		if (listener != null) {
			// Call the OnPaymentRequestPrepared listener with the new PreparedPaymentRequest()
			if (metaData != null && !metaData.isEmpty()) {
				listener.onPaymentRequestPrepared(new PreparedPaymentRequest(encryptedData, Util.getBase64EncodedMetadata(metaData)));
			} else {
				listener.onPaymentRequestPrepared(new PreparedPaymentRequest(encryptedData, Util.getBase64EncodedMetadata(context)));
			}
		}

	}

	public void onPublicKeyReceived(@NonNull PublicKeyResponse response) {
		EncryptDataAsyncTask task = new EncryptDataAsyncTask(response, paymentRequest, clientSessionId, this);
		task.execute();
	}


	/**
     * Interface for OnPaymentRequestPreparedListener.
     * Is called from the {@link Session} when it has encrypted the given payment product fields and composed the {@link PreparedPaymentRequest} object with them.
	 *
	 * @deprecated Use {@link PaymentRequestPreparedListener} instead
     */
	@Deprecated
    public interface OnPaymentRequestPreparedListener {
        void onPaymentRequestPrepared(PreparedPaymentRequest preparedPaymentRequest);
    }
}
