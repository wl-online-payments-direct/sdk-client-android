/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.model.currencyconversion

/**
 * @param exchangeRate expressed as a percentage, applied to convert the original amount into the resulting amount without charge
 * @param invertedExchangeRate exchange rate, expressed as a percentage, applied to convert the resulting amount into the original amount
 * @param markUpRate the markup is the percentage added to the exchange rate by a provider when they sell you currency
 * @param quotationDateTime date and time at which the exchange rate has been quoted
 * @param source indicates the exchange rate source name. The rate source is supplied for receipt printing purposes and to meet regulatory requirements where applicable
 */
data class RateDetails(
    val exchangeRate: Double,
    val invertedExchangeRate: Double,
    val markUpRate: Double,
    val quotationDateTime: String,
    val source: String
)
