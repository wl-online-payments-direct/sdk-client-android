/*
 * Copyright 2023 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.listener;

import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;

/**
 * Callback Interface that is invoked when a Payment Product API request completes.
 */
public interface PaymentProductResponseListener extends GenericResponseListener<PaymentProduct> {
}
