/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;
import java.util.List;

/**
 * POJO which holds the ValueMap data and it's PaymentProductFields.
 * If the {@link FormElement} is a list, ValueMap is used to display a value and its displayElements.
 */
public class ValueMap implements Serializable{

	private static final long serialVersionUID = -8334806247597370688L;


	private String value;
	@Deprecated
	private String displayName;
	private List<PaymentProductFieldDisplayElement> displayElements;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public ValueMap() {}

	public String getValue() {
		return value;
	}

	/**
	 * Returns the displayName that can be used in the UI.
	 *
	 * @deprecated Use {@link #getDisplayElements()} instead. Where displayName is expected you can get it from the displayElements List with id "displayName".
	 */
	@Deprecated
	public String getDisplayName() {
		return displayName;
	}

	public List<PaymentProductFieldDisplayElement> getDisplayElements() {
		return displayElements;
	}

}
