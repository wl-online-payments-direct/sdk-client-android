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
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField
import com.onlinepayments.sdk.client.android.domain.paymentProduct.specificData.PaymentProduct302SpecificData
import com.onlinepayments.sdk.client.android.domain.paymentProduct.specificData.PaymentProduct320SpecificData
import java.io.Serializable

@Suppress("LongParameterList")
class PaymentProduct internal constructor(
    id: Int? = null,
    paymentMethod: String? = null,
    paymentProductGroup: String? = null,
    allowsRecurring: Boolean? = null,
    allowsTokenization: Boolean? = null,
    usesRedirectionTo3rdParty: Boolean? = null,
    paymentProduct302SpecificData: PaymentProduct302SpecificData? = null,
    paymentProduct320SpecificData: PaymentProduct320SpecificData? = null,
    logo: String? = null,
    label: String? = null,
    displayOrder: Number? = null,
    accountsOnFile: List<AccountOnFile>,
    val fields: List<PaymentProductField> = listOf()
) : BasicPaymentProduct(
    id = id,
    paymentMethod = paymentMethod,
    paymentProductGroup = paymentProductGroup,
    allowsRecurring = allowsRecurring,
    allowsTokenization = allowsTokenization,
    usesRedirectionTo3rdParty = usesRedirectionTo3rdParty,
    paymentProduct302SpecificData = paymentProduct302SpecificData,
    paymentProduct320SpecificData = paymentProduct320SpecificData,
    logo = logo,
    label = label,
    displayOrder = displayOrder,
    accountsOnFile = accountsOnFile
), Serializable {

//    val fields: List<PaymentProductField>
//        get() = _fields.sortedBy { it.displayHints.displayOrder }

    val requiredFields: List<PaymentProductField>
        get() = fields.filter { it.isRequired }

    fun getField(id: String): PaymentProductField? {
        return fields.firstOrNull { it.id == id }
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 2619483756091827465L
    }
}
