/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsPaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.specificdata.PaymentProduct302SpecificData;
import com.onlinepayments.sdk.client.android.model.paymentproduct.specificdata.PaymentProduct320SpecificData;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO which holds the BasicPaymentProduct properties.
 */
public class BasicPaymentProduct implements BasicPaymentItem, Serializable {

	private static final long serialVersionUID = -8362704974696989741L;

	private String id;
	private String paymentMethod;
	private String paymentProductGroup;
	/**
	 * @deprecated In a future release, this property will be removed since it is not returned from the API.
	 */
	@Deprecated
	private Long minAmount;
	/**
	 * @deprecated In a future release, this property will be removed since it is not returned from the API.
	 */
	@Deprecated
	private Long maxAmount;
	private Boolean allowsRecurring;
	private Boolean allowsTokenization;
	private Boolean usesRedirectionTo3rdParty;
	/**
	 * @deprecated In a future release, this property will be removed since it is not returned from the API.
	 */
	@Deprecated
	private MobileIntegrationLevel mobileIntegrationLevel;
	@Deprecated
	private DisplayHintsPaymentItem displayHints;
	private List<DisplayHintsPaymentItem> displayHintsList = new ArrayList<>();

	// List containing all AccountsOnFile
	private List<AccountOnFile> accountsOnFile = new ArrayList<>();

	// Payment product specific data
	private PaymentProduct302SpecificData paymentProduct302SpecificData;
	private PaymentProduct320SpecificData paymentProduct320SpecificData;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public BasicPaymentProduct() {}

	public String getId(){
		return id;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public String getPaymentProductGroup() {
		return paymentProductGroup;
	}

	public Boolean allowsRecurring() {
		return allowsRecurring;
	}

	public Boolean allowsTokenization(){
		return allowsTokenization;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since its value is not returned from the API.
	 */
	@Deprecated
	public Long getMinAmount(){
		return minAmount;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since its value is not returned from the API.
	 */
	@Deprecated
	public Long getMaxAmount(){
		return maxAmount;
	}

	public Boolean usesRedirectionTo3rdParty(){
		return usesRedirectionTo3rdParty;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since its value is not returned from the API.
	 */
	@Deprecated
	public MobileIntegrationLevel mobileIntegrationLevel(){
		return mobileIntegrationLevel;
	}

	public List<AccountOnFile> getAccountsOnFile() {
		return accountsOnFile;
	}

	public AccountOnFile getAccountOnFileById(String accountOnFileId) {

		if (accountOnFileId == null) {
			throw new InvalidParameterException("Error getting AccountOnFile by id, accountOnFileId may not be null");
		}

		for (AccountOnFile accountOnFile : accountsOnFile) {
			if (accountOnFile.getId().equals(accountOnFileId)) {
				return accountOnFile;
			}
		}
		return null;
	}

	/**
	 * @deprecated use {@link #getDisplayHintsList()} )}
	 */
	@Deprecated
	public DisplayHintsPaymentItem getDisplayHints() {
		return displayHints;
	}

	public List<DisplayHintsPaymentItem> getDisplayHintsList() {
		return displayHintsList;
	}

	public PaymentProduct302SpecificData getPaymentProduct302SpecificData() {
		return paymentProduct302SpecificData;
	}

	public PaymentProduct320SpecificData getPaymentProduct320SpecificData() {
		return paymentProduct320SpecificData;
	}
}
