/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators

import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.EmailAddressDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.ExpirationDateDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.FixedListDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.IBANDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.LengthDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.LuhnDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.RangeDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.RegularExpressionDto
import com.onlinepayments.sdk.client.android.infrastructure.apiModels.validators.ruleDefinitions.TermsAndConditionsDto

/**
 * Data class which holds the Validator data.
 * Containing all the validation types.
 */
internal data class ValidatorsDto(
    val expirationDate: ExpirationDateDto? = null,
    val emailAddress: EmailAddressDto? = null,
    val iban: IBANDto? = null,
    val fixedList: FixedListDto? = null,
    val length: LengthDto? = null,
    val luhn: LuhnDto? = null,
    val range: RangeDto? = null,
    val regularExpression: RegularExpressionDto? = null,
    val termsAndConditions: TermsAndConditionsDto? = null
)
