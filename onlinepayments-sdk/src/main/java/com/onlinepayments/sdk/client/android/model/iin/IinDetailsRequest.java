/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.iin;

import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * POJO that contains the request for IIN lookup.
 */
public class IinDetailsRequest implements Serializable {

	private static final long serialVersionUID = 8401271765455867950L;

	@SerializedName("bin")
	private String ccPartial;

	private PaymentContext paymentContext;

	/**
	 @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public IinDetailsRequest(String ccPartial) {
		this.ccPartial = ccPartial;
	}

	/**
	 @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public IinDetailsRequest(String ccPartial, PaymentContext paymentContext) {
		this.ccPartial = ccPartial;
		this.paymentContext = paymentContext;
	}

	public String getCcPartial() {
		return ccPartial;
	}

	public PaymentContext getPaymentContext () {
		return paymentContext;
	}

}
