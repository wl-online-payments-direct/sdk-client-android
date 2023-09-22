package com.onlinepayments.sdk.client.android.model.surcharge.response

/**
 * @param surcharges list of surcharge calculations matching the bin and (optional) paymentProductId
 */
data class SurchargeCalculationResponse internal constructor(val surcharges: List<Surcharge>)