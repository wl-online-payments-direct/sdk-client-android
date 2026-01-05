# 5.0.0

The SDK was internally refactored to be unified with Client SDKs in other technologies. This log states only externally
visible changes.
For usage example, check the [README.md](README.md) file.

## Changed

- The main entry `Session` class changed to `OnlinePaymentSdk`. It is instantiated with
  `OnlinePaymentSdk(sessionData, context, configuration?)` provided in the main SDK export.
- Fluent API added for domain models:
    - `PaymentProductField`:
        - `validate(value) `returns array of `ValidationErrorMessage` (previously `isValid()`).
        - Added methods: `getLabel`, `getPlaceholder`, `shouldObfuscate`, `isRequired`, `applyMask(value)`,
          `removeMask(value)`
    - `PaymentRequest`:
        - Constructor now requires `PaymentProduct` (previously optional).
        - `validate()` returns `ValidationResult` containing validation errors.
        - Field values managed through `PaymentRequestField` instances.
        - Setting `AccountOnFile` automatically clears non-writable field values set previously on the payment request.
        - Setting a value to READ_ONLY field throws `InvalidArgumentException`
- `PaymentProduct.id` is now of Int type, as this is how the API returns it. The corresponding methods for retrieving
  a payment product or product networks now accepts Int instead of String parameter:
- `OnlinePaymentsSdk` methods accept product Id as Int instead of String. These methods are changed:
    - `getPaymentProduct` and corresponding overloads and Sync method
    - `getPaymentProductNetworks` and corresponding overloads and Sync method
- `OnlinePaymentsSdk.getIinDetails` now accepts `PaymentContextWithAmount` since the amount is required for fetching IIN
  details.

## Added

- `PaymentProduct`: added methods and properties for field access:
    - `fields` - returns all fields
    - `requiredFields` - returns only required fields
    - `getField(id)` - returns specific field by ID
- `PaymentRequestField` - Internal class for field value management with methods:
    - `getValue()`, `setValue(value)`, `clearValue()`
    - `maskedValue()`, `type()`
    - `getId()`, `getLabel()`, `getPlaceholder()`, `isRequired()`, `shouldObfuscate`
    - `validate()`
- `PaymentRequest`: added methods:
    - `getField(id)` - returns `PaymentRequestField` for fluent API
    - `getValues()` - returns all unmasked values as object
    - `validate()` - validates entire request, returns `ValidationResult` (see below)
- `AccountOnFile`: added methods:
    - `value(id)` - get stored value for field
    - `requiredAttributes()` -get attributes that must be provided
    - `isWritable(fieldId)` - check if field can be modified
- `BasicPaymentProduct`: added method
    - `accountOnFile(id)` - retrieves specific account on file
- `ValidationResult` - New class wrapping validation results with:
    - `isValid` - boolean indicating if validation passed
    - `errors` - array of `ValidationErrorMessage`
- Error (exception) hierarchy:
    - `SDKException` - Abstract base class for all SDK exceptions
    - `ConfigurationException` - Invalid session/config data
    - `InvalidArgumentException` - Invalid method arguments
    - `ResponseException` - API request failures (includes HTTP status)
    - `EncryptionException` - Encryption or validation failures
    - `IllegalStateExcption` - Operation called at wrong time
    - `CommunicationException` - For handling communication exceptions.
- `SdkConfiguration` parameter with `appIdentifier`, old `sdkIdentifier`, `environmentIsProduction` and `loggingEnabled`
  for identifying and configuring application.

## Removed:

- `BasicPaymentItems` class removed.
- `getBasicPaymentItems()` method removed from facade. Use `getBasicPaymentProducts()` instead.
- `Session` class removed. Use `OnlinePaymentSdk` constructor to create `OnlinePaymentSdk` instance. The interface of
  the previous `Session` and new `OnlinePaymentSdk` instances is mostly the same.
- `setLoggingEnabled` and `getLogginEnabled` methods removed from the facade
- `SdkConfiguration` no longer checks and parses `sdkIdentifier`.

# 4.3.1

## Changed

- Fixed supported TLS versions on older Android devices.

# 4.3.0

## Changed

- Updated dependencies to fix vulnerabilities
- Increased minSdk to 23 because of dependencies
- Fixed code issues

# 4.2.0

## Added

A new class `CreditCardTokenRequest` has been added. It is used to create a request for a credit
card tokenization. It works similarly to the `PaymentRequest` class, but it does not validate
values, nor unmasks them. Encryptor class is extended with a new method `encryptTokenRequest`.
You can use it to get the encrypted data for the tokenization request.

## Changed

- Added support for SDK 35
- Upgraded gradle to 8.13, kotlin to 2.2.21.

# 4.1.1

## Changed

* Updated the expiry date validator to support dates in MMyy and MMyyyy formats.

# 4.1.0

## Changed

* Handling coroutines has been change to better support consumer handling threads.

# 4.0.1

Fix broken release package.

# 4.0.0

The whole SDK is converted to Kotlin. For the clarity of code, some getters are removed and
instead, properties are used.
When calling from Kotlin, use property names, for example `paymentContext.amountOfMoney`.
When calling from Java, you can still use getters, e.g. `paymentContext.getAmountOfMoney()`.

## Removed

Removed deprecated classes:

* `PaymentProductFieldDisplayElement`; no replacement is needed.
* `ValueMap`; no replacement is needed.
* `KeyValuePair`; use `AccountOnFileAttribute` instead.
* `Size`; no replacement is needed.
* `ImageUtil`; this class became deprecated when image methods were removed.

The following deprecated elements have been removed

* `FormElement.getValueMapping()`; no replacement needed.
* `FormElement.getFormElementType()`; use `getType()` instead. Keep in mind that the return
  type has been changed.
* `FormElement.setType()`; no replacement needed.
* `FormElement.ListType` enum; use `FormElement.Type` instead.
* `IinDetailsResponse.getCountryCodeString()` enum; use `countryCode` instead.
* `DisplayHintsPaymentItem.getLogo()`; use `getLogoUrl` instead and create Drawable yourself.
* `DisplayHintsProductFields.getLink()`; no replacement is needed.
* `BasicPaymentItem.getDisplayHints()`; use `getDisplayHintsList()` instead.
* `BasicPaymentProduct.getDisplayHints()`; use `getDisplayHintsList()` instead.
* `BasicPaymentProduct.getMinAmount()`; no replacement available, since this property is not
  returned from the API.
* `BasicPaymentProduct.getMaxAmount()`; no replacement available, since this property is not
  returned from the API.
* `BasicPaymentProduct.mobileIntegrationLevel()`; no replacement available, since this property
  is not returned from the API.
* `Tooltip.getImageURL()`, `Tooltip.getImage()`, `Tooltip.getImageDrawable()`; no replacement is
  available since the tooltip does not contain image anymore.
* `DataRestrictions.getValidator()`; use `getValidationRules()` instead.
* Validators `validate(String)`; use 'validate(PaymentRequest, String)` instead.
* `ValidationRuleLength.getMaskedMaxLength()`; no replacement is needed.
* `AmountOfMoney.getCurrencyCodeString()`; use `getCurrencyCode()` instead.
* `PaymentContext.getCountryCodeString()`; use `getCountryCode()` instead.
* `PaymentRequest.mergePaymentRequest()`; no replacement is needed.
* `PaymentProductField.validateValue(String)`; use `validateValue(paymentRequest: PaymentRequest)`
  instead.

## Changed

The following classes' constructors or methods have been made internal:

* `AbstractValidationRule`
* `DataRestrictions`
* `BasicPaymentProduct`
* `PaymentProduct.setPaymentProductFields()`
* For all validators

Because of this, the direct property access has been removed for some properties so you will have to use the getter
methods. For example, `ValidationRuleLength.maxLength` is not directly accessible. Instead, use
`ValidationRuleLength.getMaxLength()`.

Most POJO classes have been converted to Kotlin data classes for convenience and to better align
to Kotlin paradigm. This means that from Kotlin, you can now directly use properties, while in Java
you can still use getters and setters.

The method signature changed for the following methods:

* The parameter `Context` has been removed from all `Session` methods. It has been added to the constructor of
  the `Session` object since context should not change during a life cycle of a session.
* Methods `Session.getCurrencyConversionQuote` and `Session.getSurchargeCalculation` now accept `String` for the
  payment product ID instead of `Int` to be aligned with other methods.
* `AccountOnFile.getAttributes()`; the return type has changed to `MutableList<AccountOnFileAttribute>`
* `FormElement.getType()`; the return type has changed to "FormElement.Type".
* Validators `validate(paymentRequest: PaymentRequest, fieldId: String)`; the parameter `fieldId` cannot
  be null anymore.

The `Session` class methods now have asynchronous (suspend) methods in parallel with the versions with
listeners. They return successful response, or throw either:

* `ApiErrorException` (for exceptions thrown by the API, such as the bad request exception),
* `CommunicationException` (for exceptions regarding the API communication, like network error,
  forbidden requests and similar), or
* `ValidationException` (for exceptions regarding the internal request validations).

If you are calling from Kotlin code, you can use the suspend methods directly. For Java, use the
listener-based methods for asynchronous operations or use methods with the `Sync` suffix. These methods
will run synchronously which means they will block the thread.

The `StringFormatter` class has been made static.

Filtering of payment products that cannot be sent in the encrypted customer input has been added to
`C2sCommunicator`. The following methods are currently unsupported:

* Maestro
* Intersolve
* Sodexo & Sport Culture
* VVV Giftcard
