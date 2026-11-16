package com.example.checkout

import android.text.InputType
import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductFieldType
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PreferredInputType
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk

// [START get-payment-product]
class SelectedProduct(
    val paymentProduct: PaymentProduct,
    val accountOnFile: AccountOnFile?,
)

suspend fun loadSelectedProduct(
    sdk: OnlinePaymentsSdk,
    paymentContext: PaymentContext,
    paymentProductId: Int, // for a stored card: accountOnFile.paymentProductId
    accountOnFileId: String?,
): SelectedProduct {
    val paymentProduct = sdk.getPaymentProduct(paymentProductId, paymentContext)
    val accountOnFile = accountOnFileId?.let { paymentProduct.getAccountOnFile(it) }
    return SelectedProduct(paymentProduct, accountOnFile)
}
// [END get-payment-product]

// [START describe-form-fields]
class FieldSpec(
    val id: String,
    val label: String,
    val placeholder: String?,
    val required: Boolean,
    val obfuscate: Boolean, // the input must hide the text, like a password
    val type: PaymentProductFieldType?,
    val inputType: Int, // an android.text.InputType value for the EditText or keyboard options
    val prefilledValue: String?, // the stored value of the account on file, for example a masked card number
    val readOnly: Boolean, // a field of the selected account on file that the customer cannot change
)

// Build the form from the fields the API returns. Do not hard-code fields, masks or validation
// rules: they differ per payment product and per merchant configuration. The SDK returns the
// fields sorted by display order.
fun describeFormFields(paymentProduct: PaymentProduct, accountOnFile: AccountOnFile?): List<FieldSpec> =
    paymentProduct.fields.map { field ->
        FieldSpec(
            id = field.id,
            label = field.label,
            placeholder = field.placeholder,
            required = field.isRequired,
            obfuscate = field.shouldObfuscate,
            type = field.type,
            inputType = androidInputType(field),
            prefilledValue = accountOnFile?.getValue(field.id),
            readOnly = accountOnFile != null && !accountOnFile.isWritable(field.id),
        )
    }

private fun androidInputType(field: PaymentProductField): Int {
    // A field with a mask (spaces, slashes) needs a text input type: a numeric input type removes
    // the characters of the mask from the text.
    val numeric = field.displayHints.mask == null &&
        field.displayHints.preferredInputType == PreferredInputType.INTEGER_KEYBOARD
    val base = when {
        numeric -> InputType.TYPE_CLASS_NUMBER
        field.displayHints.preferredInputType == PreferredInputType.EMAIL_ADDRESS_KEYBOARD ->
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        field.displayHints.preferredInputType == PreferredInputType.PHONE_NUMBER_KEYBOARD -> InputType.TYPE_CLASS_PHONE
        else -> InputType.TYPE_CLASS_TEXT
    }
    return when {
        field.shouldObfuscate && numeric -> base or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        field.shouldObfuscate -> base or InputType.TYPE_TEXT_VARIATION_PASSWORD
        // Keep the keyboard from learning or suggesting what the customer types.
        base == InputType.TYPE_CLASS_TEXT -> base or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        else -> base
    }
}
// [END describe-form-fields]
