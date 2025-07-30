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

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.security.InvalidParameterException

/**
 * Data class with convenience methods for getting PaymentProduct and AccountOnFile objects.
 */
data class BasicPaymentProducts(
    @SerializedName("paymentProducts")
    private val basicPaymentProducts: MutableList<BasicPaymentProduct> = mutableListOf(),
    private val accountsOnFile: MutableList<AccountOnFile> = mutableListOf()
) : Serializable {

    private var hasBeenSorted = false

    /**
     * Gets all basicPaymentProducts.
     *
     * @return a sorted list of basicPaymentProducts
     */
    fun getBasicPaymentProducts(): MutableList<BasicPaymentProduct> {
        sortList()
        return basicPaymentProducts
    }

    private fun sortList() {
        if (!hasBeenSorted) {
            basicPaymentProducts.sortWith { product1, product2 ->
                when {
                    product1 == product2 -> 0
                    product1 == null -> -1
                    product2 == null -> 1
                    product1.getDisplayHintsList().isEmpty() -> -1
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

    /**
     * Gets all AccountsOnFile for all BasicPaymentProducts.
     *
     * @return a list of all AccountsOnFile
     */
    fun getAccountsOnFile(): MutableList<AccountOnFile> {
        if (accountsOnFile.isEmpty()) {
            for (product in getBasicPaymentProducts()) {
                accountsOnFile.addAll(product.getAccountsOnFile())
            }
        }

        return accountsOnFile
    }

    /**
     * Returns a list of basicPaymentItems instead of basicPaymentProducts.
     *
     * @return list of basicPaymentItems
     */
    fun getPaymentProductsAsItems(): MutableList<BasicPaymentItem> {
        val basicPaymentItems: MutableList<BasicPaymentItem> = ArrayList<BasicPaymentItem>()
        for (paymentProduct in getBasicPaymentProducts()) {
            basicPaymentItems.add(paymentProduct)
        }

        return basicPaymentItems
    }

    /**
     * Gets a [BasicPaymentProduct] by its id.
     *
     * @param basicPaymentProductId the id of the [BasicPaymentProduct] that should be retrieved
     *
     * @return the retrieved [BasicPaymentProduct], or null if not found
     */
    @Suppress("Unused")
    fun getBasicPaymentProductById(basicPaymentProductId: String): BasicPaymentProduct? {
        return basicPaymentProductId.let { id -> basicPaymentProducts.firstOrNull { it.getId() == id } }
            ?: throw InvalidParameterException("basicPaymentProductId may not be null")
    }


    /**
     * Gets a [BasicPaymentProduct] by its AccountOnFileId.
     *
     * @param accountOnFileId the accountOnFileId for which the belonging [BasicPaymentProduct] should be retrieved
     *
     * @return the retrieved [BasicPaymentProduct], or null if not found
     */
    @Suppress("Unused")
    fun getBasicPaymentProductByAccountOnFileId(accountOnFileId: String): BasicPaymentProduct? {
        return basicPaymentProducts.firstOrNull { product ->
            product.getAccountsOnFile().any { it.id == accountOnFileId }
        }
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = 6385568686033699522L
    }
}
