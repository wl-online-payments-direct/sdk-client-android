/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.services

import android.content.Context
import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.configuration.SdkConfiguration
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.ICacheManager
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IPaymentProductFactory
import com.onlinepayments.sdk.client.android.infrastructure.utils.GooglePayUtil
import com.onlinepayments.sdk.client.android.infrastructure.utils.SupportedProductsUtil
import com.onlinepayments.sdk.client.android.services.interfaces.IPaymentProductService

internal class PaymentProductService(
    private val apiClient: IApiClient,
    private val context: Context,
    private val sessionData: SessionData,
    private val configuration: SdkConfiguration?,
    private val cacheManager: ICacheManager,
    private val paymentProductFactory: IPaymentProductFactory
) : IPaymentProductService {

    override suspend fun getBasicPaymentProducts(
        paymentContext: PaymentContext
    ): BasicPaymentProducts {
        val cacheKey = cacheManager.createCacheKeyFromContext(
            prefix = "getPaymentProducts",
            context = paymentContext
        )

        return cacheManager.getOrFetch(cacheKey) {
            val response = apiClient.getBasicPaymentProducts(
                sessionData.customerId,
                paymentContext.toMap()
            )

            val basicPaymentProducts = paymentProductFactory.createBasicPaymentProducts(response)

            filterUnsupportedProducts(basicPaymentProducts.paymentProducts)

            basicPaymentProducts
        }
    }

    override suspend fun getPaymentProduct(
        productId: Int,
        paymentContext: PaymentContext
    ): PaymentProduct {
        if (!SupportedProductsUtil.isSupportedInSdk(productId)) {
            throw404(productId)
        }

        val cacheKey = cacheManager.createCacheKeyFromContext(
            prefix = "getPaymentProduct-$productId",
            context = paymentContext
        )

        return cacheManager.getOrFetch(cacheKey) {
            val response = apiClient.getPaymentProduct(
                sessionData.customerId,
                productId.toString(),
                paymentContext.toMap()
            )

            val paymentProduct = paymentProductFactory.createPaymentProduct(response)
            if (!this.isProductSupported(paymentProduct)) {
                throw404(productId)
            }

            paymentProduct
        }
    }

    override suspend fun getPaymentProductNetworks(
        productId: Int,
        paymentContext: PaymentContext
    ): PaymentProductNetworksResponse {
        val cacheKey = cacheManager.createCacheKeyFromContext(
            prefix = "getPaymentProductNetworks-$productId",
            context = paymentContext
        )

        return cacheManager.getOrFetch(cacheKey) {
            val result = apiClient.getPaymentProductNetworks(
                sessionData.customerId,
                productId.toString(),
                paymentContext.toMap()
            )

            PaymentProductNetworksResponse(result.networks)
        }
    }

    private fun throw404(productId: Int) {
        throw ResponseException(
            httpStatusCode = 404,
            message = "Product with id $productId not found or not available.",
            apiError = SupportedProductsUtil.get404Error()
        )
    }

    private fun filterUnsupportedProducts(products: MutableList<BasicPaymentProduct>) {
        products.removeAll { product -> !isProductSupported(product) }
    }

    private fun isProductSupported(product: BasicPaymentProduct): Boolean {
        return SupportedProductsUtil.isSupportedInSdk(product.id!!)
                || (product.id == Constants.PAYMENT_PRODUCT_ID_GOOGLEPAY && GooglePayUtil.isGooglePayAllowed(
            this.context,
            this.configuration?.environmentIsProduction ?: false,
            product
        ))
    }
}
