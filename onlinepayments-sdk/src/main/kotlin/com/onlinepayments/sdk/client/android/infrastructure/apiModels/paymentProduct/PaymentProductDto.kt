/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct

import com.google.gson.annotations.SerializedName

/**
 * DTO for PaymentProduct (extends BasicPaymentProductDto with fields).
 *
 * Note: Parent class properties are inherited and will be deserialized by Gson.
 * This class only adds the `fields` property specific to PaymentProduct.
 */
internal class PaymentProductDto : BasicPaymentProductDto() {
    @SerializedName("fields")
    lateinit var fields: List<PaymentProductFieldDto>
}
