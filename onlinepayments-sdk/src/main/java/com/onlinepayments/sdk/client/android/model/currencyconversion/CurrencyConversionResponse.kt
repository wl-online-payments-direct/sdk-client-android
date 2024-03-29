/*
 * Copyright 2024 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.currencyconversion

/**
 * @param dccSessionId the identifier of the Dynamic Currency Conversion(DCC) session that has been created
 * @param result result of a requested currency conversion
 * @param proposal details of currency conversion to be proposed to the cardholder
 */
data class CurrencyConversionResponse internal constructor(
    val dccSessionId: String,
    val result: CurrencyConversionResult,
    val proposal: DccProposal
)
