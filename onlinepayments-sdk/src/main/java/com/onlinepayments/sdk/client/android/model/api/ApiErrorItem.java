/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.api;

import java.io.Serializable;

/**
 * The error item in the error body.
 */
public class ApiErrorItem implements Serializable {

    private static final long serialVersionUID = 1983759919374923872L;

    private String errorCode;
    private String category;
    private Integer httpStatusCode;
    private String id;
    private String propertyName;
    private boolean retriable = true;

    /**
     * The error code
     *
     * @deprecated In a future release, this property will be removed. Use error code instead.
     */
    @Deprecated
    public String code = "This error does not contain a code";

    /**
     * The error message
     */
    public String message = "This error does not contain a message";

    /**
     * The error code
     */
    public String getErrorCode() { return errorCode; }

    /**
     * The category the error belongs to
     */
    public String getCategory() { return category; }

    /**
     * The HTTP status code
     */
    public Integer getHttpStatusCode() { return httpStatusCode; }

    /**
     * The error id
     */
    public String getId() { return id; }

    /**
     * The name of the property
     */
    public String getPropertyName() { return propertyName; }

    /**
     * Indicating whether the request is retriable
     */
    public boolean isRetriable() { return retriable; }
}
