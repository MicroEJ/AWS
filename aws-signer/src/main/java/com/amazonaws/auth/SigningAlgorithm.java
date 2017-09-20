/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package com.amazonaws.auth;

import javax.crypto.Mac;

import com.amazonaws.SdkClientException;

import ej.security.NoSuchAlgorithmException;

public enum SigningAlgorithm {

	HmacSHA256;

	private final Mac macReference;

	private SigningAlgorithm() {
		final String algorithmName = this.toString();
		try {
			macReference = Mac.getInstance(algorithmName);
		} catch (NoSuchAlgorithmException e) {
			throw new SdkClientException("Unable to fetch Mac instance for Algorithm "
					+ algorithmName + e.getMessage(),e);

		}
	}

	/**
	 * Returns the thread local reference for the crypto algorithm
	 */
	public Mac getMac() {
		return macReference;
	}
}
