/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.configuration

internal object Constants {
    // SDK version
    const val SDK_VERSION = "4.0.0"
    const val SDK_IDENTIFIER = "OnlinePaymentsAndroidClientSDK/v$SDK_VERSION"

    // SDK creator
    const val SDK_CREATOR = "OnlinePayments"

    // Time constant that should be used to determine if a call took too long to return
    const val ACCEPTABLE_WAIT_TIME_IN_MILLISECONDS = 10000

    // Apple Pay product ID
    const val PAYMENT_PRODUCT_ID_APPLEPAY = "302"

    // Google Pay product ID
    const val PAYMENT_PRODUCT_ID_GOOGLEPAY = "320"

    const val PAYMENT_PRODUCT_ID_AMEX = "2"

    // Google Pay constants
    const val GOOGLE_API_VERSION = 2

    private const val PAYMENT_PRODUCT_ID_MAESTRO = "117"
    private const val PAYMENT_PRODUCT_ID_INTERSOLVE = "5700"
    private const val PAYMENT_PRODUCT_ID_SODEXO_SPORT_CULTURE = "5772"
    private const val PAYMENT_PRODUCT_ID_VVV_GIFTCARD = "5784"

    @JvmField
    val UNAVAILABLE_PAYMENT_PRODUCT_IDS = listOf(
        PAYMENT_PRODUCT_ID_MAESTRO,
        PAYMENT_PRODUCT_ID_INTERSOLVE,
        PAYMENT_PRODUCT_ID_SODEXO_SPORT_CULTURE,
        PAYMENT_PRODUCT_ID_VVV_GIFTCARD
    )
}
