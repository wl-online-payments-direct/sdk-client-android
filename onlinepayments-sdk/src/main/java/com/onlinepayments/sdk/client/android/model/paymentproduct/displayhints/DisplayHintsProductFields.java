package com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints;

import java.io.Serializable;

import com.onlinepayments.sdk.client.android.model.paymentproduct.FormElement;
import com.onlinepayments.sdk.client.android.model.paymentproduct.Tooltip;
import com.google.gson.annotations.SerializedName;

/**
 * POJO that represents an DisplayHintsProductFields object
 * This class is filled by deserialising a JSON string from the GC gateway
 * Contains information for payment product fields
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class DisplayHintsProductFields implements Serializable {

	private static final long serialVersionUID = -4396644758512959868L;

	/**
	 * Enum containing all the possible input types
	 *
	 */
	public enum PreferredInputType {
		@SerializedName("IntegerKeyboard")
		INTEGER_KEYBOARD,

		@SerializedName("StringKeyboard")
		STRING_KEYBOARD,

		@SerializedName("PhoneNumberKeyboard")
		PHONE_NUMBER_KEYBOARD,

		@SerializedName("EmailAddressKeyboard")
		EMAIL_ADDRESS_KEYBOARD,

		@SerializedName("DateKeyboard")
		DATE_PICKER
    }

	private Boolean alwaysShow;
	private Boolean obfuscate;
	private Integer displayOrder;
	private String label;
	private String placeholderLabel;
	private String link;
	private String mask;
	private PreferredInputType preferredInputType;
	private Tooltip tooltip;
	private FormElement formElement;


	/** Getters **/
	public Tooltip getTooltip(){
		return tooltip;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public String getLabel() {
		return label;
	}

	public String getPlaceholderLabel() {
		return placeholderLabel;
	}

	public String getLink() {
		return link;
	}

	public String getMask() {
		return mask;
	}

	/**
	 * @deprecated
	 * 	 * Do not use this method. This method is intented for a temporary internal fix, and will be removed when no longer required.
	 *
	 * @param mask the new mask for this FormElement
	 */
	public void setMask(String mask) { this.mask = mask; }

	public Boolean getAlwaysShow() {
		return alwaysShow;
	}

	public Boolean isObfuscate() {
		return obfuscate;
	}

	public PreferredInputType getPreferredInputType(){
		return preferredInputType;
	}

	public FormElement getFormElement() {
		return formElement;
	}

	public void setFormElement(FormElement formElement) {
		this.formElement = formElement;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}




}
