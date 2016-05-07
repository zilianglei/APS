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
 *         2012-02-19: Created!
 *
 */
package se.natusoft.osgi.aps.core.config.model;

import se.natusoft.docutations.Implements;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEnvironment;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueStore;
import se.natusoft.osgi.aps.core.config.model.admin.APSConfigEditModelImpl;
import se.natusoft.osgi.aps.tools.APSLogger;

import java.util.List;

/**
 * Creates instances of APSConfigValue, APSConfigValueList, and APSConfigList. The point of this
 * is not to mess down other code with extra object needed to create these, those only get this
 * factory and all requirements are held by this factory.
 */
public class APSConfigObjectFactory implements ConfigEnvironmentProvider, ConfigValueStoreProvider {
    //
    // Private Members¨
    //

    /** The currently active config environment. */
    private ConfigEnvironmentProvider configEnvironment = null;

    /** The actual config values. Note that this might not have any values at time of usage here! */
    private APSConfigValueStore configValueStore = null;

    private APSLogger logger;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigObjectFactory instance.
     *
     * @param configEnvironment The currently active config environment.
     * @param configValueStore The actual config values.
     * @param logger The logger to log to.
     */
    public APSConfigObjectFactory(ConfigEnvironmentProvider configEnvironment, APSConfigValueStore configValueStore, APSLogger logger) {
        this.configEnvironment = configEnvironment;
        this.configValueStore = configValueStore;
        this.logger = logger;
    }

    //
    // Methods
    //

    /**
     * Returns the active config environment.
     */
    @Override
    @Implements(ConfigEnvironmentProvider.class)
    public APSConfigEnvironment getActiveConfigEnvironment() {
        return this.configEnvironment.getActiveConfigEnvironment();
    }

    /**
     * @return The available environments.
     */
    @Override
    @Implements(ConfigEnvironmentProvider.class)
    public List<APSConfigEnvironment> getConfigEnvironments() {
        return this.configEnvironment.getConfigEnvironments();
    }

    /**
     * Returns an environment by its name.
     *
     * @param name The name of the environment to get.
     * @return The named environment or null if it does not exist.
     */
    @Override
    @Implements(ConfigEnvironmentProvider.class)
    public APSConfigEnvironment getConfigEnvironmentByName(String name) {
        return this.configEnvironment.getConfigEnvironmentByName(name);
    }

    /**
     * Returns the configuration value storage.
     */
    @Override
    @Implements(ConfigValueStoreProvider.class)
    public APSConfigValueStore getConfigValueStore() {
        return this.configValueStore;
    }

    /**
     * Creates a new APSConfigValue.
     *
     * @param valueEditModel The value model representing this value key in the value store.
     */
    public APSConfigValue createAPSConfigValue(APSConfigValueEditModel valueEditModel) {
        return new APSConfigValueImpl(valueEditModel, this, this, this.logger);
    }

    /**
     * Creates a new APSConfigValueList.
     *
     * @param valueEditModel The value model representing this value keys in the value store.
     */
    public APSConfigValueList createAPSConfigValueList(APSConfigValueEditModel valueEditModel) {
        return new APSConfigValueListImpl(valueEditModel, this, this, this.logger);
    }

    /**
     * Creates a new APSConfigList.
     *
     * @param configModel The config model representing this config list and its keys in teh value store.
     */
    public APSConfigList createAPSConfigList(APSConfigEditModel configModel) {
        return new APSConfigListImpl((APSConfigEditModelImpl)configModel, this, this, this, this.logger);
    }

}
