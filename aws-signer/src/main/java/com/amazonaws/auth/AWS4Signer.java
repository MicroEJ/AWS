/*
 * Copyright 2013-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import static com.amazonaws.auth.internal.SignerConstants.AUTHORIZATION;
import static com.amazonaws.auth.internal.SignerConstants.AWS4_SIGNING_ALGORITHM;
import static com.amazonaws.auth.internal.SignerConstants.AWS4_TERMINATOR;
import static com.amazonaws.auth.internal.SignerConstants.HOST;
import static com.amazonaws.auth.internal.SignerConstants.LINE_SEPARATOR;
import static com.amazonaws.auth.internal.SignerConstants.X_AMZ_CONTENT_SHA256;
import static com.amazonaws.auth.internal.SignerConstants.X_AMZ_DATE;
import static com.amazonaws.util.StringUtils.UTF8;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.amazonaws.ReadLimitInfo;
import com.amazonaws.SdkClientException;
import com.amazonaws.SignableRequest;
import com.amazonaws.auth.internal.AWS4SignerRequestParams;
import com.amazonaws.util.BinaryUtils;
import com.amazonaws.util.SdkHttpUtils;
import com.amazonaws.util.StringUtils;

/**
 * Signer implementation that signs requests with the AWS4 signing protocol.
 */
public class AWS4Signer extends AbstractAWSSigner {

	public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

	private static final List<String> listOfHeadersToIgnoreInLowerCase = Arrays.asList("connection", "x-amzn-trace-id");

	/**
	 * Service name override for use when the endpoint can't be used to
	 * determine the service name.
	 */
	protected String serviceName;

	/**
	 * Endpoint prefix to compute the region name for signing
	 * when the {@link #regionName} is null.
	 */
	private String endpointPrefix;

	/**
	 * Region name override for use when the endpoint can't be used to determine
	 * the region name.
	 */
	protected String regionName;

	/** Date override for testing only */
	protected Date overriddenDate;

	/**
	 * Whether double url-encode the resource path when constructing the
	 * canonical request. By default, we enable double url-encoding.
	 *
	 * TODO: Different sigv4 services seem to be inconsistent on this. So for
	 * services that want to suppress this, they should use new
	 * AWS4Signer(false).
	 */
	protected boolean doubleUrlEncode;

	/**
	 * Construct a new AWS4 signer instance. By default, enable double
	 * url-encoding.
	 */
	public AWS4Signer() {
		this(true);
	}

	/**
	 * Construct a new AWS4 signer instance.
	 *
	 * @param doubleUrlEncoding
	 *            Whether double url-encode the resource path when constructing
	 *            the canonical request.
	 */
	public AWS4Signer(boolean doubleUrlEncoding) {
		this.doubleUrlEncode = doubleUrlEncoding;
	}

	/**
	 * Sets the service name that this signer should use when calculating
	 * request signatures. This can almost always be determined directly from
	 * the request's end point, so you shouldn't need this method, but it's
	 * provided for the edge case where the information is not in the endpoint.
	 *
	 * @param serviceName
	 *            The service name to use when calculating signatures in this
	 *            signer.
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Sets the region name that this signer should use when calculating request
	 * signatures. This can almost always be determined directly from the
	 * request's end point, so you shouldn't need this method, but it's provided
	 * for the edge case where the information is not in the endpoint.
	 *
	 * @param regionName
	 *            The region name to use when calculating signatures in this
	 *            signer.
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	/**
	 * Sets the endpoint prefix which is used to compute the region that is
	 * used for signing the request.
	 *
	 * This value is passed to {@link AWS4SignerRequestParams} class which
	 * has the logic to compute region.
	 *
	 * @param endpointPrefix The endpoint prefix of the service
	 */
	public void setEndpointPrefix(String endpointPrefix) {
		this.endpointPrefix = endpointPrefix;
	}

	/**
	 * Sets the date that overrides the signing date in the request. This method
	 * is internal and should be used only for testing purposes.
	 */
	public void setOverrideDate(Date overriddenDate) {
		if (overriddenDate != null) {
			this.overriddenDate = new Date(overriddenDate.getTime());
		} else {
			this.overriddenDate = null;
		}
	}

	@Override
	public void sign(SignableRequest<?> request, AWSCredentials credentials) {
		AWSCredentials sanitizedCredentials = sanitizeCredentials(credentials);

		final AWS4SignerRequestParams signerParams = new AWS4SignerRequestParams(
				request, overriddenDate, regionName, serviceName,
				AWS4_SIGNING_ALGORITHM, endpointPrefix);

		addHostHeader(request);
		request.addHeader(X_AMZ_DATE,
				signerParams.getFormattedSigningDateTime());

		String contentSha256 = calculateContentHash(request);

		if ("required".equals(request.getHeaders().get(X_AMZ_CONTENT_SHA256))) {
			request.addHeader(X_AMZ_CONTENT_SHA256, contentSha256);
		}

		final String canonicalRequest = createCanonicalRequest(request,
				contentSha256);

		final String stringToSign = createStringToSign(canonicalRequest,
				signerParams);

		final byte[] signingKey = deriveSigningKey(sanitizedCredentials,
				signerParams);

		final byte[] signature = computeSignature(stringToSign, signingKey,
				signerParams);

		request.addHeader(
				AUTHORIZATION,
				buildAuthorizationHeader(request, signature,
						sanitizedCredentials, signerParams));

		processRequestPayload(request, signature, signingKey,
				signerParams);
	}

	/**
	 * Step 1 of the AWS Signature version 4 calculation. Refer to
	 * http://docs.aws
	 * .amazon.com/general/latest/gr/sigv4-create-canonical-request.html to
	 * generate the canonical request.
	 */
	protected String createCanonicalRequest(SignableRequest<?> request,
			String contentSha256) {
		/* This would url-encode the resource path for the first time. */
		final String path = SdkHttpUtils.appendUri(
				request.getEndpoint().getPath(), request.getResourcePath());

		final StringBuilder canonicalRequestBuilder = new StringBuilder(request
				.getHttpMethod().toString());

		canonicalRequestBuilder.append(LINE_SEPARATOR)
		// This would optionally double url-encode the resource path
		.append(getCanonicalizedResourcePath(path, doubleUrlEncode))
		.append(LINE_SEPARATOR)
		.append(getCanonicalizedQueryString(request))
		.append(LINE_SEPARATOR)
		.append(getCanonicalizedHeaderString(request))
		.append(LINE_SEPARATOR)
		.append(getSignedHeadersString(request)).append(LINE_SEPARATOR)
		.append(contentSha256);

		final String canonicalRequest = canonicalRequestBuilder.toString();
		return canonicalRequest;
	}

	/**
	 * Step 2 of the AWS Signature version 4 calculation. Refer to
	 * http://docs.aws
	 * .amazon.com/general/latest/gr/sigv4-create-string-to-sign.html.
	 */
	protected String createStringToSign(String canonicalRequest,
			AWS4SignerRequestParams signerParams) {
		final StringBuilder stringToSignBuilder = new StringBuilder(
				signerParams.getSigningAlgorithm());
		stringToSignBuilder.append(LINE_SEPARATOR)
		.append(signerParams.getFormattedSigningDateTime())
		.append(LINE_SEPARATOR)
		.append(signerParams.getScope())
		.append(LINE_SEPARATOR)
		.append(BinaryUtils.toHex(hash(canonicalRequest)));

		final String stringToSign = stringToSignBuilder.toString();
		return stringToSign;
	}

	/**
	 * Step 3 of the AWS Signature version 4 calculation. It involves deriving
	 * the signing key and computing the signature. Refer to
	 * http://docs.aws.amazon
	 * .com/general/latest/gr/sigv4-calculate-signature.html
	 */
	private final byte[] deriveSigningKey(AWSCredentials credentials,
			AWS4SignerRequestParams signerRequestParams) {
		byte[] signingKey = newSigningKey(credentials,
				signerRequestParams.getFormattedSigningDate(),
				signerRequestParams.getRegionName(),
				signerRequestParams.getServiceName());
		return signingKey;
	}

	/**
	 * Step 3 of the AWS Signature version 4 calculation. It involves deriving
	 * the signing key and computing the signature. Refer to
	 * http://docs.aws.amazon
	 * .com/general/latest/gr/sigv4-calculate-signature.html
	 */
	protected final byte[] computeSignature(String stringToSign,
			byte[] signingKey, AWS4SignerRequestParams signerRequestParams) {
		try {
			return sign(stringToSign.getBytes(UTF8), signingKey, SigningAlgorithm.HmacSHA256);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates the authorization header to be included in the request.
	 */
	private String buildAuthorizationHeader(SignableRequest<?> request,
			byte[] signature, AWSCredentials credentials,
			AWS4SignerRequestParams signerParams) {
		final String signingCredentials = credentials.getAWSAccessKeyId() + "/"
				+ signerParams.getScope();

		final String credential = "Credential="
				+ signingCredentials;
		final String signerHeaders = "SignedHeaders="
				+ getSignedHeadersString(request);
		final String signatureHeader = "Signature="
				+ BinaryUtils.toHex(signature);

		final StringBuilder authHeaderBuilder = new StringBuilder();

		authHeaderBuilder.append(AWS4_SIGNING_ALGORITHM)
		.append(" ")
		.append(credential)
		.append(", ")
		.append(signerHeaders)
		.append(", ")
		.append(signatureHeader);

		return authHeaderBuilder.toString();
	}

	protected String getCanonicalizedHeaderString(SignableRequest<?> request) {
		final List<String> sortedHeaders = new ArrayList<String>(request.getHeaders()
				.keySet());
		Collections.sort(sortedHeaders, CASE_INSENSITIVE_ORDER);

		final Map<String, String> requestHeaders = request.getHeaders();
		StringBuilder buffer = new StringBuilder();
		for (String header : sortedHeaders) {
			if (shouldExcludeHeaderFromSigning(header)) {
				continue;
			}
			String key = StringUtils.lowerCase(header);
			String value = requestHeaders.get(header);

			StringUtils.appendCompactedString(buffer, key);
			buffer.append(":");
			if (value != null) {
				StringUtils.appendCompactedString(buffer, value);
			}

			buffer.append("\n");
		}

		return buffer.toString();
	}

	private static class CaseInsensitiveComparator implements Comparator<String> {
		// use serialVersionUID from JDK 1.2.2 for interoperability
		private static final long serialVersionUID = 8575799808933029326L;

		@Override
		public int compare(String s1, String s2) {
			int n1 = s1.length();
			int n2 = s2.length();
			int min = Math.min(n1, n2);
			for (int i = 0; i < min; i++) {
				char c1 = s1.charAt(i);
				char c2 = s2.charAt(i);
				if (c1 != c2) {
					c1 = Character.toUpperCase(c1);
					c2 = Character.toUpperCase(c2);
					if (c1 != c2) {
						c1 = Character.toLowerCase(c1);
						c2 = Character.toLowerCase(c2);
						if (c1 != c2) {
							// No overflow because of numeric promotion
							return c1 - c2;
						}
					}
				}
			}
			return n1 - n2;
		}
	}

	protected String getSignedHeadersString(SignableRequest<?> request) {
		final List<String> sortedHeaders = new ArrayList<String>(request
				.getHeaders().keySet());
		Collections.sort(sortedHeaders, CASE_INSENSITIVE_ORDER);

		StringBuilder buffer = new StringBuilder();
		for (String header : sortedHeaders) {
			if (shouldExcludeHeaderFromSigning(header)) {
				continue;
			}
			if (buffer.length() > 0) {
				buffer.append(";");
			}
			buffer.append(StringUtils.lowerCase(header));
		}

		return buffer.toString();
	}

	protected boolean shouldExcludeHeaderFromSigning(String header) {
		return listOfHeadersToIgnoreInLowerCase.contains(header.toLowerCase());
	}

	protected void addHostHeader(SignableRequest<?> request) {
		// AWS4 requires that we sign the Host header so we
		// have to have it in the request by the time we sign.

		final URI endpoint = request.getEndpoint();
		final StringBuilder hostHeaderBuilder = new StringBuilder(
				endpoint.getHost());
		if (SdkHttpUtils.isUsingNonDefaultPort(endpoint)) {
			hostHeaderBuilder.append(":").append(endpoint.getPort());
		}

		request.addHeader(HOST, hostHeaderBuilder.toString());
	}

	/**
	 * Calculate the hash of the request's payload. Subclass could override this
	 * method to provide different values for "x-amz-content-sha256" header or
	 * do any other necessary set-ups on the request headers. (e.g. aws-chunked
	 * uses a pre-defined header value, and needs to change some headers
	 * relating to content-encoding and content-length.)
	 */
	protected String calculateContentHash(SignableRequest<?> request) {
		InputStream payloadStream = getBinaryRequestPayloadStream(request);
		ReadLimitInfo info = request.getReadLimitInfo();
		payloadStream.mark(info == null ? -1 : info.getReadLimit());
		String contentSha256 = BinaryUtils.toHex(hash(payloadStream));
		try {
			payloadStream.reset();
		} catch (IOException e) {
			throw new SdkClientException(
					"Unable to reset stream after calculating AWS4 signature",
					e);
		}
		return contentSha256;
	}

	/**
	 * Subclass could override this method to perform any additional procedure
	 * on the request payload, with access to the result from signing the
	 * header. (e.g. Signing the payload by chunk-encoding). The default
	 * implementation doesn't need to do anything.
	 */
	protected void processRequestPayload(Object request, byte[] signature,
			byte[] signingKey, AWS4SignerRequestParams signerRequestParams) {
	}

	/**
	 * Generates a new signing key from the given parameters and returns it.
	 */
	protected byte[] newSigningKey(AWSCredentials credentials,
			String dateStamp, String regionName, String serviceName) {
		byte[] kSecret;
		try {
			kSecret = ("AWS4" + credentials.getAWSSecretKey()).getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		byte[] kDate = sign(dateStamp, kSecret, SigningAlgorithm.HmacSHA256);
		byte[] kRegion = sign(regionName, kDate, SigningAlgorithm.HmacSHA256);
		byte[] kService = sign(serviceName, kRegion,
				SigningAlgorithm.HmacSHA256);
		return sign(AWS4_TERMINATOR, kService, SigningAlgorithm.HmacSHA256);
	}
}
