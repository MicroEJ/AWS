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
 * Class which implements this interface allow to manage a network.
 */
public interface INetworkManager {

	/**
	 * Initialize the network.
	 *
	 * @throws IOException
	 *             if I/O error occurred
	 */
	public void init() throws IOException;

	/**
	 * Deinit the network.
	 *
	 * @throws IOException
	 *             if I/O error occurred
	 */
	public void deinit() throws IOException;

}
