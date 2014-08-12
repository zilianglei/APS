/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
 *         2011-08-13: Created!
 *
 */
package se.natusoft.osgi.aps.api.core.config.model;

import java.util.Date;

/**
 * This represents a configuration value.
 */
public interface APSConfigValue {

    /**
     * Returns the value as a String.
     */
    @Override
    String toString();

    /**
     * Returns the value as a boolean.
     */
    boolean toBoolean();

    /**
     * Returns the value as a double.
     */
    double toDouble();

    /**
     * Returns the value as a float.
     */
    float toFloat();

    /**
     * Returns the value as an int.
     */
    int toInt();

    /**
     * Returns the value as a long.
     */
    long toLong();

    /**
     * Returns the value is a byte.
     */
    public byte toByte();

    /**
     * Returns the value as a short.
     */
    public short toShort();

    /**
     * Returns the value as a Date.
     */
    Date toDate();

    /**
     * Returns the value as a String.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    String toString(String configEnvironment);

    /**
     * Returns the value as a boolean.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    boolean toBoolean(String configEnvironment);

    /**
     * Returns the value as a double.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    double toDouble(String configEnvironment);

    /**
     * Returns the value as a float.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    float toFloat(String configEnvironment);

    /**
     * Returns the value as an int.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    int toInt(String configEnvironment);

    /**
     * Returns the value as a long.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    long toLong(String configEnvironment);

    /**
     * Returns the value is a byte.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    public byte toByte(String configEnvironment);

    /**
     * Returns the value as a short.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    public short toShort(String configEnvironment);

    /**
     * Returns the value as a Date.
     *
     * @param configEnvironment The config environment to get config value for.
     */
    Date toDate(String configEnvironment);

    /**
     * Returns true if value is empty.
     */
    boolean isEmpty();

    /**
     * Returns true if value is empty.
     */
    public boolean isEmpty(String configEnvironment);

}
