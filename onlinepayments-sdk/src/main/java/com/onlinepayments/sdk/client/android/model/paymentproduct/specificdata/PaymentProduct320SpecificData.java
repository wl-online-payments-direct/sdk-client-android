/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct.specificdata;

import java.io.Serializable;
import java.util.List;

/**
 * POJO which holds the payment product 320 specific properties.
 */
public class PaymentProduct320SpecificData implements Serializable {

	private static final long serialVersionUID = 8538500042642795722L;

	private String gateway;
	private List<String> networks;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public PaymentProduct320SpecificData() {}

	public String getGateway() {
		return gateway;
	}

	public List<String> getNetworks() {
		return networks;
	}
}
