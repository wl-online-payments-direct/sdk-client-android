/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentProduct.productField

import java.io.Serializable

@Suppress("unused", "LongParameterList")
class ProductFieldDisplayHints internal constructor(
    val alwaysShow: Boolean?,
    val obfuscate: Boolean?,
    val displayOrder: Int,
    val label: String?,
    val placeholderLabel: String?,
    val mask: String?,
    val preferredInputType: PreferredInputType?,
    val tooltipLabel: String?,
    val formElementType: String?
) : Serializable {
    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -4396644758512959868L
    }
}
