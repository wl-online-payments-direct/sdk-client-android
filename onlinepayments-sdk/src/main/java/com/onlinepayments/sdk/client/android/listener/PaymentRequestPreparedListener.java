package com.onlinepayments.sdk.client.android.listener;

import com.onlinepayments.sdk.client.android.exception.EncryptDataException;
import com.onlinepayments.sdk.client.android.model.PreparedPaymentRequest;

public interface PaymentRequestPreparedListener {
    void onPaymentRequestPrepared(PreparedPaymentRequest preparedPaymentRequest);
    void onFailure(EncryptDataException e);
}
