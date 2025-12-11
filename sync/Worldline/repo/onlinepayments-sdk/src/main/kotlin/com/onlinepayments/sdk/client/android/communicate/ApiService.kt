/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.communicate

import com.onlinepayments.sdk.client.android.model.PaymentProductNetworkResponse
import com.onlinepayments.sdk.client.android.model.PublicKeyResponse
import com.onlinepayments.sdk.client.android.model.currencyconversion.CurrencyConversionRequest
import com.onlinepayments.sdk.client.android.model.currencyconversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsRequest
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct
import com.onlinepayments.sdk.client.android.model.surcharge.request.SurchargeCalculationRequest
import com.onlinepayments.sdk.client.android.model.surcharge.response.SurchargeCalculationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.util.Date

internal interface ApiService {
    @GET("{customerId}/products")
    suspend fun getBasicPaymentProducts(
        @Path("customerId") customerId: String,
        @QueryMap params: Map<String, String>,
        @Query("hide") hide: String = "fields",
        @Query("cacheBuster") cacheBuster: Long = Date().time
    ): BasicPaymentProducts

    @GET("{customerId}/products/{productId}")
    suspend fun getPaymentProduct(
        @Path("customerId") customerId: String,
        @Path("productId") productId: String,
        @QueryMap params: Map<String, String>?,
        @Query("cacheBuster") cacheBuster: Long = Date().time
    ): PaymentProduct

    @GET("{customerId}/products/{productId}/networks")
    suspend fun getPaymentProductNetworks(
        @Path("customerId") customerId: String,
        @Path("productId") productId: String,
        @QueryMap params: Map<String, String>?,
        @Query("cacheBuster") cacheBuster: Long = Date().time
    ): PaymentProductNetworkResponse

    @POST("{customerId}/services/getIINdetails")
    suspend fun getIinDetails(
        @Path("customerId") customerId: String,
        @Body request: IinDetailsRequest
    ): IinDetailsResponse

    @GET("{customerId}/crypto/publickey")
    suspend fun getPublicKey(
        @Path("customerId") customerId: String
    ): PublicKeyResponse

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
