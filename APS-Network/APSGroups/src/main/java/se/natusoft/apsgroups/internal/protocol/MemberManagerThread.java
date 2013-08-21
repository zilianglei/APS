/* 
 * 
 * PROJECT
 *     Name
 *         APS Groups
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides network groups where named groups can be joined as members and then send and
 *         receive data messages to the group. This is based on multicast and provides a verified
 *         multicast delivery with acknowledgements of receive to the sender and resends if needed.
 *         The sender will get an exception if not all members receive all data. Member actuality
 *         is handled by members announcing themselves relatively often and will be removed when
 *         an announcement does not come in expected time. So if a member dies unexpectedly
 *         (network goes down, etc) its membership will resolve rather quickly. Members also
 *         tries to inform the group when they are doing a controlled exit. Most network aspects
 *         are configurable. Please note that this does not support streaming! That would require
 *         a far more complex protocol. It waits in all packets of a message before delivering
 *         the message.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files.
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
 *         2012-12-28: Created!
 *         
 */
package se.natusoft.apsgroups.internal.protocol;

import se.natusoft.apsgroups.Debug;
import se.natusoft.apsgroups.config.APSGroupsConfig;
import se.natusoft.apsgroups.internal.net.Transports;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacket;
import se.natusoft.apsgroups.internal.protocol.message.MessagePacketListener;
import se.natusoft.apsgroups.internal.protocol.message.PacketType;
import se.natusoft.apsgroups.logging.APSGroupsLogger;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * This thread handles repeated announcements of members added to the thread. It also
 * handles eviction of expired members for each members group.
 */
public class MemberManagerThread extends Thread implements MemberManager, MessagePacketListener {
    //
    // Private Members
    //

    /** The member to announce. */
    private List<Member> members = new LinkedList<>();

    /** The transports to use to announce members on. */
    private Transports transports = null;

    /** The logger to log to. */
    private APSGroupsLogger logger = null;

    /** Our config. */
    private APSGroupsConfig config = null;

    /** Set to true on start and false on stop. */
    private boolean running = false;

    //
    // Constructors
    //

    /**
     * Creates a new DataReceiverThread.
     *
     * @param logger The logger for the thread to log on.
     * @param transports The transport to use for announcing members.
     * @param config The config to use.
     */
    public MemberManagerThread(APSGroupsLogger logger, Transports transports, APSGroupsConfig config) {
        this.logger = logger;
        this.transports = transports;
        this.config = config;
    }

    //
    // Methods
    //

    /**
     * This to avoid having to save the transport outside of this class just to be able to
     * close it again.
     */
    public Transports getTransport() {
        return this.transports;
    }

    /**
     * Starts the thread.
     */
    @Override
    public void start() {
        this.running = true;
        super.start();
    }

    /**
     * Terminates the thread.
     */
    public synchronized void terminate() {
        this.running = false;
    }

    /**
     * Adds a member.
     *
     * @param member The member to add.
     */
    public synchronized void addMember(Member member) {
        this.members.add(member);
        this.logger.debug("Added member '" + member.getId() + "' to MemberManager:" + hashCode());
    }

    /**
     * Removes a member.
     *
     * @param member The member to remove.
     */
    public synchronized void removeMember(Member member) {
        this.members.remove(member);
        this.logger.debug("Removed member '" + member.getId() + "' from MemberManager:" + hashCode());
    }

    /**
     * Returns a concurrent safe member list.
     */
    private synchronized List<Member> getMembers() {
        List<Member> membersCopy = new LinkedList<>();
        membersCopy.addAll(this.members);
        return membersCopy;
    }

    /**
     * @return true if we are still supposed to be running.
     */
    private synchronized boolean isRunning() {
        return this.running;
    }

    /**
     * Announces a member.
     *
     * @param member The member to announce.
     *
     * @throws IOException
     */
    private void announceMember(Member member) throws IOException {
        MessagePacket mp = new MessagePacket(member.getGroup(), member, UUID.randomUUID(),0 , PacketType.MEMBER_ANNOUNCEMENT);
        ObjectOutputStream oos = new ObjectOutputStream(mp.getOutputStream());
        oos.writeObject(member.getMemberUserData());
        oos.close();
        this.transports.send(mp.getPacketBytes());
        member.announced();
    }

    /**
     * The main thread execution.
     */
    @Override
    public void run() {
        while (isRunning()) {
            try {
                for (Member member : getMembers()) {
                    if (member.lastAnnounced() < 0) {
                        announceMember(member);
                    }
                    else {
                        long now = new Date().getTime();
                        long lastAnnounce = member.lastAnnounced();
                        long interval = this.config.getMemberAnnounceInterval() * 1000l;
                        Debug.println2("" + now + " >= " + (lastAnnounce + interval) + " : " + (now >= (lastAnnounce + interval)));
                        if (now >= (lastAnnounce + interval)) {
                            announceMember(member);
                        }
                    }

                    // Lets also evict old member of the members group.
                    if (member.getGroup() != null) {
                        member.getGroup().evictExpiredMembers();
                    }
                }
            }
            catch (IOException ioe) {
                this.logger.error("MemberManagerThread: Communication problem!", ioe);
            }
            catch (Exception e) {
                e.printStackTrace();
                this.logger.error("MemberManagerThread: Unknown failure!", e);
            }

            try {Thread.sleep(2000);} catch (InterruptedException ie) {
                this.logger.warn("MemberManagerThread: Thread.sleep() was unexpectedly interrupted! If this happens consecutively " +
                        "too often it can cause highly increased CPU usage!");
            }
        }
    }

    /**
     * Notification of receive of a new MessagePacket.
     *
     * @param messagePacket The received MessagePacket.
     */
    @Override
    public void messagePacketReceived(MessagePacket messagePacket) {
        if (messagePacket.getType() == PacketType.MEMBER_LEAVING) {
            for (Member member : getMembers()) {
                if (member.getGroup().equals(messagePacket.getGroup())) {
                    Group group = member.getGroup();
                    group.removeMember(messagePacket.getMember());
                }
            }
        }
    }
}
