/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model;

import java.security.InvalidParameterException;

/**
 * Contains all encrypted paymentrequest data needed for doing a payment.
 */
public class PreparedPaymentRequest {

	private String encryptedFields;
	private String encodedClientMetaInfo;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public PreparedPaymentRequest(String encryptedFields, String encodedClientMetaInfo) {

		if (encryptedFields == null) {
			throw new InvalidParameterException("Error creating PreparedPaymentRequest, encryptedFields may not be null");
		}
		if (encodedClientMetaInfo == null) {
			throw new InvalidParameterException("Error creating PreparedPaymentRequest, encodedClientMetaInfo may not be null");
		}

		this.encryptedFields = encryptedFields;
		this.encodedClientMetaInfo = encodedClientMetaInfo;
	}

	public String getEncodedClientMetaInfo() {
		return encodedClientMetaInfo;
	}

	public String getEncryptedFields() {
		return encryptedFields;
	}
}
