# Online Payments - Android SDK

The Online Payments Android SDK helps you accept payments in your Android app, supporting API level 21 and up, through
the Online Payments platform.

The SDK's primary function is establishing a secure channel between your Android app and our server. This channel
processes security credentials to guarantee the safe transit of your customers' data during the payment process.

**The Online Payments SDK helps you with:**

- Handling encryption of the payment context
- Convenient Java wrappers for API responses
- User-friendly formatting of payment data, such as card numbers and expiry dates
- Validating user input
- Determining the card's associated payment provider

## Table of Contents

- [Online Payments - Android SDK](#online-payments---android-sdk)
    - [Table of Contents](#table-of-contents)
    - [Installation](#installation)
    - [Example apps](#example-apps)
    - [Quick overview](#quick-overview)
    - [Type definitions](#type-definitions)
        - [OnlinePaymentSdk](#onlinepaymentsdk)
        - [PaymentContext](#paymentcontext)
        - [BasicPaymentProduct](#basicpaymentproduct)
        - [AccountOnFile](#accountonfile)
        - [PaymentProduct](#paymentproduct)
        - [PaymentProductField](#paymentproductfield)
        - [PaymentRequest](#paymentrequest)
            - [Tokenize payment request](#tokenize-payment-request)
            - [Set field values to payment request](#set-field-values-to-payment-request)
            - [Validate payment request](#validate-payment-request)
            - [Encrypt payment request](#encrypt-payment-request)
        - [CreditCardTokenRequest](#creditcardtokenrequest)
        - [IINDetails](#iindetails)
    - [Payment Steps](#payment-steps)
        - [1. Initialize the Android SDK for this payment](#1-initialize-the-android-sdk-for-this-payment)
        - [2. Retrieve payment product details](#2-retrieve-payment-product-details)
        - [3. Encrypt payment information](#3-encrypt-payment-information)
        - [4. Response from the Server API call](#4-response-from-the-server-api-call)

## Installation

Add a dependency to the SDK in your app's `build.gradle` file, where `x.y.z` is the version number:

    dependencies {
        // other dependencies
        implementation 'com.worldline-solutions:sdk-client-android:x.y.z'
    }

## Example apps

For your convenience, the SDK comes with an example app in both Kotlin and Java that you can use as
a basis for your own implementation. The Kotlin example app offers a Jetpack Compose implementation
as well as a traditional XML based implementation, whereas the Java example app offers only a
traditional XML implementation. If you are fine with the look-and-feel of the example app, you do
not need to make any changes at all. Take a look at
the [Kotlin](https://github.com/wl-online-payments-direct/sdk-client-android-example-kotlin)
or [Java](https://github.com/wl-online-payments-direct/sdk-client-android-example) example apps.

## Quick overview

To accept your first payment using the SDK, complete the steps below. Also see the
section [Payment Steps](#payment-steps) for more details on these steps.

1. Request your server to create a Client Session, using one of our available Server SDKs. Return the session details to
   your app.

2. Initialize the SDK using the session details. Note that the first four arguments come from the Server SDK Create
   Session call one in the previous step.

   **_java:_**
   ```java
    OnlinePaymentSdk sdk = new OnlinePaymentSdk(
        new SessionData(
           "47e9dc332ca24273818be2a46072e006", // client session id
           "9991-0d93d6a0e18443bd871c89ec6d38a873", // customer id
           "https://clientapi.com", // client API URL
           "https://assets.com" // asset URL
        ),
       getApplicationContext(), // this will get your Java application context
       new SdkConfiguration( //optional
           false, // states if the environment is production, this property is used to determine the Google Pay environment
           "Android Example Application/v2.0.4", // your application identifier
           "AndroidSDK/v4.0.0", // SDK identifier
           true // if true, requests and responses will be logged to the console; not supplying this parameter means it is false; should be false in production
       )
    );
   ```

   **_kotlin:_**
   ```kotlin
    // Create session data
    val sessionData = SessionData(
       clientSessionId = "47e9dc332ca24273818be2a46072e006", // client session id
       customerId = "9991-0d93d6a0e18443bd871c89ec6d38a873", // customer id
       clientApiUrl = "https://clientapi.com", // client API URL
       assetUrl = "https://assets.com" // asset URL
    )

    // Create SDK configuration
    val sdkConfiguration = SdkConfiguration(
       environmentIsProduction = false, // states if the environment is production, this property is used to determine the Google Pay environment
       appIdentifier = "Android Example Application/v2.0.4", // your application identifier
       sdkIdentifier = "AndroidSDK/v4.0.0", // SDK identifier
       loggingEnabled = true // if true, requests and responses will be logged to the console; not supplying this parameter means it is false; should be false in production
    )

    // Initialize SDK
    val sdk = OnlinePaymentSdk(
       sessionData = sessionData,
       context = getApplication<Application>().applicationContext, // application context
       configuration = sdkConfiguration //optional
    )
   ```

3. Configure your payment context.

   **_java:_**
   ```java
   AmountOfMoney amountOfMoney = new AmountOfMoney(
       1298L, // in cents as a Long
       "EUR" // three letter currency code as defined in ISO 4217
   );
    
   PaymentContext paymentContext = new PaymentContext(
       amountOfMoney,
       "NL", // two letter country code as defined in ISO 3166-1 alpha-2
       false // true, if it is a recurring payment
   );
   ```

   **_kotlin:_**
   ```kotlin
   val amountOfMoney = AmountOfMoney(
       1298L, // in cents as a Long
       "EUR" // three letter currency code as defined in ISO 4217
   )
    
   val paymentContext = PaymentContext(
       amountOfMoney,
       "NL", // two letter country code as defined in ISO 3166-1 alpha-2
       false // true, if it is a recurring payment
   )
   ```

4. Retrieve the available Payment Products. After successfully retrieving the Payment Products,
   display the `BasicPaymentProduct` and `AccountOnFile` lists and request your customer to select one.\
   **Note:** each session call can throw errors. Wrap your code into the try/catch block.\
   **Note 2:** Since Kotlin methods are asynchronous (using coroutines), there are synchronous overloads with `Sync`
   suffixes that you can use in Java, but be aware they will block the main thread. For async calls in Java, you can use
   the listener-based approach. We give an example for this here, and you can use the same approach for other calls.

   **_java:_**
   ```java
   // sync call - blocks the main thread
    try {
    BasicPaymentProducts basicPaymentProducts = sdk.getBasicPaymentProductsSync(paymentContext);
       // Display the contents of basicPaymentProducts to your customer.
    } catch (ResponseException e) {
       // Handle HTTP/API errors (usually 4xx)
    } catch (CommunicationException e) {
      // Handle communication errors (network connection failed, timeout, malformed URL, etc.)
    } catch (Exception e) {
      // Handle any other unhandled exception
    }

   // *** Listener-based async call ****
   session.getBasicPaymentProducts(
       paymentContext, // PaymentContext
       new BasicPaymentProductsResponseListener() {
           @Override
           public void onSuccess(@NonNull BasicPaymentProducts basicPaymentProducts) {
              // Display the contents of basicPaymentProducts to your customer
           }

           @Override
           public void onApiError(ErrorResponse errorResponse) {
               // Handle API failure of retrieving the available Payment Products
           }

           @Override
           public void onException(Throwable throwable) {
               // Handle failure of retrieving the available Payment Products
           }
       }
   );
   ```

   **_kotlin:_**
   ```kotlin
    try {
        BasicPaymentProducts basicPaymentProducts = sdk.getBasicPaymentProducts(paymentContext);
        // Display the contents of basicPaymentProducts to your customer.
    } catch (ResponseException e) {
        // Handle the exception thrown by the API. Usually, these are 4xx exceptions.
    } catch (CommunicationException e) {
       // Handle communication errors (network connection failed, timeout, malformed URL, etc.)
    } catch (Exception e) {
       // Handle any other unhandled exception
    }
   ```

5. Once the customer has selected the desired payment product, retrieve the enriched`PaymentProduct`
   detailing what information the customer needs to provide to authorize the payment. After successfully retrieving
   the `PaymentProduct`, display the required information fields to your customer.

   **_java:_**
   ```java
   // sync call
   try {
       PaymentProduct paymentProduct = sdk.getPaymentProductSync(paymentProductId, paymentContext);
       // Display the fields to your customer.
   } catch (e: ResponseException) {
       // Handle the exception thrown by the API. Usually, these are 4xx exceptions.
   } catch (e: CommunicationException) {
       // Handle the communication exception - it can happen when the API call could not be established.
   } catch (e: Exception) {
       // Handle any other unhandled exception.
   }
   ```

   **_kotlin:_**
   ```kotlin
   try {
       val paymentProduct = sdk.getPaymentProduct(paymentProductId, paymentContext)
       // Display the fields to your customer.
   } catch (e: ResponseException) {
       // Handle the exception thrown by the API. Usually, these are 4xx exceptions.
   } catch (e: CommunicationException) {
       // Handle the communication exception - it can happen when the API call could not be established.
   } catch (e: Exception) {
       // Handle any other unhandled exception.
   }
   ```

6. Save the customer's input for the required information fields in a `PaymentRequest`.

   **_java:_**
   ```java
   //payment request with paymentProduct, accountOnFile, tokenize status
   PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, null, false);
    
   //new way of setting (unmasked) values 
   paymentRequest.field("cardNumber).setValue("12451254457545")
   
   //backward compatibility
   paymentRequest.setValue("cvv","123");
   paymentRequest.setValue("expiryDate","1225");
   ```

   **_kotlin:_**
   ```kotlin
   var paymentRequest = PaymentRequest(paymentProduct)
    
   //new way of setting (unmasked) values
   paymentRequest.field("cardNumber").setValue("12451254457545")

   //backward compatibility
   paymentRequest.setValue("cvv","123")
   paymentRequest.setValue("expiryDate","1225")
   ```

7. Validate and encrypt the payment request. After successfully encrypting the `PaymentRequest`, you will have access
   to the encrypted version, `EncryptedRequest`. The encrypted customer data should then be sent to your server.

   **_java:_**
   ```java
    try {
        EncryptedRequest encryptedRequest = sdk.encryptPaymentRequestSync(paymentRequest);
        // Send the encryptedRequest.encryptedCustomerInput to your server and use it to create the payment with the Server SDK.
    } catch (EncryptionException e) {
        // Handle encryption failure (invalid payment data, encryption failed, etc.)
    } catch (ResponseException e) {
        // Handle HTTP/API errors (usually 4xx)
    } catch (CommunicationException e) {
        // Handle communication errors when fetching public key (network issues, timeout, etc.)
    } catch (Exception e) {
       // handle any other unhandled exception
    }
   ```

   **_kotlin:_**
   ```kotlin
    try {
        val encryptedRequest = sdk.encryptPaymentRequest(paymentRequest)
        // Send the encryptedRequest.encryptedCustomerInput to your server and use it to create the payment with the Server SDK.
    } catch (e: EncryptionException) {
        // Handle encryption failure (invalid payment data, encryption failed, etc.)
    } catch (e: ResponseException) {
        // Handle HTTP/API errors (usually 4xx)
    } catch (e: CommunicationException) {
       // Handle communication errors when fetching public key (network issues, timeout, etc.)
    } catch (e: Exception) {
       // Handle any other unhandled exception
    }
   ```

8. Request your server to create a payment request using the Server APIs Create Payment call.
   Provide the encrypted data in the `encryptedCustomerInput` field.
   Check
   the [API documentation](https://docs.direct.worldline-solutions.com/en/api-reference#tag/Payments/operation/CreatePaymentApi)
   for more details.

## Type definitions

### OnlinePaymentSdk

An instance of the `OnlinePaymentSdk` class is required for all interactions with the SDK. The following code fragment
shows how
`OnlinePaymentSdk` is initialized. The session details are obtained by performing a Create Client Session call using the
Server
API (not part of this SDK).

**_java:_**

   ```java
    OnlinePaymentsSDK sdk = new OnlinePaymentsSDK(
        new SessionData(
           "47e9dc332ca24273818be2a46072e006", // client session id
           "9991-0d93d6a0e18443bd871c89ec6d38a873", // customer id
           "https://clientapi.com", // client API URL
           "https://assets.com" // asset URL
        ),
       getApplicationContext(), // this will get your Java application context
       new SdkConfiguration(
           false, // states if the environment is production, this property is used to determine the Google Pay environment
           "Android Example Application/v2.0.4", // your application identifier
           "AndroidSDK/v4.0.0", // SDK identifier
           true // if true, requests and responses will be logged to the console; not supplying this parameter means it is false; should be false in production
       )
    );
   ```

**_kotlin:_**

   ```kotlin
    // Create session data
    val sessionData = SessionData(
       clientSessionId = "47e9dc332ca24273818be2a46072e006", // client session id
       customerId = "9991-0d93d6a0e18443bd871c89ec6d38a873", // customer id
       clientApiUrl = "https://clientapi.com", // client API URL
       assetUrl = "https://assets.com" // asset URL
    )

    // Create SDK configuration
    val sdkConfiguration = SdkConfiguration(
       environmentIsProduction = false, // states if the environment is production, this property is used to determine the Google Pay environment
       appIdentifier = "Android Example Application/v2.0.4", // your application identifier
       sdkIdentifier = "AndroidSDK/v4.0.0", // SDK identifier
       loggingEnabled = true // if true, requests and responses will be logged to the console; not supplying this parameter means it is false; should be false in production
    )

    // Initialize SDK
    val sdk = OnlinePaymentsSDK(
       sessionData = sessionData,
       context = getApplication<Application>().applicationContext, // application context
       configuration = sdkConfiguration
    )
   ```

Almost all methods that are offered by `OnlinePaymentSdk` are simple wrappers around the Client API. They create the
request and
convert the response to Java objects that may contain convenience functions.

### PaymentContext

`PaymentContext` is an object that contains the context/settings of the upcoming payment. It is required as an argument
to some of the methods of the `OnlinePaymentSdk` instance. This object can contain the following details:

**_java:_**

```java
public class PaymentContext {
    private AmountOfMoney amountOfMoney; // contains the total amount and the ISO 4217 currency code
    private String countryCode; // ISO 3166-1 alpha-2 country code
    private boolean isRecurring; // Set `true` when payment is recurring. Default false.
}
```

**_kotlin:_**

```kotlin
data class PaymentContext(
    var amountOfMoney: AmountOfMoney? = null, // contains the total amount and the ISO 4217 currency code
    var countryCode: String? = null, // ISO 3166-1 alpha-2 country code
    var isRecurring: Boolean = false // Set `true` when payment is recurring. Default false.
) : Serializable { ... }
```

### BasicPaymentProduct

The SDK offers two types to represent information about payment products: `BasicPaymentProduct` and `PaymentProduct`.
Practically speaking, instances of `BasicPaymentProduct` contain only the information that is required to display a
simple list of payment products from which the customer can select one.

The type `PaymentProduct` contains additional information, such as the specific form fields definitions that the
customer is required to fill out. This type is typically used when creating a form that asks the customer for their
details. See the [PaymentProduct](#paymentproduct) section for more info.

Below is an example for how to obtain display names and assets for the Visa product.

**_java:_**

```java
BasicPaymentProducts basicPaymentProducts = sdk.getBasicPaymentProducts(paymentContext)

List<BasicPaymentProduct> paymentProducts = basicPaymentProducts.getPaymentProducts();

String label = paymentProducts[0].getLabel
String logoUrl = paymentProducts[0].getLogo
```

**_kotlin:_**

```kotlin
val basicPaymentProducts = sdk.getBasicPaymentProducts(paymentContext)

val paymentProducts = basicPaymentProducts.paymentProducts

val label = paymentProducts[0].label
val logoUrl = paymentProducts[0].logo
```

### AccountOnFile

An instance of `AccountOnFile` represents information about a stored card product for the current customer.
`AccountOnFile` IDs available for the current payment must be provided in the request body of the Server API Create
Client Session call. If the customer wishes to use an existing `AccountOnFile` for a payment, the selected
`AccountOnFile` should be added to the `PaymentRequest`. The code fragment below shows how display data for an account
on file can be retrieved. This label can be shown to the customer, along with the logo of the corresponding payment
product.

**_java:_**

```java
// All available accounts on file for the payment product
List<AccountOnFile> allAccountsOnFile = basicPaymentProduct.getAccountsOnFile();

// Get specific account on file for the payment product
AccountOnFile accountOnFile = null;

for (AccountOnFile aof : basicPaymentProduct.getAccountsOnFile) {
    if (Objects.equals(aof.getId(), identifier)) { // 'identifier' is the ID of the AccountOnFile selected by the customer
        accountOnFile = aof;
        break;
    }
}

// Shows a mask based formatted value for the obfuscated cardNumber.
String label = accountOnFile.getLabel
```

**_kotlin:_**

```kotlin
// All available accounts on file for the payment product
val allAccountsOnFile = basicPaymentProduct.accountsOnFile

// Get specific account on file for the payment product
val accountOnFile = basicPaymentProduct.accountsOnFile
    .firstOrNull { it.id == identifier } // 'identifier' is the ID of the AccountOnFile selected by the customer

// Shows a mask-based formatted value for the obfuscated card number
val label = accountOnFile?.label
```

### PaymentProduct

`BasicPaymentProduct` only contains the information required by a customer to distinguish one payment product from
another. However, once a payment product has been selected, the customer must provide additional information, such as
a bank account number, a credit card number, or an expiry date, before a payment can be processed. Each payment product
can have several fields that need to be completed to process a payment. Instances of `BasicPaymentProduct` do not
contain any information about these fields.

Information about the fields of payment products are represented by instances of`PaymentProductField`, which are
contained in instances of `PaymentProduct`. The class `PaymentProductField` is described further down below.

The `OnlinePaymentSdk` instance can be used to retrieve the instance of a `PaymentProduct`, as shown in the following
code
fragment.

**_java:_**

```java
// Note that exception handling is omitted here. Check the code above for more info. 
PaymentProduct paymentProduct = sdk.getPaymentProductSync(
    "1", // id of the payment product you want to retrieve
    paymentContext, // PaymentContext
);
```

**_kotlin:_**

```kotlin
// Note that exception handling is omitted here. Check the code above for more info. 
val paymentProduct = sesssdkion.getPaymentProduct(
    "1", // id of the payment product you want to retrieve
    paymentContext, // PaymentContext
)
```

### PaymentProductField

The fields of payment products are represented by instances of `PaymentProductField`. Each field has an identifier, a
type, a definition of restrictions that apply to the value of the field, and information about how the field should be
presented graphically to the customer. Additionally, an instance of a field can be used to determine whether a given
value is valid for the field.

In the code fragment below, the field with identifier `"cvv"` is retrieved from a payment product. The data restrictions
of the field are inspected to see whether the field is a required field or an optional field. Additionally, the display
hints of the field are inspected to see whether the values a customer provides should be obfuscated in a user interface.

**_java:_**

```java
PaymentProductField ccvField = paymentProduct.getField("cvv");

Boolean isRequired = ccvField.isRequired(); // state if value is required for this field
Boolean shouldObfuscate = ccvField.shouldObfuscate(); // state if field value should be obfuscated
```

**_kotlin:_**

```kotlin
val ccvField = paymentProduct.getField("cvv")

val isRequired = ccvField.isRequired()
val shouldObfuscate = ccvField.shouldObfuscate()
```

### PaymentRequest

Once a payment product has been selected and an instance of `PaymentProduct` has been retrieved, a payment request can
be constructed. This class must be used as a container for all the values the customer provides.

PaymentRequest internally has list of `PaymentRequestField`. Each field provides methods to set and retrieve values, get
the field label, obtain masked values for display, and validate user input against the field's restrictions.

Set field values to the payment request
Once a payment request has been created, the values for the payment request fields can be supplied as follows.

**_java:_**

```java
boolean tokenize = true; // true or false 

//provide paymentProduct, accountOnFile, tokenize status
PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, null, tokenize);

//or
PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, accountOnFile, tokenize);
```

**_kotlin:_**

```kotlin
//tokenize status set to false by default
var paymentRequest = PaymentRequest(paymentProduct)

//with account on file
var paymentRequest = PaymentRequest(paymentProduct, accountOnFile)
```

When no `AccountOnFile` is selected for the specific `PaymentRequest`, all payment request fields(such as cardNumber)
are writable and can be set normally.
Once an `AccountOnFile` is set on the `PaymentRequest`, the SDK enforces the following behavior:

All previously set unwritable field values are cleared from the `PaymentRequest`.
Read-only fields cannot be set manually anymore. Calling setter will throw `InvalidArgumentException`.
Calling `paymentRequest.getField(readOnlyFieldId).getValue()` will return undefined.
This ensures only values that can be changed are submitted.

#### Tokenize payment request

A `PaymentProduct` has a property `tokenize`, which is used to indicate whether a payment request should be stored as an
account on file for later use. The code fragment below shows how a payment request should be constructed when the
request should be stored as an account on file. By default, `tokenize` is set to `false`.

**_java:_**

```java
// you can supply tokenize via the constructor
PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, accountOnFile, true);
```

**_kotlin:_**

```kotlin
// you can supply tokenize via the constructor
var paymentRequest = PaymentRequest(paymentProduct, true)
```

If the customer selected an account on file, both the account on file and the corresponding payment product must be
supplied while constructing the payment request, as shown in the code fragment below. Instances of `AccountOnFile` can
be retrieved from instances of `BasicPaymentProduct` and `PaymentProduct`.

**_java:_**

```java
PaymentRequest paymentRequest = new PaymentRequest(
    paymentProduct,
    accountOnFile,
    tokenizeStatus
); // when you do not pass an accountOnFile argument, it will be null
```

**_kotlin:_**

```kotlin
val paymentRequest = PaymentRequest(paymentProduct, accountOnFile)
```

#### Set field values to payment request

Once a payment request has been configured, the value for the payment product's fields can be supplied as shown below.
The identifiers of the fields, such as "cardNumber" and "cvv" in the example below, are used to set the values of the
fields using the payment request. In general, you can retrieve all available fields from the `PaymentProduct` instance.

**_java:_**

```java
PaymentRequest paymentRequest = new PaymentRequest(paymentProduct, null, false);

paymentRequest.getField("cardNumber").setValue("12451254457545")
paymentRequest.getField("cvv").setValue("123")
paymentRequest.getField("expiryDate").setValue("1225")
```

**_kotlin:_**

```kotlin
var paymentRequest = PaymentRequest(paymentProduct)

paymentRequest.getField("cardNumber").setValue("12451254457545")
paymentRequest.getField("cvv").setValue("123")
paymentRequest.getField("expiryDate").setValue("1225")
```

#### Validate payment request

Once all values have been supplied, the payment request can be validated. Behind the scenes the validation uses the
`DataRestrictions` class for each of the fields that were added to the`PaymentRequest`. A list of errors is available
after the validation, which indicates any issues that have occurred during validation. If there are no errors, the
payment request can be encrypted and sent to our platform via your server (Server SDK). If there are validation errors,
the customer should be provided with feedback about these errors.

The validations are the `Validator`s linked to the `PaymentProductField`, and are returned as a
`ValidationErrorMessage` list inside `ValidationResult`:

**_java:_**

```java
// validate all fields in the payment request
// validate() return ValidationResult object with isValid status and errors
ValidationResult validationResult = paymentRequest.validate();

// check if the payment request is valid - result will return isValid: true and errors: []
if (validationResult.isValid()) {
    // payment request is valid
} else {
    // payment request has errors
    List<ValidationErrorMessage> errors = validationResult.getErrors();
    
    for (ValidationErrorMessage error : errors) {
        //show errors
    }
}
```

**_kotlin:_**

```kotlin
// validate all fields in the payment request
// validate() return ValidationResult object with isValid status and errors
val validationResult = paymentRequest.validate()

// check if the // check if the payment request is valid - result will return isValid: true and errors: []
if (validationResult.isValid) {
    // payment request is valid
} else {
    // payment request has errors
    for (error in validationResult.errors) {
      //show errors
    }
}
```

#### Encrypt payment request

The `PaymentRequest` is ready for encryption once the `PaymentProduct` is set, the `PaymentProductField` values have
been provided and validated, and potentially the selected `AccountOnFile` or `tokenize` properties have been set. After
successfully encrypting the `PaymentRequest`, you will have access to the encrypted version, `EncryptedRequest`
which contains the encrypted payment request fields and encoded client meta info.

**_java:_**

```java
try {
    EncryptedRequest encryptedRequest = sdk.encryptPaymentRequestSync(paymentRequest);
    // Send the encryptedRequest.encryptedCustomerInput to your server and use it to create the payment with the Server SDK.
} catch (EncryptionException e) {
    // Handle encryption failure (invalid payment data, encryption failed, etc.)
} catch (ResponseException e) {
    // Handle HTTP/API errors (usually 4xx)
} catch (CommunicationException e) {
    // Handle communication errors when fetching public key (network issues, timeout, etc.)
} catch (Exception e) {
    // Handle any other unhandled exception
}
```

**_kotlin:_**

```kotlin
try {
    val encryptedRequest = sdk.encryptPaymentRequest(paymentRequest)

    // Send the encryptedRequest.encryptedCustomerInput to your server and use it to create the payment with the Server SDK.
} catch (EncryptionException e) {
    // Handle encryption failure (invalid payment data, encryption failed, etc.)
} catch (ResponseException e) {
    // Handle HTTP/API errors (usually 4xx)
} catch (CommunicationException e) {
    // Handle communication errors when fetching public key (network issues, timeout, etc.)
} catch (Exception e) {
    // Handle any other unhandled exception
}
```

> Although it is possible to use your own encryption algorithms to encrypt a payment request, we
> advise you to use the encryption functionality that is offered by the SDK.

### CreditCardTokenRequest

This class is used to create a Card Tokenization request. It contains the essential credit card fields: card number,
cardholder name, expiry date, cvv, and payment product id.
**_java:_**

```java
CreditCardTokenRequest tokenRequest = new CreditCardTokenRequest();
tokenRequest.setCardNumber("1234567890123452");
tokenRequest.setCardHolderName("John Doe");
tokenRequest.setExpiryDate("1225");
tokenRequest.setSecurityCode("123");
tokenRequest.setPaymentProductId(1);
```

**_kotlin:_**

```kotlin
val tokenRequest = CreditCardTokenRequest()
tokenRequest.cardNumber = "1234567890123452"
tokenRequest.cardholderName = "John Doe"
tokenRequest.expiryDate = "1225"
tokenRequest.securityCode = "123"
tokenRequest.paymentProductId = 1
```

Note that there are no validation rules applied for values set in the token request since it is detached from the
instance of the payment product. This class is meant to be used as a helper for encrypting data required for creating a
token using Server SDK. However, if the invalid data is provided, the Create Token request will fail.

**_java:_**

```java
try {
    EncryptedRequest encryptedRequest = sdk.encryptTokenRequest(tokenRequest);
    // Send the encryptedRequest.encryptedCustomerInput to your server and use it to create the payment with the Server SDK.
} catch (EncryptionException e) {
    // Handle encryption failure (invalid payment data, encryption failed, etc.)
} catch (ResponseException e) {
    // Handle HTTP/API errors (usually 4xx)
} catch (CommunicationException e) {
    // Handle communication errors when fetching public key (network issues, timeout, etc.)
} catch (Exception e) {
    // Handle any other unhandled exception
}
```

**_kotlin:_**

```kotlin
try {
    val encryptedRequest = sdk.encryptTokenRequest(tokenRequest)
    // Send the encryptedRequest.encryptedCustomerInput to your server and use it to create the payment with the Server SDK.
} catch (EncryptionException e) {
    // Handle encryption failure (invalid payment data, encryption failed, etc.)
} catch (ResponseException e) {
    // Handle HTTP/API errors (usually 4xx)
} catch (CommunicationException e) {
    // Handle communication errors when fetching public key (network issues, timeout, etc.)
} catch (Exception e) {
    // Handle any other unhandled exception
}
```

### IINDetails

The first six digits of a payment card number are known as the _Issuer Identification Number (IIN)_. As soon as the
first 6 digits of the card number have been captured, you can use the `sdk.getIinDetails` call to retrieve the
payment product and network that are associated with the provided IIN. Then you can verify the card type and check if
you can accept this card.

An instance of `OnlinePaymentSdk` can be used to check which payment product is associated with an IIN. This is done via
the
`session.getIinDetails` function. The result of this check is an instance of `IinDetailsResponse`. This class has a
property status that indicates the result of the check and a property `paymentProductId` that indicates which payment
product is associated with the IIN. The returned `paymentProductId` can be used to provide visual feedback to the
customer by showing the appropriate payment product logo.

The `IinDetailsResponse` has a status property represented through the `IinStatus` enum. The `IinStatus` enum values
are:

- `SUPPORTED` indicates that the IIN is associated with a payment product that is supported by our platform.
- `UNKNOWN` indicates that the IIN is not recognized.
- `NOT_ENOUGH_DIGITS` indicates that fewer than six digits have been provided and that the IIN check cannot be
  performed.
- `EXISTING_BUT_NOT_ALLOWED` indicates that the provided IIN is recognized, but that the corresponding product is not
  allowed for the current payment.

**_java:_**

```java
// The exception hanlding is omitted from this example. See the examples above.
sdk.getIinDetailsSync(
    "123456", // partial credit card number
    paymentContext // PaymentContext
);
```

**_kotlin:_**

```kotlin
// The exception hanlding is omitted from this example. See the examples above.
sdk.getIinDetails("123456", paymentContext)
```

Some cards are dual branded and could be processed as either a local card _(with a local brand)_ or an international
card _(with an international brand)_. In case you are not set to process these local cards, this API call will not
return that card type in its response.

## Payment Steps

Setting up and completing a payment using the Android SDK involves the following steps:

### 1. Initialize the Android SDK for this payment

This is done using information such as session and customer identifiers, connection URLs and payment context information
like currency and total amount.

**_java:_**

```java
OnlinePaymentSdk sdk = new OnlinePaymentSdk(
   new SessionData(
       "47e9dc332ca24273818be2a46072e006", // client session id
       "9991-0d93d6a0e18443bd871c89ec6d38a873", // customer id
       "https://clientapi.com", // client API URL
       "https://assets.com" // asset URL
   ),
   getApplicationContext(), // this will get your Java application context
   new SdkConfiguration( //optional
        false, // states if the environment is production, this property is used to determine the Google Pay environment
        "Android Example Application/v2.0.4", // your application identifier
        "AndroidSDK/v4.0.0", // SDK identifier
        true // if true, requests and responses will be logged to the console; not supplying this parameter means it is false; should be false in production
       )
    );

AmountOfMoney amountOfMoney = new AmountOfMoney(
    1298L, // in cents as a Long
    "EUR" // three letter currency code as defined in ISO 4217
);

PaymentContext paymentContext = new PaymentContext(
    amountOfMoney,
    "NL", // two letter country code as defined in ISO 3166-1 alpha-2
    false // true, if it is a recurring payment
);
```

**_kotlin:_**

```kotlin
// Create session data
val sessionData = SessionData(
   clientSessionId = "47e9dc332ca24273818be2a46072e006", // client session id
   customerId = "9991-0d93d6a0e18443bd871c89ec6d38a873", // customer id
   clientApiUrl = "https://clientapi.com", // client API URL
   assetUrl = "https://assets.com" // asset URL
)

// Create SDK configuration
val sdkConfiguration = SdkConfiguration(
    environmentIsProduction = false, // states if the environment is production, this property is used to determine the Google Pay environment
    appIdentifier = "Android Example Application/v2.0.4", // your application identifier
    sdkIdentifier = "AndroidSDK/v4.0.0", // SDK identifier
    loggingEnabled = true // if true, requests and responses will be logged to the console; not supplying this parameter means it is false; should be false in production
)

// Initialize SDK
val sdk = OnlinePaymentSdk(
    sessionData = sessionData,
    context = getApplication<Application>().applicationContext, // application context
    configuration = sdkConfiguration //optional
)

val amountOfMoney = AmountOfMoney(
    1298L, // in cents as a Long
    "EUR" // three letter currency code as defined in ISO 4217
)

val paymentContext = PaymentContext(
    amountOfMoney,
    "NL", // two letter country code as defined in ISO 3166-1 alpha-2
    false // true, if it is a recurring payment
)
```

> A successful response from Create Session can be used directly as input for the Session
> constructor.

- `clientSessionId` / `customerId` properties are used to authentication purposes. These can be obtained your server,
  using one of our available Server SDKs.
- The `clientApiUrl` and `assetBaseUrl` are the URLs the SDK should connect to. The SDK communicates with two types of
  servers to perform its tasks. One type of server offers the Client API as discussed above. And the other type of
  server stores the static resources used by the SDK, such as the logos of payment products.
- Payment information (`paymentContext`) is not needed to construct a session, but you will need to provide it when
  requesting any payment product information. The payment products that the customer can choose from depend on the
  provided payment information, so the Client SDK needs this information to be able to do its job. The payment
  information that is needed is:
    - the total amount of the payment, defined as property `amountOfMoney.amount`
    - the currency that should be used, defined as property `amountOfMoney.currencyCode`
    - the country of the person that is performing the payment, defined as property `countryCode`
    - whether the payment is a single payment or a recurring payment

### 2. Retrieve payment product details

Retrieve all the details about the payment product - including it's fields - that the customer needs to provide based on
the selected payment product or account on file. Your app can use this information to create the payment product details
screen.

For some payment products, customers can indicate that they want the Online Payments platform to store part of the data
they entered while using such a payment product. For example, it is possible to store the card holder name and the card
number for most credit card payment products. The stored data is referred to as an `AccountOnFile` or token.
`AccountOnFile` IDs available for the current payment must be provided in the request body of the Server API Create
Client Session call. When the customer wants to use the same payment product for another payment, it is possible to
select one of the stored accounts on file for this payment. In this case, the customer does not have to enter the
information that is already stored in the `AccountOnFile`. The list of available payment products that the SDK receives
from the Client API also contains the accounts on file for each payment product. Your application can present this list
of payment products and accounts on file to the customer.

If the customer wishes to use an existing `AccountOnFile` for a payment, the selected`AccountOnFile` should be added to
the `PaymentRequest`.

**_java:_**

```java
// sync call
try {
   BasicPaymentProducts basicPaymentProducts = sdk.getBasicPaymentProductsSync(paymentContext);
   // Display the contents of basicPaymentProducts to your customer.
} catch (ResponseException e) {
   // Handle the exception thrown by the API. Usually, these are 4xx exceptions.
} catch (CommunicationException e) {
   // Handle communication errors (network connection failed, timeout, malformed URL, etc.)
} catch (Exception e) {
   // Handle any other unhandled exception
}
```

**_kotlin:_**

```kotlin
try {
   val paymentProduct = session.getPaymentProduct(paymentProductId, paymentContext)
   // Display the fields to your customer.
} catch (e: ApiException) {
   // Handle the exception thrown by the API. Usually, these are 4xx exceptions.
} catch (e: CommunicationException) {
   // Handle the communication exception - it can happen when the API call could not be established.
} catch (e: Exception) {
   // Handle any other unhandled exception.
}
```

Once the customer has selected a payment product or stored account on file, the SDK can request which information needs
to be provided by the customer in order to perform a payment. When a single product is retrieved, the SDK provides a
list of all the fields that should be rendered, including display hints and validation rules. If the customer selected
an account on file, information that is already in this account on file can be prefilled in the input fields, instead of
requesting it from the customer. The data that can be stored and prefilled on behalf of the customer is of course in
line with applicable regulations. For instance, for a credit card transaction, the customer is still expected to input
the CVC. The details entered by the customer are stored in a `PaymentRequest`. Again, the example app can be used as the
starting point to create your screen. If there is no additional information that needs to be entered, this screen can be
skipped.

### 3. Encrypt payment information

Encrypt all the provided payment information in the `PaymentRequest` using `sdk.createPaymentRequest` or
`sdk.createPaymentRequestSync`.
This function will return a `EncryptedRequest` which contains the encrypted payment request fields and encoded client
meta info. The encrypted fields result is in a format that can be processed by the Server API. The only thing you need
to provide to the SDK are the values the customer provided in your screens. Once you have retrieved the encrypted fields
String from the `EncryptedRequest`, your application should send it to your server, which in turn should forward
it to the Server API.

**_java:_**
// Note that exception handling is omitted here. Check the code above for more info.

```java
// Note that exception handling is omitted here. Check the code above for more info. 
EncryptedRequest encryptedRequest = sdk.encryptPaymentRequest(paymentRequest);
```

**_kotlin:_**

```kotlin
// Note that exception handling is omitted here. Check the code above for more info. 
val encryptedRequest = sdk.encryptPaymentRequest(paymentRequest)
```

All the heavy lifting, such as requesting a public key from the Client API, performing the encryption and BASE-64
encoding the result into one string, is done for you by the SDK. You only need to make sure that the `PaymentRequest`
object contains all the information entered by the user.

From your server, make a create payment request, providing the encrypted data in the `encryptedCustomerInput` field.

### 4. Response from the Server API call

It is up to you and your application to show the customer the correct screens based on the response of the Server API
call. In some cases, the payment has not finished yet since the customer must be redirected to a third party (such as a
bank or PayPal) to authorise the payment. See the Server API documentation on what kinds of responses the Server API can
return. The Client API has no part in the remainder of the payment.

## Development

### Running Tests

The SDK includes two types of tests:

#### Unit Tests

Unit tests use mocked dependencies and do not require any configuration. Run them with:

```bash
./gradlew test
```

#### Integration Tests

Integration tests make real API calls to the preprod environment and require valid credentials.

**Setup:**

1. Copy `local.properties.example` to `local.properties`:
   ```bash
   cp local.properties.example local.properties
   ```

2. Fill in your credentials in `local.properties`:
   ```properties
   ONLINEPAYMENTS_SDK_MERCHANT_ID=your-merchant-id
   ONLINEPAYMENTS_SDK_API_ID=your-api-key-id
   ONLINEPAYMENTS_SDK_API_SECRET=your-api-secret
   ```

3. Contact Worldline to obtain test credentials if you don't have them.

**Running Integration Tests:**

You can run only integration tests:

```bash
./gradlew :onlinepayments-sdk:testDebugUnitTest --tests "com.onlinepayments.sdk.client.android.integration.*"
```

or, run all tests (unit + integration):

```bash
./gradlew :onlinepayments-sdk:testDebugUnitTest
```

Integration tests are automatically included and will be skipped if credentials are not configured in
`local.properties`.

**Important Notes:**

- Integration tests will be automatically skipped if credentials are not configured
- Integration tests use session caching to avoid excessive API calls during development
- Integration tests are located in `src/test/kotlin/com/onlinepayments/sdk/client/android/integration/`

**Integration Test Coverage:**

The integration tests cover:

- Getting payment products from the real API
- Encrypting payment requests with real public keys
- IIN (card number) lookup functionality
- Session caching and invalidation
- Real payment product field validation
- End-to-end encryption workflows
