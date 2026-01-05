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
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.util.Date

/**
 * Retrofit interface for making HTTP API calls.
 * Internal to infrastructure layer - not exposed to services.
 */
internal interface IGoPayApi {
    @GET("{customerId}/products")
    suspend fun getBasicPaymentProducts(
        @Path("customerId") customerId: String,
        @QueryMap params: Map<String, String>,
        @Query("hide") hide: String = "fields",
        @Query("cacheBuster") cacheBuster: Long = Date().time
    ): BasicPaymentProductsDto

    @GET("{customerId}/products/{productId}")
    suspend fun getPaymentProduct(
        @Path("customerId") customerId: String,
        @Path("productId") productId: String,
        @QueryMap params: Map<String, String>?,
        @Query("cacheBuster") cacheBuster: Long = Date().time
    ): PaymentProductDto

    @GET("{customerId}/products/{productId}/networks")
    suspend fun getPaymentProductNetworks(
        @Path("customerId") customerId: String,
        @Path("productId") productId: String,
        @QueryMap params: Map<String, String>?,
        @Query("cacheBuster") cacheBuster: Long = Date().time
    ): PaymentProductNetworksResponse

    @POST("{customerId}/services/getIINdetails")
    suspend fun getIinDetails(
        @Path("customerId") customerId: String,
        @Body request: IinDetailsRequest
    ): IinDetailsResponse

    @GET("{customerId}/crypto/publickey")
    suspend fun getPublicKey(
        @Path("customerId") customerId: String
    ): PublicKeyResponseDto

    @POST("{customerId}/services/dccrate")
    suspend fun getCurrencyConversionQuote(
        @Path("customerId") customerId: String,
        @Body request: CurrencyConversionRequest
    ): CurrencyConversionResponse

    @POST("{customerId}/services/surchargecalculation")
    suspend fun getSurchargeCalculation(
        @Path("customerId") customerId: String,
        @Body request: SurchargeCalculationRequest
    ): SurchargeCalculationResponse
}
