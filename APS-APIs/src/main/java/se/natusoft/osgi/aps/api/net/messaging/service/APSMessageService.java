/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-10-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.messaging.service;

import se.natusoft.osgi.aps.api.net.messaging.exception.APSMessagingException;
import se.natusoft.osgi.aps.api.net.messaging.messages.APSMessage;

import java.util.List;

/**
 * This is a basic low level messaging API that should be implementable using almost any messaging provider.
 */
public interface APSMessageService {

    /** This means that the service can handle any group name, that group names aren't important. */
    public static final String ANY_GROUP = "*";

    /**
     * The defined groups made available by this service. That is, the group names that can be passed
     * to sendMessage(...) and readMessage(...) without being guaranteed to throw an exception.
     */
    List<String> providedGroups();

    /**
     * Sends a messaging.
     *
     * @param message The message to send.
     *
     * @throws APSMessagingException on failure.
     *
     * @return true if the messaging was sent.
     */
    boolean sendMessage(APSMessage message) throws APSMessagingException;

    /**
     * Adds a listener for messages.
     *
     * @param listener The listener to add.
     */
    void addMessageListener(Listener listener);

    /**
     * Removes a messaging listener.
     *
     * @param listener The listener to remove.
     */
    void removeMessageListener(Listener listener);

    /**
     * Listener for messages.
     */
    interface Listener {

        /**
         * This is called when a messaging is received.
         *
         * @param message The received message.
         */
        void messageReceived(APSMessage message);
    }
}
