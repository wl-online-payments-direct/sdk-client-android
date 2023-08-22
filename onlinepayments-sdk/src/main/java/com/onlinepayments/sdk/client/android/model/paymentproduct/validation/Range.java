/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct.validation;

import java.io.Serializable;

/**
 * POJO which holds the Range data.
 * Used for validation.
 */
public class Range implements Serializable {

	private static final long serialVersionUID = 4659640500627126711L;

	private Integer minValue;
	private Integer maxValue;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public Range() {}

	public Integer getMinValue(){
		return minValue;
	}

	public Integer getMaxValue(){
		return maxValue;
	}
}
