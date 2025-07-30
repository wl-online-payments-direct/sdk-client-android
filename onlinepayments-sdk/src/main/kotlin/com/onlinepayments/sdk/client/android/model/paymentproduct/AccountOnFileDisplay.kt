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
 * Data class that represents an AccountOnFile object.
 */
class AccountOnFileDisplay(
    val attributeKey: String?,
    val mask: String?
) : Serializable {

    fun getKey(): String? {
        return attributeKey
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -7793293988073972532L
    }
}
