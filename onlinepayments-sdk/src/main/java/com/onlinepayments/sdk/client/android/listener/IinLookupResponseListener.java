/*
 * Copyright 2023 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.listener;

import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse;

/**
 * Callback Interface that is invoked when a Iin Details API request completes.
 */
public interface IinLookupResponseListener extends GenericResponseListener<IinDetailsResponse> {
}
