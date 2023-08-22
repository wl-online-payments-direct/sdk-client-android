/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model;

import java.util.ArrayList;

/**
 * POJO that contains the response for PaymentProductNetwork lookup.
 */
public class PaymentProductNetworkResponse {

    private ArrayList<String> networks;

    /**
     * @deprecated In a future release, this constructor will become internal to the SDK.
     */
    @Deprecated
    public PaymentProductNetworkResponse(ArrayList<String> networks) {
        this.networks = networks;
    }

    public ArrayList<String> getNetworks() { return networks; }

}
