/*
 *
 * PROJECT
 *     Name
 *         APS JSON Library
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides a JSON parser and creator. Please note that this bundle has no dependencies to any
 *         other APS bundle! It can be used as is without APS in any Java application and OSGi container.
 *         The reason for this is that I do use it elsewhere and don't want to keep 2 different copies of
 *         the code. OSGi wise this is a library. All packages are exported and no activator nor services
 *         are provided.
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
 *         2012-01-15: Created!
 *
 */
package se.natusoft.osgi.aps.json;

/**
 * This exception is thrown on failure to convert from JSON to Java or Java to JSON.
 *
 * Almost all exceptions within the APS services and libraries extend either _APSException_ or _APSRuntimeException_.
 * I decided to just extend RuntimeException here to avoid any other dependencies for this library since it can
 * be useful outside of APS and can be used as any jar if not deployed in OSGi container.
 */
public class JSONConvertionException extends RuntimeException {

    /**
     * Creates a new _JSONConvertionException_.
     *
     * @param message The exception message
     */
    public JSONConvertionException(final String message) {
        super(message);
    }

    /**
     * Creates a new _JSONConvertionException_.
     *
     * @param message The exception message
     * @param cause The cause of this exception.
     */
    public JSONConvertionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

