/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;
import java.util.List;

/**
 * POJO that represents a paymentItem.
 */
public interface PaymentItem extends BasicPaymentItem, Serializable {

    List<PaymentProductField> getPaymentProductFields();
}
