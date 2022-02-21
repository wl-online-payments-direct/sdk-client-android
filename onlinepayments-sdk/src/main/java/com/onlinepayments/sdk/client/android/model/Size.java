package com.onlinepayments.sdk.client.android.model;

/**
 * Model for getting scaled images
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class Size {

	private Integer width;
	private Integer height;

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
