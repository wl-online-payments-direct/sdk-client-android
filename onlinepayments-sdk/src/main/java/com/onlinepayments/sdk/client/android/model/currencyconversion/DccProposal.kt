/*
 * Copyright 2024 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.currencyconversion

import com.onlinepayments.sdk.client.android.model.AmountOfMoney

/**
 * @param baseAmount the base amount for this currency conversion
 * @param targetAmount the target amount for this currency conversion
 * @param rate contains information about the conversion rate
 * @param disclaimerReceipt card scheme disclaimer to print within cardholder receipt
 * @param disclaimerDisplay card scheme disclaimer to present to the cardholder
 */
data class DccProposal(
    val baseAmount: AmountOfMoney,
    val targetAmount: AmountOfMoney,
    val rate: RateDetails,
    val disclaimerReceipt: String?,
    val disclaimerDisplay: String?
)
