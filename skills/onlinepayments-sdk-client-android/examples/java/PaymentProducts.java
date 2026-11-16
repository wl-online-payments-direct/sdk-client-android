package com.example.checkoutjava;

import com.onlinepayments.sdk.client.android.domain.PaymentContext;
import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile;
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProducts;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;
import com.onlinepayments.sdk.client.android.facade.listeners.BasicPaymentProductsResponseListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class PaymentProducts {
    private PaymentProducts() {
    }

    // [START list-payment-products-java]
    // Google Pay (320) is returned by the SDK when the device can pay with it, but the payment sheet
    // is your code (see the Google Pay section). Leave it out when your app does not implement it.
    public static final int GOOGLE_PAY_PAYMENT_PRODUCT_ID = 320;

    // product.getLogo() is a partial path: build the full URL with the assetUrl of the session data.
    // Returns null when there is no logo.
    public static String logoUrl(String assetUrl, String logo) {
        if (logo == null || logo.isEmpty()) {
            return null;
        }
        String base = assetUrl.endsWith("/") ? assetUrl.substring(0, assetUrl.length() - 1) : assetUrl;
        return base + "/" + (logo.startsWith("/") ? logo.substring(1) : logo);
    }

    private static int displayOrder(BasicPaymentProduct product) {
        return product.getDisplayOrder() == null ? Integer.MAX_VALUE : product.getDisplayOrder().intValue();
    }

    public static final class StoredAccount {
        public final AccountOnFile accountOnFile; // getLabel() is the display text, for example "4242 **** **** 4242"
        public final String logoUrl; // may be null

        StoredAccount(AccountOnFile accountOnFile, String logoUrl) {
            this.accountOnFile = accountOnFile;
            this.logoUrl = logoUrl;
        }
    }

    public static final class PaymentOptions {
        public final List<StoredAccount> storedAccounts;
        public final List<BasicPaymentProduct> products; // show each with logoUrl(assetUrl, product.getLogo())

        PaymentOptions(List<StoredAccount> storedAccounts, List<BasicPaymentProduct> products) {
            this.storedAccounts = storedAccounts;
            this.products = products;
        }
    }

    // The callbacks run on the main thread.
    public static void loadPaymentOptions(
            OnlinePaymentsSdk sdk,
            PaymentContext paymentContext,
            String assetUrl, // from the session data
            MerchantBackend.Callback<PaymentOptions> callback) {
        sdk.getBasicPaymentProducts(paymentContext, new BasicPaymentProductsResponseListener() {
            @Override
            public void onSuccess(BasicPaymentProducts basicPaymentProducts) {
                // Accounts on file are the customer's stored cards. They are only returned when your
                // backend passed the customer's tokens to Create Session.
                List<StoredAccount> storedAccounts = new ArrayList<>();
                for (AccountOnFile accountOnFile : basicPaymentProducts.getAccountsOnFile()) {
                    String logo = null;
                    for (BasicPaymentProduct product : basicPaymentProducts.getPaymentProducts()) {
                        if (product.getId() != null && product.getId() == accountOnFile.getPaymentProductId()) {
                            logo = product.getLogo();
                        }
                    }
                    storedAccounts.add(new StoredAccount(accountOnFile, logoUrl(assetUrl, logo)));
                }

                // The SDK does not sort the products: sort them by displayOrder.
                List<BasicPaymentProduct> products = new ArrayList<>();
                for (BasicPaymentProduct product : basicPaymentProducts.getPaymentProducts()) {
                    if (product.getId() == null || product.getId() != GOOGLE_PAY_PAYMENT_PRODUCT_ID) {
                        products.add(product);
                    }
                }
                Collections.sort(products, new Comparator<BasicPaymentProduct>() {
                    @Override
                    public int compare(BasicPaymentProduct a, BasicPaymentProduct b) {
                        return Integer.compare(displayOrder(a), displayOrder(b));
                    }
                });

                callback.onSuccess(new PaymentOptions(storedAccounts, products));
            }

            @Override
            public void onFailure(SdkException exception) {
                // A ResponseException with httpStatusCode 404 means no payment product is available.
                callback.onError(exception);
            }
        });
    }
    // [END list-payment-products-java]
}
