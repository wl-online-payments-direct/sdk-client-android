/*
 * Copyright 2024 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.currencyconversion

/**
 * @param result the result type for this conversion
 * @param resultReason plain text explaining the result of the currency conversion request
 */
data class CurrencyConversionResult(
    val result: ConversionResultType,
    val resultReason: String?
)
