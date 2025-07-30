/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct

import java.io.Serializable

/**
 * POJO that represents a paymentItem.
 */
interface PaymentItem : BasicPaymentItem, Serializable {
    fun getPaymentProductFields(): List<PaymentProductField>
}
