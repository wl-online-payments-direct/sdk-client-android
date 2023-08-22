/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileDisplay;

/**
 * POJO that represents an DisplayHintsAccountOnFile object.
 */
public class DisplayHintsAccountOnFile implements Serializable {

	private static final long serialVersionUID = 3446099654728722104L;

	private List<AccountOnFileDisplay> labelTemplate = new ArrayList<>();

	public List<AccountOnFileDisplay> getLabelTemplate(){
		return labelTemplate;
	}

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public DisplayHintsAccountOnFile() {}
}
