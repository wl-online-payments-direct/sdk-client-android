package com.onlinepayments.sdk.client.android.configuration;

/**
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class Constants {

	/** SDK version **/
	public final static String SDK_IDENTIFIER = "OnlinePaymentsAndroidClientSDK/v2.0.1";

	/** SDK creator **/
	public final static String SDK_CREATOR = "OnlinePayments";

	/** Available paths on the Online Payments Client API  **/
	public final static String OP_GATEWAY_RETRIEVE_PAYMENTPRODUCTS_PATH = "[cid]/products";
	public final static String OP_GATEWAY_RETRIEVE_PAYMENTPRODUCT_PATH = "[cid]/products/[pid]";
	public final static String OP_GATEWAY_RETRIEVE_PAYMENTPRODUCT_NETWORKS_PATH = "[cid]/products/[pid]/networks";
	public final static String OP_GATEWAY_IIN_LOOKUP_PATH = "[cid]/services/getIINdetails";
	public final static String OP_GATEWAY_PUBLIC_KEY_PATH = "[cid]/crypto/publickey";

	/** SharedPreferences keys **/
	public final static String PREFERENCES_NAME = "onlinepayments.sdk.client.android.preferences";
	public final static String PREFERENCES_LOGO_MAP = "payment_product_logos_map";

	/** File location settings **/
	public final static String DIRECTORY_IINRESPONSES = "/files/";
	public final static String FILENAME_IINRESPONSE_CACHE = "iinresponse.cache";
	public final static String DIRECTORY_LOGOS = "/files//";
	public final static String FILENAME_LOGO_PREFIX = "logo_logos";

	/** Disable/Enable logging of all requests and responses made to the Online Payments Client API **/
	public final static Boolean ENABLE_REQUEST_LOGGING = true;

	/** Time constant that should be used to determine if a call took to long to return **/
	public static final int ACCEPTABLE_WAIT_TIME_IN_MILISECONDS = 10000;

	/** Cards Group ID **/
	public static String PAYMENTPRODUCTGROUPID_CARDS = "cards";

	/** Payment product Id's of android and apple pay **/
	public final static String PAYMENTPRODUCTID_APPLEPAY = "302";
	public final static String PAYMENTPRODUCTID_GOOGLEPAY = "320";

	public final static int GOOGLE_API_VERSION = 2;
	public final static String 	GOOGLE_PAY_TOKEN_FIELD_ID = "encryptedPaymentData";

	/** Link placeholder for label texts **/
	public static String LINK_PLACEHOLDER = "{link}";
}
