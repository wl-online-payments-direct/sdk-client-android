@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.extensions

@JvmSynthetic
internal fun StringBuilder.appendIf(predicate: (StringBuilder) -> Boolean, text: String) : StringBuilder {
    return if (predicate(this)) {
        append(text)
    } else this
}
