/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model;

/**
 * POJO for getting scaled images.
 */
public class Size {

	private Integer width;
	private Integer height;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public Size(Integer width, Integer height){
		this.width = width;
		this.height = height;
	}

	public Integer getWidth(){
		return width;
	}

	public Integer getHeight(){
		return height;
	}
}
