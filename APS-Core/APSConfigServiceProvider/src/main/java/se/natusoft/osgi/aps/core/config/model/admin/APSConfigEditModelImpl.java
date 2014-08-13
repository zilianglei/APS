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
 *     tommy ()
 *         Changes:
 *         2011-08-06: Created!
 *         2012-02-13: Updated to reflect changes in APSConfigDefinitionModel.
 *         2012-02-18: Fixed general name suck. Removed the stupid, not very well though through value model!
 *                     Now there are only 2 model types representing the structure of registered configuration
 *                     classes. These model types are responsible for the keys of values in the APSConfigValueStore.
 *                     So all you need to get or set a value is a model object and an APSConfigValueStore instance.
 *                     The APSConfig subclass instances produced by this (getInstance()) are completely dynamic!
 *                     All references to any configuration value will get it directly from the APSConfigValueStore.
 *                     The APSConfigValueList and APSConfigList values are also dynamic. If the number if entries
 *                     are changed the change will immediately be reflected since all config value references
 *                     even those in lists are fetched directly from the APSConfigValueStore on each access.
 *
 */
package se.natusoft.osgi.aps.core.config.model.admin;

import se.natusoft.osgi.aps.api.core.config.APSConfig;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSConfigItemDescription;
import se.natusoft.osgi.aps.api.core.config.annotation.APSDefaultValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigList;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValue;
import se.natusoft.osgi.aps.api.core.config.model.APSConfigValueList;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigEditModel;
import se.natusoft.osgi.aps.api.core.config.model.admin.APSConfigValueEditModel;
import se.natusoft.osgi.aps.api.core.config.service.APSConfigException;
import se.natusoft.osgi.aps.core.config.model.APSConfigObjectFactory;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * This is a model describing a subclass of APSConfig, modeling its structure. The annotated config classes
 * extending APSConfig is used for both describing the configuration and to provide configuration values.
 * Since such a class can contain both APSConfigValue members and other subclass of APSConfig members and
 * lists of both the configuration can have a structure. Internally however the configuration is stored
 * with key and value for each value where keys are automatically built and are internal to the implementation.
 * <p/>
 * Implementations of this interface describes/models a specific APSConfig subclass. The getValues() method
 * returns a list of the values held by the modelled config class. The entries in the list is either an
 * APSConfigValueModel or another APSConfigModel depending on the value type.
 * <p/>
 * This is intended for configuration editors using APSConfigAdminService to edit configuration. This models
 * the structure of the original and makes it easier to any editor to represent and edit this structure.
 * <p/>
 * Since this class do scan the whole APSConfig subclass class to build the model of it, it also at the same
 * time creates an instance of the APSConfig subclass and initializes it with valid values (which in turn
 * wraps a model which is used for the key to the value in the value store). So this basically does 2
 * different things at the same time to save time and make things easier. Once a model have been created
 * then all information for creating an instance and populating it, is right there available! Most of the
 * code here have to be duplicated to do this separately.
 */
public class APSConfigEditModelImpl<APSConfigSubclass extends APSConfig> extends APSConfigValueEditModelImpl
        implements APSConfigEditModel {
    //
    // Constants
    //

    /** A default empty set of default defaults. */
    private static final List<APSConfigDefaultValue> NO_DEFAULTS = new ArrayList<>();

    //
    // Private Members
    //

    /** The value models of this configuration class. */
    private List<APSConfigValueEditModel> values = new ArrayList<>();

    /** The value models of this configuration class by name. */
    private Map<String /*name*/, APSConfigValueEditModel> valuesByName = new HashMap<>();

    /** The configClass interface. */
    private Class<APSConfigSubclass> configClass;

    /** The configClass version. */
    private String version;

    /** The configuration id. */
    private String configId;

    /** The group of the config. */
    private String group;

    /** A factory for APSConfig* objects. */
    private APSConfigObjectFactory configObjectFactory = null;

    /** The instance created along with the model. */
    private APSConfigSubclass instance = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSConfigModelImpl instance from a subclass of APSConfig.
     *
     * @param configClass The config definition class to parse.
     * @param configObjectFactory A factory for APSConfig* objects.
     *
     * @throws APSConfigException On bad config class.
     */
    public APSConfigEditModelImpl(
            Class<APSConfigSubclass> configClass,
            APSConfigObjectFactory configObjectFactory
    ) throws APSConfigException {
        this(/*name*/null, configClass, /*parent*/null, configObjectFactory);
    }

    /**
     * Creates a new APSConfigModelImpl instance for representing an entry in a list.
     *
     * This model represents a configuration class. When there is only one instance only
     * one model is needed to represent it. However in this case we are dealing with a
     * specific instance that is part of a list of instances of this config.
     *
     * @param configClass The configClass interface class.
     * @param parent The parent of this config model. For top level model this should be null.
     * @param configObjectFactory A factory for APSConfig* objects.
     *
     * @throws APSConfigException on bad configClass.
     */
    public APSConfigEditModelImpl(
            Class<APSConfigSubclass> configClass,
            APSConfigEditModel parent,
            APSConfigObjectFactory configObjectFactory
    ) throws APSConfigException {
        this("", configClass, parent, configObjectFactory);
    }

    /**
     * Creates a new APSConfigModelImpl instance for representing an entry in a list.
     *
     * This model represents a configuration class. When there is only one instance only
     * one model is needed to represent it. However in this case we are dealing with a
     * specific instance that is part of a list of instances of this config.
     *
     * @param name The name of the instance.
     * @param configClass The configClass interface class.
     * @param parent The parent of this config model. For top level model this should be null.
     * @param configObjectFactory A factory for APSConfig* objects.
     *
     * @throws APSConfigException on bad configClass.
     */
    public APSConfigEditModelImpl(
            String name,
            Class<APSConfigSubclass> configClass,
            APSConfigEditModel parent,
            APSConfigObjectFactory configObjectFactory
    ) throws APSConfigException {
        super(new ConfigPartKey(ConfigPartKey.KeyType.CONFIG).name(name), name, parent, NO_DEFAULTS);
        this.configClass = configClass;
        this.configObjectFactory = configObjectFactory;

        try {
            this.instance = this.configClass.newInstance();
        }
        catch (IllegalAccessException | InstantiationException iae) {
            throw new APSConfigException(iae.getMessage(), iae);
        }

        APSConfigDescription configAnn = this.configClass.getAnnotation(APSConfigDescription.class);
        if (configAnn == null) {
            throw new APSConfigException("Configuration configClass '" + configClass.getName() +
                    "' is not annotated with @APSConfigDescription(...)! This is required!");
        }
        setDescription(configAnn.description());
        this.version = configAnn.version();
        this.configId = configAnn.configId();
        this.group = configAnn.group();
        getKey().
                configId(this.configId).
                version(this.version);

        for (Field field : this.configClass.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                APSConfigItemDescription configItem = field.getAnnotation(APSConfigItemDescription.class);
                if (configItem != null) {
                    parse(field, configAnn, configItem);
                }
            }
        }
    }

    //
    // Methods
    //

    /**
     * @return A populated instance of APSConfig subclass this class was constructed with.
     */
    public APSConfigSubclass getInstance() {
        return this.instance;
    }

    /**
     * @return The Class for the APSConfig subclass parsed by this model.
     */
    @Override
    public Class<APSConfigSubclass> getConfigClass() {
        return this.configClass;
    }

    /**
     * Returns the configClass version.
     */
    @Override
    public String getVersion() {
       return this.version;
    }

    /**
     * Returns the configuration id specified by the configClass.
     */
    @Override
    public String getConfigId() {
        return this.configId;
    }

    /**
     * The group of the config.
     */
    @Override
    public String getGroup() {
        return this.group;
    }

    /**
     * Returns the values for this configClass.
     */
    @Override
    public List<APSConfigValueEditModel> getValues() {
        return this.values;
    }

    /**
     * Gets a value by its name (java bean property name but in all lowercase).
     *
     * @param name The name of the value to get.
     *
     * @return The named value.
     */
    @Override
    public APSConfigValueEditModel getValueByName(String name) {
        name = name.toLowerCase();
        return this.valuesByName.get(name);
    }

    /**
     * Returns the names of all available values of the configuration class represented by this model.
     */
    @Override
    public Set<String> getValueNames() {
        return this.valuesByName.keySet();
    }

    /**
     * Utility method to set field and convert exception.
     *
     * @param field Field to set.
     * @param value Value to set field with.
     *
     * @throws APSConfigException On failure to set field.
     */
    private void setField(Field field, Object value) throws APSConfigException {
        try {
            field.set(this.instance, value);
        }
        catch (IllegalAccessException iae) {
            throw new APSConfigException(iae.getMessage(), iae);
        }
    }

    /**
     * Parses the field to get configuration data.
     *
     * @param field The field to parse.
     * @param apsConfig The APSConfigDescription annotation.
     * @param configItem The APSConfigItemDescription annotation.
     *
     * @throws APSConfigException On bad configClass interface.
     */
    private void parse(Field field, APSConfigDescription apsConfig, APSConfigItemDescription configItem) throws APSConfigException {

        String name = field.getName().toLowerCase();

        Class type = field.getType();
        validateType(type);

        APSConfigValueEditModelImpl valueModel;

        // List of normal config value.
        if (APSConfigValueList.class.isAssignableFrom(type)) {
            valueModel = addValue(name, APSConfigValue.class, true, configItem);

            // Initialize instance value.
            setField(field, this.configObjectFactory.createAPSConfigValueList(valueModel));
        }

        // Simple, normal config value.
        else if (APSConfigValue.class.isAssignableFrom(type)) {
            valueModel = addValue(name, APSConfigValue.class, false, configItem);

            // Initialize instance value.
            setField(field, this.configObjectFactory.createAPSConfigValue(valueModel));
        }

        // List of subclass of APSConfig.
        else if (APSConfigList.class.isAssignableFrom(type)) {
            // For fields this actually returns something sensible!
            if (ParameterizedType.class.isAssignableFrom(field.getGenericType().getClass())) {
                type = (Class)((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

                valueModel = addValue(name, type, true, configItem);

                // Initialize instance value.
                setField(field, this.configObjectFactory.createAPSConfigList((APSConfigEditModel)valueModel));
            }
            else {
                throw new APSConfigException(
                        "Bad configuration field: '" + field.getName() +
                        "' type! This is a List of an APSConfig subclass, but of unknown generic type! " +
                        "Must be APSConfigList<? extends APSConfig>."
                );
            }
        }

        // Subclass of APSConfig (sub config).
        else if (APSConfig.class.isAssignableFrom(type)) {
            if (APSConfig.class.equals(type)) {
                throw new APSConfigException("APSConfig is not a valid config value in itself! A subclass of APSConfig is needed!");
            }

            valueModel = addValue(name, type, false, configItem);

            // Initialize instance value.
            setField(field, ((APSConfigEditModelImpl)valueModel).getInstance());
        }

        // Bad APSConfig subclass!
        else {
            throw new APSConfigException("Bad configuration value type: '" + type.getName() +
                    "'! Only APSConfigValue, APSConfigValueList, ? extends APSConfig, and APSConfigList<? extends APSConfig> is allowed!");
        }
    }

    /**
     * Adds a value to this configClass model.
     *
     * @param name The name of the value
     * @param type The value type (either APSConfigValue, APSConfigValues or APSConfigSchema subclass).
     * @param isMany true if the type is an array.
     * @param configItem The config interface method annotation.
     *
     * @return The created APSConfigValueModelImpl.
     *
     * @throws APSConfigException On bad config configClass interface.
     */
    @SuppressWarnings("unchecked")
    private APSConfigValueEditModelImpl addValue(String name, Class type, boolean isMany, APSConfigItemDescription configItem) throws APSConfigException {
        APSConfigValueEditModelImpl valueEditModel;

        if (APSConfigValue.class.isAssignableFrom(type)) {
            List<APSConfigDefaultValue> defValues = new ArrayList<>();
            for (APSDefaultValue defValue : configItem.defaultValue()) {
                APSConfigDefaultValue defaultValue = new APSConfigDefaultValue(new APSConfigEnvironmentImpl(defValue.configEnv(), "", 0),
                        defValue.value());
                defValues.add(defaultValue);
            }
            valueEditModel = new APSConfigValueEditModelImpl(name, this, defValues);
        }
        else if (APSConfig.class.isAssignableFrom(type)) {

            valueEditModel = new APSConfigEditModelImpl(name, (Class<? extends APSConfig>)type, this, this.configObjectFactory);
        }
        else {
            throw new APSConfigException("Bad configuration field ('" + name + "') type! Must be either APSConfigValue, List<APSConfigValue> or " +
                "List<? extends APSConfig>!");
        }

        valueEditModel.setDescription(configItem.description());
        valueEditModel.setName(name);
        valueEditModel.setMany(isMany);
        valueEditModel.setConfigEnvSpecific(configItem.environmentSpecific());
        valueEditModel.setDatePattern(configItem.datePattern());
        valueEditModel.setIsBoolean(configItem.isBoolean());
        valueEditModel.setValidValues(configItem.validValues());
//        System.out.println(valueEditModel.getKey().toString());

        this.values.add(valueEditModel);
        this.valuesByName.put(name, valueEditModel);

        return valueEditModel;
    }

    /**
     * Throws an APSRuntimeException if the type is neither an APSConfigSchema nor an APSConfigValue.
     *
     * @param type The type to check.
     */
    private void validateType(Class type) {
        if (
            !APSConfig.class.isAssignableFrom(type) &&
            !APSConfigValue.class.isAssignableFrom(type) &&
            !APSConfigList.class.isAssignableFrom(type) &&
            !APSConfigValueList.class.isAssignableFrom(type)
        ) {
            throw new APSRuntimeException("Bad configuration value! Configuration API getter must return either a APSConfigValue or a " +
                    "APSConfig subclass! Offending type: " + type.getName());
        }
    }

    /**
     * @return A hash code for this object.
     */
    @Override
    public int hashCode() {
        return this.configId != null ? this.configId.hashCode() : super.hashCode();
    }

    /**
     * Compares this object with another object for equality.
     *
     * @param obj The object to compare to.
     *
     * @return true if both are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (!APSConfigEditModelImpl.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        if (this.configId != null) {
            return this.configId.equals(((APSConfigEditModelImpl)obj).configId);
        }

        return super.equals(obj);
    }
}
