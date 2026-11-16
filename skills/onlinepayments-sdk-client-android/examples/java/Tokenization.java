package com.example.checkoutjava;

import com.onlinepayments.sdk.client.android.domain.AmountOfMoneyWithAmount;
import com.onlinepayments.sdk.client.android.domain.PaymentContextWithAmount;
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.productField.PaymentProductField;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.CreditCardTokenRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest;
import com.onlinepayments.sdk.client.android.domain.validation.ValidationErrorMessage;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentRequestPreparedListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Tokenization {
    private Tokenization() {
    }

    // [START encrypt-token-request-java]
    // There is no order amount when a card is stored without a payment: use an amount of 1, the
    // smallest unit of the currency (1 cent for EUR). Do not use 0: the Client API answers with an error.
    // Use this context for getIinDetails and getPaymentProduct to load the card product.
    public static PaymentContextWithAmount createTokenizationContext(String currencyCode, String countryCode) {
        return new PaymentContextWithAmount(new AmountOfMoneyWithAmount(1, currencyCode), countryCode, false);
    }

    private static final String[] CARD_FIELD_IDS = {"cardNumber", "cardholderName", "expiryDate", "cvv"};

    public interface StoreCardListener {
        // Show these next to the fields. Nothing was sent.
        void onInvalid(Map<String, List<String>> errorsByField);

        void onStored(String token);

        void onError(Exception error); // see error handling
    }

    public static void storeCard(
            OnlinePaymentsSdk sdk,
            PaymentProduct cardProduct, // from sdk.getPaymentProduct for the detected card product
            Map<String, String> form, // field id -> the value as shown in the input, with or without mask
            StoreCardListener listener) {
        // Remove the masks first ("12/26" becomes "1226"): the validation rules and the token request
        // expect values without formatting. Then validate: CreditCardTokenRequest applies no validation.
        Map<String, String> values = new LinkedHashMap<>();
        Map<String, List<String>> errorsByField = new LinkedHashMap<>();
        for (String fieldId : CARD_FIELD_IDS) {
            String value = form.get(fieldId) == null ? "" : form.get(fieldId);
            PaymentProductField field = cardProduct.getField(fieldId);
            if (field != null) {
                String unmasked = field.removeMask(value);
                value = unmasked == null ? value : unmasked;
                List<String> messages = new ArrayList<>();
                for (ValidationErrorMessage error : field.validate(value)) {
                    messages.add(error.getErrorMessage());
                }
                if (!messages.isEmpty()) {
                    errorsByField.put(fieldId, messages);
                }
            }
            values.put(fieldId, value);
        }
        if (!errorsByField.isEmpty()) {
            listener.onInvalid(errorsByField);
            return;
        }

        CreditCardTokenRequest tokenRequest = new CreditCardTokenRequest(
                values.get("cardNumber"),
                values.get("cardholderName"),
                values.get("expiryDate"),
                values.get("cvv"),
                // Required: encryption fails with an EncryptionException without it.
                cardProduct.getId());

        sdk.encryptTokenRequest(tokenRequest, new PaymentRequestPreparedListener() {
            @Override
            public void onSuccess(EncryptedRequest encryptedRequest) {
                // Your backend creates the token with the encrypted data.
                MerchantBackend.submitEncryptedToken(encryptedRequest, new MerchantBackend.Callback<String>() {
                    @Override
                    public void onSuccess(String token) {
                        listener.onStored(token);
                    }

                    @Override
                    public void onError(Exception error) {
                        listener.onError(error);
                    }
                });
            }

            @Override
            public void onFailure(SdkException exception) {
                listener.onError(exception);
            }
        });
    }
    // [END encrypt-token-request-java]
}
