/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct.validation;

import java.io.Serializable;

/**
 * POJO which holds the Length data.
 * Used for validation.
 */
public class Length implements Serializable {

	private static final long serialVersionUID = -8127911803708372125L;

	private Integer minLength;
	private Integer maxLength;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public Length() {}

	public Integer getMinLength(){
		return minLength;
	}

	public Integer getMaxLength(){
		return maxLength;
	}
}
