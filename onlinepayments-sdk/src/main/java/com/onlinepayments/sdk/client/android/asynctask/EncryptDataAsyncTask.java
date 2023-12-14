/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.asynctask;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.os.AsyncTask;

import com.onlinepayments.sdk.client.android.encryption.EncryptData;
import com.onlinepayments.sdk.client.android.encryption.Encryptor;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProductField;
import com.onlinepayments.sdk.client.android.session.SessionEncryptionHelper;

/**
 * AsyncTask which encrypts all PaymentProductFields with the Online Payments gateway public key.
 *
 * @deprecated In a future release, this class will become internal to the SDK. Use {@link com.onlinepayments.sdk.client.android.session.Session#preparePaymentRequest(PaymentRequest, Context, SessionEncryptionHelper.OnPaymentRequestPreparedListener)} to obtain an encrypted Prepared Payment Request.
 */
@Deprecated
public class EncryptDataAsyncTask extends AsyncTask<String, Void, String>{

	// The listener which will be called by the AsyncTask when the PaymentProductFields are encrypted
	private OnEncryptDataCompleteListener listener;

	// PaymentRequest for which the PaymentProductFields will be encrypted
	private PaymentRequest paymentRequest;
	// Contains the Online Payments gateway public key that will be used for encrypting the PaymentProductFields
	private PublicKeyResponse publicKeyResponse;
	// Used for identifying the customer on the Online Payments gateway
	private String clientSessionId;

	/**
	 * Create a EncryptDataAsyncTask
	 *
	 * @param publicKeyResponse {@link PublicKeyResponse} contains the Online Payments gateway public key that will be used for encrypting the PaymentProductFields
	 * @param paymentRequest the {@link PaymentRequest} for which the PaymentProductFields will be encrypted
	 * @param clientSessionId used for identifying the session on the Online Payments gateway
	 * @param listener {@link OnEncryptDataCompleteListener} which will be called by the AsyncTask when the PaymentProductFields are encrypted
	 */
    public EncryptDataAsyncTask(PublicKeyResponse publicKeyResponse, PaymentRequest paymentRequest, String clientSessionId, OnEncryptDataCompleteListener listener) {

    	if (publicKeyResponse == null) {
			throw new InvalidParameterException("Error creating EncryptDataAsyncTask, publicKeyResponse may not be null");
		}
    	if (paymentRequest == null) {
			throw new InvalidParameterException("Error creating EncryptDataAsyncTask, paymentRequest may not be null");
		}
    	if (clientSessionId == null) {
			throw new InvalidParameterException("Error creating EncryptDataAsyncTask, clientSessionId may not be null");
		}
    	if (listener == null) {
			throw new InvalidParameterException("Error creating EncryptDataAsyncTask, listener may not be null");
		}

    	this.clientSessionId = clientSessionId;
        this.listener = listener;
        this.paymentRequest = paymentRequest;
        this.publicKeyResponse = publicKeyResponse;
    }


    @Override
    protected String doInBackground(String... params) {

    	EncryptData encryptData = new EncryptData();

    	// Format all values based on their paymentproductfield.type and them to the encryptedValues
    	Map<String, String> formattedPaymentValues = new HashMap<>();
    	for (PaymentProductField field : paymentRequest.getPaymentProduct().getPaymentProductFields()) {

    		String value = paymentRequest.getValue(field.getId());
			if (value != null) {
				String unmaskedValue = paymentRequest.getUnmaskedValue(field.getId(), value);

				// The date and expiry date are already in the correct format.
				// If the masks given by the Online Payments gateway are correct
				if (field.getType() != null && unmaskedValue != null) {
					if (field.getType().equals(PaymentProductField.Type.NUMERICSTRING)) {
						formattedPaymentValues.put(field.getId(), unmaskedValue.replaceAll("[^\\d.]", ""));
					} else {
						formattedPaymentValues.put(field.getId(), unmaskedValue);
					}
				}
			}
    	}

    	encryptData.setPaymentValues(formattedPaymentValues);

    	// Add the clientSessionId
    	encryptData.setClientSessionId(clientSessionId);

    	// Add UUID nonce
    	encryptData.setNonce(UUID.randomUUID().toString());

       	// Add paymentproductId and accountOnFileId to the encryptData
    	if (paymentRequest.getAccountOnFile() != null) {
    		encryptData.setAccountOnFileId(paymentRequest.getAccountOnFile().getId());
    	}
    	encryptData.setPaymentProductId(Integer.parseInt(paymentRequest.getPaymentProduct().getId()));

    	// See if the payment must be remembered
    	if (paymentRequest.getTokenize()) {
    		encryptData.setTokenize(true);
    	}

    	// Encrypt all the fields in the PaymentProduct
    	Encryptor encryptor = new Encryptor(publicKeyResponse);
		return encryptor.encrypt(encryptData);
    }


    @Override
    protected void onPostExecute(String encryptedData) {

    	// Call listener callback
    	listener.onEncryptDataComplete(encryptedData);
    }


    /**
     * Callback Interface that is invoked when the encryption is completed.
	 * On success the {@link #onEncryptDataComplete(String)} callback will be invoked.
	 *
	 * @deprecated In a future release, this class will become internal to the SDK. Use {@link com.onlinepayments.sdk.client.android.session.Session#preparePaymentRequest(PaymentRequest, Context, SessionEncryptionHelper.OnPaymentRequestPreparedListener)} to obtain an encrypted Prepared Payment Request.
     */
	@Deprecated
    public interface OnEncryptDataCompleteListener {
		/**
		 * Invoked when the task was successful and data is available.
		 *
		 * @param encryptedData the encrypted data. Send this to your server to create a payment.
		 */
        void onEncryptDataComplete(String encryptedData);
    }
}
