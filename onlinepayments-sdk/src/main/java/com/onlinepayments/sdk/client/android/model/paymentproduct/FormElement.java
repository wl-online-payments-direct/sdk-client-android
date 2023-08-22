/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * POJO that represents a Formelement object.
 * The FormElement is used for determining its list type (text, list, currency, date or boolean).
 * In case of a list, it also has values inside the valueMapping.
 */
public class FormElement implements Serializable {

	private static final long serialVersionUID = 7081218270681792356L;


	public enum ListType {
		@SerializedName("text")
		TEXT,

		@SerializedName("list")
		LIST,

		@SerializedName("currency")
		CURRENCY,

		@SerializedName("date")
		DATE,

		@SerializedName("boolean")
		BOOLEAN,
		;
	}

	private ListType type;
	private List<ValueMap> valueMapping = new ArrayList<>();

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public FormElement() {}

	public ListType getType(){
		return type;
	}

	/**
	 * @deprecated
	 * Do not use this method. This method is intended for a temporary internal fix, and will be removed when no longer required.
	 *
	 * @param type the new type for this FormElement
	 */
	public void setType(ListType type) {
		this.type = type;
	}

	public List<ValueMap> getValueMapping(){
		return valueMapping;
	}
}
