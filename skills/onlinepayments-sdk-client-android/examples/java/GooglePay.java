package com.example.checkoutjava;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.contract.ApiTaskResult;
import com.google.android.gms.wallet.contract.TaskResultContracts;
import com.onlinepayments.sdk.client.android.domain.PaymentContext;
import com.onlinepayments.sdk.client.android.domain.exceptions.SdkException;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.domain.paymentProduct.specificData.PaymentProduct320SpecificData;
import com.onlinepayments.sdk.client.android.domain.paymentRequest.PaymentRequest;
import com.onlinepayments.sdk.client.android.facade.OnlinePaymentsSdk;
import com.onlinepayments.sdk.client.android.facade.listeners.PaymentProductResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public final class GooglePay {
    private GooglePay() {
    }

    // [START google-pay-configuration-java]
    // The SDK has no Google Pay payment flow: it does not show the payment sheet. It lists Google Pay
    // (product 320) only when the device can pay with it, and returns the gateway and card networks
    // for the payment sheet request.
    public static final class GooglePayConfiguration {
        public final String gateway;
        public final List<String> networks;

        GooglePayConfiguration(String gateway, List<String> networks) {
            this.gateway = gateway;
            this.networks = networks;
        }
    }

    // Returns null when the product has no Google Pay data.
    public static GooglePayConfiguration googlePayConfiguration(BasicPaymentProduct product) {
        PaymentProduct320SpecificData data = product.getPaymentProduct320SpecificData();
        if (data == null || data.getGateway() == null) {
            return null;
        }
        List<String> networks = new ArrayList<>();
        if (data.getNetworks() != null) {
            for (String network : data.getNetworks()) {
                if (network != null) {
                    networks.add(network);
                }
            }
        }
        return new GooglePayConfiguration(data.getGateway(), networks);
    }
    // [END google-pay-configuration-java]

    // [START google-pay-request-java]
    // Google wants the price as a decimal string in the currency's units ("12.98"), not in minor units.
    // The number of decimals depends on the currency (JPY has none, KWD has three).
    public static String googlePayAmount(long totalInMinorUnits, String currencyCode) {
        int fractionDigits = Math.max(0, Currency.getInstance(currencyCode).getDefaultFractionDigits());
        return BigDecimal.valueOf(totalInMinorUnits, fractionDigits).toPlainString();
    }

    public static PaymentDataRequest buildPaymentDataRequest(
            GooglePayConfiguration configuration,
            String gatewayMerchantId, // your merchant id (PSPID) on the Worldline platform
            String googleMerchantId, // from the Google Pay & Wallet Console; required in production, else null
            String merchantName,
            PaymentContext paymentContext) throws JSONException { // the real order values
        Long total = paymentContext.getAmountOfMoney().getAmount();
        if (total == null || paymentContext.getCountryCode() == null) {
            throw new IllegalArgumentException("The payment context needs an amount and a country code");
        }
        String currencyCode = paymentContext.getAmountOfMoney().getCurrencyCode();

        JSONObject cardMethod = new JSONObject()
                .put("type", "CARD")
                .put("parameters", new JSONObject()
                        .put("allowedAuthMethods", new JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
                        .put("allowedCardNetworks", new JSONArray(configuration.networks)))
                .put("tokenizationSpecification", new JSONObject()
                        .put("type", "PAYMENT_GATEWAY")
                        .put("parameters", new JSONObject()
                                .put("gateway", configuration.gateway)
                                .put("gatewayMerchantId", gatewayMerchantId)));
        JSONObject merchantInfo = new JSONObject().put("merchantName", merchantName);
        if (googleMerchantId != null) {
            merchantInfo.put("merchantId", googleMerchantId);
        }

        JSONObject request = new JSONObject()
                .put("apiVersion", 2)
                .put("apiVersionMinor", 0)
                .put("allowedPaymentMethods", new JSONArray().put(cardMethod))
                .put("merchantInfo", merchantInfo)
                .put("transactionInfo", new JSONObject()
                        .put("totalPriceStatus", "FINAL")
                        .put("totalPrice", googlePayAmount(total, currencyCode))
                        .put("currencyCode", currencyCode)
                        // Required for merchants in the European Economic Area.
                        .put("countryCode", paymentContext.getCountryCode()));
        return PaymentDataRequest.fromJson(request.toString());
    }
    // [END google-pay-request-java]

    // [START google-pay-launcher-java]
    public interface GooglePayListener {
        void onSuccess(PaymentData paymentData);

        void onCancelled(); // the customer closed the sheet: not an error

        void onFailed(int statusCode); // log only the status code: never the PaymentData or the token
    }

    // Create it while the Activity is being created (before it is started), for example in onCreate.
    public static final class GooglePayLauncher {
        private final PaymentsClient paymentsClient;
        private final ActivityResultLauncher<Task<PaymentData>> launcher;

        public GooglePayLauncher(ComponentActivity activity, boolean isProduction, GooglePayListener listener) {
            // isProduction: the same value as SdkConfiguration.environmentIsProduction
            paymentsClient = Wallet.getPaymentsClient(
                    activity,
                    new Wallet.WalletOptions.Builder()
                            .setEnvironment(isProduction
                                    ? WalletConstants.ENVIRONMENT_PRODUCTION
                                    : WalletConstants.ENVIRONMENT_TEST)
                            .build());

            launcher = activity.registerForActivityResult(
                    new TaskResultContracts.GetPaymentDataResult(),
                    (ApiTaskResult<PaymentData> result) -> {
                        int statusCode = result.getStatus().getStatusCode();
                        if (statusCode == CommonStatusCodes.SUCCESS && result.getResult() != null) {
                            listener.onSuccess(result.getResult());
                        } else if (statusCode == CommonStatusCodes.CANCELED) {
                            listener.onCancelled();
                        } else {
                            listener.onFailed(statusCode);
                        }
                    });
        }

        public void start(PaymentDataRequest request) {
            launcher.launch(paymentsClient.loadPaymentData(request));
        }
    }
    // [END google-pay-launcher-java]

    // [START google-pay-payment-request-java]
    public interface GooglePayRequestListener {
        void onReady(PaymentRequest paymentRequest);

        void onError(Exception error); // see error handling
    }

    // Put the Google Pay token into a PaymentRequest for product 320 and pay it like a card payment:
    // encrypt it (see Encryption.payWithEncryptedRequest) and send the result to your backend. Never
    // log, store or send the token or the PaymentData itself.
    public static void createGooglePayPaymentRequest(
            OnlinePaymentsSdk sdk,
            PaymentContext paymentContext,
            PaymentData paymentData,
            GooglePayRequestListener listener) {
        String token;
        try {
            token = new JSONObject(paymentData.toJson())
                    .getJSONObject("paymentMethodData")
                    .getJSONObject("tokenizationData")
                    .getString("token");
        } catch (JSONException e) {
            listener.onError(e);
            return;
        }

        sdk.getPaymentProduct(PaymentProducts.GOOGLE_PAY_PAYMENT_PRODUCT_ID, paymentContext,
                new PaymentProductResponseListener() {
                    @Override
                    public void onSuccess(PaymentProduct googlePayProduct) {
                        PaymentRequest paymentRequest = new PaymentRequest(googlePayProduct, null, false);
                        // The one required field of product 320. The token is set as a quoted JSON
                        // string (as in the SDK's example apps).
                        paymentRequest.setValue("encryptedPaymentData", JSONObject.quote(token));
                        listener.onReady(paymentRequest);
                    }

                    @Override
                    public void onFailure(SdkException exception) {
                        listener.onError(exception);
                    }
                });
    }
    // [END google-pay-payment-request-java]
}
