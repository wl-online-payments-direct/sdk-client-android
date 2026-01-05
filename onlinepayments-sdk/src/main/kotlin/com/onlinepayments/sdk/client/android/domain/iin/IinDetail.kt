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

/**
 * Data object that contains IinDetail.
 */
@ConsistentCopyVisibility
data class IinDetail internal constructor(
    @SerializedName("isAllowedInContext")
    val isAllowedInContext: Boolean?,
    @SerializedName("paymentProductId")
    val paymentProductId: String?
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 3918472059847261509L
    }
}
