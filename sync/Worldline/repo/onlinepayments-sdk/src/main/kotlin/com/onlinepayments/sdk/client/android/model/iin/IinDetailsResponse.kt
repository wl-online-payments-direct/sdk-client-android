/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.iin

import java.io.Serializable

/**
 * Data class that contains the response for IIN lookup.
 */
data class IinDetailsResponse(
    var status: IinStatus? = null,
    val paymentProductId: String? = null,
    val countryCode: String? = null,
    val isAllowedInContext: Boolean = false,
    val coBrands: MutableList<IinDetail?>? = null,
    val cardType: CardType? = CardType.CREDIT
) : Serializable {

    constructor(status: IinStatus) : this(status, null, null, false, null, CardType.CREDIT)

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -4043745317792003304L
    }
}
