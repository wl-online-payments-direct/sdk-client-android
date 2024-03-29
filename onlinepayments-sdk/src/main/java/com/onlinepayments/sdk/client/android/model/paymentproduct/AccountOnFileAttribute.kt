package com.onlinepayments.sdk.client.android.model.paymentproduct

import java.io.Serializable

data class AccountOnFileAttribute internal constructor(
    val key: String,
    val value: String,
    val status: Status
) : Serializable {
    companion object {
        private const val serialVersionUID = -31120L
    }

    /**
     * Enum containing all the possible AccountOnFileAttribute statuses
     */
    enum class Status(val isEditingAllowed: Boolean) {
        READ_ONLY(false),
        CAN_WRITE(true),
        MUST_WRITE(true)
    }

    fun isEditingAllowed(): Boolean {
        return status.isEditingAllowed
    }
}
