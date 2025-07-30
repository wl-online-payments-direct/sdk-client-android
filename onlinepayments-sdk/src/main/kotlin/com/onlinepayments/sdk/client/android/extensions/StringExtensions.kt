/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.extensions

internal fun StringBuilder.appendIf(predicate: (StringBuilder) -> Boolean, text: String) : StringBuilder {
    return if (predicate(this)) {
        append(text)
    } else this
}
