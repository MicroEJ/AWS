/*
 * Copyright 2014-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Copyright (C) 2017, IS2T - EDC compliance and optimizations.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.auth.internal;

import java.util.Calendar;

/**
 * Utility methods that is used by the different AWS Signer implementations.
 * This class is strictly internal and is subjected to change.
 */
public final class AWS4SignerUtils {

	/**
	 * Returns a string representation of the given date time in yyyyMMdd
	 * format. The date returned is in the UTC zone.
	 *
	 * For example, given a time "1416863450581", this method returns "20141124"
	 */
	public static String formatDateStamp(long timeMilli) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMilli);

		StringBuilder formattedTime = new StringBuilder();
		formattedTime.append(calendar.get(Calendar.YEAR));
		formattedTime.append(prependZeros(calendar.get(Calendar.MONTH) + 1));
		formattedTime.append(prependZeros(calendar.get(Calendar.DAY_OF_MONTH)));
		return formattedTime.toString();
	}

	/**
	 * Returns a string representation of the given date time in
	 * yyyyMMdd'T'HHmmss'Z' format. The date returned is in the UTC zone.
	 *
	 * For example, given a time "1416863450581", this method returns
	 * "20141124T211050Z"
	 */
	public static String formatTimestamp(long timeMilli) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeMilli);

		StringBuilder formattedTime = new StringBuilder();
		formattedTime.append(calendar.get(Calendar.YEAR));
		formattedTime.append(prependZeros(calendar.get(Calendar.MONTH) + 1));
		formattedTime.append(prependZeros(calendar.get(Calendar.DAY_OF_MONTH)));
		formattedTime.append('T');
		formattedTime.append(prependZeros(calendar.get(Calendar.HOUR_OF_DAY)));
		formattedTime.append(prependZeros(calendar.get(Calendar.MINUTE)));
		formattedTime.append(prependZeros(calendar.get(Calendar.SECOND)));
		formattedTime.append('Z');
		return formattedTime.toString();
	}

	private static String prependZeros(int value) {
		StringBuilder builder = new StringBuilder();

		if (value < 10) {
			builder.append('0');
		}
		builder.append(value);

		return builder.toString();
	}
}
