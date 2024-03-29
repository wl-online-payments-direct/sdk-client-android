package com.onlinepayments.sdk.client.android;

import com.onlinepayments.sdk.client.android.encryption.EncryptDataJsonSerializerTest;
import com.onlinepayments.sdk.client.android.encryption.EncryptUtilTest;
import com.onlinepayments.sdk.client.android.encryption.EncryptorTest;
import com.onlinepayments.sdk.client.android.formatter.StringFormatterTest;
import com.onlinepayments.sdk.client.android.model.PaymentProductCacheKeyTest;
import com.onlinepayments.sdk.client.android.model.PaymentProductFieldTest;
import com.onlinepayments.sdk.client.android.model.PaymentRequestTest;
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponseTest;
import com.onlinepayments.sdk.client.android.model.PaymentRequestValidationTest;
import com.onlinepayments.sdk.client.android.model.paymentproduct.AccountOnFileTest;
import com.onlinepayments.sdk.client.android.model.surcharge.SurchargeCalculationResponseTest;
import com.onlinepayments.sdk.client.android.model.validation.ValidationRuleExpirationDateTest;
import com.onlinepayments.sdk.client.android.networking.TLSSocketFactoryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Android InstrumentationTestRunner used for running all the tests in the SDK
 *
 * Copyright 2017 Global Collect Services B.V
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        EncryptDataJsonSerializerTest.class,
        EncryptorTest.class,
        EncryptUtilTest.class,
        StringFormatterTest.class,
        IinDetailsResponseTest.class,
        AccountOnFileTest.class,
        SurchargeCalculationResponseTest.class,
        ValidationRuleExpirationDateTest.class,
        PaymentProductCacheKeyTest.class,
        PaymentProductFieldTest.class,
        PaymentRequestTest.class,
        PaymentRequestValidationTest.class,
        TLSSocketFactoryTest.class,
        UtilTest.class
})
public class SDKTest {

}
