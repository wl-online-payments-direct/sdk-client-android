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

	/**
	 * Enum containing all the possible input types for a {@link PaymentProductField}.
	 */
	public enum Type {
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

	/**
	 * @deprecated In a future release, this enum will be removed. Use {@link Type} instead.
	 */
	@Deprecated
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

	private Type type;

	/**
	 * @deprecated In a future release, this getter will be removed since it is not returned from the API.
	 */
	@Deprecated
	private List<ValueMap> valueMapping = new ArrayList<>();

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public FormElement() {}

	public Type getFormElementType(){
		return type;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since its object type has been changed to {@link Type}. Use {{@link #getFormElementType()}} instead.
	 */
	@Deprecated
	public ListType getType() {
		switch (type) {
			case DATE: return ListType.DATE;
			case BOOLEAN: return ListType.BOOLEAN;
			case LIST: return ListType.LIST;
			case TEXT: return ListType.TEXT;
			case CURRENCY: return ListType.CURRENCY;
			default: return null;
		}
	}

	/**
	 * @deprecated
	 * Do not use this method. This method is intended for a temporary internal fix, and will be removed when no longer required.
	 *
	 * @param type the new type for this FormElement
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since its value is not returned from the API.
	 */
	@Deprecated
	public List<ValueMap> getValueMapping(){
		return valueMapping;
	}
}
