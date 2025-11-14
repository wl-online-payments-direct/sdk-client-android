/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct

import java.io.Serializable

/**
 * Data class that represents a Tooltip object.
 * Tooltips are payment product specific and are used to show extra information about an input field.
 */
data class Tooltip(
    val label: String? = null
) : Serializable {

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -317203058533669043L
    }
}
