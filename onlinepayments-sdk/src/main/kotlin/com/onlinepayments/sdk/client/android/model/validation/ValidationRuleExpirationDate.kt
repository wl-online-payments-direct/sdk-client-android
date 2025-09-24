/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2025 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */
package com.onlinepayments.sdk.client.android.model.validation

import com.onlinepayments.sdk.client.android.model.PaymentRequest
import java.lang.Exception
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

/**
 * Validation rule for expiration date.
 */
class ValidationRuleExpirationDate internal constructor() : AbstractValidationRule(
    "expirationDate",
    ValidationType.EXPIRATIONDATE
) {

    /**
     * Validates an expiration date.
     *
     * @param paymentRequest the fully filled [PaymentRequest] that will be used for doing a payment
     * @param fieldId the ID of the field to which to apply the current validator
     *
     * @return true, if the value in the field with fieldId is a valid expiration date; false, if it is not a valid expiration date or the fieldId could not be found
     */
    override fun validate(paymentRequest: PaymentRequest, fieldId: String): Boolean {
        val text = getUnmaskedValue(paymentRequest, fieldId) ?: return false

        try {
            val enteredDate = obtainEnteredDateFromUnmaskedValue(text)

            val calendar: Calendar = GregorianCalendar()
            calendar.setTime(Date())
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 25)
            val futureLimit = calendar.getTime()

            return validateDateIsBetween(Date(), futureLimit, enteredDate!!)
        } catch (_: Exception) {
            return false
        }
    }

    @Throws(ParseException::class)
    fun obtainEnteredDateFromUnmaskedValue(text: String): Date? {
        // Parse the input to date and see if this is in the future
        val fieldDateFormat: DateFormat = SimpleDateFormat(
            DATE_FORMAT_PATTERN_MONTH_YEAR,
            Locale.ROOT
        )
        val centuryDateFormat: DateFormat = SimpleDateFormat(
            DATE_FORMAT_PATTERN_CENTURY,
            Locale.ROOT
        )

        fieldDateFormat.isLenient = false

        // Add centuries to prevent swapping back to previous century with yy date format pattern:
        // http://docs.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#year
        val now = Date()
        val year = centuryDateFormat.format(now)

        var textWithCentury = text
        if (text.length == 4) {
            textWithCentury = (text.substring(0, 2) + year.substring(0, 2) + text.substring(2, 4))
        }

        return fieldDateFormat.parse(textWithCentury)
    }

    /**
     * Validates whether the month and year of the 'dateToValidate' is between month and year of 'now' and 'futureDate'.
     * Validation happens inclusive. E.g. if dateToValidate = 01-2019 and now = 01-2019, true is returned.
     *
     * @param now lower threshold of the comparison, is expected to be the current Date
     * @param futureDate upper threshold of the comparison, futureDate should be > now
     * @param dateToValidate the date that should be checked to be between now and futureDate
     *
     * @return true, if and only if dateToValidate is inclusive between now and futureDate; false otherwise.
     */
    fun validateDateIsBetween(now: Date, futureDate: Date, dateToValidate: Date): Boolean {
        // Before comparison, this method generates Dates that only have a month and year (for the lower bound)
        // or just a year (for the upper bound).

        var now = now
        var futureDate = futureDate
        var dateToValidate = dateToValidate
        val calendar = Calendar.getInstance()

        val nowCalendar = Calendar.getInstance()
        nowCalendar.setTime(now)

        // Set the current time a month earlier, to make sure that an expiry date in the current month
        // is accepted.
        calendar.clear()
        calendar.set(Calendar.MONTH, nowCalendar.get(Calendar.MONTH) - 1)
        calendar.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR))
        now = calendar.getTime()

        val dateToValidateCalendar = Calendar.getInstance()
        dateToValidateCalendar.setTime(dateToValidate)

        calendar.clear()
        calendar.set(Calendar.MONTH, dateToValidateCalendar.get(Calendar.MONTH))
        calendar.set(Calendar.YEAR, dateToValidateCalendar.get(Calendar.YEAR))
        dateToValidate = calendar.getTime()

        // Validate lower bound
        if (!dateToValidate.after(now)) {
            return false
        }

        // Prepare upperbound validation
        val futureDateCalendar = Calendar.getInstance()
        futureDateCalendar.setTime(futureDate)

        // Increase the upperbound by a year, in order to be inclusive of expiry dates that are in the
        // same year as the upperbound.
        calendar.clear()
        calendar.set(Calendar.YEAR, futureDateCalendar.get(Calendar.YEAR) + 1)
        futureDate = calendar.getTime()

        calendar.clear()
        calendar.set(Calendar.YEAR, dateToValidateCalendar.get(Calendar.YEAR))
        dateToValidate = calendar.getTime()

        // Validate upperbound
        return dateToValidate.before(futureDate)
    }

    companion object {
        @Suppress("Unused")
        private val serialVersionUID = -8737074337688865517L

        private const val DATE_FORMAT_PATTERN_MONTH_YEAR = "MMyyyy"
        private const val DATE_FORMAT_PATTERN_CENTURY = "yyyy"
    }
}
