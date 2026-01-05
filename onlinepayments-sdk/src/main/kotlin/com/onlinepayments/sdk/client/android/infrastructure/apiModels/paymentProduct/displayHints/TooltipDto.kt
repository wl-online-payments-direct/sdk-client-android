/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.displayHints

import java.io.Serializable

/**
 * Data class that represents a Tooltip object.
 * Tooltips are payment product specific and are used to show extra information about an input field.
 */
@ConsistentCopyVisibility
data class TooltipDto internal constructor(
    val label: String? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -317203058533669043L
    }
}
