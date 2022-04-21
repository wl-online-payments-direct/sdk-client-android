package com.onlinepayments.sdk.client.android.model.paymentproduct;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Pojo that represents a Tooltip object
 * This class is filled by deserialising a JSON string from the GC gateway
 * Tooltips are used when showing tooltips payment product specific
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class Tooltip implements Serializable {

	private static final long serialVersionUID = -317203058533669043L;

	private String label;
	@SerializedName("image")
	private String imageURL;
	private transient Drawable imageDrawable;

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

	public Drawable getImageDrawable() {
		return imageDrawable;
	}

	public void setImageDrawable(Drawable imageDrawable){
		this.imageDrawable = imageDrawable;
	}
}
