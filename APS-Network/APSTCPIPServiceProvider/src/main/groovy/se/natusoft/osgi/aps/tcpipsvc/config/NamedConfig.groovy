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
package se.natusoft.osgi.aps.tcpipsvc.config

import se.natusoft.osgi.aps.api.core.config.APSConfig
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue

@APSConfigDescription(
        configId = "named",
        description = "This is a named config entry.",
        version = "1.0.0"
)
public class NamedConfig extends APSConfig {

    @APSConfigItemDescription(
            description = "The name of this config. To use this specify: groupName/configName.",
            environmentSpecific = true
    )
    public APSConfigValue configName

    @SuppressWarnings("GroovyUnusedDeclaration")
    @APSConfigItemDescription(
            description = "A comment / description of the entry.",
            environmentSpecific = true
    )
    public APSConfigValue comment

    @APSConfigItemDescription(
            description = "Is this a TCP, UDP, or Multicast configuration ?",
            // https://jira.codehaus.org/browse/GROOVY-3278
            validValues = ["TCP", "UDP", "Multicast"],
            environmentSpecific = true
    )
    public APSConfigValue type

    @APSConfigItemDescription(
            description =
                    "An ip address or hostname. For receiving this address is bound to, for sending this address is connected to. For multicast you can leave this blank to default to 224.0.0.1.",
            environmentSpecific = true
    )
    public APSConfigValue address

    @APSConfigItemDescription(
            description = "The port to listen or connect to.",
            environmentSpecific = true
    )
    public APSConfigValue port

    @APSConfigItemDescription(
            description = "Make connection secure when security is available.",
            isBoolean = true,
            defaultValue = @APSDefaultValue(value = "false"),
            environmentSpecific = true
    )
    public APSConfigValue secure
}
