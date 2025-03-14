/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.paymentproduct;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * POJO with convenience methods for getting PaymentProduct and AccountOnFile objects.
 */
public class BasicPaymentProducts implements Serializable {

	private static final long serialVersionUID = 6385568686033699522L;

	// List containing all BasicPaymentProducts
	@SerializedName("paymentProducts")
	private List<BasicPaymentProduct> basicPaymentProducts = new ArrayList<>();

	// List containing all AccountsOnFile
	private List<AccountOnFile> accountsOnFile = new ArrayList<>();

	// Boolean containing whether or not the list has already been sorted
	private Boolean hasBeenSorted = false;

	/**
	 * @deprecated In a future release, this constructor will become internal to the SDK.
	 */
	@Deprecated
	public BasicPaymentProducts() {}

	/**
	 * Gets all basicPaymentProducts.
	 *
	 * @return a sorted list of basicPaymentProducts
	 */
	public List<BasicPaymentProduct> getBasicPaymentProducts() {
		sortList();
		return basicPaymentProducts;
	}

	private void sortList(){
		if (!hasBeenSorted) {
			Collections.sort(basicPaymentProducts, new Comparator<BasicPaymentProduct>() {
			    public int compare(BasicPaymentProduct product1, BasicPaymentProduct product2) {
				   if (Objects.equals(product1, product2)) return 0;
				   if (product1 == null) return -1;
				   if (product2 == null) return 1;

				   if (product1.getDisplayHintsList().isEmpty()) return -1;
				   if (product2.getDisplayHintsList().isEmpty())return 1;
				   Integer displayOrder1 = product1.getDisplayHintsList().get(0).getDisplayOrder();
				   Integer displayOrder2 = product2.getDisplayHintsList().get(0).getDisplayOrder();

				   if (Objects.equals(displayOrder1, displayOrder2)) return 0;
				   if (displayOrder1 == null) return -1;
				   if (displayOrder2 == null) return 1;
				   return displayOrder1.compareTo(displayOrder2);
			    }
			});
			hasBeenSorted = true;
		}
	}


	/**
	 * Gets all AccountsOnFile for all BasicPaymentProducts.
	 *
	 * @return a list of all AccountsOnFile
	 */
	public List<AccountOnFile> getAccountsOnFile() {

		// Check if accountsOnFile list is filled, else fill it and return it
		if (accountsOnFile.isEmpty()) {
			for (BasicPaymentProduct product : getBasicPaymentProducts()) {
				accountsOnFile.addAll(product.getAccountsOnFile());
			}
		}

		return accountsOnFile;
	}


	/**
	 * Gets a {@link BasicPaymentProduct} by its id.
	 *
	 * @param basicPaymentProductId the id of the {@link BasicPaymentProduct} that should be retrieved
	 *
	 * @return the retrieved {@link BasicPaymentProduct}, or null if not found
	 */
	public BasicPaymentProduct getBasicPaymentProductById(String basicPaymentProductId) {

		if (basicPaymentProductId == null) {
			throw new InvalidParameterException("Error getting BasicPaymentProduct by id, basicPaymentProductId may not be null");
		}

		for (BasicPaymentProduct basicPaymentProduct : basicPaymentProducts) {
			if (basicPaymentProduct.getId().equals(basicPaymentProductId)) {
				return basicPaymentProduct;
			}
		}

		return null;
	}


	/**
	 * Gets a {@link BasicPaymentProduct} by its AccountOnFileId.
	 *
	 * @param accountOnFileId the accountOnFileId for which the belonging {@link BasicPaymentProduct} should be retrieved
	 *
	 * @return the retrieved {@link BasicPaymentProduct}, or null if not found
	 */
	public BasicPaymentProduct getBasicPaymentProductByAccountOnFileId(String accountOnFileId) {

		if (accountOnFileId == null) {
			throw new InvalidParameterException("Error getting paymentproduct by accountonfile id, accountOnFileId may not be null");
		}

		// Loop trough al basicPaymentProducts to search for the paymentProduct with given AccountOnFileId
		for (BasicPaymentProduct product : basicPaymentProducts) {
			for (AccountOnFile accountOnFile : accountsOnFile) {
				if(accountOnFileId.equals(accountOnFile.getId())) {
					return product;
				}
			}
		}

		return null;
	}

	/**
	 * Returns a list of basicPaymentItems instead of basicPaymentProducts.
	 *
	 * @return list of basicPaymentItems
	 */
	public List<BasicPaymentItem> getPaymentProductsAsItems() {
		List<BasicPaymentItem> basicPaymentItems = new ArrayList<>();
		for (BasicPaymentProduct paymentProduct: getBasicPaymentProducts()) {
			basicPaymentItems.add(paymentProduct);
		}
		return basicPaymentItems;
	}
}
