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
 * POJO which holds the BasicPaymentProduct data and its PaymentProductFields.
 */
class PaymentProduct internal constructor(
    private var fields: MutableList<PaymentProductField> = mutableListOf()
) : BasicPaymentProduct(), PaymentItem, Serializable {

    private var hasBeenSorted = false

    override fun getPaymentProductFields(): List<PaymentProductField> {
        sortList()

        // return immutable list
        return fields.toList()
    }

    @Suppress("Unused")
    fun getPaymentProductFieldById(id: String?): PaymentProductField? {
        return fields.firstOrNull { it.id == id }
    }

    private fun sortList() {
        if (!hasBeenSorted) {
            fields.sortWith { field1, field2 ->
                when {
                    field1 === field2 -> 0
                    field1 == null -> -1
                    field2 == null -> 1
                    else -> {
                        val displayOrder1 = field1.displayHints.displayOrder ?: -1
                        val displayOrder2 = field2.displayHints.displayOrder ?: -1
                        displayOrder1.compareTo(displayOrder2)
                    }
                }
            }

            hasBeenSorted = true
        }
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -8362704974696989741L
    }
}
