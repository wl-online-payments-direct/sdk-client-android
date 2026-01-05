/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.surchargeCalculation

import com.onlinepayments.sdk.client.android.domain.AmountOfMoney
import com.onlinepayments.sdk.client.android.domain.card.CardSource

/**
 * Data class that contains the request for retrieving surcharge calculations.
 *
 * @param amountOfMoney contains the amount and currency code for which the Surcharge should be calculated
 * @param cardSource contains the card or token for which the Surcharge should be calculated
 */
internal class SurchargeCalculationRequest(
    val amountOfMoney: AmountOfMoney,
    val cardSource: CardSource
)
