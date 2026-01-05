/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.services.interfaces

import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse

internal interface IPaymentProductService {
    suspend fun getBasicPaymentProducts(
        paymentContext: PaymentContext
    ): BasicPaymentProducts

    suspend fun getPaymentProduct(
        productId: Int,
        paymentContext: PaymentContext
    ): PaymentProduct

    suspend fun getPaymentProductNetworks(
        productId: Int,
        paymentContext: PaymentContext
    ): PaymentProductNetworksResponse
}
