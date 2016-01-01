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
 *         2016-01-01: Created!
 *         
 */
package se.natusoft.osgi.aps.tcpipsvc.security

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.api.net.tcpip.APSUDPSecurityService
import se.natusoft.osgi.aps.tools.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiService
import se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException

/**
 * Wraps APSUDPSecurityService and throwing an IOException if none is available.
 */
@CompileStatic
@TypeChecked
class UDPSecurityHandler {

    //
    // Private Members
    //

    @OSGiService(timeout = "10 seconds")
    private APSServiceTracker<APSUDPSecurityService> udpSecurityServiceTracker

    private Object securityContext = null

    //
    // Methods
    //

    /**
     * Utility method to create security context once and then cache it.
     *
     * @param name This corresponds to a named config in APSTCPIPService.
     * @param securityService An instance of the APSUDPSecurityService which will be called to create the security context.
     *
     * @return The security context.
     */
    private Object getSecurityContext(String name, APSUDPSecurityService securityService) {
        if (this.securityContext == null) {
            this.securityContext = securityService.createSecurityContext(name)
        }
        return this.securityContext
    }

    /**
     * Secures a DatagramPacket.
     *
     * @param name name This corresponds to a named config in APSTCPIPService.
     * @param data The DatagramPacket to secure.
     * @param secure The DatagramPacket will only be secured if this is true and there is an APSUDPSecurityService available.
     *               This flag is set in configuration.
     *
     * @throws IOException on any failure.
     */
    public void secure(String name, DatagramPacket data, boolean secure) throws IOException {
        if (secure && this.udpSecurityServiceTracker.hasTrackedService()) {
            //noinspection SpellCheckingInspection
            try {
                APSUDPSecurityService securityService = this.udpSecurityServiceTracker.allocateService()
                securityService.secure(data, getSecurityContext(name, securityService))
                this.udpSecurityServiceTracker.releaseService()
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new IOException("Failed to secure packet, no APSUDPSecurityService available!", nsae)
            }
        }
    }

    /**
     * Unsecures a DatagramPacket.
     *
     * @param name name This corresponds to a named config in APSTCPIPService.
     * @param data The DatagramPacket to secure.
     * @param secure The DatagramPacket will only be unsecured if this is true and there is an APSUDPSecurityService available.
     *               This flag is set in configuration.
     *
     * @throws IOException on any failure.
     */
    public void unsecure(String name, DatagramPacket data, boolean secure) throws IOException {
        if (secure && this.udpSecurityServiceTracker.hasTrackedService()) {
            //noinspection SpellCheckingInspection
            try {
                APSUDPSecurityService securityService = this.udpSecurityServiceTracker.allocateService()
                securityService.unsecure(data, getSecurityContext(name, securityService))
                this.udpSecurityServiceTracker.releaseService()
            }
            catch (APSNoServiceAvailableException nsae) {
                throw new IOException("Failed to unsecure packet, no APSUDPSecurityService available!", nsae)
            }
        }
    }
}
