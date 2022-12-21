package com.onlinepayments.sdk.client.android.session;

import android.content.Context;

import com.onlinepayments.sdk.client.android.asynctask.BasicPaymentItemsAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PaymentProductNetworkAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PublicKeyAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.BasicPaymentProductsAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.IinLookupAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.IinLookupAsyncTask.OnIinLookupCompleteListener;
import com.onlinepayments.sdk.client.android.asynctask.PaymentProductAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.PaymentProductAsyncTask.OnPaymentProductCallCompleteListener;
import com.onlinepayments.sdk.client.android.asynctask.BasicPaymentProductsAsyncTask.OnBasicPaymentProductsCallCompleteListener;
import com.onlinepayments.sdk.client.android.communicate.C2sCommunicator;
import com.onlinepayments.sdk.client.android.model.CountryCode;
import com.onlinepayments.sdk.client.android.model.CurrencyCode;
import com.onlinepayments.sdk.client.android.model.PaymentContext;
import com.onlinepayments.sdk.client.android.model.PaymentItemCacheKey;
import com.onlinepayments.sdk.client.android.model.PaymentRequest;
import com.onlinepayments.sdk.client.android.model.api.ErrorResponse;
import com.onlinepayments.sdk.client.android.model.iin.IinDetailsResponse;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItems;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.PaymentProduct;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentProducts;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session contains all methods needed for making a payment
 *
 * Copyright 2017 Global Collect Services B.V
 *
 */
public class Session implements OnBasicPaymentProductsCallCompleteListener, OnIinLookupCompleteListener, OnPaymentProductCallCompleteListener, BasicPaymentItemsAsyncTask.OnBasicPaymentItemsCallCompleteListener, Serializable, BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener, BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener, PaymentProductAsyncTask.PaymentProductCallListener {

    private static final long serialVersionUID = 686891053207055508L;

    // Cache which contains all payment products that are loaded from the GC gateway
    private Map<PaymentItemCacheKey, BasicPaymentItem> basicPaymentItemMapping = new HashMap<>();
    private Map<PaymentItemCacheKey, PaymentItem> paymentItemMapping = new HashMap<>();

    // Communicator used for communicating with the GC gateway
    private C2sCommunicator communicator;

    // C2sPaymentProductContext which contains all necessary data for making a call to the GC gateway to retrieve payment products
    private PaymentContext paymentContext;

    // Flag to determine if the iinlookup is being executed,
    // so it won't be fired every time a character is typed in the edittext while another call is being executed
    private Boolean iinLookupPending = false;

    // Used for identifying the customer on the GC gateway
    private String clientSessionId;


    private Session(C2sCommunicator communicator) {
        this.communicator = communicator;
    }


    /**
     * Gets instance of the Session
     *
     * @param communicator, used for communicating with the GC gateway
     * @return Session instance
     */
    public static Session getInstance(C2sCommunicator communicator) {
        if (communicator == null) {
            throw new InvalidParameterException("Error creating Session instance, communicator may not be null");
        }
        return new Session(communicator);
    }


    /**
     * Returns true when the application is running in production; false otherwise
     */
    public boolean isEnvironmentTypeProduction() {
        return communicator.isEnvironmentTypeProduction();
    }

    /**
     * Returns the asset base URL that was used to create the Session
     */
    public String getAssetUrl() {
        return communicator.getAssetUrl();
    }

    /**
     * Gets all basicPaymentItems for a given payment context
     *
     * @param context              Used for reading device metadata which is send to the GC gateway
     * @param paymentContext       PaymentContext which contains all neccessary payment info to retrieve the allowed payment items
     * @param listener             Listener that will be called when the lookup is done
     * @param groupPaymentProducts In the Online Payments sdk, this boolean is always false regardless of input. boolean that controls whether the basicPaymentItem call will group the retrieved payment items; true for grouping, false otherwise
     * @deprecated use {@link #getBasicPaymentItems(Context, PaymentContext, BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener, boolean)}
     */
    public void getBasicPaymentItems(Context context, PaymentContext paymentContext, BasicPaymentItemsAsyncTask.OnBasicPaymentItemsCallCompleteListener listener, boolean groupPaymentProducts) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        nullCheck("PaymentItems", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentItemsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<BasicPaymentItemsAsyncTask.OnBasicPaymentItemsCallCompleteListener> listeners = new ArrayList<>();
        listeners.add(this);
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentItemsAsyncTask task = new BasicPaymentItemsAsyncTask(context, paymentContext, communicator, listeners, false);
        task.execute();
    }

    /**
     * Gets all basicPaymentItems for a given payment context
     *
     * @param context              Used for reading device metadata which is send to the GC gateway
     * @param paymentContext       PaymentContext which contains all necessary payment info to retrieve the allowed payment items
     * @param listener             Listener that will be called when the lookup is done
     * @param groupPaymentProducts In the Online Payments, this boolean is always false regardless of input. boolean that controls whether the basicPaymentItem call will group the retrieved payment items; true for grouping, false otherwise
     */
    public void getBasicPaymentItems(Context context, PaymentContext paymentContext, BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener listener, boolean groupPaymentProducts) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("PaymentItems", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentItemsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<BasicPaymentItemsAsyncTask.BasicPaymentItemsCallListener> listeners = new ArrayList<>();
        listeners.add(this);
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentItemsAsyncTask task = new BasicPaymentItemsAsyncTask(context, paymentContext, communicator, false, listeners);
        task.execute();
    }


    /**
     * Gets BasicPaymentProducts for the given PaymentRequest
     *
     * @param context,        used for reading device metadata which is send to the GC gateway
     * @param paymentContext, PaymentContext which contains all neccesary data for doing call to the GC gateway to retrieve paymentproducts
     * @param listener,       OnPaymentProductsCallComplete which will be called by the BasicPaymentProductsAsyncTask when the BasicPaymentProducts are loaded
     * @deprecated use {@link #getBasicPaymentProducts(Context, PaymentContext, BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener)}
     */
    public void getBasicPaymentProducts(Context context, PaymentContext paymentContext, OnBasicPaymentProductsCallCompleteListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("PaymentProducts", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentProductsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<OnBasicPaymentProductsCallCompleteListener> listeners = new ArrayList<OnBasicPaymentProductsCallCompleteListener>();
        listeners.add(this);
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentProductsAsyncTask task = new BasicPaymentProductsAsyncTask(context, paymentContext, communicator, listeners);
        task.execute();
    }


    /**
     * Gets BasicPaymentProducts for the given PaymentRequest
     *
     * @param context,        used for reading device metadata which is send to the GC gateway
     * @param paymentContext, PaymentContext which contains all neccesary data for doing call to the GC gateway to retrieve paymentproducts
     * @param listener,       OnPaymentProductsCallComplete which will be called by the BasicPaymentProductsAsyncTask
     *                        when the BasicPaymentProducts are loaded, and OnPaymentProductsCallError when an error occurred
     */
    public void getBasicPaymentProducts(Context context, PaymentContext paymentContext, BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("PaymentProducts", objectsToCheck);

        this.paymentContext = paymentContext;

        // Add OnBasicPaymentProductsCallCompleteListener and this class to list of listeners so we can store the paymentproducts here
        List<BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener> listeners = new ArrayList<BasicPaymentProductsAsyncTask.BasicPaymentProductsCallListener>();
        listeners.add(this);
        listeners.add(listener);

        // Start the task which gets paymentproducts
        BasicPaymentProductsAsyncTask task = new BasicPaymentProductsAsyncTask(context, communicator, paymentContext, listeners);
        task.execute();
    }


    /**
     * Gets PaymentProduct with fields from the GC gateway
     *
     * @param context,        used for reading device metada which is send to the GC gateway
     * @param productId,      the productId of the product which needs to be retrieved from the GC gateway
     * @param paymentContext, PaymentContext which contains all neccesary data for doing call to the GC gateway to retrieve BasicPaymentProducts
     * @param listener,       listener which will be called by the AsyncTask when the PaymentProduct with fields is retrieved
     * @deprecated use {@link #getPaymentProduct(Context, String, PaymentContext, PaymentProductAsyncTask.PaymentProductCallListener)}
     */
    public void getPaymentProduct(Context context, String productId, PaymentContext paymentContext, OnPaymentProductCallCompleteListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        objectsToCheck.put("productId", productId);
        nullCheck("PaymentProduct", objectsToCheck);

        this.paymentContext = paymentContext;

        // Create the cache key for this paymentProduct
        PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, productId);

        // If the paymentProduct is already in the cache, call the listener with that paymentproduct
        if (paymentItemMapping.containsKey(key)) {
            PaymentProduct cachedPP = (PaymentProduct) paymentItemMapping.get(key);
            listener.onPaymentProductCallComplete(cachedPP);
        } else {

            // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can store the paymentproduct here
            List<OnPaymentProductCallCompleteListener> listeners = new ArrayList<OnPaymentProductCallCompleteListener>();
            listeners.add(this);
            listeners.add(listener);

            // Do the call to the GC gateway
            PaymentProductAsyncTask task = new PaymentProductAsyncTask(context, productId, paymentContext, communicator, listeners);
            task.execute();
        }
    }

    /**
     * Gets PaymentProduct with fields from the GC gateway
     *
     * @param context,        used for reading device metada which is send to the GC gateway
     * @param productId,      the productId of the product which needs to be retrieved from the GC gateway
     * @param paymentContext, PaymentContext which contains all neccesary data for doing call to the GC gateway to retrieve BasicPaymentProducts
     * @param listener,       listener which will be called by the AsyncTask when the PaymentProduct with fields is retrieved
     */
    public void getPaymentProduct(Context context, String productId, PaymentContext paymentContext, PaymentProductAsyncTask.PaymentProductCallListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentContext", paymentContext);
        objectsToCheck.put("productId", productId);
        nullCheck("PaymentProduct", objectsToCheck);

        this.paymentContext = paymentContext;

        // Create the cache key for this paymentProduct
        PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, productId);

        // If the paymentProduct is already in the cache, call the listener with that paymentproduct
        if (paymentItemMapping.containsKey(key)) {
            PaymentProduct cachedPP = (PaymentProduct) paymentItemMapping.get(key);
            listener.onPaymentProductCallComplete(cachedPP);
        } else {

            // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can store the paymentproduct here
            List<PaymentProductAsyncTask.PaymentProductCallListener> listeners = new ArrayList<PaymentProductAsyncTask.PaymentProductCallListener>();
            listeners.add(this);
            listeners.add(listener);

            // Do the call to the GC gateway
            PaymentProductAsyncTask task = new PaymentProductAsyncTask(context, productId, communicator, paymentContext, listeners);
            task.execute();
        }
    }

    /**
     * @deprecated use {@link #getNetworkForCustomerAndPaymentProductId} instead
     */
    @Deprecated
    public void getNetworkForCustomerAndPaymentProductId(String customerId, String productId, CountryCode countryCode, CurrencyCode currencyCode, Context context, PaymentContext paymentContext, PaymentProductNetworkAsyncTask.PaymentProductNetworkListener listener) {
        getNetworkForCustomerAndPaymentProductId(customerId, productId, countryCode.toString(), currencyCode.toString(), context, paymentContext, listener);
    }

    public void getNetworkForCustomerAndPaymentProductId(String customerId, String productId, String countryCode, String currencyCode, Context context, PaymentContext paymentContext, PaymentProductNetworkAsyncTask.PaymentProductNetworkListener listener) {

        Map<String, Object> objectToCheck = new HashMap();
        objectToCheck.put("context", context);
        objectToCheck.put("listener", listener);
        objectToCheck.put("productId", productId);
        objectToCheck.put("customerId", customerId);
        objectToCheck.put("countryCode", countryCode);
        objectToCheck.put("currencyCode", currencyCode);
        objectToCheck.put("paymentContext", paymentContext);

        PaymentProductNetworkAsyncTask task = new PaymentProductNetworkAsyncTask(productId, customerId, currencyCode, countryCode, communicator, context, paymentContext, listener);
        task.execute();
    }

    /**
     * Gets the IinDetails for a given partialCreditCardNumber
     *
     * @param context,                 used for reading device metada which is send to the GC gateway
     * @param partialCreditCardNumber, entered partial creditcardnumber for which the IinDetails will be retrieved
     * @param listener,                listener which will be called by the AsyncTask when the IIN result is retrieved
     * @param paymentContext,          payment information for which the IinDetails will be retrieved
     */
    public void getIinDetails(Context context, String partialCreditCardNumber, OnIinLookupCompleteListener listener, PaymentContext paymentContext) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("partialCreditCardNumber", partialCreditCardNumber);
        objectsToCheck.put("paymentContext", paymentContext);
        nullCheck("IinDetails", objectsToCheck);

        // Add OnPaymentProductsCallComplete listener and this class to list of listeners so we can reset the iinLookupPending flag
        List<OnIinLookupCompleteListener> listeners = new ArrayList<OnIinLookupCompleteListener>();
        listeners.add(this);
        listeners.add(listener);

        if (!iinLookupPending) {

            IinLookupAsyncTask task = new IinLookupAsyncTask(context, partialCreditCardNumber, communicator, listeners, paymentContext);
            task.execute();

            iinLookupPending = true;
        }
    }

    /**
     * Retrieves the publickey from the GC gateway
     *
     * @param context,  used for reading device metadata which is send to the GC gateway
     * @param listener, OnPublicKeyLoaded listener which is called when the publickey is retrieved
     * @deprecated use {@link #getPublicKey(Context, PublicKeyAsyncTask.PublicKeyListener)} instead
     */
    public void getPublicKey(Context context, PublicKeyAsyncTask.OnPublicKeyLoadedListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        nullCheck("PublicKey", objectsToCheck);

        PublicKeyAsyncTask task = new PublicKeyAsyncTask(context, communicator, listener);
        task.execute();
    }

    /**
     * Retrieves the publickey from the GC gateway
     *
     * @param context,  used for reading device metadata which is send to the GC gateway
     * @param listener, OnPublicKeyLoaded listener which is called when the publickey is retrieved
     */
    public void getPublicKey(Context context, PublicKeyAsyncTask.PublicKeyListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        nullCheck("PublicKey", objectsToCheck);

        PublicKeyAsyncTask task = new PublicKeyAsyncTask(context, communicator, listener);
        task.execute();
    }

    /**
     * Prepares a PreparedPaymentRequest from the current paymentRequest
     *
     * @param paymentRequest, the paymentRequest which contains all values for all fields
     * @param context,        used for reading device metada which is send to the GC gateway
     * @param listener,       OnPaymentRequestPrepared which is called when the PreparedPaymentRequest is created
     */
    public void preparePaymentRequest(PaymentRequest paymentRequest, Context context, SessionEncryptionHelper.OnPaymentRequestPreparedListener listener) {

        Map<String, Object> objectsToCheck = new HashMap();
        objectsToCheck.put("context", context);
        objectsToCheck.put("listener", listener);
        objectsToCheck.put("paymentRequest", paymentRequest);
        nullCheck("preparing payment request", objectsToCheck);

        Map<String, String> metaData = communicator.getMetadata(context);
        SessionEncryptionHelper sessionEncryptionHelper = new SessionEncryptionHelper(paymentRequest, clientSessionId, metaData, listener);

        // Execute the getPublicKey, which will trigger the listener in the SessionEncryptionHelper
        getPublicKey(context, (PublicKeyAsyncTask.PublicKeyListener) sessionEncryptionHelper);
    }

    /**
     * Utility methods for setting clientSessionId
     *
     * @param clientSessionId
     */
    public void setClientSessionId(String clientSessionId) {

        if (clientSessionId == null) {
            throw new InvalidParameterException("Error setting clientSessionId, clientSessionId may not be null");
        }

        this.clientSessionId = clientSessionId;
    }

    /**
     * Utility methods for getting clientSessionId
     */
    public String getClientSessionId() {
        return clientSessionId;
    }


    private PaymentItemCacheKey createPaymentItemCacheKey(PaymentContext paymentContext, String paymentItemId) {
        // Create the cache key for this retrieved BasicPaymentitem
        return new PaymentItemCacheKey(paymentContext.getAmountOfMoney().getAmount(),
                paymentContext.getCountryCodeString(),
                paymentContext.getAmountOfMoney().getCurrencyCodeString(),
                paymentContext.isRecurring(),
                paymentItemId);
    }

    private void cacheBasicPaymentItem(BasicPaymentItem basicPaymentItem) {
        // Add basicPaymentItem to the basicPaymentItemMapping cache
        if (basicPaymentItem != null) {

            // Create the cache key for and put it in the cache
            PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, basicPaymentItem.getId());
            basicPaymentItemMapping.put(key, basicPaymentItem);
        }
    }

    private void cachePaymentItem(PaymentItem paymentItem) {
        // Add paymentItem to the paymentItemMapping cache
        if (paymentItem != null) {

            // Create the cache key for this retrieved PaymentItem
            PaymentItemCacheKey key = createPaymentItemCacheKey(paymentContext, paymentItem.getId());
            paymentItemMapping.put(key, paymentItem);
        }
    }

    private void nullCheck(String methodName, Map<String, Object> objectsToCheck) {
        for (Map.Entry<String, Object> objectToCheck : objectsToCheck.entrySet()) {
            if (objectToCheck.getValue() == null) {
                throw new InvalidParameterException("Error getting " + methodName + ", " + objectToCheck.getKey() + " may not be null");
            }
        }
    }

    /**
     * Listener for retrieved basicpaymentproducts from the GC gateway
     */
    @Override
    public void onBasicPaymentProductsCallComplete(BasicPaymentProducts basicPaymentProducts) {

        // Store the loaded basicPaymentProducts in the cache
        for (BasicPaymentProduct paymentProduct : basicPaymentProducts.getBasicPaymentProducts()) {
            cacheBasicPaymentItem(paymentProduct);
        }
    }

    @Override
    public void onBasicPaymentProductsCallError(ErrorResponse error) {
        // Not needed for the Session object, leave it to the external listener to act upon
    }

    /**
     * Listener for retrieved paymentproduct from the GC gateway
     */
    @Override
    public void onPaymentProductCallComplete(PaymentProduct paymentProduct) {

        // Store the loaded paymentProduct in the cache
        cachePaymentItem(paymentProduct);
    }

    @Override
    public void onPaymentProductCallError(ErrorResponse error) {
        // Not needed for the Session object, leave it to the external listener to act upon
        // Unless we want to invalidate the cache?
    }

    /**
     * Listener for retrieved paymentitems from the GC gateway
     */
    @Override
    public void onBasicPaymentItemsCallComplete(BasicPaymentItems basicPaymentItems) {

        if (basicPaymentItems != null) {
            // Store the loaded basicPaymentItems in the cache
            for (BasicPaymentItem basicPaymentItem : basicPaymentItems.getBasicPaymentItems()) {
                cacheBasicPaymentItem(basicPaymentItem);
            }
        }
    }

    /**
     * Listener for when payment items failed
     */
    @Override
    public void onBasicPaymentItemsCallError(ErrorResponse error) {
        // No need for caching errors so let it slip...
    }

    /**
     * Listener for retrieved iindetails from the GC gateway
     */
    @Override
    public void onIinLookupComplete(IinDetailsResponse response) {
        iinLookupPending = false;
    }
}
