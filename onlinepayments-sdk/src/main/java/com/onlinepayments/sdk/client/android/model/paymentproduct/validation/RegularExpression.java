/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct.validation;

import java.io.Serializable;

/**
 * POJO which holds the RegularExpression data.
 * Used for validation.
 */
public class RegularExpression implements Serializable {

	private static final long serialVersionUID = -1242536946684504857L;

	private String regularExpression;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public RegularExpression() {}

	public String getRegularExpression(){
		return regularExpression;
	}
}
