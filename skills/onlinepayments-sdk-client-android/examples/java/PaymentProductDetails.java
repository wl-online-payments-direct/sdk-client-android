package com.example.checkoutjava;

import android.text.InputType;

import com.onlinepayments.sdk.client.android.domain.PaymentContext;
import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile;
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductFieldType;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PreferredInputType;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentProductResponseListener;

import java.util.ArrayList;
import java.util.List;

public final class PaymentProductDetails {
    private PaymentProductDetails() {
    }

    // [START get-payment-product-java]
    public static final class SelectedProduct {
        public final PaymentProduct paymentProduct;
        public final AccountOnFile accountOnFile; // null when no stored card was selected

        SelectedProduct(PaymentProduct paymentProduct, AccountOnFile accountOnFile) {
            this.paymentProduct = paymentProduct;
            this.accountOnFile = accountOnFile;
        }
    }

    public static void loadSelectedProduct(
            OnlinePaymentsSdk sdk,
            PaymentContext paymentContext,
            int paymentProductId, // for a stored card: accountOnFile.getPaymentProductId()
            String accountOnFileId, // null when no stored card was selected
            MerchantBackend.Callback<SelectedProduct> callback) {
        sdk.getPaymentProduct(paymentProductId, paymentContext, new PaymentProductResponseListener() {
            @Override
            public void onSuccess(PaymentProduct paymentProduct) {
                AccountOnFile accountOnFile =
                        accountOnFileId == null ? null : paymentProduct.getAccountOnFile(accountOnFileId);
                callback.onSuccess(new SelectedProduct(paymentProduct, accountOnFile));
            }

            @Override
            public void onFailure(SdkException exception) {
                callback.onError(exception);
            }
        });
    }
    // [END get-payment-product-java]

    // [START describe-form-fields-java]
    public static final class FieldSpec {
        public final String id;
        public final String label;
        public final String placeholder; // may be null
        public final boolean required;
        public final boolean obfuscate; // the input must hide the text, like a password
        public final PaymentProductFieldType type; // may be null
        public final int inputType; // an android.text.InputType value for the EditText
        public final String prefilledValue; // the stored value of the account on file, may be null
        public final boolean readOnly; // a field of the selected account on file that the customer cannot change

        FieldSpec(String id, String label, String placeholder, boolean required, boolean obfuscate,
                  PaymentProductFieldType type, int inputType, String prefilledValue, boolean readOnly) {
            this.id = id;
            this.label = label;
            this.placeholder = placeholder;
            this.required = required;
            this.obfuscate = obfuscate;
            this.type = type;
            this.inputType = inputType;
            this.prefilledValue = prefilledValue;
            this.readOnly = readOnly;
        }
    }

    // Build the form from the fields the API returns. Do not hard-code fields, masks or validation
    // rules: they differ per payment product and per merchant configuration. The SDK returns the
    // fields sorted by display order.
    public static List<FieldSpec> describeFormFields(PaymentProduct paymentProduct, AccountOnFile accountOnFile) {
        List<FieldSpec> specs = new ArrayList<>();
        for (PaymentProductField field : paymentProduct.getFields()) {
            specs.add(new FieldSpec(
                    field.getId(),
                    field.getLabel(),
                    field.getPlaceholder(),
                    field.isRequired(),
                    field.getShouldObfuscate(),
                    field.getType(),
                    androidInputType(field),
                    accountOnFile == null ? null : accountOnFile.getValue(field.getId()),
                    accountOnFile != null && !accountOnFile.isWritable(field.getId())));
        }
        return specs;
    }

    private static int androidInputType(PaymentProductField field) {
        PreferredInputType preferred = field.getDisplayHints().getPreferredInputType();
        // A field with a mask (spaces, slashes) needs a text input type: a numeric input type removes
        // the characters of the mask from the text.
        boolean numeric = field.getDisplayHints().getMask() == null
                && preferred == PreferredInputType.INTEGER_KEYBOARD;

        int base;
        if (numeric) {
            base = InputType.TYPE_CLASS_NUMBER;
        } else if (preferred == PreferredInputType.EMAIL_ADDRESS_KEYBOARD) {
            base = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        } else if (preferred == PreferredInputType.PHONE_NUMBER_KEYBOARD) {
            base = InputType.TYPE_CLASS_PHONE;
        } else {
            base = InputType.TYPE_CLASS_TEXT;
        }

        if (field.getShouldObfuscate()) {
            return base | (numeric ? InputType.TYPE_NUMBER_VARIATION_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        if (base == InputType.TYPE_CLASS_TEXT) {
            // Keep the keyboard from learning or suggesting what the customer types.
            return base | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        }
        return base;
    }
    // [END describe-form-fields-java]
}
