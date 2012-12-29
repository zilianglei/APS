/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
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
 *         2011-05-15: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.config.service;

import se.natusoft.osgi.aps.api.core.config.APSConfig;


/**
 * This defines the Application-Platform-Services configuration service API.
 * <p>
 * Please note that this will always return the configuration for the currently
 * selected environment (which only APSConfigAdminService can change).
 */
public interface APSConfigService {

    /**
     * Registers a configuration Class with the configuration service. The passed
     * class must extend APSConfig and be annotated with @APSConfigDescription. Values
     * must be public fields annotated with @APSConfigItemDescription and of one of the
     * following types:
     * <ul>
     *     <li>APSConfigValue - A simple value.</li>
     *     <li>APSConfigValueList - A list of values.</li>
     *     <li>? extends APSConfig - Another configuration class following the same rules as this one.</li>
     *     <li>APSConfigList&lt;? extends APSConfig&gt; - A list of another configuration class.</li>
     * </ul>
     * <p>
     * The values of the configuration are editable with the APSConfigAdminService
     * which will also persist the configuration values.
     * <p>
     * If the version of the config Class is new (first time registered) and the prevVersion has been provided
     * then the configuration values of the previous version will be loaded and then saved with this version.
     * Any new values will ofcourse have the default values.
     * <p>
     * This should be called on bundle start. It will load the configuration from persistent store
     * (when such is available) into memory for fast access. A configuration needs to be edited
     * through APSConfigAdminService before it is persisted. Before that only the default values will
     * be returned.
     * <p/>
     * Please also call unregisterConfiguration(...) on bundle stop!
     *
     * @param configClass The config class to register.
     * @param forService If true then this configuration is for a service and will also be registered in the
     *                   standard OSGi configuration service.
     *
     * @throws APSConfigException on bad configClass interface.
     */
    void registerConfiguration(Class<? extends APSConfig> configClass, boolean forService) throws APSConfigException;

    /**
     * This tells the APSConfigService that the specified configuration is no longer actively used by anyone and will be
     * removed from memory.
     * <p>
     * This should always be done on bundle stop.
     *
     * @param configClass The config Class for the configuration.
     */
    void unregisterConfiguration(Class<? extends APSConfig> configClass);

    /**
     * Returns the configuration for the specified configuration Class.
     *
     * @param <Config> The configuration type which must be a subclass of APSConfig.
     * @param configClass The configuration Class to get the configuration for.
     *
     * @return An populated config Class instance.
     *
     * @throws APSConfigException on failure to get configuration.
     */
    <Config extends APSConfig> Config getConfiguration(Class<Config> configClass) throws APSConfigException;
}