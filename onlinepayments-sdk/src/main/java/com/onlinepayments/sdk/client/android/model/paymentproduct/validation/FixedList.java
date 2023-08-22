/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO which holds the FixedList data.
 * Used for validation.
 */
public class FixedList implements Serializable {

	private static final long serialVersionUID = -7191166722186646029L;

	private List<String> allowedValues = new ArrayList<>();

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public FixedList() {}

	public List<String> getAllowedValues(){
		return allowedValues;
	}
}
