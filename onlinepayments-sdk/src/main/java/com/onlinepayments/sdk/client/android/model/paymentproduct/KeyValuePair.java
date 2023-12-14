/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import java.io.Serializable;

/**
 * POJO that represents an KeyValuePair object.
 * The KeyValuePairs contains the information from the account on file.
 *
 * @deprecated In a future release, this class will be removed, please use {@link AccountOnFileAttribute} instead.
 */
@Deprecated
public class KeyValuePair implements Serializable {

	private static final long serialVersionUID = -8520100614239969197L;

	/**
	 * Enum containing all the possible KeyValuePair statuses
	 */
	public enum Status {

		READ_ONLY(false),
		CAN_WRITE(true),
		MUST_WRITE(true);

		private boolean isEditingAllowed;

		Status(boolean isEditingAllowed) {
			this.isEditingAllowed = isEditingAllowed;
		}

		public boolean isEditingAllowed() {
			return isEditingAllowed;
		}
	}

	private String key;
	private String value;
	private Status status;

	/**
	 * @deprecated In a future release, this property will be removed since it is not returned from the API.
	 */
	@Deprecated
	private String mustWriteReason;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public KeyValuePair() {}

	protected KeyValuePair(String key, String value, AccountOnFileAttribute.Status status) {
		this.key = key;
		this.value = value;

		switch (status) {
			case READ_ONLY:
				this.status = Status.READ_ONLY;
				break;
			case CAN_WRITE:
				this.status = Status.CAN_WRITE;
				break;
			case MUST_WRITE:
				this.status = Status.MUST_WRITE;
				break;
		}
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @deprecated In a future release, this getter will be removed since it is not returned from the API.
	 */
	@Deprecated
	public String getMustWriteReason() {
		return mustWriteReason;
	}

	/**
	 * @deprecated In a future release, this setter will be removed.
	 */
	@Deprecated
	public void setMustWriteReason(String mustWriteReason) {
		this.mustWriteReason = mustWriteReason;
	}

	public boolean isEditingAllowed() {
		return status != null && status.isEditingAllowed();
	}
}
