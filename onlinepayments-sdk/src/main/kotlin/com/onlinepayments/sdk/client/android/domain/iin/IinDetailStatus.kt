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

@Suppress("unused")
enum class IinDetailStatus {
    SUPPORTED,
    UNSUPPORTED,
    UNKNOWN,
    NOT_ENOUGH_DIGITS,
    EXISTING_BUT_NOT_ALLOWED
}

