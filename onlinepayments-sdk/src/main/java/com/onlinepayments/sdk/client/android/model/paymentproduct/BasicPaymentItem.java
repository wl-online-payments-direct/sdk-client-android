package com.onlinepayments.sdk.client.android.model.paymentproduct;

import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsPaymentItem;

import java.io.Serializable;
import java.util.List;

/**
 * Copyright 2020 Global Collect Services B.V
 */
public interface BasicPaymentItem extends Serializable {

    String getId();

    /**
     * @deprecated use {@link #getDisplayHintsList()} )}
     */
    @Deprecated
    DisplayHintsPaymentItem getDisplayHints();

    List<DisplayHintsPaymentItem> getDisplayHintsList();

    List<AccountOnFile> getAccountsOnFile();

}
