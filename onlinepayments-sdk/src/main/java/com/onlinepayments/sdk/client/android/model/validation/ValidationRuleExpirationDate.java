/*
 * Copyright 2020 Global Collect Services B.V
 */

package com.onlinepayments.sdk.client.android.model.validation;

import android.util.Log;

import com.onlinepayments.sdk.client.android.model.PaymentRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Validation rule for expiration date.
 */
public class ValidationRuleExpirationDate extends AbstractValidationRule {

	private static final long serialVersionUID = -8737074337688865517L;

	private static final String TAG = ValidationRuleExpirationDate.class.getName();

	private static String DATE_FORMAT_PATTERN_MONTH_YEAR = "MMyyyy";
	private static String DATE_FORMAT_PATTERN_CENTURY = "yyyy";

	/**
	 * @deprecated This constructor is for internal use only.
	 */
	@Deprecated
	public ValidationRuleExpirationDate() {
		super("expirationDate", ValidationType.EXPIRATIONDATE);
	}

	/**
	 * @deprecated In a future release, this constructor will be removed.
	 */
	@Deprecated
	public ValidationRuleExpirationDate(String errorMessage, ValidationType type) {
		super(errorMessage, type);
	}

	/**
	 * Validates an expiration date.
	 *
	 * @param text the expiration date to be validated, as a String
	 *
	 * @return whether the expiration date is valid or not
	 *
	 * @deprecated use {@link #validate(PaymentRequest, String)} instead.
	 */
	@Override
	@Deprecated
	public boolean validate(String text) {
		Log.w(TAG, "This method is deprecated and should not be used! Use <validate(PaymentRequest paymentRequest, String)> instead.");

		if (text == null) {
			return false;
		}

		// Parse the input to date and see if this is in the future
		DateFormat fieldDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN_MONTH_YEAR, Locale.ROOT);
		DateFormat centuryDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN_CENTURY, Locale.ROOT);
		fieldDateFormat.setLenient(false);

		try {

			// Add centuries to prevent swapping back to previous century with yy dateformatpattern:
			// http://docs.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#year
			Date now = new Date();
			String year = centuryDateFormat.format(now);

			String textWithCentury = text.substring(0, 2) + year.substring(0, 2) +  text.substring(2, 4);

			//text = text.replace("/", "/" + year.substring(0, 2));
			Date enteredDate = fieldDateFormat.parse(textWithCentury);

			// Add 1 month so it's easier to compare, since this month is also a valid date
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(enteredDate);
			calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
			enteredDate = calendar.getTime();

			// Check if enteredDate > today
			if (enteredDate.before(new Date())) {
				return false;
			}

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Validates an expiration date.
	 *
	 * @param paymentRequest the fully filled {@link PaymentRequest} that will be used for doing a payment
	 * @param fieldId the ID of the field to which to apply the current validator
	 *
	 * @return true, if the value in the field with fieldId is a valid expiration date; false, if it is not a valid expiration date or the fieldId could not be found
	 */
	@Override
	public boolean validate(PaymentRequest paymentRequest, String fieldId) {

		String text = paymentRequest.getValue(fieldId);

		if (text == null) {
			return false;
		}

		text = paymentRequest.getUnmaskedValue(fieldId, text);

		try {

			Date enteredDate = obtainEnteredDateFromUnmaskedValue(text);

			Calendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 25);
			Date futureLimit = calendar.getTime();

			return validateDateIsBetween(new Date(), futureLimit, enteredDate);
		} catch (Exception e) {
			return false;
		}
	}

	Date obtainEnteredDateFromUnmaskedValue(String text) throws ParseException {
		// Parse the input to date and see if this is in the future
		DateFormat fieldDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN_MONTH_YEAR, Locale.ROOT);
		DateFormat centuryDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN_CENTURY, Locale.ROOT);
		fieldDateFormat.setLenient(false);

		// Add centuries to prevent swapping back to previous century with yy dateformatpattern:
		// http://docs.oracle.com/javase/1.5.0/docs/api/java/text/SimpleDateFormat.html#year
		Date now = new Date();
		String year = centuryDateFormat.format(now);

		String textWithCentury = text.substring(0, 2) + year.substring(0, 2) +  text.substring(2, 4);

		return fieldDateFormat.parse(textWithCentury);
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
	boolean validateDateIsBetween(Date now, Date futureDate, Date dateToValidate) {
		// Before comparison, this method generates Dates that only have a month and year (for the lower bound)
		// or just a year (for the upper bound).

		Calendar calendar = Calendar.getInstance();

		Calendar nowCalendar = Calendar.getInstance();
		nowCalendar.setTime(now);

		// Set the current time a month earlier, to make sure that an expiry date in the current month
		// is accepted.
		calendar.clear();
		calendar.set(Calendar.MONTH, nowCalendar.get(Calendar.MONTH) - 1);
		calendar.set(Calendar.YEAR, nowCalendar.get(Calendar.YEAR));
		now = calendar.getTime();


		Calendar dateToValidateCalendar = Calendar.getInstance();
		dateToValidateCalendar.setTime(dateToValidate);

		calendar.clear();
		calendar.set(Calendar.MONTH, dateToValidateCalendar.get(Calendar.MONTH));
		calendar.set(Calendar.YEAR, dateToValidateCalendar.get(Calendar.YEAR));
		dateToValidate = calendar.getTime();

		// Validate lowerbound
		if (!dateToValidate.after(now)) {
			return false;
		}


		// Prepare upperbound validation
		Calendar futureDateCalendar = Calendar.getInstance();
		futureDateCalendar.setTime(futureDate);

		// Increase the upperbound by a year, in order to be inclusive of expiry dates that are in the
		// same year as the upperbound.
		calendar.clear();
		calendar.set(Calendar.YEAR, futureDateCalendar.get(Calendar.YEAR) + 1);
		futureDate = calendar.getTime();

		calendar.clear();
		calendar.set(Calendar.YEAR, dateToValidateCalendar.get(Calendar.YEAR));
		dateToValidate = calendar.getTime();

		// Validate upperbound
        return dateToValidate.before(futureDate);
    }

}
