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

import com.onlinepayments.sdk.client.android.formatter.StringFormatter
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsAccountOnFile
import java.io.Serializable

/**
 * Data class that represents an AccountOnFile object.
 */
data class AccountOnFile(
    val id: String,
    val paymentProductId: String,
    val displayHints: DisplayHintsAccountOnFile? = null,
    val attributes: MutableList<AccountOnFileAttribute> = mutableListOf(),
    private var label: String? = null
) : Serializable {

    fun getLabel(): String = label ?: determineLabel()

    private fun determineLabel(): String {
        label = displayHints?.labelTemplate?.firstOrNull()?.let { getMaskedValue(it.getKey()) }

        return label ?: ""
    }

    fun getMaskedValue(paymentProductFieldId: String?): String? {
        val mask =
            displayHints?.labelTemplate?.firstOrNull { it?.getKey() == paymentProductFieldId }?.mask
                ?: ""

        return getMaskedValue(paymentProductFieldId, mask)
    }

    fun getMaskedValue(paymentProductFieldId: String?, mask: String): String? {
        val value = attributes.firstOrNull { it.key == paymentProductFieldId }?.value ?: ""
        val relaxedMask = StringFormatter.relaxMask(mask)

        return StringFormatter.applyMask(relaxedMask, value)
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 4898075257024154390L
    }
}
