/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.currencyConversion

import com.onlinepayments.sdk.client.android.domain.card.CardSource

/**
 * Data class that contains the request for retrieving a currency conversion quote.
 *
 * @param cardSource contains the card or token for which the currency conversion rate should be returned
 * @param transaction contains the transaction for which the currency conversion rate should be returned
 */
data class CurrencyConversionRequest(
    val cardSource: CardSource,
    val transaction: Transaction
)

