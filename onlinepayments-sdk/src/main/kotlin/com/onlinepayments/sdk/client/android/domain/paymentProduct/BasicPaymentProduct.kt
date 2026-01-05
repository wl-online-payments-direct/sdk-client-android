/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.paymentProduct

import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile
import com.onlinepayments.sdk.client.android.domain.paymentProduct.specificData.PaymentProduct302SpecificData
import com.onlinepayments.sdk.client.android.domain.paymentProduct.specificData.PaymentProduct320SpecificData
import java.io.Serializable

@Suppress("LongParameterList")
open class BasicPaymentProduct internal constructor(
    val id: Int? = null,
    val paymentMethod: String? = null,
    val paymentProductGroup: String? = null,
    val allowsRecurring: Boolean? = null,
    val allowsTokenization: Boolean? = null,
    val usesRedirectionTo3rdParty: Boolean? = null,
    val paymentProduct302SpecificData: PaymentProduct302SpecificData? = null,
    val paymentProduct320SpecificData: PaymentProduct320SpecificData? = null,
    val logo: String? = null,
    val label: String? = null,
    val displayOrder: Number? = null,
    val accountsOnFile: List<AccountOnFile>,
) : Serializable {

    fun getAccountOnFile(id: String): AccountOnFile? {
        return accountsOnFile.firstOrNull { it.id == id }
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = -8362704974696989741L
    }
}
