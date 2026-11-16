package com.example.checkout

import android.text.Editable
import android.text.TextWatcher
import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest

// [START create-payment-request]
fun createPaymentRequest(
    paymentProduct: PaymentProduct,
    accountOnFile: AccountOnFile?,
    customerWantsToStoreCard: Boolean,
): PaymentRequest {
    // Ask to store the card only when the product allows it and the customer agreed.
    val tokenize = customerWantsToStoreCard && paymentProduct.allowsTokenization == true
    return PaymentRequest(paymentProduct, accountOnFile, tokenize)
}
// [END create-payment-request]

// [START mask-input]
// Stores the text the customer typed in the PaymentRequest and returns it with the field's mask.
// setValue removes the mask before it stores the value, so "4111 1111" is stored as "41111111".
// An empty string clears the value. Do not call it for a read-only field of the selected account
// on file: setValue throws an InvalidArgumentException for those (see FieldSpec.readOnly).
// Use it from a TextWatcher (below) or from the onValueChange of a Compose TextField.
fun formatFieldInput(paymentRequest: PaymentRequest, fieldId: String, input: String): String {
    val field = paymentRequest.getField(fieldId)
    field.setValue(input)
    return field.getMaskedValue().orEmpty()
}

// Add to the EditText of a writable field: editText.addTextChangedListener(PaymentFieldWatcher(...)).
// After the mask is applied, the cursor moves to the end of the text.
class PaymentFieldWatcher(
    private val paymentRequest: PaymentRequest,
    private val fieldId: String,
) : TextWatcher {
    private var updating = false

    override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(editable: Editable) {
        if (updating) {
            return
        }
        val masked = formatFieldInput(paymentRequest, fieldId, editable.toString())
        if (masked != editable.toString()) {
            updating = true
            editable.replace(0, editable.length, masked)
            updating = false
        }
    }
}
// [END mask-input]

// [START validate-field]
// Call when a field loses focus. Returns the messages to show next to the field. The texts are in
// English: to translate them, map ValidationErrorMessage.type (the name of a ValidationRuleType,
// for example "LUHN" or "REQUIRED") to your own texts.
fun fieldErrors(paymentRequest: PaymentRequest, fieldId: String): List<String> =
    paymentRequest.getField(fieldId).validate().errors.map { it.errorMessage }
// [END validate-field]

// [START validate-payment-request]
// Call before encrypting. Returns the error messages per field id; empty when the request is valid.
// With an account on file, the SDK validates the fields the customer must enter (MUST_WRITE) and
// the fields the customer changed.
fun validatePaymentRequest(paymentRequest: PaymentRequest): Map<String, List<String>> =
    paymentRequest.validate().errors.groupBy(
        keySelector = { it.paymentProductFieldId },
        valueTransform = { it.errorMessage },
    )
// [END validate-payment-request]
