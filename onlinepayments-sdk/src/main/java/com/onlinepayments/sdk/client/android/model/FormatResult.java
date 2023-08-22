/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model;

/**
 * Result after a masking is applied on a field.
 */
public class FormatResult {

	private String formattedResult;
	private Integer cursorIndex;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public FormatResult(String formattedResult, Integer cursorIndex) {
		this.formattedResult = formattedResult;
		this.cursorIndex = cursorIndex;
	}

	public Integer getCursorIndex() {
		return cursorIndex;
	}

	public String getFormattedResult() {
		return formattedResult;
	}
}
