/*
 * Do not remove or alter the notices in this preamble.
 *
 * Copyright © 2026 Worldline and/or its affiliates.
 *
 * All rights reserved. License grant and user rights and obligations according to the applicable license agreement.
 *
 * Please contact Worldline for questions regarding license and user rights.
 */

package com.onlinepayments.sdk.client.android.domain.validation.rules

import com.onlinepayments.sdk.client.android.domain.validation.RuleValidationResult
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

/**
 * Validation rule for expiration date.
 * Note: should be rewritten with modern Java API when min SDK is 26+
 */
class ValidationRuleExpirationDate internal constructor() : ValidationRule(
    "expirationDate",
    ValidationRuleType.EXPIRATIONDATE
) {
    /**
     * Validates an expiration date.
     *
     * Accepts dates in MMYY or MMYYYY format. The date must be:
     * - In the current month or later
     * - Within the next 25 years
     *
     * @param value Expiration date in MMYY or MMYYYY format
     * @return Validation result indicating if the date is valid
     */
    override fun validate(value: String?): RuleValidationResult {
        if (value.isNullOrEmpty()) {
            return RuleValidationResult(false, "Expiration date is required.")
        }

        val isValid = runCatching {
            val enteredDate = obtainEnteredDateFromUnmaskedValue(value)

            val futureLimit = GregorianCalendar().apply {
                time = Date()
                set(
                    Calendar.YEAR,
                    get(Calendar.YEAR) + MAX_YEARS
                )
            }.time

            validateDateIsBetween(Date(), futureLimit, enteredDate!!)
        }.getOrDefault(false)

        return RuleValidationResult(
            valid = isValid,
            message = if (isValid) "" else "Invalid expiration date."
        )
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
        val century = centuryDateFormat.format(now).take(2)

        var textWithCentury = text
        val month = text.take(2)
        if (text.length == SHORT_YEAR_DIGITS) {
            val year = text.substring(2, SHORT_YEAR_DIGITS)
            textWithCentury = month + century + year
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
        private const val serialVersionUID = -8737074337688865517L

        /**
         * Maximum number of years in the future that an expiration date can be.
         * Credit cards typically have a maximum validity of 5 years, but we allow 25 years
         * to accommodate edge cases and future-dated test cards.
         */
        private const val MAX_YEARS = 25

        private const val DATE_FORMAT_PATTERN_MONTH_YEAR = "MMyyyy"
        private const val DATE_FORMAT_PATTERN_CENTURY = "yyyy"
        private const val SHORT_YEAR_DIGITS = 4
    }
}

