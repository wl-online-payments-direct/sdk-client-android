/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;


/**
 * POJO that represents an AccountOnFile object.
 */
public class AccountOnFileDisplay implements Serializable {

	private static final long serialVersionUID = -7793293988073972532L;

	private String attributeKey;
	private String mask;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public AccountOnFileDisplay(String attributeKey, String mask) {
		this.attributeKey = attributeKey;
		this.mask = mask;
	}

	public String getKey() {
		return attributeKey;
	}

	public String getMask() {
		return mask;
	}

}
