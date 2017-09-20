/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * Copyright (C) 2017, IS2T - EDC compliance and optimizations.
 *
 * Portions copyright 2006-2009 James Murty. Please see LICENSE.txt
 * for applicable license terms and NOTICE.txt for applicable notices.
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
package com.amazonaws.util;

/**
 * Utilities for encoding and decoding binary data to and from different forms.
 */
public class BinaryUtils {
	/**
	 * Converts byte data to a Hex-encoded string in lower case.
	 *
	 * @param data
	 *            data to hex encode.
	 *
	 * @return hex-encoded string.
	 */
	public static String toHex(byte[] data) {
		return Base16Lower.encodeAsString(data);
	}

}
