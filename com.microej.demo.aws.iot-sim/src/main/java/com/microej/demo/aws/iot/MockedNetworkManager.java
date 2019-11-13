/*
 * Java
 *
 * Copyright 2018-2019 MicroEJ Corp. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be found with this software.
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
