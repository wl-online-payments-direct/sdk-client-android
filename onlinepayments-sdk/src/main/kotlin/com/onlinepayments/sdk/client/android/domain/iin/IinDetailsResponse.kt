/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.iin

import com.google.gson.annotations.SerializedName
import java.io.Serializable

@ConsistentCopyVisibility
data class IinDetailsResponse internal constructor(
    @SerializedName("paymentProductId")
    val paymentProductId: String? = null,
    @SerializedName("countryCode")
    val countryCode: String? = null,
    @SerializedName("isAllowedInContext")
    val isAllowedInContext: Boolean = false,
    @SerializedName("coBrands")
    val coBrands: MutableList<IinDetail?>? = null,
    @SerializedName("cardType")
    val cardType: CardType? = CardType.CREDIT
) : Serializable {
    var status: IinDetailStatus? = null
        internal set

    internal constructor(status: IinDetailStatus) : this() {
        this.status = status
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -4043745317792003304L
    }
}
