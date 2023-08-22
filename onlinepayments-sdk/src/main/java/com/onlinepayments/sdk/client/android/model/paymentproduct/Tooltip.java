/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * POJO that represents a Tooltip object.
 * Tooltips are payment product specific and are used to show extra information about an input field.
 */
public class Tooltip implements Serializable {

	private static final long serialVersionUID = -317203058533669043L;

	private String label;
	@SerializedName("image")
	private String imageURL;
	private transient Drawable imageDrawable;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public Tooltip() {}

	public String getLabel(){
		return label;
	}

	public String getImageURL(){
		return imageURL;
	}

	/**
	 * @deprecated use {@link #getImageDrawable()} instead.
	 */
	@Deprecated
	public Drawable getImage(){
		return imageDrawable;
	}

	/**
	 * When passing a {@link PaymentItem} via an {@link android.content.Intent}, the tooltip image cannot be retrieved via this function.
	 * Instead you will need to retrieve the tooltip image in your own app using the URL returned from {@link #getImageURL()}.
	 *
	 * @return the tooltip image as a {@link Drawable}
	 */
	public Drawable getImageDrawable() {
		return imageDrawable;
	}

	public void setImageDrawable(Drawable imageDrawable){
		this.imageDrawable = imageDrawable;
	}
}
