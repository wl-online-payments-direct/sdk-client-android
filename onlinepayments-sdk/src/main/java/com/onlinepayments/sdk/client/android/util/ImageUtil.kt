/*
 * Copyright 2017 Global Collect Services B.V
 */

@file:JvmSynthetic

package com.onlinepayments.sdk.client.android.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsPaymentItem
import com.onlinepayments.sdk.client.android.model.paymentproduct.displayhints.DisplayHintsProductFields
import java.io.IOException
import java.io.InputStream
import java.net.URL

internal object ImageUtil {
    @JvmSynthetic
    fun setLogoForDisplayHintsList(displayHintsList: List<DisplayHintsPaymentItem>, context: Context) {
        for (displayHints in displayHintsList) {
            this.setLogoForDisplayHints(displayHints, context)
        }
    }

    @JvmSynthetic
    fun setLogoForDisplayHints(displayHints: DisplayHintsPaymentItem, context: Context) {
        val logo = this.getImageFromStringUrl(displayHints.logoUrl, context)

        displayHints.logo = logo
    }

    @JvmSynthetic
    fun setImageForTooltip(displayHintsProductFields: DisplayHintsProductFields, context: Context) {
        val tooltip = displayHintsProductFields.tooltip
        val tooltipImage = this.getImageFromStringUrl(tooltip.imageURL, context)
        displayHintsProductFields.tooltip.imageDrawable = tooltipImage
    }

    private fun getImageFromStringUrl(url: String?, context: Context): Drawable? {
        return try {
            val bitmap = BitmapFactory.decodeStream(URL(url).content as InputStream)
            BitmapDrawable(context.resources, bitmap)
        } catch (e: IOException) {
            null
        }
    }
}
