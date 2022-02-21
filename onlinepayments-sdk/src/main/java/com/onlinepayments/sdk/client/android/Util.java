package com.onlinepayments.sdk.client.android;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.encryption.EncryptUtil;
import com.google.gson.Gson;

/**
 * Contains util methods for getting device metadata
 *
 * Copyright 2020 Global Collect Services B.V
 *
 */
public class Util {

	private static final EncryptUtil encryptUtil = new EncryptUtil();

	// Metadata map keys
	private static final String METADATA_PLATFORM_IDENTIFIER = "platformIdentifier";
	private static final String METADATA_APP_IDENTIFIER      = "appIdentifier";
	private static final String METADATA_SDK_IDENTIFIER      = "sdkIdentifier";
	private static final String METADATA_SDK_CREATOR         = "sdkCreator";

	private static final String METADATA_SCREENSIZE          = "screenSize";
	private static final String METADATA_DEVICE_BRAND        = "deviceBrand";
	private static final String METADATA_DEVICE_TYPE         = "deviceType";
	private static final String METADATA_IP_ADDRESS          = "ipAddress";

	/**
	 * Returns map of metadata of the device this SDK is running on
	 * The map contains the SDK version, OS, OS version and screensize
	 *
	 * @return Map<String, String> containing key/values of metadata
	 * @deprecated use {@link #getMetadata(Context, String, String)} instead.
	 */
	@Deprecated
	public static Map<String, String> getMetadata(Context context) {
		return getMetadata(context, null, null);
	}

	/**
	 * Returns map of metadata of the device this SDK is running on
	 * The map contains the SDK version, OS, OS version and screensize
	 *
	 * @param appIdentifier a String that describes the application, preferably with version number.
	 * @param ipAddress the public ip-address of the device.
	 * @return Map<String, String> containing key/values of metadata
	 */
	public static Map<String, String> getMetadata(Context context, String appIdentifier, String ipAddress) {

		if (context == null) {
			throw new InvalidParameterException("Error creating metadata, context may not be null.");
		}

		Map<String, String> metaData = new HashMap<String, String>();

		// Add OS + buildversion
		metaData.put(METADATA_PLATFORM_IDENTIFIER, "Android/" + android.os.Build.VERSION.RELEASE);

		// Add APP identifier (optional)
		if (appIdentifier != null && !appIdentifier.isEmpty()) {
			metaData.put(METADATA_APP_IDENTIFIER, appIdentifier);
		} else {
			metaData.put(METADATA_APP_IDENTIFIER, "unknown");
		}

		// Add SDK version
		metaData.put(METADATA_SDK_IDENTIFIER, Constants.SDK_IDENTIFIER);
		metaData.put(METADATA_SDK_CREATOR,    Constants.SDK_CREATOR);

		// Add screensize
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);
		metaData.put(METADATA_SCREENSIZE, metrics.heightPixels + "x" + metrics.widthPixels);

		// Add device brand
		metaData.put(METADATA_DEVICE_BRAND, android.os.Build.MANUFACTURER);

		// Add device type
		metaData.put(METADATA_DEVICE_TYPE, android.os.Build.MODEL);

		// Add ipAddress (can only be set by the merchant)
		if (ipAddress != null && !ipAddress.isEmpty()) {
			metaData.put(METADATA_IP_ADDRESS, ipAddress);
		}
		return metaData;
	}

	/**
	 * Returns base64 encoded version of a map of metadata of the device this SDK is running on
	 * The map contains the SDK version, OS, OS version and screensize
	 *
	 * @return String containing base64 url of json representation of the metadata
	 * @deprecated use {@link #getBase64EncodedMetadata(Context, String)} or {@link #getBase64EncodedMetadata(Context, String, String)} instead.
	 */
	@Deprecated
	public static String getBase64EncodedMetadata(Context context) {

		Gson gson = new Gson();
		String jsonMetadata = gson.toJson(getMetadata(context));
		String encodedData = encryptUtil.base64UrlEncode(jsonMetadata.getBytes());

		return encodedData;
	}

	/**
	 * Returns base64 encoded version of a map of metadata of the device this SDK is running on
	 * The map contains the SDK version, OS, OS version and screensize
	 *
	 *  @param appIdentifier a String that describes the application, preferably with version number.
	 * @return String containing base64 url of json representation of the metadata
	 */
	public static String getBase64EncodedMetadata(Context context, String appIdentifier) {

		Gson gson = new Gson();
		String jsonMetadata = gson.toJson(getMetadata(context, appIdentifier, null));
		String encodedData = encryptUtil.base64UrlEncode(jsonMetadata.getBytes());

		return encodedData;
	}

	/**
	 * Returns base64 encoded version of a map of metadata of the device this SDK is running on
	 * The map contains the SDK version, OS, OS version and screensize
	 *
	 * @param appIdentifier a String that describes the application, preferably with version number.
	 * @param ipAddress the public ip-address of the device.
	 * @return String containing base64 url of json representation of the metadata
	 */
	public static String getBase64EncodedMetadata(Context context, String appIdentifier, String ipAddress) {

		Gson gson = new Gson();
		String jsonMetadata = gson.toJson(getMetadata(context, appIdentifier, ipAddress));
		String encodedData = encryptUtil.base64UrlEncode(jsonMetadata.getBytes());

		return encodedData;
	}


	/**
	 * Returns base64 encoded map of metadata.
	 *
	 * @param metadata, map of metadata which is base64 encoded
	 *
	 * @return String containing base64 url of json representation of the metadata
	 */
	public static String getBase64EncodedMetadata(Map<String, String> metadata) {

		Gson gson = new Gson();
		String jsonMetadata = gson.toJson(metadata);
		String encodedData = encryptUtil.base64UrlEncode(jsonMetadata.getBytes());

		return encodedData;
	}
}
