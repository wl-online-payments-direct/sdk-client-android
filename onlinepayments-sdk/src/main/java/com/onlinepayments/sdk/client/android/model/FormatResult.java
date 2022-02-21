package com.onlinepayments.sdk.client.android.model;

/**
 * Result after a masking is apploed on a field
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class FormatResult {

	private String formattedResult;
	private Integer cursorIndex;

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
