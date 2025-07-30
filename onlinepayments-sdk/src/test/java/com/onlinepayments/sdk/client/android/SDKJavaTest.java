/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android;

import com.onlinepayments.sdk.client.android.encryption.EncryptDataJsonSerializerJavaTest;
import com.onlinepayments.sdk.client.android.encryption.EncryptUtilJavaTest;
import com.onlinepayments.sdk.client.android.encryption.EncryptorJavaTest;
import com.onlinepayments.sdk.client.android.formatter.StringFormatterJavaTest;
import com.onlinepayments.sdk.client.android.model.AmountOfMoneyJavaTest;
import com.onlinepayments.sdk.client.android.model.PaymentProductCacheKeyJavaTest;
import com.onlinepayments.sdk.client.android.model.PaymentProductFieldJavaTest;
import com.onlinepayments.sdk.client.android.model.PaymentRequestJavaTest;
import com.onlinepayments.sdk.client.android.model.PaymentRequestValidationJavaTest;
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponseJavaTest;
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileJavaTest;
import com.onlinepayments.sdk.client.android.model.surcharge.SurchargeCalculationResponseJavaTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleEmailAddressJavaTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleExpirationDateJavaTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleFixedListJavaTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleIBANJavaTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLengthJavaTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLuhnJavaTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRangeJavaTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRegexJavaTest;
import com.onlinepayments.sdk.client.android.networking.TLSSocketFactoryJavaTest;
import com.onlinepayments.sdk.client.android.session.SessionJavaTest;
import com.onlinepayments.sdk.client.android.session.SessionListenersJavaTest;
import com.onlinepayments.sdk.client.android.util.JavaVersionJavaTest;
import com.onlinepayments.sdk.client.android.util.UtilJavaTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Android InstrumentationTestRunner used for running all the tests in the SDK
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    JavaVersionJavaTest.class,
    EncryptDataJsonSerializerJavaTest.class,
    EncryptorJavaTest.class,
    EncryptUtilJavaTest.class,
    StringFormatterJavaTest.class,
    IinDetailsResponseJavaTest.class,
    AccountOnFileJavaTest.class,
    SurchargeCalculationResponseJavaTest.class,
    ValidationRuleExpirationDateJavaTest.class,
    ValidationRuleEmailAddressJavaTest.class,
    ValidationRuleLuhnJavaTest.class,
    ValidationRuleIBANJavaTest.class,
    ValidationRuleFixedListJavaTest.class,
    ValidationRuleLengthJavaTest.class,
    ValidationRuleRangeJavaTest.class,
    ValidationRuleRegexJavaTest.class,
    AmountOfMoneyJavaTest.class,
    PaymentProductCacheKeyJavaTest.class,
    PaymentProductFieldJavaTest.class,
    PaymentRequestJavaTest.class,
    PaymentRequestValidationJavaTest.class,
    TLSSocketFactoryJavaTest.class,
    UtilJavaTest.class,
    SessionJavaTest.class,
    SessionListenersJavaTest.class
})
public class SDKJavaTest {

}
