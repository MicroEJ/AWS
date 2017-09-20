/*
 * Copyright (c) 1998, 2014, Oracle and/or its affiliates. All rights reserved.
 * Copyright (C) 2017, IS2T - EDC compliance and optimizations.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javax.crypto;

import com.sun.crypto.provider.HmacCore;

import ej.security.InvalidAlgorithmParameterException;
import ej.security.InvalidKeyException;
import ej.security.Key;
import ej.security.NoSuchAlgorithmException;
import ej.security.spec.AlgorithmParameterSpec;

/**
 * This class provides the functionality of a "Message Authentication Code"
 * (MAC) algorithm.
 *
 * <p> A MAC provides a way to check
 * the integrity of information transmitted over or stored in an unreliable
 * medium, based on a secret key. Typically, message
 * authentication codes are used between two parties that share a secret
 * key in order to validate information transmitted between these
 * parties.
 *
 * <p> A MAC mechanism that is based on cryptographic hash functions is
 * referred to as HMAC. HMAC can be used with any cryptographic hash function,
 * e.g., MD5 or SHA-1, in combination with a secret shared key. HMAC is
 * specified in RFC 2104.
 *
 * <p> Every implementation of the Java platform is required to support
 * the following standard <code>Mac</code> algorithms:
 * <ul>
 * <li><tt>HmacMD5</tt></li>
 * <li><tt>HmacSHA1</tt></li>
 * <li><tt>HmacSHA256</tt></li>
 * </ul>
 * These algorithms are described in the
 * <a href="{@docRoot}/../technotes/guides/security/StandardNames.html#Mac">
 * Mac section</a> of the
 * Java Cryptography Architecture Standard Algorithm Name Documentation.
 * Consult the release documentation for your implementation to see if any
 * other algorithms are supported.
 *
 * @author Jan Luehe
 *
 * @since 1.4
 */

public class Mac implements Cloneable {

	// The provider implementation (delegate)
	private MacSpi spi;

	// The name of the MAC algorithm.
	private final String algorithm;

	// Has this object been initialized?
	private boolean initialized = false;

	/**
	 * Creates a MAC object.
	 *
	 * @param macSpi the delegate
	 * @param provider the provider
	 * @param algorithm the algorithm
	 */
	protected Mac(MacSpi macSpi, String algorithm) {
		this.spi = macSpi;
		this.algorithm = algorithm;
	}

	/**
	 * Returns the algorithm name of this <code>Mac</code> object.
	 *
	 * <p>This is the same name that was specified in one of the
	 * <code>getInstance</code> calls that created this
	 * <code>Mac</code> object.
	 *
	 * @return the algorithm name of this <code>Mac</code> object.
	 */
	public final String getAlgorithm() {
		return this.algorithm;
	}

	/**
	 * Returns a <code>Mac</code> object that implements the
	 * specified MAC algorithm.
	 *
	 * <p> This method traverses the list of registered security Providers,
	 * starting with the most preferred Provider.
	 * A new Mac object encapsulating the
	 * MacSpi implementation from the first
	 * Provider that supports the specified algorithm is returned.
	 *
	 * <p> Note that the list of registered providers may be retrieved via
	 * the {@link Security#getProviders() Security.getProviders()} method.
	 *
	 * @param algorithm the standard name of the requested MAC algorithm.
	 * See the Mac section in the <a href=
	 *   "{@docRoot}/../technotes/guides/security/StandardNames.html#Mac">
	 * Java Cryptography Architecture Standard Algorithm Name Documentation</a>
	 * for information about standard algorithm names.
	 *
	 * @return the new <code>Mac</code> object.
	 *
	 * @exception NoSuchAlgorithmException if no Provider supports a
	 *          MacSpi implementation for the
	 *          specified algorithm.
	 *
	 * @see java.security.Provider
	 */
	public static final Mac getInstance(String algorithm)
			throws NoSuchAlgorithmException {
		switch (algorithm) {
		case "HmacSHA256":
			return new Mac(new HmacCore.HmacSHA256(), algorithm);

		default:
			throw new NoSuchAlgorithmException("Algorithm " + algorithm + " not available");
		}
	}

	/**
	 * Choose the Spi from the first provider available. Used if
	 * delayed provider selection is not possible because init()
	 * is not the first method called.
	 */
	void chooseFirstProvider() {
	}

	private void chooseProvider(Key key, AlgorithmParameterSpec params)
			throws InvalidKeyException, InvalidAlgorithmParameterException {
	}

	/**
	 * Returns the length of the MAC in bytes.
	 *
	 * @return the MAC length in bytes.
	 */
	public final int getMacLength() {
		chooseFirstProvider();
		return spi.engineGetMacLength();
	}

	/**
	 * Initializes this <code>Mac</code> object with the given key.
	 *
	 * @param key the key.
	 *
	 * @exception InvalidKeyException if the given key is inappropriate for
	 * initializing this MAC.
	 */
	public final void init(Key key) throws InvalidKeyException {
		try {
			if (spi != null) {
				spi.engineInit(key, null);
			} else {
				chooseProvider(key, null);
			}
		} catch (InvalidAlgorithmParameterException e) {
			throw new InvalidKeyException("init() failed", e);
		}
		initialized = true;
	}

	/**
	 * Initializes this <code>Mac</code> object with the given key and
	 * algorithm parameters.
	 *
	 * @param key the key.
	 * @param params the algorithm parameters.
	 *
	 * @exception InvalidKeyException if the given key is inappropriate for
	 * initializing this MAC.
	 * @exception InvalidAlgorithmParameterException if the given algorithm
	 * parameters are inappropriate for this MAC.
	 */
	public final void init(Key key, AlgorithmParameterSpec params)
			throws InvalidKeyException, InvalidAlgorithmParameterException {
		if (spi != null) {
			spi.engineInit(key, params);
		} else {
			chooseProvider(key, params);
		}
		initialized = true;
	}

	/**
	 * Processes the given byte.
	 *
	 * @param input the input byte to be processed.
	 *
	 * @exception IllegalStateException if this <code>Mac</code> has not been
	 * initialized.
	 */
	public final void update(byte input) throws IllegalStateException {
		chooseFirstProvider();
		if (initialized == false) {
			throw new IllegalStateException("MAC not initialized");
		}
		spi.engineUpdate(input);
	}

	/**
	 * Processes the given array of bytes.
	 *
	 * @param input the array of bytes to be processed.
	 *
	 * @exception IllegalStateException if this <code>Mac</code> has not been
	 * initialized.
	 */
	public final void update(byte[] input) throws IllegalStateException {
		chooseFirstProvider();
		if (initialized == false) {
			throw new IllegalStateException("MAC not initialized");
		}
		if (input != null) {
			spi.engineUpdate(input, 0, input.length);
		}
	}

	/**
	 * Processes the first <code>len</code> bytes in <code>input</code>,
	 * starting at <code>offset</code> inclusive.
	 *
	 * @param input the input buffer.
	 * @param offset the offset in <code>input</code> where the input starts.
	 * @param len the number of bytes to process.
	 *
	 * @exception IllegalStateException if this <code>Mac</code> has not been
	 * initialized.
	 */
	public final void update(byte[] input, int offset, int len)
			throws IllegalStateException {
		chooseFirstProvider();
		if (initialized == false) {
			throw new IllegalStateException("MAC not initialized");
		}

		if (input != null) {
			if ((offset < 0) || (len > (input.length - offset)) || (len < 0)) {
				throw new IllegalArgumentException("Bad arguments");
			}
			spi.engineUpdate(input, offset, len);
		}
	}

	/**
	 * Finishes the MAC operation.
	 *
	 * <p>A call to this method resets this <code>Mac</code> object to the
	 * state it was in when previously initialized via a call to
	 * <code>init(Key)</code> or
	 * <code>init(Key, AlgorithmParameterSpec)</code>.
	 * That is, the object is reset and available to generate another MAC from
	 * the same key, if desired, via new calls to <code>update</code> and
	 * <code>doFinal</code>.
	 * (In order to reuse this <code>Mac</code> object with a different key,
	 * it must be reinitialized via a call to <code>init(Key)</code> or
	 * <code>init(Key, AlgorithmParameterSpec)</code>.
	 *
	 * @return the MAC result.
	 *
	 * @exception IllegalStateException if this <code>Mac</code> has not been
	 * initialized.
	 */
	public final byte[] doFinal() throws IllegalStateException {
		chooseFirstProvider();
		if (initialized == false) {
			throw new IllegalStateException("MAC not initialized");
		}
		byte[] mac = spi.engineDoFinal();
		spi.engineReset();
		return mac;
	}

	/**
	 * Finishes the MAC operation.
	 *
	 * <p>A call to this method resets this <code>Mac</code> object to the
	 * state it was in when previously initialized via a call to
	 * <code>init(Key)</code> or
	 * <code>init(Key, AlgorithmParameterSpec)</code>.
	 * That is, the object is reset and available to generate another MAC from
	 * the same key, if desired, via new calls to <code>update</code> and
	 * <code>doFinal</code>.
	 * (In order to reuse this <code>Mac</code> object with a different key,
	 * it must be reinitialized via a call to <code>init(Key)</code> or
	 * <code>init(Key, AlgorithmParameterSpec)</code>.
	 *
	 * <p>The MAC result is stored in <code>output</code>, starting at
	 * <code>outOffset</code> inclusive.
	 *
	 * @param output the buffer where the MAC result is stored
	 * @param outOffset the offset in <code>output</code> where the MAC is
	 * stored
	 *
	 * @exception ShortBufferException if the given output buffer is too small
	 * to hold the result
	 * @exception IllegalStateException if this <code>Mac</code> has not been
	 * initialized.
	 */
	public final void doFinal(byte[] output, int outOffset)
			throws ShortBufferException, IllegalStateException
	{
		chooseFirstProvider();
		if (initialized == false) {
			throw new IllegalStateException("MAC not initialized");
		}
		int macLen = getMacLength();
		if (output == null || output.length-outOffset < macLen) {
			throw new ShortBufferException
			("Cannot store MAC in output buffer");
		}
		byte[] mac = doFinal();
		System.arraycopy(mac, 0, output, outOffset, macLen);
		return;
	}

	/**
	 * Processes the given array of bytes and finishes the MAC operation.
	 *
	 * <p>A call to this method resets this <code>Mac</code> object to the
	 * state it was in when previously initialized via a call to
	 * <code>init(Key)</code> or
	 * <code>init(Key, AlgorithmParameterSpec)</code>.
	 * That is, the object is reset and available to generate another MAC from
	 * the same key, if desired, via new calls to <code>update</code> and
	 * <code>doFinal</code>.
	 * (In order to reuse this <code>Mac</code> object with a different key,
	 * it must be reinitialized via a call to <code>init(Key)</code> or
	 * <code>init(Key, AlgorithmParameterSpec)</code>.
	 *
	 * @param input data in bytes
	 * @return the MAC result.
	 *
	 * @exception IllegalStateException if this <code>Mac</code> has not been
	 * initialized.
	 */
	public final byte[] doFinal(byte[] input) throws IllegalStateException
	{
		chooseFirstProvider();
		if (initialized == false) {
			throw new IllegalStateException("MAC not initialized");
		}
		update(input);
		return doFinal();
	}

	/**
	 * Resets this <code>Mac</code> object.
	 *
	 * <p>A call to this method resets this <code>Mac</code> object to the
	 * state it was in when previously initialized via a call to
	 * <code>init(Key)</code> or
	 * <code>init(Key, AlgorithmParameterSpec)</code>.
	 * That is, the object is reset and available to generate another MAC from
	 * the same key, if desired, via new calls to <code>update</code> and
	 * <code>doFinal</code>.
	 * (In order to reuse this <code>Mac</code> object with a different key,
	 * it must be reinitialized via a call to <code>init(Key)</code> or
	 * <code>init(Key, AlgorithmParameterSpec)</code>.
	 */
	public final void reset() {
		chooseFirstProvider();
		spi.engineReset();
	}

	/**
	 * Returns a clone if the provider implementation is cloneable.
	 *
	 * @return a clone if the provider implementation is cloneable.
	 *
	 * @exception CloneNotSupportedException if this is called on a
	 * delegate that does not support <code>Cloneable</code>.
	 */
	@Override
	public final Object clone() throws CloneNotSupportedException {
		chooseFirstProvider();
		Mac that = (Mac)super.clone();
		that.spi = (MacSpi)this.spi.clone();
		return that;
	}
}
