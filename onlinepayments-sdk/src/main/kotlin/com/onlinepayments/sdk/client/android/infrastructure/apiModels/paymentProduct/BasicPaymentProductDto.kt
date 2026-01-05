/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct

import com.google.gson.annotations.SerializedName
import com.onlinepayments.sdk.client.android.domain.paymentProduct.specificData.PaymentProduct302SpecificData
import com.onlinepayments.sdk.client.android.domain.paymentProduct.specificData.PaymentProduct320SpecificData
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.accountOnFile.AccountOnFileDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.displayHints.PaymentProductDisplayHintsDto

/**
 * DTO for BasicPaymentProduct from JSON.
 */
internal open class BasicPaymentProductDto {
    @SerializedName("id")
    var id: Int? = null

    @SerializedName("paymentMethod")
    var paymentMethod: String? = null

    @SerializedName("paymentProductGroup")
    var paymentProductGroup: String? = null

    @SerializedName("allowsRecurring")
    var allowsRecurring: Boolean? = null

    @SerializedName("allowsTokenization")
    var allowsTokenization: Boolean? = null

    @SerializedName("usesRedirectionTo3rdParty")
    var usesRedirectionTo3rdParty: Boolean? = null

    @SerializedName("displayHints")
    var displayHints: PaymentProductDisplayHintsDto? = null

    @SerializedName("accountsOnFile")
    var accountsOnFile: List<AccountOnFileDto>? = null

    @SerializedName("paymentProduct302SpecificData")
    var paymentProduct302SpecificData: PaymentProduct302SpecificData? = null

    @SerializedName("paymentProduct320SpecificData")
    var paymentProduct320SpecificData: PaymentProduct320SpecificData? = null
}
