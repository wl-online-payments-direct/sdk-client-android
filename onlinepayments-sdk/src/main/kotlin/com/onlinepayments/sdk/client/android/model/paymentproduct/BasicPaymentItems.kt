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
import java.security.InvalidParameterException

/**
 * Data class with convenience methods for getting BasicPaymentItem and AccountOnFile objects.
 */
data class BasicPaymentItems(
    val basicPaymentItems: MutableList<BasicPaymentItem> = mutableListOf(),
    private val _accountsOnFile: MutableList<AccountOnFile> = mutableListOf() // Private backing list
) : Serializable {

    private var hasBeenSorted = false
    private var _accountsOnFilePopulated = false // Track population

    @Suppress("Unused")
    val sortedBasicPaymentItems: List<BasicPaymentItem> // Read-only sorted list
        get() {
            sortList()
            return basicPaymentItems
        }

    @Suppress("Unused")
    val accountsOnFile: List<AccountOnFile> // Read-only list
        get() {
            populateAccountsOnFile()
            return _accountsOnFile
        }

    private fun sortList() {
        if (!hasBeenSorted) {
            basicPaymentItems.sortWith { product1, product2 ->
                when {
                    product1 == product2 -> 0
                    product1 == null -> -1
                    product2 == null -> 1
                    product1.getDisplayHintsList().isEmpty() -> -1 // Direct access, no need for get
                    product2.getDisplayHintsList().isEmpty() -> 1
                    else -> {
                        val displayOrder1 =
                            product1.getDisplayHintsList().firstOrNull()?.displayOrder ?: -1 // Safe access
                        val displayOrder2 =
                            product2.getDisplayHintsList().firstOrNull()?.displayOrder ?: -1 // Safe access
                        displayOrder1.compareTo(displayOrder2)
                    }
                }
            }

            hasBeenSorted = true
        }
    }

    @Suppress("Unused")
    fun getBasicPaymentItemById(basicPaymentItemId: String?): BasicPaymentItem? {
        return basicPaymentItemId?.let { id -> basicPaymentItems.firstOrNull { it.getId() == id } }
            ?: throw InvalidParameterException("basicPaymentItemId may not be null")
    }

    private fun populateAccountsOnFile() {
        if (!_accountsOnFilePopulated) {
            basicPaymentItems.forEach { product -> _accountsOnFile.addAll(product.getAccountsOnFile()) } // Direct access
            _accountsOnFilePopulated = true
        }
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 2481207529146031966L
    }
}