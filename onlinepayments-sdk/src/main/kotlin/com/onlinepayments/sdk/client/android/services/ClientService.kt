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

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.Constants
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.card.CardSource
import com.onlinepayments.sdk.client.android.domain.configuration.SessionData
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionRequest
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.domain.currencyConversion.Transaction
import com.onlinepayments.sdk.client.android.domain.exceptions.IllegalStateSdkException
import com.onlinepayments.sdk.client.android.domain.exceptions.ResponseException
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailStatus
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsRequest
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationRequest
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IApiClient
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.ICacheManager
import com.onlinepayments.sdk.client.android.services.interfaces.IClientService

internal class ClientService(
    private val apiClient: IApiClient,
    private val sessionData: SessionData,
    private val cacheManager: ICacheManager,
) : IClientService {
    @Volatile
    private var iinLookupPending = false

    override suspend fun getIinDetails(
        partialCreditCardNumber: String,
        paymentContext: PaymentContextWithAmount
    ): IinDetailsResponse {

        if (iinLookupPending) {
            throw IllegalStateSdkException("IIN lookup is already in progress")
        }

        val partialCCNumber =
            partialCreditCardNumber.take(Constants.MAX_CHARS_PAYMENT_PRODUCT_ID_LOOKUP)

        if (partialCCNumber.length < Constants.MIN_CHARS_PAYMENT_PRODUCT_ID_LOOKUP) {
            return IinDetailsResponse(IinDetailStatus.NOT_ENOUGH_DIGITS)
        }

        val cacheKey = "getIinDetails-$partialCCNumber"

        iinLookupPending = true

        return try {
            cacheManager.getOrFetch(cacheKey) {
                try {
                    val request = IinDetailsRequest(partialCCNumber, paymentContext)
                    val response = apiClient.getIinDetails(sessionData.customerId, request)

                    if (response.paymentProductId == null) {
                        IinDetailsResponse(IinDetailStatus.UNKNOWN)
                    } else {
                        val result = IinDetailsResponse(
                            response.paymentProductId,
                            response.countryCode, response.isAllowedInContext, response.coBrands, response.cardType
                        )

                        result.status =
                            if (response.isAllowedInContext) IinDetailStatus.SUPPORTED
                            else IinDetailStatus.EXISTING_BUT_NOT_ALLOWED

                        result
                    }
                } catch (e: ResponseException) {
                    if (e.httpStatusCode == Constants.NOT_FOUND_ERROR) {
                        IinDetailsResponse(IinDetailStatus.UNKNOWN)
                    } else {
                        throw e
                    }
                }
            }
        } finally {
            iinLookupPending = false
        }
    }

    override suspend fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        cardSource: CardSource,
    ): CurrencyConversionResponse {
        val cacheKey =
            listOfNotNull(
                "getCurrencyConversionQuote",
                amountOfMoney.amount.toString(),
                amountOfMoney.currencyCode,
                getCardNumberOrTokenSuffix(cardSource)
            ).joinToString("-")

        return cacheManager.getOrFetch(cacheKey) {
            val response = apiClient.getCurrencyConversionQuote(
                sessionData.customerId,
                CurrencyConversionRequest(cardSource, Transaction(amountOfMoney))
            )

            CurrencyConversionResponse(
                dccSessionId = response.dccSessionId,
                result = response.result,
                proposal = response.proposal
            )
        }
    }

    override suspend fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        cardSource: CardSource,
    ): SurchargeCalculationResponse {
        val cacheKey =
            listOfNotNull(
                "getSurchargeCalculation",
                amountOfMoney.amount.toString(),
                amountOfMoney.currencyCode,
                getCardNumberOrTokenSuffix(cardSource)
            ).joinToString("-")

        return cacheManager.getOrFetch(cacheKey) {
            apiClient.getSurchargeCalculation(
                sessionData.customerId,
                SurchargeCalculationRequest(amountOfMoney, cardSource)
            )
        }
    }

    private fun getCardNumberOrTokenSuffix(cardSource: CardSource): String? {
        return cardSource.card?.cardNumber ?: cardSource.token
    }
}


