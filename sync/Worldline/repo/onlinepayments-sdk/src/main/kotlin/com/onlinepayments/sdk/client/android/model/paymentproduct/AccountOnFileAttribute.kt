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

data class AccountOnFileAttribute(
    val key: String,
    val value: String,
    val status: Status
) : Serializable {
    /**
     * Enum containing all the possible AccountOnFileAttribute statuses
     */
    @Suppress("Unused")
    enum class Status(val isEditingAllowed: Boolean) {
        READ_ONLY(false),
        CAN_WRITE(true),
        MUST_WRITE(true)
    }

    fun isEditingAllowed(): Boolean {
        return status.isEditingAllowed
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -31120L
    }
}
