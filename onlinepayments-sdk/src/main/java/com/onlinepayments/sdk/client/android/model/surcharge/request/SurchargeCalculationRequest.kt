package com.onlinepayments.sdk.client.android.model.surcharge.request

import com.onlinepayments.sdk.client.android.model.AmountOfMoney

/**
 * POJO that contains the request for retrieving surcharge calculations.
 *
 * @param amountOfMoney contains the amount and currency code for which the Surcharge should be calculated
 * @param cardSource contains the card or token for which the Surcharge should be calculated
 */
internal class SurchargeCalculationRequest(val amountOfMoney: AmountOfMoney, val cardSource: CardSource)