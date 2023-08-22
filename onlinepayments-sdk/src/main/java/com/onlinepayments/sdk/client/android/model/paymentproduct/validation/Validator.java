/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct.validation;

import java.io.Serializable;

/**
 * POJO which holds the Validator data.
 * Containing all the validation types.
 */
public class Validator implements Serializable {

	private static final long serialVersionUID = 8524174888810141991L;

	private ExpirationDate expirationDate;
	private EmailAddress emailAddress;
	private IBAN iban;
	private FixedList fixedList;
	private Length length;
	private Luhn luhn;
	private Range range;
	private RegularExpression regularExpression;
	private TermsAndConditions termsAndConditions;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public Validator() {}

	public ExpirationDate getExpirationDate(){
		return expirationDate;
	}

	public EmailAddress getEmailAddress(){
		return emailAddress;
	}

	public FixedList getFixedList(){
		return fixedList;
	}

	public IBAN getIBAN(){ return iban; }

	public Length getLength(){
		return length;
	}

	public Luhn getLuhn(){
		return luhn;
	}

	public Range getRange(){
		return range;
	}

	public RegularExpression getRegularExpression(){
		return regularExpression;
	}

	public TermsAndConditions getTermsAndConditions() {
		return termsAndConditions;
	}

}
