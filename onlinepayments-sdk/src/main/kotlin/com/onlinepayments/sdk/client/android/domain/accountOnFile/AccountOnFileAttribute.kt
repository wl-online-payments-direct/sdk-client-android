/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.accountOnFile

import java.io.Serializable

class AccountOnFileAttribute internal constructor(
    val key: String,
    val value: String,
    val status: Status
) : Serializable {
    enum class Status : Serializable {
        READ_ONLY,
        CAN_WRITE,
        MUST_WRITE;

        companion object {
            @Suppress("Unused")
            private const val serialVersionUID = 1L
        }
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 2619483756091877765L
    }
}
