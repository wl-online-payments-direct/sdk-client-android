# Checkout flows (Java)

The same steps as [flows.md](flows.md), written for Java with the callback (listener) variants of the
SDK methods. Every code block below is copied from a file in `../examples/java/`. Read flows.md for
the reasons behind each step: the rules and notes there apply to Java as well.

How the SDK looks from Java:

- Use the listener variants: `sdk.getPaymentProduct(id, paymentContext, listener)`. `onSuccess` and
  `onFailure` run on the main thread, so you can update the UI in them. Remember that they also run
  when the screen is gone (see [android.md](android.md#three-variants-of-every-method)).
- Every listener implements `onSuccess(response)` and `onFailure(SdkException)`. Always override
  both. A listener that does not override `onFailure` ignores every failure without any message, and
  the customer would wait for a result that never comes. Do not use the deprecated `onApiError` and
  `onException`.
- `getIinDetails` takes `(partialCreditCardNumber, listener, paymentContext)` in the listener variant.
- Kotlin properties are getters in Java: `product.getLogo()`, `field.isRequired()`,
  `field.getShouldObfuscate()`, `response.getCoBrands()`, `result.getHttpStatusCode()`. Kotlin does not
  declare exceptions, so Java does not force you to catch them, and `catch (ResponseException e)`
  around a `...Sync` call does not compile: catch `Exception` and test it with `instanceof`.
- Nullable Kotlin types can be `null` in Java; check them (`getDisplayOrder()`, `getId()`,
  `getPaymentProductId()` of an IIN co-brand).
- `SdkConfiguration` has no default values for Java: pass all four values.
- Because both methods must be overridden, write the listeners as anonymous classes, not lambdas.

## 1. Create the SDK and the payment context

<!-- region: initialise-sdk-java -->
```java
public static final class CheckoutSession {
    public final OnlinePaymentsSdk sdk;
    public final String assetUrl; // from the session data: needed to build logo URLs

    CheckoutSession(OnlinePaymentsSdk sdk, String assetUrl) {
        this.sdk = sdk;
        this.assetUrl = assetUrl;
    }
}

// Call once per checkout. Your backend creates new session data with Create Session; it holds no
// secret. Never put the API key or secret in the app.
public static void createCheckoutSession(
        Context context, boolean isProduction, MerchantBackend.Callback<CheckoutSession> callback) {
    MerchantBackend.fetchSessionData(new MerchantBackend.Callback<SessionData>() {
        @Override
        public void onSuccess(SessionData sessionData) {
            // Throws a ConfigurationException when a session value is blank or clientApiUrl is not
            // a valid URL.
            OnlinePaymentsSdk sdk = new OnlinePaymentsSdk(
                    sessionData,
                    // The SDK keeps the context: pass the application context, never an Activity.
                    context.getApplicationContext(),
                    // From Java, pass all four values: environmentIsProduction (true in release
                    // builds: it selects the Google Pay PRODUCTION environment), your app's name and
                    // version, sdkIdentifier (null) and loggingEnabled, which must be false in production.
                    new SdkConfiguration(isProduction, "MyShopApp/1.0.0", null, false));
            callback.onSuccess(new CheckoutSession(sdk, sessionData.getAssetUrl()));
        }

        @Override
        public void onError(Exception error) {
            callback.onError(error);
        }
    });
}
```

<!-- region: payment-context-java -->
```java
// The payment context is passed to most SDK calls. Use the real values of the order being paid: the
// payment products, surcharge and currency conversion depend on them. A PaymentContextWithAmount is
// a PaymentContext that also works for getIinDetails.
public static PaymentContextWithAmount createPaymentContext(
        long totalInMinorUnits, // smallest unit of the currency: 1298 = EUR 12.98
        String currencyCode, // ISO 4217, for example "EUR"
        String countryCode) { // ISO 3166-1 alpha-2, for example "NL"
    return new PaymentContextWithAmount(
            new AmountOfMoneyWithAmount(totalInMinorUnits, currencyCode),
            countryCode,
            false); // isRecurring
}
```

## 2. List payment products and stored cards

<!-- region: list-payment-products-java -->
```java
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
```

## 3. Render the payment form

<!-- region: get-payment-product-java -->
```java
public static final class SelectedProduct {
    public final PaymentProduct paymentProduct;
    public final AccountOnFile accountOnFile; // null when no stored card was selected

    SelectedProduct(PaymentProduct paymentProduct, AccountOnFile accountOnFile) {
        this.paymentProduct = paymentProduct;
        this.accountOnFile = accountOnFile;
    }
}

public static void loadSelectedProduct(
        OnlinePaymentsSdk sdk,
        PaymentContext paymentContext,
        int paymentProductId, // for a stored card: accountOnFile.getPaymentProductId()
        String accountOnFileId, // null when no stored card was selected
        MerchantBackend.Callback<SelectedProduct> callback) {
    sdk.getPaymentProduct(paymentProductId, paymentContext, new PaymentProductResponseListener() {
        @Override
        public void onSuccess(PaymentProduct paymentProduct) {
            AccountOnFile accountOnFile =
                    accountOnFileId == null ? null : paymentProduct.getAccountOnFile(accountOnFileId);
            callback.onSuccess(new SelectedProduct(paymentProduct, accountOnFile));
        }

        @Override
        public void onFailure(SdkException exception) {
            callback.onError(exception);
        }
    });
}
```

<!-- region: describe-form-fields-java -->
```java
public static final class FieldSpec {
    public final String id;
    public final String label;
    public final String placeholder; // may be null
    public final boolean required;
    public final boolean obfuscate; // the input must hide the text, like a password
    public final PaymentProductFieldType type; // may be null
    public final int inputType; // an android.text.InputType value for the EditText
    public final String prefilledValue; // the stored value of the account on file, may be null
    public final boolean readOnly; // a field of the selected account on file that the customer cannot change

    FieldSpec(String id, String label, String placeholder, boolean required, boolean obfuscate,
              PaymentProductFieldType type, int inputType, String prefilledValue, boolean readOnly) {
        this.id = id;
        this.label = label;
        this.placeholder = placeholder;
        this.required = required;
        this.obfuscate = obfuscate;
        this.type = type;
        this.inputType = inputType;
        this.prefilledValue = prefilledValue;
        this.readOnly = readOnly;
    }
}

// Build the form from the fields the API returns. Do not hard-code fields, masks or validation
// rules: they differ per payment product and per merchant configuration. The SDK returns the
// fields sorted by display order.
public static List<FieldSpec> describeFormFields(PaymentProduct paymentProduct, AccountOnFile accountOnFile) {
    List<FieldSpec> specs = new ArrayList<>();
    for (PaymentProductField field : paymentProduct.getFields()) {
        specs.add(new FieldSpec(
                field.getId(),
                field.getLabel(),
                field.getPlaceholder(),
                field.isRequired(),
                field.getShouldObfuscate(),
                field.getType(),
                androidInputType(field),
                accountOnFile == null ? null : accountOnFile.getValue(field.getId()),
                accountOnFile != null && !accountOnFile.isWritable(field.getId())));
    }
    return specs;
}

private static int androidInputType(PaymentProductField field) {
    PreferredInputType preferred = field.getDisplayHints().getPreferredInputType();
    // A field with a mask (spaces, slashes) needs a text input type: a numeric input type removes
    // the characters of the mask from the text.
    boolean numeric = field.getDisplayHints().getMask() == null
            && preferred == PreferredInputType.INTEGER_KEYBOARD;

    int base;
    if (numeric) {
        base = InputType.TYPE_CLASS_NUMBER;
    } else if (preferred == PreferredInputType.EMAIL_ADDRESS_KEYBOARD) {
        base = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    } else if (preferred == PreferredInputType.PHONE_NUMBER_KEYBOARD) {
        base = InputType.TYPE_CLASS_PHONE;
    } else {
        base = InputType.TYPE_CLASS_TEXT;
    }

    if (field.getShouldObfuscate()) {
        return base | (numeric ? InputType.TYPE_NUMBER_VARIATION_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
    if (base == InputType.TYPE_CLASS_TEXT) {
        // Keep the keyboard from learning or suggesting what the customer types.
        return base | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
    }
    return base;
}
```

## 4. Detect the card brand

`getIinDetails` allows one lookup at a time: the detector below queues the latest input while a
lookup runs. For what the statuses mean, see [flows.md](flows.md#5-detect-the-card-brand).

<!-- region: iin-details-java -->
```java
public static final class CardDetection {
    public enum Kind {
        NUMBER_TOO_SHORT,
        SUPPORTED,
        NOT_ALLOWED, // a known card, but not accepted for this payment
        UNKNOWN // see the comment in CardDetector
    }

    public final Kind kind;
    public final Integer paymentProductId; // only for SUPPORTED
    public final List<Integer> coBrandProductIds; // co-branded card: let the customer choose the brand

    CardDetection(Kind kind, Integer paymentProductId, List<Integer> coBrandProductIds) {
        this.kind = kind;
        this.paymentProductId = paymentProductId;
        this.coBrandProductIds = coBrandProductIds;
    }
}

// Create one CardDetector per checkout and call detect() while the customer types the card
// number. Call it on the main thread: the callbacks of the SDK run there.
public static final class CardDetector {
    public interface Listener {
        void onDetected(CardDetection detection);

        void onError(SdkException error);
    }

    private final OnlinePaymentsSdk sdk;
    private final PaymentContextWithAmount paymentContext; // getIinDetails needs the amount
    private final Listener listener;
    private boolean lookupRunning;
    private String queuedDigits;

    public CardDetector(OnlinePaymentsSdk sdk, PaymentContextWithAmount paymentContext, Listener listener) {
        this.sdk = sdk;
        this.paymentContext = paymentContext;
        this.listener = listener;
    }

    public void detect(String cardNumberInput) {
        String digits = cardNumberInput.replaceAll("\\D", "");
        if (digits.length() < 6) {
            // The SDK would answer NOT_ENOUGH_DIGITS without a request.
            listener.onDetected(new CardDetection(CardDetection.Kind.NUMBER_TOO_SHORT, null, null));
            return;
        }
        // The SDK allows one IIN lookup at a time: a second call while one is running fails with
        // an IllegalStateSdkException. Keep the latest input and look it up afterwards.
        if (lookupRunning) {
            queuedDigits = digits;
            return;
        }
        lookup(digits);
    }

    private void lookup(String digits) {
        lookupRunning = true;
        // The SDK sends only the first 8 digits.
        sdk.getIinDetails(digits, new IinLookupResponseListener() {
            @Override
            public void onSuccess(IinDetailsResponse response) {
                lookupRunning = false;
                listener.onDetected(toDetection(response));
                runQueuedLookup();
            }

            @Override
            public void onFailure(SdkException exception) {
                lookupRunning = false;
                // A ResponseException (for example an expired session) ends up here: see error handling.
                listener.onError(exception);
                runQueuedLookup();
            }
        }, paymentContext);
    }

    private void runQueuedLookup() {
        if (queuedDigits != null) {
            String digits = queuedDigits;
            queuedDigits = null;
            lookup(digits);
        }
    }

    private static CardDetection toDetection(IinDetailsResponse response) {
        IinDetailStatus status = response.getStatus();
        if (status == IinDetailStatus.SUPPORTED && response.getPaymentProductId() != null) {
            List<Integer> coBrandProductIds = new ArrayList<>();
            if (response.getCoBrands() != null) {
                for (IinDetail coBrand : response.getCoBrands()) {
                    if (coBrand != null
                            && Boolean.TRUE.equals(coBrand.isAllowedInContext())
                            && coBrand.getPaymentProductId() != null) {
                        coBrandProductIds.add(Integer.valueOf(coBrand.getPaymentProductId()));
                    }
                }
            }
            return new CardDetection(
                    CardDetection.Kind.SUPPORTED,
                    Integer.valueOf(response.getPaymentProductId()),
                    coBrandProductIds);
        }
        if (status == IinDetailStatus.EXISTING_BUT_NOT_ALLOWED) {
            return new CardDetection(CardDetection.Kind.NOT_ALLOWED, null, null);
        }
        if (status == IinDetailStatus.NOT_ENOUGH_DIGITS) {
            return new CardDetection(CardDetection.Kind.NUMBER_TOO_SHORT, null, null);
        }
        // UNKNOWN: the card number matches no payment product of your account (the API answered
        // HTTP 404). Do not block the form. UNSUPPORTED is declared but never returned.
        return new CardDetection(CardDetection.Kind.UNKNOWN, null, null);
    }
}
```

## 5. Format and validate input

<!-- region: create-payment-request-java -->
```java
public static PaymentRequest createPaymentRequest(
        PaymentProduct paymentProduct,
        AccountOnFile accountOnFile, // null when no stored card was selected
        boolean customerWantsToStoreCard) {
    // Ask to store the card only when the product allows it and the customer agreed.
    boolean tokenize = customerWantsToStoreCard && Boolean.TRUE.equals(paymentProduct.getAllowsTokenization());
    return new PaymentRequest(paymentProduct, accountOnFile, tokenize);
}
```

<!-- region: mask-input-java -->
```java
// Stores the text the customer typed in the PaymentRequest and returns it with the field's mask.
// setValue removes the mask before it stores the value, so "4111 1111" is stored as "41111111".
// An empty string clears the value. Do not call it for a read-only field of the selected account
// on file: setValue throws an InvalidArgumentException for those (see FieldSpec.readOnly).
public static String formatFieldInput(PaymentRequest paymentRequest, String fieldId, String input) {
    PaymentRequestField field = paymentRequest.getField(fieldId);
    field.setValue(input);
    String masked = field.getMaskedValue();
    return masked == null ? "" : masked;
}

// Add to the EditText of a writable field: editText.addTextChangedListener(new PaymentFieldWatcher(...)).
// After the mask is applied, the cursor moves to the end of the text.
public static final class PaymentFieldWatcher implements TextWatcher {
    private final PaymentRequest paymentRequest;
    private final String fieldId;
    private boolean updating;

    public PaymentFieldWatcher(PaymentRequest paymentRequest, String fieldId) {
        this.paymentRequest = paymentRequest;
        this.fieldId = fieldId;
    }

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (updating) {
            return;
        }
        String masked = formatFieldInput(paymentRequest, fieldId, editable.toString());
        if (!masked.equals(editable.toString())) {
            updating = true;
            editable.replace(0, editable.length(), masked);
            updating = false;
        }
    }
}
```

<!-- region: validate-field-java -->
```java
// Call when a field loses focus. Returns the messages to show next to the field. The texts are in
// English: to translate them, map ValidationErrorMessage.getType() (the name of a
// ValidationRuleType, for example "LUHN" or "REQUIRED") to your own texts.
public static List<String> fieldErrors(PaymentRequest paymentRequest, String fieldId) {
    List<String> messages = new ArrayList<>();
    for (ValidationErrorMessage error : paymentRequest.getField(fieldId).validate().getErrors()) {
        messages.add(error.getErrorMessage());
    }
    return messages;
}
```

<!-- region: validate-payment-request-java -->
```java
// Call before encrypting. Returns the error messages per field id; empty when the request is valid.
// With an account on file, the SDK validates the fields the customer must enter (MUST_WRITE) and
// the fields the customer changed.
public static Map<String, List<String>> validatePaymentRequest(PaymentRequest paymentRequest) {
    Map<String, List<String>> errorsByField = new LinkedHashMap<>();
    for (ValidationErrorMessage error : paymentRequest.validate().getErrors()) {
        List<String> messages = errorsByField.get(error.getPaymentProductFieldId());
        if (messages == null) {
            messages = new ArrayList<>();
            errorsByField.put(error.getPaymentProductFieldId(), messages);
        }
        messages.add(error.getErrorMessage());
    }
    return errorsByField;
}
```

## 6. Surcharge and currency conversion (optional)

Pass at most the first 8 digits of the card number: these requests are not encrypted. The properties
of `Surcharge` and `CurrencyConversionResponse` that the API leaves out are `null`: check the result
first, or read them through `getProposalOrNull()`, `getDccSessionIdOrNull()`, `getNetAmountOrNull()`,
`getSurchargeAmountOrNull()` and `getTotalAmountOrNull()`, which return `null` as a declared value.

<!-- region: card-source-java -->
```java
// The card for a surcharge or currency conversion request. These requests are not encrypted: send
// only the first 8 digits of a card number being entered (at least 6) with the product id from the
// IIN details, never the complete number. The SDK sends what you pass. For a stored card, send the
// account on file id (the token) instead.
public static final class QuoteCard {
    final String firstDigits; // null for a stored card
    final int paymentProductId;
    final String accountOnFileId; // null for a card being entered

    private QuoteCard(String firstDigits, int paymentProductId, String accountOnFileId) {
        this.firstDigits = firstDigits;
        this.paymentProductId = paymentProductId;
        this.accountOnFileId = accountOnFileId;
    }

    public static QuoteCard entered(String cardNumberDigits, int paymentProductId) {
        String firstDigits = cardNumberDigits.length() > 8 ? cardNumberDigits.substring(0, 8) : cardNumberDigits;
        return new QuoteCard(firstDigits, paymentProductId, null);
    }

    public static QuoteCard stored(String accountOnFileId) {
        return new QuoteCard(null, 0, accountOnFileId);
    }
}

public interface OfferListener<T> {
    void onOffer(T offer); // offer is null when there is nothing to offer

    void onError(SdkException error); // see error handling
}
```

<!-- region: surcharge-calculation-java -->
```java
public static final class SurchargeOffer {
    public final AmountOfMoney surchargeAmount;
    public final AmountOfMoney totalAmount;

    SurchargeOffer(AmountOfMoney surchargeAmount, AmountOfMoney totalAmount) {
        this.surchargeAmount = surchargeAmount;
        this.totalAmount = totalAmount;
    }
}

// Call once the card is known. onOffer receives null when no surcharge applies.
// netAmount is the order amount from the payment context: it does not include the surcharge. The
// platform adds the surcharge when your backend creates the payment with
// order.surchargeSpecificInput.mode set to "on-behalf-of".
public static void getSurcharge(
        OnlinePaymentsSdk sdk, AmountOfMoney netAmount, QuoteCard card, OfferListener<SurchargeOffer> listener) {
    SurchargeCalculationResponseListener responseListener = new SurchargeCalculationResponseListener() {
        @Override
        public void onSuccess(SurchargeCalculationResponse response) {
            List<Surcharge> surcharges = response.getSurcharges();
            if (surcharges == null || surcharges.isEmpty()) {
                listener.onOffer(null);
                return;
            }
            Surcharge surcharge = surcharges.get(0);

            // Check the result before reading the amounts: the API leaves them out when no
            // surcharge applies, and they are then null.
            if (surcharge.getResult() != SurchargeResult.OK) {
                listener.onOffer(null);
                return;
            }

            // Show both amounts to the customer before they pay.
            listener.onOffer(new SurchargeOffer(surcharge.getSurchargeAmount(), surcharge.getTotalAmount()));
        }

        @Override
        public void onFailure(SdkException exception) {
            listener.onError(exception);
        }
    };

    if (card.accountOnFileId != null) {
        sdk.getSurchargeCalculation(netAmount, card.accountOnFileId, responseListener);
    } else {
        sdk.getSurchargeCalculation(
                netAmount, card.firstDigits, String.valueOf(card.paymentProductId), responseListener);
    }
}
```

<!-- region: currency-conversion-java -->
```java
public static final class CurrencyConversionOffer {
    public final String dccSessionId;
    public final DccProposal proposal;

    CurrencyConversionOffer(String dccSessionId, DccProposal proposal) {
        this.dccSessionId = dccSessionId;
        this.proposal = proposal;
    }
}

// Call once the card is known. onOffer receives null when there is no offer: then pay in your
// currency and send no currency conversion data.
// amountToPay is the order amount, or the surcharge's totalAmount when a surcharge applies.
public static void getCurrencyConversionOffer(
        OnlinePaymentsSdk sdk,
        AmountOfMoney amountToPay,
        QuoteCard card,
        OfferListener<CurrencyConversionOffer> listener) {
    CurrencyConversionResponseListener responseListener = new CurrencyConversionResponseListener() {
        @Override
        public void onSuccess(CurrencyConversionResponse response) {
            // INVALID_CARD, INVALID_MERCHANT, NO_RATE and NOT_AVAILABLE mean: no offer.
            // dccSessionId is then empty and the response has no proposal (it is null): check
            // the result before reading response.getProposal().
            if (response.getResult().getResult() != ConversionResultType.ALLOWED) {
                listener.onOffer(null);
                return;
            }

            // Show two options with the same size, colour and font: proposal.getBaseAmount()
            // (your currency) and proposal.getTargetAmount() (the card's currency), with
            // proposal.getRate().getExchangeRate(), proposal.getRate().getMarkUpRate() (a
            // percentage: 3.12 means 3.12 %) and proposal.getDisclaimerDisplay(). Preselect neither
            // option and do not ask a yes/no question: the customer must actively choose.
            listener.onOffer(new CurrencyConversionOffer(response.getDccSessionId(), response.getProposal()));
        }

        @Override
        public void onFailure(SdkException exception) {
            listener.onError(exception);
        }
    };

    if (card.accountOnFileId != null) {
        sdk.getCurrencyConversionQuote(amountToPay, card.accountOnFileId, responseListener);
    } else {
        sdk.getCurrencyConversionQuote(
                amountToPay, card.firstDigits, String.valueOf(card.paymentProductId), responseListener);
    }
}
```

<!-- region: currency-conversion-choice-java -->
```java
// Call when the customer actively selects one of the two options. Pass the result to
// MerchantBackend.submitEncryptedPayment, also when the customer chose your currency. Once the
// customer chose your currency, do not offer the conversion again in this checkout.
public static MerchantBackend.CurrencyConversionChoice chooseCurrency(
        CurrencyConversionOffer offer, boolean payInCardCurrency) {
    return new MerchantBackend.CurrencyConversionChoice(offer.dccSessionId, payInCardCurrency);
}
```

## 7. Encrypt and send to your backend

<!-- region: encrypt-payment-request-java -->
```java
public interface PaymentListener {
    // Show these next to the fields. Nothing was sent.
    void onInputInvalid(Map<String, List<String>> errorsByField);

    void onSubmitted(MerchantBackend.MerchantPaymentResponse result);

    void onError(Exception error); // see error handling
}

public static void payWithEncryptedRequest(
        OnlinePaymentsSdk sdk,
        PaymentRequest paymentRequest,
        String orderId,
        MerchantBackend.CurrencyConversionChoice currencyConversion, // null unless a conversion offer was shown
        PaymentListener listener) {
    Map<String, List<String>> errorsByField = ValidationAndMasking.validatePaymentRequest(paymentRequest);
    if (!errorsByField.isEmpty()) {
        listener.onInputInvalid(errorsByField); // do not encrypt
        return;
    }

    // The only way card data leaves the device: encrypted by the SDK. It validates the request
    // again and fails with an InvalidArgumentException when it is not valid.
    // Never send, log or store the raw field values.
    sdk.encryptPaymentRequest(paymentRequest, new PaymentRequestPreparedListener() {
        @Override
        public void onSuccess(EncryptedRequest encryptedRequest) {
            // Your backend passes encryptedCustomerInput to Create Payment and decides the outcome.
            MerchantBackend.submitEncryptedPayment(orderId, encryptedRequest, currencyConversion,
                    new MerchantBackend.Callback<MerchantBackend.MerchantPaymentResponse>() {
                        @Override
                        public void onSuccess(MerchantBackend.MerchantPaymentResponse result) {
                            listener.onSubmitted(result);
                        }

                        @Override
                        public void onError(Exception error) {
                            listener.onError(error);
                        }
                    });
        }

        @Override
        public void onFailure(SdkException exception) {
            listener.onError(exception);
        }
    });
}
```

## 8. Handle the result and the return from 3-D Secure

<!-- region: pending-payment-store-java -->
```java
// Remembers the order that waits for its result while the customer is on the 3-D Secure page.
// Persist it, because the system can stop the app meanwhile. Store only the order id: never card
// data or session data.
public interface PendingPaymentStore {
    String readOrderId(); // null when no payment waits

    void saveOrderId(String orderId);

    void clear();
}

// commit() writes before it returns (apply() writes later): the app can be stopped as soon as the
// browser opens.
@SuppressLint("ApplySharedPref")
public static final class SharedPreferencesPendingPaymentStore implements PendingPaymentStore {
    private final SharedPreferences preferences;

    public SharedPreferencesPendingPaymentStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences("pending_payment", Context.MODE_PRIVATE);
    }

    @Override
    public String readOrderId() {
        return preferences.getString("orderId", null);
    }

    @Override
    public void saveOrderId(String orderId) {
        preferences.edit().putString("orderId", orderId).commit();
    }

    @Override
    public void clear() {
        preferences.edit().remove("orderId").commit();
    }
}
```

<!-- region: handle-payment-result-java -->
```java
public static void handlePaymentResult(
        Activity activity,
        MerchantBackend.MerchantPaymentResponse result,
        String orderId,
        PendingPaymentStore pendingPayments) {
    if (result.redirectUrl != null) {
        // For example 3-D Secure or a third-party payment page. Open the URL exactly as received:
        // do not build or change it. Your backend sets the payment's returnUrl to a deep link of
        // this app, so the customer comes back to the app afterwards.
        pendingPayments.saveOrderId(orderId);
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result.redirectUrl)));
        } catch (ActivityNotFoundException e) {
            pendingPayments.clear();
            UiPlaceholders.showStatusUnknown(); // the payment exists; let the customer check the order later
        }
        return;
    }

    // The status comes from your backend. The app never decides that a payment succeeded.
    UiPlaceholders.showPaymentOutcome(result.status);
}
```

<!-- region: handle-return-deep-link-java -->
```java
// The start of the returnUrl your backend sets in Create Payment. Your manifest has an intent filter
// for it (see references/android.md).
public static final String RETURN_URL_PREFIX = "myshop://payment-return";

// Runs when the customer comes back. Acts only when a payment waits for its result: the same link
// can be delivered again, for example after the Activity is recreated.
public static void onPaymentReturn(PendingPaymentStore pendingPayments) {
    String orderId = pendingPayments.readOrderId();
    if (orderId == null) {
        return;
    }
    pendingPayments.clear();

    // Do not read the outcome from the link: ask your backend for the status.
    MerchantBackend.fetchPaymentStatus(orderId, new MerchantBackend.Callback<MerchantBackend.MerchantPaymentResponse>() {
        @Override
        public void onSuccess(MerchantBackend.MerchantPaymentResponse result) {
            UiPlaceholders.showPaymentOutcome(result.status);
        }

        @Override
        public void onError(Exception error) {
            // The outcome is unknown, not failed: let the customer check the order later.
            UiPlaceholders.showStatusUnknown();
        }
    });
}

// Handle the link in the Activity that is the root of the app (the one with the launcher intent
// filter and android:launchMode="singleTask" or "singleTop"), not in the checkout screen: the
// customer can come back while another screen is shown.
public static class MainActivity extends ComponentActivity {
    private PendingPaymentStore pendingPayments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pendingPayments = new SharedPreferencesPendingPaymentStore(this);
        // If the system stopped the app while the customer was on the 3-D Secure page, the deep link
        // starts the app again: the launch intent holds it. onPaymentReturn ignores an old link
        // because no payment waits any more.
        handleReturnLink(getIntent());
    }

    // The app is running: the link arrives here.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleReturnLink(intent);
    }

    private void handleReturnLink(Intent intent) {
        Uri link = intent == null ? null : intent.getData();
        if (link != null && link.toString().startsWith(RETURN_URL_PREFIX)) {
            onPaymentReturn(pendingPayments);
        }
    }
}
```

## 9. Errors

See [errors.md](errors.md) for what each exception means. The listener's `onFailure` receives an
`SdkException`; classify it like this:

<!-- region: handle-sdk-error-java -->
```java
public enum ErrorAction {
    SHOW_VALIDATION_ERRORS, // the customer can fix the input
    RETRY, // a network problem, or a call that overlapped another: the same call can be tried again
    RESTART_CHECKOUT, // get new session data from your backend and create a new OnlinePaymentsSdk
    INTEGRATION_ERROR // a bug in the integration: fix the code
}

// Call with the SdkException that a listener's onFailure receives. Kotlin does not declare the
// exceptions, so around the ...Sync methods you can only catch Exception and test it with instanceof.
public static ErrorAction classifySdkError(Throwable error) {
    // Invalid input: encryptPaymentRequest with a request that does not validate (the metadata
    // entry "data" is the ValidationResult), setValue on a READ_ONLY field, getField with an unknown id.
    if (error instanceof InvalidArgumentException) {
        return ErrorAction.SHOW_VALIDATION_ERRORS;
    }

    // No response, a network error, or a response that is not JSON. error.getCause() has details.
    if (error instanceof CommunicationException) {
        return ErrorAction.RETRY;
    }

    // getIinDetails while another IIN lookup is running.
    if (error instanceof IllegalStateSdkException) {
        return ErrorAction.RETRY;
    }

    // The Client API returned an error, for example because the session is no longer valid, or
    // getHttpStatusCode() 404 when no payment product is available.
    if (error instanceof ResponseException) {
        ResponseException response = (ResponseException) error;
        // Log only the status code, error id and error codes: never payment data.
        StringBuilder codes = new StringBuilder();
        List<ApiErrorItem> items = response.getApiError().getErrors();
        if (items != null) {
            for (ApiErrorItem item : items) {
                codes.append(item.getErrorCode()).append(',');
            }
        }
        Log.w("Checkout", "Client API error " + response.getHttpStatusCode() + " "
                + response.getApiError().getErrorId() + " " + codes);
        return ErrorAction.RESTART_CHECKOUT;
    }

    // Encryption failed, or encryptTokenRequest without a payment product id.
    if (error instanceof EncryptionException) {
        return ErrorAction.RESTART_CHECKOUT;
    }

    // A blank session value or an invalid clientApiUrl when the SDK is created.
    if (error instanceof ConfigurationException) {
        return ErrorAction.INTEGRATION_ERROR;
    }

    return ErrorAction.INTEGRATION_ERROR;
}
```

## Tokenization without a payment

<!-- region: encrypt-token-request-java -->
```java
// There is no order amount when a card is stored without a payment: use an amount of 1, the
// smallest unit of the currency (1 cent for EUR). Do not use 0: the Client API answers with an error.
// Use this context for getIinDetails and getPaymentProduct to load the card product.
public static PaymentContextWithAmount createTokenizationContext(String currencyCode, String countryCode) {
    return new PaymentContextWithAmount(new AmountOfMoneyWithAmount(1, currencyCode), countryCode, false);
}

private static final String[] CARD_FIELD_IDS = {"cardNumber", "cardholderName", "expiryDate", "cvv"};

public interface StoreCardListener {
    // Show these next to the fields. Nothing was sent.
    void onInvalid(Map<String, List<String>> errorsByField);

    void onStored(String token);

    void onError(Exception error); // see error handling
}

public static void storeCard(
        OnlinePaymentsSdk sdk,
        PaymentProduct cardProduct, // from sdk.getPaymentProduct for the detected card product
        Map<String, String> form, // field id -> the value as shown in the input, with or without mask
        StoreCardListener listener) {
    // Remove the masks first ("12/26" becomes "1226"): the validation rules and the token request
    // expect values without formatting. Then validate: CreditCardTokenRequest applies no validation.
    Map<String, String> values = new LinkedHashMap<>();
    Map<String, List<String>> errorsByField = new LinkedHashMap<>();
    for (String fieldId : CARD_FIELD_IDS) {
        String value = form.get(fieldId) == null ? "" : form.get(fieldId);
        PaymentProductField field = cardProduct.getField(fieldId);
        if (field != null) {
            String unmasked = field.removeMask(value);
            value = unmasked == null ? value : unmasked;
            List<String> messages = new ArrayList<>();
            for (ValidationErrorMessage error : field.validate(value)) {
                messages.add(error.getErrorMessage());
            }
            if (!messages.isEmpty()) {
                errorsByField.put(fieldId, messages);
            }
        }
        values.put(fieldId, value);
    }
    if (!errorsByField.isEmpty()) {
        listener.onInvalid(errorsByField);
        return;
    }

    CreditCardTokenRequest tokenRequest = new CreditCardTokenRequest(
            values.get("cardNumber"),
            values.get("cardholderName"),
            values.get("expiryDate"),
            values.get("cvv"),
            // Required: encryption fails with an EncryptionException without it.
            cardProduct.getId());

    sdk.encryptTokenRequest(tokenRequest, new PaymentRequestPreparedListener() {
        @Override
        public void onSuccess(EncryptedRequest encryptedRequest) {
            // Your backend creates the token with the encrypted data.
            MerchantBackend.submitEncryptedToken(encryptedRequest, new MerchantBackend.Callback<String>() {
                @Override
                public void onSuccess(String token) {
                    listener.onStored(token);
                }

                @Override
                public void onError(Exception error) {
                    listener.onError(error);
                }
            });
        }

        @Override
        public void onFailure(SdkException exception) {
            listener.onError(exception);
        }
    });
}
```

## Google Pay

See [flows.md](flows.md#google-pay) for what the SDK does and does not do. The Java code needs
`com.google.android.gms:play-services-wallet` and `androidx.activity:activity` in your app.

<!-- region: google-pay-configuration-java -->
```java
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
```

<!-- region: google-pay-request-java -->
```java
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
```

<!-- region: google-pay-launcher-java -->
```java
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
```

<!-- region: google-pay-payment-request-java -->
```java
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
```

## Your backend

<!-- region: merchant-backend-java -->
```java
// The URL, paths and JSON shapes are placeholders for your own backend; the SDK never calls them.
public final class MerchantBackend {
    private static final String BACKEND_URL = "https://shop.example.com";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** The payment status as your backend reports it. */
    public enum PaymentStatus { SUCCESS, PENDING, FAILED }

    /** What your backend returns after it created the payment, or when the app asks for the status. */
    public static final class MerchantPaymentResponse {
        public final PaymentStatus status;
        /** Not null when the customer must be redirected, for example for 3-D Secure. */
        public final String redirectUrl;

        MerchantPaymentResponse(PaymentStatus status, String redirectUrl) {
            this.status = status;
            this.redirectUrl = redirectUrl;
        }
    }

    /**
     * The customer's currency conversion choice. Your backend passes both fields to Create Payment in
     * cardPaymentMethodSpecificInput.currencyConversion. Both are mandatory once a quote was shown.
     */
    public static final class CurrencyConversionChoice {
        public final String dccSessionId;
        /** true: pay in the card's currency; false: pay in your currency. */
        public final boolean acceptedByUser;

        public CurrencyConversionChoice(String dccSessionId, boolean acceptedByUser) {
            this.dccSessionId = dccSessionId;
            this.acceptedByUser = acceptedByUser;
        }
    }

    /** The result is delivered on the main thread. */
    public interface Callback<T> {
        void onSuccess(T result);

        void onError(Exception error);
    }

    // Never log the body you send: it holds the encrypted customer input.
    private static JSONObject postJson(String path, JSONObject body) throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BACKEND_URL + path).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            if (code < 200 || code > 299) {
                throw new IOException(path + " failed with HTTP " + code);
            }
            try (InputStream in = connection.getInputStream()) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    bytes.write(buffer, 0, read);
                }
                return new JSONObject(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
            }
        } finally {
            connection.disconnect();
        }
    }

    private static <T> void run(Callable<T> work, Callback<T> callback) {
        EXECUTOR.execute(() -> {
            try {
                T result = work.call();
                MAIN.post(() -> callback.onSuccess(result));
            } catch (Exception error) {
                MAIN.post(() -> callback.onError(error));
            }
        });
    }

    private static MerchantPaymentResponse toPaymentResponse(JSONObject json) throws JSONException {
        return new MerchantPaymentResponse(
                PaymentStatus.valueOf(json.getString("status").toUpperCase(Locale.ROOT)),
                json.isNull("redirectUrl") ? null : json.getString("redirectUrl"));
    }

    /** Your backend creates a new client session for this checkout (Create Session) and returns its data. */
    public static void fetchSessionData(Callback<SessionData> callback) {
        run(() -> {
            JSONObject json = postJson("/api/checkout/session", new JSONObject());
            return new SessionData(
                    json.getString("clientSessionId"),
                    json.getString("customerId"),
                    json.getString("clientApiUrl"),
                    json.getString("assetUrl"));
        }, callback);
    }

    /** Your backend creates the payment (Create Payment) with the encrypted customer input. */
    public static void submitEncryptedPayment(
            String orderId,
            EncryptedRequest encryptedRequest,
            CurrencyConversionChoice currencyConversion, // null unless a currency conversion offer was shown
            Callback<MerchantPaymentResponse> callback) {
        run(() -> {
            JSONObject body = new JSONObject()
                    .put("orderId", orderId)
                    .put("encryptedCustomerInput", encryptedRequest.getEncryptedCustomerInput())
                    .put("encodedClientMetaInfo", encryptedRequest.getEncodedClientMetaInfo());
            if (currencyConversion != null) {
                body.put("currencyConversion", new JSONObject()
                        .put("dccSessionId", currencyConversion.dccSessionId)
                        .put("acceptedByUser", currencyConversion.acceptedByUser));
            }
            return toPaymentResponse(postJson("/api/checkout/payment", body));
        }, callback);
    }

    /** Your backend reads the payment status, for example after the customer returned from 3-D Secure. */
    public static void fetchPaymentStatus(String orderId, Callback<MerchantPaymentResponse> callback) {
        run(() -> toPaymentResponse(postJson("/api/checkout/status", new JSONObject().put("orderId", orderId))),
                callback);
    }

    /** Your backend creates a payment token (Create Token) from the encrypted card data. Returns the token id. */
    public static void submitEncryptedToken(EncryptedRequest encryptedRequest, Callback<String> callback) {
        run(() -> {
            JSONObject body = new JSONObject()
                    .put("encryptedCustomerInput", encryptedRequest.getEncryptedCustomerInput())
                    .put("encodedClientMetaInfo", encryptedRequest.getEncodedClientMetaInfo());
            return postJson("/api/checkout/token", body).getString("token");
        }, callback);
    }
}
```
