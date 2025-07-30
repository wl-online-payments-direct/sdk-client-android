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
 * @param result the result type for this conversion
 * @param resultReason plain text explaining the result of the currency conversion request
 */
data class CurrencyConversionResult(
    val result: ConversionResultType,
    val resultReason: String?
)
