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
import com.onlinepayments.sdk.client.android.domain.exceptions.CommunicationException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsRequest
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProductNetworksResponse
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationRequest
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.publicKey.PublicKeyResponseDto

/**
 * API client interface for making API calls.
 */
internal interface IApiClient {
    /**
     * Retrieves basic payment products.
     *
     * @throws ResponseException if API returns error
     * @throws CommunicationException if network error occurs
     */
    suspend fun getBasicPaymentProducts(
        customerId: String,
        params: Map<String, String>
    ): BasicPaymentProductsDto

    /**
     * Retrieves detailed payment product information.
     *
     * @throws ResponseException if API returns error
     * @throws CommunicationException if network error occurs
     */
    suspend fun getPaymentProduct(
        customerId: String,
        productId: String,
        params: Map<String, String>?
    ): PaymentProductDto

    /**
     * Retrieves payment product networks.
     *
     * @throws ResponseException if API returns error
     * @throws CommunicationException if network error occurs
     */
    suspend fun getPaymentProductNetworks(
        customerId: String,
        productId: String,
        params: Map<String, String>?
    ): PaymentProductNetworksResponse

    /**
     * Performs IIN (Issuer Identification Number) lookup.
     *
     * @throws ResponseException if API returns error
     * @throws CommunicationException if network error occurs
     */
    suspend fun getIinDetails(
        customerId: String,
        request: IinDetailsRequest
    ): IinDetailsResponse

    /**
     * Retrieves public key for encryption.
     *
     * @throws ResponseException if API returns error
     * @throws CommunicationException if network error occurs
     */
    suspend fun getPublicKey(
        customerId: String
    ): PublicKeyResponseDto

    /**
     * Gets currency conversion quote.
     *
     * @throws ResponseException if API returns error
     * @throws CommunicationException if network error occurs
     */
    suspend fun getCurrencyConversionQuote(
        customerId: String,
        request: CurrencyConversionRequest
    ): CurrencyConversionResponse

    /**
     * Calculates surcharge for payment.
     *
     * @throws ResponseException if API returns error
     * @throws CommunicationException if network error occurs
     */
    suspend fun getSurchargeCalculation(
        customerId: String,
        request: SurchargeCalculationRequest
    ): SurchargeCalculationResponse
}
