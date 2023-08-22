/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

/**
 * POJO that represents an DisplayHintsPaymentItem object.
 */
public class DisplayHintsPaymentItem implements Serializable{

	private static final long serialVersionUID = 5783120855027244241L;

	private Integer displayOrder;
	private String label;

	@SerializedName("logo")
	private String logoUrl;

	private transient Drawable logoDrawable;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public DisplayHintsPaymentItem() {}

	public Integer getDisplayOrder(){
		return displayOrder;
	}

	public String getLabel(){
		return label;
	}

	/**
	 * When passing a {@link com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentItem} via an {@link android.content.Intent}, the logo cannot be retrieved via this function.
	 * Instead you will need to retrieve the logo in your own app using the URL returned from {@link #getLogoUrl()}.
	 *
	 * @return the logo as a {@link Drawable}
	 */
	public Drawable getLogo(){
		return logoDrawable;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public void setLogo(Drawable logoDrawable){
		this.logoDrawable = logoDrawable;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
}
