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
	/**
	 * @deprecated In a future release, this property will be removed since it is not returned from the API.
	 */
	@Deprecated
	@SerializedName("image")
	private String imageURL;
	/**
	 * @deprecated In a future release, this property will be removed since it is not returned from the API.
	 */
	@Deprecated
	private transient Drawable imageDrawable;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public Tooltip() {}

	public String getLabel(){
		return label;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since its value is not returned from the API.
	 */
	@Deprecated
	public String getImageURL(){
		return imageURL;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since Tooltip no longer contains an image.
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
	 *
	 * @deprecated In a future release, this getter will be removed since Tooltip no longer contains an image.
	 */
	@Deprecated
	public Drawable getImageDrawable() {
		return imageDrawable;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since Tooltip no longer contains an image.
	 */
	@Deprecated
	public void setImageDrawable(Drawable imageDrawable){
		this.imageDrawable = imageDrawable;
	}
}
