/* 
 * 
 * PROJECT
 *     Name
 *         APSGroups
 *     
 *     Code Version
 *         0.9.0
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
 *         are configurable.
 *         
 *         Note that even though this is an OSGi bundle, the jar produced can also be used as a
 *         library outside of OSGi. The se.natusoft.apsgroups.APSGroups API should then be used.
 *         This API has no external dependencies, only this jar is required for that use.
 *         
 *         When run with java -jar a for test command line shell will run where you can check
 *         members, send messages and files and other things.
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
package se.natusoft.osgi.aps.groups.service;

import se.natusoft.osgi.aps.api.net.groups.service.NetTime;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Provides an implementation of NetTime.
 */
public class NetTimeProvider implements NetTime {

    //
    // Private Members
    //

    private se.natusoft.apsgroups.internal.protocol.NetTime.Time time;

    //
    // Constructors
    //

    /**
     * Creates a new NetTimeProvider.
     *
     * @param time An internal NetTime.Time instance.
     */
    public NetTimeProvider(se.natusoft.apsgroups.internal.protocol.NetTime.Time time) {
        this.time = time;
    }

    //
    // Methods
    //

    /**
     * Returns the number of milliseconds since Januray 1, 1970 in net time.
     */
    @Override
    public long getNetTime() {
        return this.time.getNetTimeValue();
    }

    /**
     * Returns the net time as a Date.
     */
    @Override
    public Date getNetTimeDate() {
        return this.time.getNetTimeDate();
    }

    /**
     * Returns the net time as a Calendar.
     */
    @Override
    public Calendar getNetTimeCalendar() {
        return this.time.getNetTimeCalendar();
    }

    /**
     * Converts the net time to local time and returns as a Calendar.
     *
     * @param locale The locale to use.
     */
    @Override
    public Calendar getNetTimeCalendar(Locale locale) {
        return this.time.getNetTimeCalendar(locale);
    }

    /**
     * Converts the net time to local time and returns as a Date.
     */
    @Override
    public Date getLocalTimeDate() {
        return this.time.getLocalTimeDate();
    }

    /**
     * Converts the net time to local time and returns as a Calendar.
     */
    @Override
    public Calendar getLocalTimeCalendar() {
        return this.time.getLocalTimeCalendar();
    }

    /**
     * Converts the net time to local time and returns as a Calendar.
     *
     * @param locale The locale to use.
     */
    @Override
    public Calendar getLocalTimeCalendar(Locale locale) {
        return this.time.getLocalTimeCalendar(locale);
    }
}
