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
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *
 */
package se.natusoft.osgi.aps.api.messaging;

import se.natusoft.osgi.aps.types.APSHandler;

/**
 * This extension of APSSender is to support messaging providers that allows for a
 * reply to a specific message. The only such I know of is Vertx, but there might be
 * more.
 *
 * Since APS at the moment makes heavy use of Vertx and that I still want to be able
 * to encapsulate it with APS generic APIs I decided to handle this feature of Vertx
 * like this.
 *
 * Note that I have allowed for 2 different types of send and reply message. Since all
 * messages in APS will be JSON or a Map&lt;String, Object&gt; representation of JSON
 * the send and reply types will be the same. I dit not however want to force it in
 * the API.
 *
 * @param <Message> The message type to send.
 * @param <ReplyMessage> The expected typeof reply message.
 */
public interface APSReplyableMessageSender<Message, ReplyMessage> extends APSMessageSender<Message> {

    /**
     * This must be called before send(...). send will use the last supplied reply subscriber.
     *
     * Note that this uses a fluent API that returns this. This allows for just adding
     * ".send(...)" after the call to this.
     *
     * @param handler the handler of the reply.
     */
    @SuppressWarnings("unused")
    APSMessageSender<Message> replyTo(APSHandler<APSMessage<ReplyMessage>> handler);

}
