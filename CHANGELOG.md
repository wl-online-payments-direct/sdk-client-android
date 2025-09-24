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
