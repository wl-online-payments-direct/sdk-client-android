package com.example.checkoutjava;

import android.text.Editable;
import android.text.TextWatcher;

import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequestField;
import com.onlinepayments.sdk.client.android.domain.validation.ValidationErrorMessage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ValidationAndMasking {
    private ValidationAndMasking() {
    }

    // [START create-payment-request-java]
    public static PaymentRequest createPaymentRequest(
            PaymentProduct paymentProduct,
            AccountOnFile accountOnFile, // null when no stored card was selected
            boolean customerWantsToStoreCard) {
        // Ask to store the card only when the product allows it and the customer agreed.
        boolean tokenize = customerWantsToStoreCard && Boolean.TRUE.equals(paymentProduct.getAllowsTokenization());
        return new PaymentRequest(paymentProduct, accountOnFile, tokenize);
    }
    // [END create-payment-request-java]

    // [START mask-input-java]
    // Stores the text the customer typed in the PaymentRequest and returns it with the field's mask.
    // setValue removes the mask before it stores the value, so "4111 1111" is stored as "41111111".
    // An empty string clears the value. Do not call it for a read-only field of the selected account
    // on file: setValue throws an InvalidArgumentException for those (see FieldSpec.readOnly).
    public static String formatFieldInput(PaymentRequest paymentRequest, String fieldId, String input) {
        PaymentRequestField field = paymentRequest.getField(fieldId);
        field.setValue(input);
        String masked = field.getMaskedValue();
        return masked == null ? "" : masked;
    }

    // Add to the EditText of a writable field: editText.addTextChangedListener(new PaymentFieldWatcher(...)).
    // After the mask is applied, the cursor moves to the end of the text.
    public static final class PaymentFieldWatcher implements TextWatcher {
        private final PaymentRequest paymentRequest;
        private final String fieldId;
        private boolean updating;

        public PaymentFieldWatcher(PaymentRequest paymentRequest, String fieldId) {
            this.paymentRequest = paymentRequest;
            this.fieldId = fieldId;
        }

        @Override
        public void beforeTextChanged(CharSequence text, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (updating) {
                return;
            }
            String masked = formatFieldInput(paymentRequest, fieldId, editable.toString());
            if (!masked.equals(editable.toString())) {
                updating = true;
                editable.replace(0, editable.length(), masked);
                updating = false;
            }
        }
    }
    // [END mask-input-java]

    // [START validate-field-java]
    // Call when a field loses focus. Returns the messages to show next to the field. The texts are in
    // English: to translate them, map ValidationErrorMessage.getType() (the name of a
    // ValidationRuleType, for example "LUHN" or "REQUIRED") to your own texts.
    public static List<String> fieldErrors(PaymentRequest paymentRequest, String fieldId) {
        List<String> messages = new ArrayList<>();
        for (ValidationErrorMessage error : paymentRequest.getField(fieldId).validate().getErrors()) {
            messages.add(error.getErrorMessage());
        }
        return messages;
    }
    // [END validate-field-java]

    // [START validate-payment-request-java]
    // Call before encrypting. Returns the error messages per field id; empty when the request is valid.
    // With an account on file, the SDK validates the fields the customer must enter (MUST_WRITE) and
    // the fields the customer changed.
    public static Map<String, List<String>> validatePaymentRequest(PaymentRequest paymentRequest) {
        Map<String, List<String>> errorsByField = new LinkedHashMap<>();
        for (ValidationErrorMessage error : paymentRequest.validate().getErrors()) {
            List<String> messages = errorsByField.get(error.getPaymentProductFieldId());
            if (messages == null) {
                messages = new ArrayList<>();
                errorsByField.put(error.getPaymentProductFieldId(), messages);
            }
            messages.add(error.getErrorMessage());
        }
        return errorsByField;
    }
    // [END validate-payment-request-java]
}
