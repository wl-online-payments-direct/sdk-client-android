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
 * @param dccSessionId the identifier of the Dynamic Currency Conversion(DCC) session that has been created
 * @param result result of a requested currency conversion
 * @param proposal details of currency conversion to be proposed to the cardholder
 */
data class CurrencyConversionResponse(
    val dccSessionId: String,
    val result: CurrencyConversionResult,
    val proposal: DccProposal
)
