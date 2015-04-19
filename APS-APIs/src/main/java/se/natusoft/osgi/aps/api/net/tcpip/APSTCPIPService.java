package se.natusoft.osgi.aps.api.net.tcpip;

import se.natusoft.osgi.aps.api.APSServiceProperties;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 * This service provides the following functions:
 *
 * * TCP, UDP & Multicast network traffic.
 *
 * * External configurations of network connections.
 *
 * * Decoupling from java.net classes allowing for test implementations where network traffic can be
 *   simulated in unittests without causing problems when run concurrently in a CI server.
 *
 * The users of this service should also have a config that specifies which config of this service
 * to use.
 *
 * If a service both sends TCP request and receives them there need to be 2 different config entries
 * for this.
 */
public interface APSTCPIPService extends APSServiceProperties {

    /**
     * Sends UDP data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param data The data to send.
     *
     * @throws IOException The one and only!
     * @throws IllegalArgumentException on bad name.
     */
    void sendUDP(String name, byte[] data) throws IOException;

    /**
     * Reads UDP data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param data The buffer to receive into.
     *
     * @return the data buffer.
     *
     * @throws IOException
     * @throws IllegalArgumentException on bad name.
     */
    DatagramPacket readUDP(String name, byte[] data) throws IOException;

    /**
     * Adds a listener for received udp data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param listener The listener to call back with messages.
     *
     * @throws IllegalArgumentException on bad name.
     */
    void addUDPListener(String name, UDPListener listener);

    /**
     * Removes a listener for received udp data.
     *
     * @param name The name of a configuration specifying address and port or multicast and port.
     * @param listener The listener to remove.
     *
     * @throws IllegalArgumentException on bad name.
     */
    void removeUDPListener(String name, UDPListener listener);

    /**
     * Sends a TCP request on a named TCP config.
     *
     * @param name The named config to send to.
     * @param request A callback that provides a request output stream and a response input stream.
     *
     * @throws IllegalArgumentException on bad name.
     */
    void sendTCPRequest(String name, TCPRequest request) throws IOException;

    /**
     * Sets a listener for incoming TCP requests. There can only be one per name.
     *
     * @param name The named config to add listener for.
     * @param listener The listener to add.
     *
     * @throws IllegalArgumentException on bad name.
     */
    void setTCPRequestListener(String name, TCPListener listener);

    /**
     * Removes a listener for incoming TCP requests.
     *
     * @param name Thge named config to remove a listener for.
     * @param listener The listener to remove.
     *
     * @throws IllegalArgumentException on bad name.
     */
    void removeTCPRequestListener(String name, TCPListener listener);

}
