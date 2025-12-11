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

import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsPaymentItem
import com.onlinepayments.sdk.client.android.model.paymentproduct.specificdata.PaymentProduct302SpecificData
import com.onlinepayments.sdk.client.android.model.paymentproduct.specificdata.PaymentProduct320SpecificData
import java.io.Serializable
import java.security.InvalidParameterException

/**
 * POJO which holds the BasicPaymentProduct properties.
 */
@Suppress("Unused", "LongParameterList")
open class BasicPaymentProduct internal constructor(
    private val id: String? = null,
    val paymentMethod: String? = null,
    val paymentProductGroup: String? = null,
    val allowsRecurring: Boolean? = null,
    val allowsTokenization: Boolean? = null,
    val usesRedirectionTo3rdParty: Boolean? = null,
    private val displayHintsList: MutableList<DisplayHintsPaymentItem> = mutableListOf(),
    private val accountsOnFile: MutableList<AccountOnFile> = mutableListOf(),
    val paymentProduct302SpecificData: PaymentProduct302SpecificData? = null,
    val paymentProduct320SpecificData: PaymentProduct320SpecificData? = null
) : BasicPaymentItem, Serializable {

    override fun getId(): String? {
        return id
    }

    override fun getAccountsOnFile(): MutableList<AccountOnFile> {
        return accountsOnFile
    }

    override fun getDisplayHintsList(): MutableList<DisplayHintsPaymentItem> {
        return displayHintsList
    }

    fun getAccountOnFileById(accountOnFileId: String?): AccountOnFile {
        return accountOnFileId?.let { id -> accountsOnFile.firstOrNull { it.id == id } }
            ?: throw InvalidParameterException("accountOnFileId may not be null")
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -8362704974696989741L
    }
}
