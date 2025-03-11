/*
 * Copyright 2020 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.configuration

internal object Constants {
    // SDK version
    @JvmSynthetic
    const val SDK_IDENTIFIER = "OnlinePaymentsAndroidClientSDK/v3.1.0"

    // SDK creator
    @JvmSynthetic
    const val SDK_CREATOR = "OnlinePayments"

    // Available paths on the Online Payments Client API
    @JvmSynthetic
    const val OP_GATEWAY_RETRIEVE_PAYMENTPRODUCTS_PATH = "[cid]/products"
    @JvmSynthetic
    const val OP_GATEWAY_RETRIEVE_PAYMENTPRODUCT_PATH = "[cid]/products/[pid]"
    @JvmSynthetic
    const val OP_GATEWAY_RETRIEVE_PAYMENTPRODUCT_NETWORKS_PATH = "[cid]/products/[pid]/networks"
    @JvmSynthetic
    const val OP_GATEWAY_IIN_LOOKUP_PATH = "[cid]/services/getIINdetails"
    @JvmSynthetic
    const val OP_GATEWAY_PUBLIC_KEY_PATH = "[cid]/crypto/publickey"
    @JvmSynthetic
    const val OP_GATEWAY_CURRENCY_CONVERSION_QUOTE_PATH = "[cid]/services/dccrate"
    @JvmSynthetic
    const val OP_GATEWAY_SURCHARGE_CALCULATION_PATH = "[cid]/services/surchargecalculation"

    // Time constant that should be used to determine if a call took too long to return
    const val ACCEPTABLE_WAIT_TIME_IN_MILISECONDS = 10000

    // Apple Pay product ID
    @JvmSynthetic
    const val PAYMENTPRODUCTID_APPLEPAY = "302"

    // Google Pay product ID
    const val PAYMENTPRODUCTID_GOOGLEPAY = "320"

    // Google Pay constants
    const val GOOGLE_API_VERSION = 2

    // HTTP success code
    @JvmSynthetic
    const val HTTP_SUCCESS = 200
}
