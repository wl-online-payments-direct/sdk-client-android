/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android

import com.onlinepayments.sdk.client.android.encryption.EncryptDataJsonSerializerTest
import com.onlinepayments.sdk.client.android.encryption.EncryptUtilTest
import com.onlinepayments.sdk.client.android.encryption.EncryptorTest
import com.onlinepayments.sdk.client.android.formatter.StringFormatterTest
import com.onlinepayments.sdk.client.android.model.AmountOfMoneyTest
import com.onlinepayments.sdk.client.android.model.PaymentProductCacheKeyJavaTest
import com.onlinepayments.sdk.client.android.model.PaymentProductFieldTest
import com.onlinepayments.sdk.client.android.model.PaymentRequestTest
import com.onlinepayments.sdk.client.android.model.PaymentRequestValidationTest
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponseTest
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileTest
import com.onlinepayments.sdk.client.android.model.surcharge.SurchargeCalculationResponseTest
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleEmailAddressTest
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleExpirationDateTest
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleFixedListTest
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleIBANTest
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLengthTest
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleLuhnTest
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRangeTest
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleRegexTest
import com.onlinepayments.sdk.client.android.networking.TLSSocketFactoryTest
import com.onlinepayments.sdk.client.android.session.SessionTest
import com.onlinepayments.sdk.client.android.util.JavaVersionTest
import com.onlinepayments.sdk.client.android.util.UtilTest
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

/**
 * Android InstrumentationTestRunner used for running all the tests in the SDK
 */
@RunWith(Suite::class)
@SuiteClasses(
    JavaVersionTest::class,
    EncryptDataJsonSerializerTest::class,
    EncryptorTest::class,
    EncryptUtilTest::class,
    StringFormatterTest::class,
    IinDetailsResponseTest::class,
    AccountOnFileTest::class,
    SurchargeCalculationResponseTest::class,
    ValidationRuleExpirationDateTest::class,
    ValidationRuleEmailAddressTest::class,
    ValidationRuleLuhnTest::class,
    ValidationRuleIBANTest::class,
    ValidationRuleFixedListTest::class,
    ValidationRuleLengthTest::class,
    ValidationRuleRangeTest::class,
    ValidationRuleRegexTest::class,
    AmountOfMoneyTest::class,
    PaymentProductCacheKeyJavaTest::class,
    PaymentProductFieldTest::class,
    PaymentRequestTest::class,
    PaymentRequestValidationTest::class,
    TLSSocketFactoryTest::class,
    UtilTest::class,
    SessionTest::class
)
class SDKTest 
