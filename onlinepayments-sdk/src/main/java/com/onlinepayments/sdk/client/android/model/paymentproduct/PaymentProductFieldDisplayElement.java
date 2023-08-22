/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;

/**
 * POJO which holds a PaymentProductFieldDisplayElement.
 */
public class PaymentProductFieldDisplayElement implements Serializable {

    private static final long serialVersionUID = 3137435990791529227L;

    public enum PaymentProductFieldDisplayElementType {
        INTEGER,
        STRING,
        CURRENCY,
        PERCENTAGE,
        URI,
        ;
    }

    private String id;
    private PaymentProductFieldDisplayElementType type;
    private String value;

    protected PaymentProductFieldDisplayElement() { }

    public String getId() {
        return id;
    }

    public PaymentProductFieldDisplayElementType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
