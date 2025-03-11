/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.onlinepayments.sdk.client.android.formatter.StringFormatter;
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsAccountOnFile;


/**
 * POJO that represents an AccountOnFile object.
 */
public class AccountOnFile implements Serializable {

	private static final long serialVersionUID = 4898075257024154390L;

	private String id;
	private String paymentProductId;

	private DisplayHintsAccountOnFile displayHints;

	private List<AccountOnFileAttribute> attributes = new ArrayList<>();

	// Used for masking fields
	private StringFormatter formatter = new StringFormatter();

	private String label;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public AccountOnFile() {}

	public String getId() {
		return id;
	}

	public String getPaymentProductId() {
		return paymentProductId;
	}

	public DisplayHintsAccountOnFile getDisplayHints() {
		return displayHints;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since its List object type has been changed to {@link AccountOnFileAttribute}. Use {{@link #getAccountOnFileAttributes()}} instead.
	 */
	@Deprecated
	public List<KeyValuePair> getAttributes() {
		List<KeyValuePair> attributesAsKeyValuePair = new ArrayList<>();

		for (AccountOnFileAttribute aofa : attributes) {
			attributesAsKeyValuePair.add(new KeyValuePair(aofa.getKey(), aofa.getValue(), aofa.getStatus()));
		}

		return attributesAsKeyValuePair;
	}

	public List<AccountOnFileAttribute> getAccountOnFileAttributes() { return attributes; }

	/**
	 * Gets the label of this AccountOnFile based on the DisplayHints and the Attributes values
	 * If a corresponding mask is present, it will applied to the label.
	 *
	 * @return the label which can be displayed on an AccountOnFile selection screen
	 */
	public String getLabel() {
		if (label == null) {
			return determineLabel();
		} else {
			return label;
		}
	}

	private String determineLabel() {
		if (getDisplayHints().getLabelTemplate().get(0) != null) {
			AccountOnFileDisplay display = getDisplayHints().getLabelTemplate().get(0);
			label = getMaskedValue(display.getKey());
		}

		return label;
	}

	/**
	 * Returns the masked value for the given payment product field id.
	 *
	 * @param paymentProductFieldId the id of the {@link PaymentProductField} whose masked value should be returned
	 *
	 * @return String which is the masked value of the provided payment product field.
	 */
	public String getMaskedValue(String paymentProductFieldId) {
		String mask = "";
		for (AccountOnFileDisplay display: displayHints.getLabelTemplate()) {
			if (display.getKey().equals(paymentProductFieldId)) {
				mask = display.getMask();
			}
		}

		return getMaskedValue(paymentProductFieldId, mask);
	}

	/**
	 * Returns the value for the given payment product field id with a custom mask applied.
	 *
	 * @param paymentProductFieldId the id of the {@link PaymentProductField} whose masked value should be returned
	 * @param mask the mask that should be applied to the value of the {@link PaymentProductField}
	 *
	 * @return String which is the value of the provided payment product field with a custom mask applied.
	 */
	public String getMaskedValue(String paymentProductFieldId, String mask) {
		String value = "";
		for (AccountOnFileAttribute attribute: attributes) {
			if (attribute.getKey().equals(paymentProductFieldId)) {
				value = attribute.getValue();
			}
		}

		String relaxedMask = formatter.relaxMask(mask);
		return formatter.applyMask(relaxedMask, value);
	}
}
