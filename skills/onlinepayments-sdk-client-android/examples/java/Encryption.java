package com.example.checkoutjava;

import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.EncryptedRequest;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentRequestPreparedListener;

import java.util.List;
import java.util.Map;

public final class Encryption {
    private Encryption() {
    }

    // [START encrypt-payment-request-java]
    public interface PaymentListener {
        // Show these next to the fields. Nothing was sent.
        void onInputInvalid(Map<String, List<String>> errorsByField);

        void onSubmitted(MerchantBackend.MerchantPaymentResponse result);

        void onError(Exception error); // see error handling
    }

    public static void payWithEncryptedRequest(
            OnlinePaymentsSdk sdk,
            PaymentRequest paymentRequest,
            String orderId,
            MerchantBackend.CurrencyConversionChoice currencyConversion, // null unless a conversion offer was shown
            PaymentListener listener) {
        Map<String, List<String>> errorsByField = ValidationAndMasking.validatePaymentRequest(paymentRequest);
        if (!errorsByField.isEmpty()) {
            listener.onInputInvalid(errorsByField); // do not encrypt
            return;
        }

        // The only way card data leaves the device: encrypted by the SDK. It validates the request
        // again and fails with an InvalidArgumentException when it is not valid.
        // Never send, log or store the raw field values.
        sdk.encryptPaymentRequest(paymentRequest, new PaymentRequestPreparedListener() {
            @Override
            public void onSuccess(EncryptedRequest encryptedRequest) {
                // Your backend passes encryptedCustomerInput to Create Payment and decides the outcome.
                MerchantBackend.submitEncryptedPayment(orderId, encryptedRequest, currencyConversion,
                        new MerchantBackend.Callback<MerchantBackend.MerchantPaymentResponse>() {
                            @Override
                            public void onSuccess(MerchantBackend.MerchantPaymentResponse result) {
                                listener.onSubmitted(result);
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
    // [END encrypt-payment-request-java]
}
