/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.exception;

/**
 * @deprecated In a future release, this class will be removed.
 */
@Deprecated
public class UnknownNetworkException extends RuntimeException {

    private static final long serialVersionUID = 7604981282147428917L;

    public UnknownNetworkException() {
        super();
    }

    public UnknownNetworkException(String message) {
        super(message);
    }

    public UnknownNetworkException(Throwable t) {
        super(t);
    }

    public UnknownNetworkException(String message, Throwable t) {
        super(message, t);
    }
}
