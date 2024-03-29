/*
 * Copyright 2024 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.model.currencyconversion

import com.onlinepayments.sdk.client.android.model.CardSource

/**
 * POJO that contains the request for retrieving a currency conversion quote.
 *
 * @param cardSource contains the card or token for which the currency conversion rate should be returned
 * @param transaction contains the transaction for which the currency conversion rate should be returned
 */
internal class CurrencyConversionRequest(
    val cardSource: CardSource,
    val transaction: Transaction
)
