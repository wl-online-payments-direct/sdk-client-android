/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.interfaces

import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto

internal interface IPaymentProductFactory {
    fun createPaymentProduct(dto: PaymentProductDto): PaymentProduct

    fun createBasicPaymentProduct(dto: BasicPaymentProductDto): BasicPaymentProduct

    fun createBasicPaymentProducts(dto: BasicPaymentProductsDto): BasicPaymentProducts
}
