package com.example.checkout

import com.onlinepayments.sdk.client.android.domain.PaymentContext
import com.onlinepayments.sdk.client.android.domain.accountOnFile.AccountOnFile
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk

// [START list-payment-products]
// Google Pay (320) is returned by the SDK when the device can pay with it, but the payment sheet
// is your code (see the Google Pay section). Leave it out when your app does not implement it.
const val GOOGLE_PAY_PAYMENT_PRODUCT_ID = 320

// product.logo is a partial path: build the full URL with the assetUrl of the session data.
fun logoUrl(assetUrl: String, logo: String?): String? {
    if (logo.isNullOrEmpty()) {
        return null
    }
    return assetUrl.trimEnd('/') + "/" + logo.trimStart('/')
}

class StoredAccount(
    val accountOnFile: AccountOnFile, // accountOnFile.label is the display text, for example "4242 **** **** 4242"
    val logoUrl: String?,
)

class PaymentOptions(
    val storedAccounts: List<StoredAccount>,
    val products: List<BasicPaymentProduct>, // show each with logoUrl(assetUrl, product.logo)
)

suspend fun loadPaymentOptions(
    sdk: OnlinePaymentsSdk,
    paymentContext: PaymentContext,
    assetUrl: String, // from the session data
): PaymentOptions {
    // Throws a ResponseException with httpStatusCode 404 when no payment product is available.
    val basicPaymentProducts = sdk.getBasicPaymentProducts(paymentContext)

    // Accounts on file are the customer's stored cards. They are only returned when your backend
    // passed the customer's tokens to Create Session.
    val storedAccounts = basicPaymentProducts.accountsOnFile.map { accountOnFile ->
        val product = basicPaymentProducts.paymentProducts.firstOrNull { it.id == accountOnFile.paymentProductId }
        StoredAccount(accountOnFile, logoUrl(assetUrl, product?.logo))
    }

    // The SDK does not sort the products: sort them by displayOrder.
    val products = basicPaymentProducts.paymentProducts
        .filter { it.id != GOOGLE_PAY_PAYMENT_PRODUCT_ID }
        .sortedBy { it.displayOrder?.toInt() ?: Int.MAX_VALUE }

    return PaymentOptions(storedAccounts, products)
}
// [END list-payment-products]
