package com.ingenico.direct.sdk.client.android.integrationtest.sessions;

import com.ingenico.direct.Client;
import com.ingenico.direct.domain.CreatePaymentRequest;
import com.ingenico.direct.domain.CreatePaymentResponse;
import com.ingenico.direct.domain.SessionRequest;
import com.ingenico.direct.domain.SessionResponse;

/**
 * Util class that sets up a session
 *
 * Copyright 2014 Global Collect Services B.V
 *
 */
public class SessionUtil {

    private final Client client;

    public SessionUtil(Client client) {
        this.client = client;
    }

    public SessionResponse createSession(String merchantId, SessionRequest createSessionJsonBody) {
        SessionResponse response = new SessionResponse();
        response.setAssetUrl("https://cdn.test.merchant.ingenico.com/s2s/8bd0b2bfc386445e81f1");
        response.setClientApiUrl("https://payment.preprod.direct.ingenico.com");
        response.setClientSessionId("34e7810a429044088d7e687bd4901af8");
        response.setCustomerId("c777aa99964c418cb49fe107e7ff1fb0");
        return response;
    }

    public CreatePaymentResponse createPayment(String merchantId, CreatePaymentRequest createPaymentRequestBody) {
        return client.merchant(merchantId).payments().createPayment(createPaymentRequestBody);
    }
}
