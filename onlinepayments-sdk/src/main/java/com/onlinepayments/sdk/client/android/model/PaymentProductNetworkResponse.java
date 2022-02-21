package com.onlinepayments.sdk.client.android.model;

import java.util.ArrayList;

/**
 * Pojo that contains the response for PaymentProductNetwork lookup
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class PaymentProductNetworkResponse {

    private ArrayList<String> networks;

    public PaymentProductNetworkResponse(ArrayList<String> networks) {
        this.networks = networks;
    }

    public ArrayList<String> getNetworks() { return networks; }

}
