/*
 * Java
 *
 * Copyright 2018 IS2T. All rights reserved.
 * For demonstration purpose only.
 * IS2T PROPRIETARY. Use is subject to license terms.
 */
package com.microej.demo.aws.iot;

import java.io.IOException;

import ej.net.util.NtpUtil;

/**
 * Class that manage a device Wi-Fi network.
 */
public class WifiNetworkManager implements INetworkManager {

	private ej.net.util.wifi.WifiNetworkManager wifiManager = null;

	@Override
	public void init() throws IOException {
		// Instantiate the wifiManager and initialize the network interface
		this.wifiManager = new ej.net.util.wifi.WifiNetworkManager();

		// Join the configured Access Point
		this.wifiManager.joinAccessPoint(Config.SSID, Config.PASSWORD, Config.SECURITY_MODE, 20_000);

		// Update the local time
		NtpUtil.updateLocalTime();
	}

	@Override
	public void deinit() throws IOException {
		if (this.wifiManager != null) {
			this.wifiManager.leaveAccessPoint();
			this.wifiManager = null;
		}
	}

}
