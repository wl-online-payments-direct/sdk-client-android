/*
 * Copyright 2023 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.listener;

import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems;

/**
 * Callback Interface that is invoked when a Basic Payment Items API request completes.
 */
public interface BasicPaymentItemsResponseListener extends GenericResponseListener<BasicPaymentItems> { }
