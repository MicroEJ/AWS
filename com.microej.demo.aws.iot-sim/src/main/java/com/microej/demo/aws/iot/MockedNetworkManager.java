/*
 * Java
 *
 * Copyright 2018 IS2T. All rights reserved.
 * For demonstration purpose only.
 * IS2T PROPRIETARY. Use is subject to license terms.
 */
package com.microej.demo.aws.iot;

import java.io.IOException;

/**
 * Class which mock a {@link INetworkManager} service.
 */
@SuppressWarnings("nls")
public class MockedNetworkManager implements INetworkManager {

	@Override
	public void init() throws IOException {
		System.out.println("[INFO] MockedNetworkManager.init()");
	}

	@Override
	public void deinit() throws IOException {
		System.out.println("[INFO] MockedNetworkManager.deinit()");
	}
}
