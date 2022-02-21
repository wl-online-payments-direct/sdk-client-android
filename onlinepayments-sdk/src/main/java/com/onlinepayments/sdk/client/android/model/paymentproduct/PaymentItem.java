package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;
import java.util.List;

/**
 * Pojo that represents a paymentItem
 *
 * Copyright 2020 Global Collect Services B.V
 */
public interface PaymentItem extends BasicPaymentItem, Serializable {

    List<PaymentProductField> getPaymentProductFields();
}
