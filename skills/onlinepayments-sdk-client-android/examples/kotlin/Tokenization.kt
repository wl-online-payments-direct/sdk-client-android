package com.example.checkout

import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk

// [START encrypt-token-request]
// There is no order amount when a card is stored without a payment: use an amount of 1, the
// smallest unit of the currency (1 cent for EUR). Do not use 0: the Client API answers with an error.
// Use this context for getIinDetails and getPaymentProduct to load the card product.
fun createTokenizationContext(currencyCode: String, countryCode: String): PaymentContextWithAmount =
    PaymentContextWithAmount(AmountOfMoneyWithAmount(1, currencyCode), countryCode, false)

private val cardFieldIds = listOf("cardNumber", "cardholderName", "expiryDate", "cvv")

sealed interface StoreCardResult {
    // Show these next to the fields. Nothing was sent.
    class Invalid(val errorsByField: Map<String, List<String>>) : StoreCardResult

    class Stored(val token: String) : StoreCardResult
}

suspend fun storeCard(
    sdk: OnlinePaymentsSdk,
    cardProduct: PaymentProduct, // from sdk.getPaymentProduct for the detected card product
    form: Map<String, String>, // field id -> the value as shown in the input, with or without mask
): StoreCardResult {
    // Remove the masks first ("12/26" becomes "1226"): the validation rules and the token request
    // expect values without formatting.
    val values = cardFieldIds.associateWith { fieldId ->
        val value = form[fieldId].orEmpty()
        cardProduct.getField(fieldId)?.removeMask(value) ?: value
    }

    // CreditCardTokenRequest applies no validation, so validate with the product's fields first.
    val errorsByField = cardFieldIds.mapNotNull { fieldId ->
        val errors = cardProduct.getField(fieldId)?.validate(values[fieldId]).orEmpty()
        if (errors.isEmpty()) null else fieldId to errors.map { it.errorMessage }
    }.toMap()
    if (errorsByField.isNotEmpty()) {
        return StoreCardResult.Invalid(errorsByField)
    }

    val tokenRequest = CreditCardTokenRequest(
        cardNumber = values["cardNumber"],
        cardholderName = values["cardholderName"],
        expiryDate = values["expiryDate"],
        securityCode = values["cvv"],
        // Required: encryption fails with an EncryptionException without it.
        paymentProductId = cardProduct.id,
    )
    val encryptedRequest = sdk.encryptTokenRequest(tokenRequest)

    // Your backend creates the token with the encrypted data.
    return StoreCardResult.Stored(submitEncryptedToken(encryptedRequest))
}
// [END encrypt-token-request]
