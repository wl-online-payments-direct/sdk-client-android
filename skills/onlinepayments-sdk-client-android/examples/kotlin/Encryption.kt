package com.example.checkout

import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk

// [START encrypt-payment-request]
sealed interface PaymentSubmission {
    // Show these next to the fields. Nothing was sent.
    class InputInvalid(val errorsByField: Map<String, List<String>>) : PaymentSubmission

    class Submitted(val result: MerchantPaymentResponse) : PaymentSubmission
}

suspend fun payWithEncryptedRequest(
    sdk: OnlinePaymentsSdk,
    paymentRequest: PaymentRequest,
    orderId: String,
    currencyConversion: CurrencyConversionChoice?, // the customer's choice, when a conversion offer was shown
): PaymentSubmission {
    val errorsByField = validatePaymentRequest(paymentRequest)
    if (errorsByField.isNotEmpty()) {
        return PaymentSubmission.InputInvalid(errorsByField) // do not encrypt
    }

    // The only way card data leaves the device: encrypted by the SDK. It validates the request
    // again and throws an InvalidArgumentException when it is not valid.
    // Never send, log or store the raw field values.
    val encryptedRequest = sdk.encryptPaymentRequest(paymentRequest)

    // Your backend passes encryptedCustomerInput to Create Payment and decides the outcome.
    val result = submitEncryptedPayment(orderId, encryptedRequest, currencyConversion)
    return PaymentSubmission.Submitted(result)
}
// [END encrypt-payment-request]
