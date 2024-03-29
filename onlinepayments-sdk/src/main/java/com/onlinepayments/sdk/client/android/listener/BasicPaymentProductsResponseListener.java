/*
 * Copyright 2023 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.listener;

import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts;

/**
 * Callback Interface that is invoked when a Basic Payment Products API request completes.
 */
public interface BasicPaymentProductsResponseListener extends GenericResponseListener<BasicPaymentProducts> { }
