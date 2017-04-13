/*
 * Java
 *
 * Copyright 2018 IS2T. All rights reserved.
 * For demonstration purpose only.
 * IS2T PROPRIETARY. Use is subject to license terms.
 */
package com.microej.demo.aws.iot;

import ej.ecom.wifi.SecurityMode;

/**
 * This class provides example configuration.
 */
@SuppressWarnings("nls")
public class Config {
	/**
	 * SSID of the wireless network.
	 */
	public static final String SSID = "my_wifi";

	/**
	 * Password of wireless network.
	 */
	public static final String PASSWORD = "passphrase";

	/**
	 * Security mode of the wireless network.
	 */
	public static final SecurityMode SECURITY_MODE = SecurityMode.WPA2;

	/**
	 * Host of the AWS broker.
	 */
	public static final String AWS_BROKER_HOST = "myowndomainid.amazonaws.com";

	/**
	 * Port of the AWS broker.
	 */
	public static final int AWS_BROKER_PORT = 8883;

	/**
	 * Identifier of the Thing.
	 */
	public static final String AWS_THING_ID = "myThing";
}
