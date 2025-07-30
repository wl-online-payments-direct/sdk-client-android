/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints

import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileDisplay
import java.io.Serializable

/**
 * Data class that represents an DisplayHintsAccountOnFile object.
 */
data class DisplayHintsAccountOnFile(
    val labelTemplate: MutableList<AccountOnFileDisplay?> = mutableListOf()
) : Serializable {

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 3446099654728722104L
    }
}
