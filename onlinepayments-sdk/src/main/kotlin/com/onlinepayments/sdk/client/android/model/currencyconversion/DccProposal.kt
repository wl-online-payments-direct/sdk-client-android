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
