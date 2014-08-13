/* 
 * 
 * PROJECT
 *     Name
 *         APS Configuration Service Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         A more advanced configuration service that uses annotated interfaces to
 *         describe and provide access to configuration. It supports structured
 *         configuration models.
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
 *         2014-03-08: Created!
 *         
 */
package se.natusoft.osgi.aps.core.config.config;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;

/**
 * This is a configuration class for this APSConfigService provider.
 */
@APSConfigDescription( version="1.0",
        configId=APSConfigServiceConfig.CONFIG_ID, group="aps",
        description="Configuration for the aps-configuration-service-provider."
)
public class APSConfigServiceConfig extends APSConfig {

    public static final String CONFIG_ID = "se.natusoft.osgi.aps.config-service";

    @APSConfigItemDescription(
            description = "Enables configuration synchronization between server installations.",
            isBoolean = true,
            environmentSpecific = true
    )
    public APSConfigValue synchronize;

    @APSConfigItemDescription(
            description = "Specifies a named group for synchronization. All members of the same group " +
                          "will have their configuration synchronized between them. For synchronization " +
                          "to work a configured APSSyncService service must also be available.",
            defaultValue = {@APSDefaultValue("aps-config-sync-default")},
            environmentSpecific = true
    )
    public APSConfigValue synchronizationGroup;
}
