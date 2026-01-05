/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.http

import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionRequest
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsRequest
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationRequest
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.publicKey.PublicKeyResponseDto
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IGoPayApi
import com.onlinepayments.sdk.client.android.infrastructure.utils.ApiCallExecutor
import java.util.Date

/**
 * Implementation of IApiClient that handles HTTP communication and error translation.
 * This class is the infrastructure layer boundary - it wraps all API calls with
 * ApiCallExecutor to translate HTTP exceptions into domain exceptions.
 *
 * @param api The Retrofit-generated API interface for making HTTP calls
 */
internal class ApiClient(
    private val api: IGoPayApi
) : IApiClient {

    override suspend fun getBasicPaymentProducts(
        customerId: String,
        params: Map<String, String>
    ): BasicPaymentProductsDto {
        return ApiCallExecutor.callApi {
            api.getBasicPaymentProducts(
                customerId = customerId,
                params = params,
                hide = "fields",
                cacheBuster = Date().time
            )
        }
    }

    override suspend fun getPaymentProduct(
        customerId: String,
        productId: String,
        params: Map<String, String>?
    ): PaymentProductDto {
        return ApiCallExecutor.callApi {
            api.getPaymentProduct(
                customerId = customerId,
                productId = productId,
                params = params,
                cacheBuster = Date().time
            )
        }
    }

    override suspend fun getPaymentProductNetworks(
        customerId: String,
        productId: String,
        params: Map<String, String>?
    ): PaymentProductNetworksResponse {
        return ApiCallExecutor.callApi {
            api.getPaymentProductNetworks(
                customerId = customerId,
                productId = productId,
                params = params,
                cacheBuster = Date().time
            )
        }
    }

    override suspend fun getIinDetails(
        customerId: String,
        request: IinDetailsRequest
    ): IinDetailsResponse {
        return ApiCallExecutor.callApi {
            api.getIinDetails(
                customerId = customerId,
                request = request
            )
        }
    }

    override suspend fun getPublicKey(
        customerId: String
    ): PublicKeyResponseDto {
        return ApiCallExecutor.callApi {
            api.getPublicKey(customerId = customerId)
        }
    }

    override suspend fun getCurrencyConversionQuote(
        customerId: String,
        request: CurrencyConversionRequest
    ): CurrencyConversionResponse {
        return ApiCallExecutor.callApi {
            api.getCurrencyConversionQuote(
                customerId = customerId,
                request = request
            )
        }
    }

    override suspend fun getSurchargeCalculation(
        customerId: String,
        request: SurchargeCalculationRequest
    ): SurchargeCalculationResponse {
        return ApiCallExecutor.callApi {
            api.getSurchargeCalculation(
                customerId = customerId,
                request = request
            )
        }
    }
}
