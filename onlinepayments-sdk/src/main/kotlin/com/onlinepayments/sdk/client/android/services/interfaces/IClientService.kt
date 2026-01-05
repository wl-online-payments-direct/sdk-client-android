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

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.card.CardSource
import com.onlinepayments.sdk.client.android.domain.currencyConversion.CurrencyConversionResponse
import com.onlinepayments.sdk.client.android.domain.iin.IinDetailsResponse
import com.onlinepayments.sdk.client.android.domain.surchargeCalculation.SurchargeCalculationResponse

internal interface IClientService {
    suspend fun getIinDetails(
        partialCreditCardNumber: String,
        paymentContext: PaymentContextWithAmount
    ): IinDetailsResponse

    suspend fun getCurrencyConversionQuote(
        amountOfMoney: AmountOfMoney,
        cardSource: CardSource,
    ): CurrencyConversionResponse

    suspend fun getSurchargeCalculation(
        amountOfMoney: AmountOfMoney,
        cardSource: CardSource,
    ): SurchargeCalculationResponse
}
