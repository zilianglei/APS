/*
 *
 * PROJECT
 *     Name
 *         APS TCPIP Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides an implementation of APSTCPIPService. This service does not provide any security of its own,
 *         but makes use of APSTCPSecurityService, and APSUDPSecurityService when available and configured for
 *         security.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2015-04-11: Created!
 *
 */
package se.natusoft.osgi.aps.tcpipsvc

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.tcpip.NetworkConfig
import se.natusoft.osgi.aps.tcpipsvc.security.UDPSecurityHandler
import se.natusoft.osgi.aps.util.APSLogger

/**
 * This sends UDP data.
 */
@CompileStatic
@TypeChecked
class UDPSender implements ConnectionProvider {
    //
    // Properties
    //

    /** The connection point to send to. */
    URI connectionPoint

    /** A logger to log to. */
    APSLogger logger

    /** The security handler to use. */
    UDPSecurityHandler securityHandler

    //
    // Private Members
    //

    /** The UDP socket to send on. */
    protected DatagramSocket socket = null

    /** The target to send to. */
    protected InetSocketAddress targetSocketAddress = null

    /** Flag to indicate if this ConnectionProvider are stopped. */
    protected synchronized boolean stopped = true

    //
    // Methods
    //

    /**
     * Starts the provider.
     *
     * @throws IOException
     */
    @Override
    void start() throws IOException {
        this.stopped = false
    }

    /**
     * Stops the provider.
     *
     * @throws IOException
     */
    @Override
    void stop() throws IOException {
        this.stopped = true
        disconnect()
        this.targetSocketAddress = null
    }

    /**
     * Returns the direction of the connection.
     */
    @Override
    ConnectionProvider.Direction getDirection() {
        ConnectionProvider.Direction.Write
    }

    /**
     * Creates an UDP socket.
     */
    protected void createSocket() {
        this.socket = new DatagramSocket()
        this.socket.reuseAddress = true
        this.socket.broadcast = false
    }

    /**
     * Connects the socket.
     *
     * @throws IOException
     */
    private void connect() throws IOException {
        if (this.socket == null) {
            createSocket()
        }
    }

    /**
     * Disconnects the socket.
     *
     * @throws IOException
     */
    private void disconnect() throws IOException {
        if (this.socket != null) {
            this.socket.disconnect()
            this.socket = null
        }
    }

    /**
     * Utility to get and cache the target address.
     */
    protected InetSocketAddress getTargetAddress() {
        if (this.targetSocketAddress == null) {
            this.targetSocketAddress = new InetSocketAddress(this.connectionPoint.host, this.connectionPoint.port)
        }
        this.targetSocketAddress
    }

    /**
     * Sends some bytes on the socket.
     *
     * @param data The data to send.
     *
     * @throws IOException
     */
    public void send(byte[] data) throws IOException {
        if (!this.stopped) {
            connect()
            DatagramPacket dp = new DatagramPacket(data, data.length, targetAddress)
            if (this.connectionPoint.getFragment() == "secure") {
                this.securityHandler.secure(this.connectionPoint, dp)
            }
            this.socket.send(dp)
        }
    }
}
