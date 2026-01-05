/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.factories

import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile
import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFileAttribute
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.DataRestrictions
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.ProductFieldDisplayHints
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.accountOnFile.AccountOnFileAttributeDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.accountOnFile.AccountOnFileDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.BasicPaymentProductsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.DataRestrictionsDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.PaymentProductFieldDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.paymentProduct.displayHints.ProductFieldDisplayHintsDto
import com.onlinepayments.sdk.client.android.infrastructure.interfaces.IPaymentProductFactory
import com.onlinepayments.sdk.client.android.infrastructure.utils.StringFormatter

internal class PaymentProductFactory : IPaymentProductFactory {
    override fun createBasicPaymentProducts(dto: BasicPaymentProductsDto): BasicPaymentProducts {
        return BasicPaymentProducts(
            paymentProducts = dto.paymentProducts?.map { createBasicPaymentProduct(it) }
                ?.toMutableList() ?: mutableListOf(),
            accountsOnFile = dto.paymentProducts
                ?.flatMap { it.accountsOnFile ?: emptyList() }
                ?.map { createAccountOnFile(it) }
                ?.distinctBy { it.id }
                ?: emptyList())
    }

    override fun createBasicPaymentProduct(dto: BasicPaymentProductDto): BasicPaymentProduct {
        return BasicPaymentProduct(
            id = dto.id,
            paymentMethod = dto.paymentMethod,
            paymentProductGroup = dto.paymentProductGroup,
            allowsRecurring = dto.allowsRecurring ?: false,
            allowsTokenization = dto.allowsTokenization ?: false,
            usesRedirectionTo3rdParty = dto.usesRedirectionTo3rdParty ?: false,
            paymentProduct302SpecificData = dto.paymentProduct302SpecificData,
            paymentProduct320SpecificData = dto.paymentProduct320SpecificData,
            logo = dto.displayHints?.logo,
            label = dto.displayHints?.label,
            displayOrder = dto.displayHints?.displayOrder,
            accountsOnFile = dto.accountsOnFile?.map { createAccountOnFile(it) } ?: emptyList(),
        )
    }

    override fun createPaymentProduct(dto: PaymentProductDto): PaymentProduct {
        return PaymentProduct(
            id = dto.id,
            paymentMethod = dto.paymentMethod,
            paymentProductGroup = dto.paymentProductGroup,
            allowsRecurring = dto.allowsRecurring ?: false,
            allowsTokenization = dto.allowsTokenization ?: false,
            usesRedirectionTo3rdParty = dto.usesRedirectionTo3rdParty ?: false,
            paymentProduct302SpecificData = dto.paymentProduct302SpecificData,
            paymentProduct320SpecificData = dto.paymentProduct320SpecificData,
            logo = dto.displayHints?.logo,
            label = dto.displayHints?.label,
            displayOrder = dto.displayHints?.displayOrder,
            accountsOnFile = dto.accountsOnFile?.map { createAccountOnFile(it) } ?: emptyList(),
            fields = dto.fields.map { createPaymentProductField(it) }.sortedBy { it.displayHints.displayOrder }
        )
    }

    fun createAccountOnFile(dto: AccountOnFileDto): AccountOnFile {
        val attributes = dto.attributes.map { dto ->
            AccountOnFileAttribute(
                key = dto.key,
                value = dto.value,
                status = when (dto.status) {
                    AccountOnFileAttributeDto.Status.READ_ONLY -> AccountOnFileAttribute.Status.READ_ONLY
                    AccountOnFileAttributeDto.Status.CAN_WRITE -> AccountOnFileAttribute.Status.CAN_WRITE
                    AccountOnFileAttributeDto.Status.MUST_WRITE -> AccountOnFileAttribute.Status.MUST_WRITE
                }
            )
        }

        return AccountOnFile(
            id = dto.id,
            paymentProductId = dto.paymentProductId,
            attributes = attributes,
            attributeByKey = attributes.associateBy { it.key },
            label = parseLabel(dto)
        )
    }

    fun createPaymentProductField(dto: PaymentProductFieldDto): PaymentProductField {
        return PaymentProductField(
            id = dto.id,
            type = dto.type,
            displayHints = createDisplayHintsForField(dto.displayHints),
            dataRestrictions = createDataRestrictions(dto.dataRestrictions)
        )
    }

    fun createDisplayHintsForField(dto: ProductFieldDisplayHintsDto): ProductFieldDisplayHints {
        return ProductFieldDisplayHints(
            alwaysShow = dto.alwaysShow,
            obfuscate = dto.obfuscate,
            displayOrder = dto.displayOrder ?: Int.MAX_VALUE,
            label = dto.label,
            placeholderLabel = dto.placeholderLabel,
            mask = dto.mask,
            preferredInputType = dto.preferredInputType,
            tooltipLabel = dto.tooltip?.label,
            formElementType = dto.formElement?.type?.toString()
        )
    }

    fun createDataRestrictions(dto: DataRestrictionsDto): DataRestrictions {
        return DataRestrictions(
            required = dto.isRequired ?: false,
            validationRules = ValidationRuleFactory().createRules(dto.validators)
        )
    }

    private fun parseLabel(dto: AccountOnFileDto): String? {
        val alias = dto.attributes.find { it.key == "alias" }?.value
        val mask = dto.displayHints.labelTemplate.find { it?.attributeKey == "alias" }?.mask

        return if (mask != null && alias != null) {
            StringFormatter.applyMask(mask, alias) ?: alias
        } else {
            alias
        }
    }
}
