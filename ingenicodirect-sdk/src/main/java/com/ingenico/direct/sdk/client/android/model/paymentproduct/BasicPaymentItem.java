package com.ingenico.direct.sdk.client.android.model.paymentproduct;

import com.ingenico.direct.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsPaymentItem;

import java.io.Serializable;
import java.util.List;

/**
 * Copyright 2020 Global Collect Services B.V
 */
public interface BasicPaymentItem extends Serializable {

    String getId();

    DisplayHintsPaymentItem getDisplayHints();

    List<AccountOnFile> getAccountsOnFile();

}
