/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.manager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.onlinepayments.sdk.client.android.configuration.Constants;
import com.onlinepayments.sdk.client.android.asynctask.LoadImageAsyncTask;
import com.onlinepayments.sdk.client.android.asynctask.LoadImageAsyncTask.OnImageLoadedListener;
import com.onlinepayments.sdk.client.android.caching.CacheHandler;
import com.onlinepayments.sdk.client.android.caching.Preferences;
import com.onlinepayments.sdk.client.android.model.Size;
import com.onlinepayments.sdk.client.android.model.paymentproduct.BasicPaymentItem;
import com.google.gson.reflect.TypeToken;


/**
 * Singleton who handles all logo related functionality.
 *
 * @deprecated In a future release this class, its functions and its properties will be removed. Instead please retrieve the logo / tooltip image directly from the PaymentItem. If an image is not available, or you wish to retrieve it again, you can retrieve it from the logo's / tooltip image's URL that is also stored in the PaymentItem.
 */
@Deprecated
public class AssetManager implements OnImageLoadedListener {
	// Tag used for logging
	private static final String TAG = AssetManager.class.getName();

	// Name of cache in preferences
	private static final String LOGO_MAPPING_FILENAME = "initial_logo_mapping.list";

	// Prefix for logo images
	private static final String LOGO_PREFIX = "pp_logo_";

	// Singleton instance
	private static AssetManager INSTANCE;

	// Context needed for reading and writing files
	private Context context;

	// Classes used for storing cache of paymentproducts and their logos
	private Preferences preferences = new Preferences();
	private CacheHandler cacheHandler;

	/**
	 * Private constructor for Singleton purposes.
	 *
	 * @param context needed for reading and writing files
	 */
	private AssetManager(Context context) {
		this.context = context;
		cacheHandler = new CacheHandler(context);
	}

	/**
	 * Gets Singleton instance of this AssetManager.
	 *
	 * @param context needed for reading and writing files
	 *
	 * @return AssetManager singleton instance
	 */
	public static synchronized AssetManager getInstance(Context context) {

		if (context == null) {
			throw new InvalidParameterException("Error creating AssetManager, context may not be null");
		}

		if (INSTANCE == null) {
			INSTANCE = new AssetManager(context.getApplicationContext());
		}
		return INSTANCE;
	}

	/**
	 * Retrieves a logo for the given paymentProductId.
	 * First the diskcache is checked for this logo.
	 * If it doesn't exist there, the version that is stored in the app is returned.
	 *
	 * @param paymentProductId the paymentProductId for which the logo is returned
	 *
	 * @return Drawable logo
	 */
	public Drawable getLogo(String paymentProductId) {

		// Check if the logo for this paymentProduct is in the cache and return it
		Drawable logo = cacheHandler.getImageFromInternalStorage(paymentProductId, context.getResources());
		if (logo != null) {
			return logo;
		} else {

			// Else return the original logo for this paymentProduct
			@SuppressLint("DiscouragedApi")
			Integer logoId = context.getResources().getIdentifier(LOGO_PREFIX + paymentProductId, "drawable", context.getPackageName());
			if (logoId != 0) {
				return context.getResources().getDrawable(logoId);
			}
		}

		return null;
	}

	/**
	 * Update the logos for the given basicPaymentItems if there is a new version.
	 *
	 * @param assetUrl the asset url for loading images
	 * @param basicPaymentItems list of basic payment items for which the logo will be updated if there is a new version
	 * @param size can be used to retrieve images of a certain size
	 */
	public void updateLogos(String assetUrl, List<BasicPaymentItem> basicPaymentItems, Size size) {

		// Get the map containg all logos per paymentProductId from the preferences
		Type listType = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> logoMapping = preferences.getMapFromSharedPreferences(Constants.PREFERENCES_LOGO_MAP,
				context,
				listType,
				new HashMap<String, String>());

		// If there is no logo mapping in the preferences,
		// Read the initial logo mapping from the LOGO_MAPPING_FILENAME file.
		// Store that mapping in the user preferences so it will be cached there.
		if (logoMapping == null) {
			logoMapping = readInitialLogoMapping();
		}

		if (logoMapping != null && basicPaymentItems != null) {

			// Loop trough all BasicPaymentProducts to see if their logo must be updated
			for (BasicPaymentItem product : basicPaymentItems) {

				String url = logoMapping.get(product.getId());

				// Check if the logo url is different for the paymentproduct
				if (url == null || !url.equals(product.getDisplayHints().getLogoUrl())) {

					// Update the image
					getImageFromUrl(assetUrl, logoMapping, product, size);
				}
			}
		}
	}

	/**
	 * Parses the key/value map in the file LOGO_MAPPING_FILENAME to a Map containing all initial logo url per paymentProductId.
	 *
	 * @return Map containing all logos for paymentProducts
	 */
	private Map<String, String> readInitialLogoMapping() {

		// Parse the LOGO_MAPPING_FILENAME as a properties file
		Properties properties = new Properties();

		try(InputStream inputStream = context.getAssets().open(LOGO_MAPPING_FILENAME)) {
			properties.load(inputStream);
		} catch (IOException e) {
			return null;
		}

		// Fill the logoMapping with all entries
		Map<String, String> logoMapping = new HashMap<>();
		for (Entry<Object, Object> property : properties.entrySet()) {
			logoMapping.put((String) property.getKey(), (String) property.getValue());
		}

		return logoMapping;
	}

	/**
	 * Loads an image from the {@link BasicPaymentItem}'s logo url.
	 *
	 * @param assetUrl the asset url for loading images
	 * @param logoMapping map containing mapping with url's and paymentProductId
	 * @param product image is loaded for this {@link BasicPaymentItem}
	 * @param size can be used to retrieve images of certain size
	 */
	private void getImageFromUrl(String assetUrl, Map<String, String> logoMapping, BasicPaymentItem product, Size size) {

		// Determine the complete url
		String logoUrl = product.getDisplayHints().getLogoUrl();
		if (logoUrl == null) {
			return;
		}

		String completeUrl = assetUrl + logoUrl;

		if (size != null) {
			//if you did specify the size, you just get the resized image
			completeUrl += "?size=" + size.getWidth() + "x" + size.getHeight();
		}

		// And load images on asynctask
		LoadImageAsyncTask task = new LoadImageAsyncTask(completeUrl, product.getId(), context, logoMapping, logoUrl, this);
		task.execute();
	}

	public Drawable getImageFromStringUrl(String url){
		try {
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent(), null, null);
			return new BitmapDrawable(context.getResources(), bitmap);
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	public Drawable getImageFromStringUrl(String url, String paymentProductId){
		try {
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent(), null, null);
			BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);
			cacheHandler.saveImageOnInternalStorage(paymentProductId, bitmapDrawable);
			return bitmapDrawable;
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void onImageLoaded(Drawable image, String productId, Map<String, String> logoMapping, String url) {

		if (image != null) {

			// Save/Update the image on the Internal Storage
			cacheHandler.saveImageOnInternalStorage(productId, image);

			// Update the logo mapping in preferences
			logoMapping.put(productId, url);

			// Store the updated mapping
			preferences.storeInSharedPreferences(Constants.PREFERENCES_LOGO_MAP, logoMapping, context);
		}
	}
}
