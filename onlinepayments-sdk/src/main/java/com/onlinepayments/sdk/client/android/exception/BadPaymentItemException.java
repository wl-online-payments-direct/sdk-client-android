/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.exception;

/**
 * @deprecated In a future release, this class will be removed.
 */
@Deprecated
public class BadPaymentItemException extends RuntimeException {


    private static final long serialVersionUID = -4164065223871993498L;

    public BadPaymentItemException() {
        super();
    }

    public BadPaymentItemException(String message) {
        super(message);
    }

    public BadPaymentItemException(Throwable t) {
        super(t);
    }

    public BadPaymentItemException(String message, Throwable t) {
        super(message, t);
    }
}
