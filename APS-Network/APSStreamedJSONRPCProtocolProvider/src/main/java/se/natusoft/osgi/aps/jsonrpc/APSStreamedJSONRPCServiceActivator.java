/* 
 * 
 * PROJECT
 *     Name
 *         APS Streamed JSONRPC Protocol Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides JSONRPC implementations for version 1.0 and 2.0.
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
 *         2012-01-22: Created!
 *         
 */
package se.natusoft.osgi.aps.jsonrpc;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import se.natusoft.osgi.aps.api.misc.json.service.APSJSONExtendedService;
import se.natusoft.osgi.aps.api.net.rpc.streamed.service.StreamedRPCProtocol;
import se.natusoft.osgi.aps.jsonrpc.versions.JSONRPC10;
import se.natusoft.osgi.aps.jsonrpc.versions.JSONRPC20;
import se.natusoft.osgi.aps.tools.APSLogger;
import se.natusoft.osgi.aps.tools.APSServiceTracker;

import java.util.Dictionary;
import java.util.Properties;

public class APSStreamedJSONRPCServiceActivator implements BundleActivator {
    //
    // Private Members
    //
    
    // Required Services

    /** A tracker for the APSJSONService service. */
    private APSServiceTracker<APSJSONExtendedService> jsonServiceTracker;

    // Provided Services
    
    /** The JSONRPC 1.0 service registration. */
    private ServiceRegistration jsonRPC10ServiceReg = null;

    /** The JSONRPC 2.0 service registration. */
    private ServiceRegistration jsonRPC20ServiceReg = null;

    // Other Members
    
    /** Our logger. */
    private APSLogger logger = null;
    
    //
    // Bundle Start.
    //
    
    @Override
    public void start(BundleContext context) throws Exception {
        this.logger = new APSLogger(System.out);
        this.logger.start(context);

        this.jsonServiceTracker = new APSServiceTracker<APSJSONExtendedService>(context, APSJSONExtendedService.class, APSServiceTracker.SHORT_TIMEOUT);
        this.jsonServiceTracker.start();
        APSJSONExtendedService jsonService = this.jsonServiceTracker.getWrappedService();

        JSONRPC10 jsonrpc10 = new JSONRPC10(this.logger, jsonService);
        Dictionary jsonRPC10ServiceProps = new Properties();
        jsonRPC10ServiceProps.put(Constants.SERVICE_PID, JSONRPC10.class.getName());
        this.jsonRPC10ServiceReg =
                context.registerService(StreamedRPCProtocol.class.getName(), jsonrpc10, jsonRPC10ServiceProps);

        JSONRPC20 jsonrpc20 = new JSONRPC20(this.logger, jsonService);
        Dictionary jsonRPC20ServiceProps = new Properties();
        jsonRPC20ServiceProps.put(Constants.SERVICE_PID, JSONRPC20.class.getName());
        this.jsonRPC20ServiceReg =
                context.registerService(StreamedRPCProtocol.class.getName(), jsonrpc20, jsonRPC20ServiceProps);
    }

    //
    // Bundle Stop.
    //
    
    @Override
    public void stop(BundleContext context) throws Exception {
        String stopMsg = "";

        if (this.jsonRPC10ServiceReg != null) {
            try {
                this.jsonRPC10ServiceReg.unregister();
                this.jsonRPC10ServiceReg = null;
            }
            catch (IllegalStateException ise) { stopMsg += ise.getMessage(); }
        }
        if (this.jsonRPC20ServiceReg != null) {
            try {
                this.jsonRPC20ServiceReg.unregister();
                this.jsonRPC20ServiceReg = null;
            }
            catch (IllegalStateException ise) { stopMsg += (" | " + ise.getMessage()); }
        }

        if (this.jsonServiceTracker != null) {
            this.jsonServiceTracker.stop(context);
            this.jsonServiceTracker = null;
        }

        if (this.logger != null) {
            this.logger.stop(context);
            this.logger = null;
        }

        // Hmm, not entirely sure here, but maybe it is a good thing to inform the OSGi container that something did go wrong here!
        if (stopMsg.length() > 0) {
            throw new Exception(stopMsg);
        }
    }

}