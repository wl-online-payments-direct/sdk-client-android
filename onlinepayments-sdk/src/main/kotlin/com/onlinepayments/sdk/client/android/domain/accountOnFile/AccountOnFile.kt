/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */


package com.onlinepayments.sdk.client.android.domain.accountOnFile

import java.io.Serializable

/**
 * Domain model that represents an AccountOnFile object.
 */
class AccountOnFile internal constructor(
    val id: String,
    val paymentProductId: Int,
    val label: String?,
    private val attributes: List<AccountOnFileAttribute>,
    private val attributeByKey: Map<String, AccountOnFileAttribute>
) : Serializable {

    fun getValue(fieldId: String): String? {
        return attributeByKey[fieldId]?.value
    }

    fun getRequiredAttributes(): List<AccountOnFileAttribute> {
        return attributes.filter { it.status == AccountOnFileAttribute.Status.MUST_WRITE }
    }

    fun isWritable(fieldId: String): Boolean {
        return attributeByKey[fieldId]?.status != AccountOnFileAttribute.Status.READ_ONLY
    }

    companion object {
        @Suppress("Unused")
        private const val serialVersionUID = 4898075257024154390L
    }
}
