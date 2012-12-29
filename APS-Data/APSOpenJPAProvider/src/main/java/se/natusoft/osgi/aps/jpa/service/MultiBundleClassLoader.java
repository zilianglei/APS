/* 
 * 
 * PROJECT
 *     Name
 *         APS OpenJPA Provider
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides an implementation of APSJPAService using OpenJPA.
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
 *         2012-08-19: Created!
 *         
 */
package se.natusoft.osgi.aps.jpa.service;

import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * This is a class loader that wraps one or more bundles providing classpath for all wrapped bundles.
 */
public class MultiBundleClassLoader extends ClassLoader {

    //
    // Private Members
    //

    /** The bundles we are wrapping. */
    private Bundle[] bundles = null;

    //
    // Constructors
    //

    /**
     * Creates a new MultiBundleClassLoader.
     *
     * @param bundles The bundles to wrap and provide classpath for.
     */
    public MultiBundleClassLoader(Bundle... bundles) {
        this.bundles = bundles;
    }

    //
    // Methods
    //

    /**
     * Finds a class of a specific name.
     *
     * @param name The fully qualified name of the class to find.
     *
     * @return The found class.
     *
     * @throws ClassNotFoundException on failure to find class.
     */
    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        Class found = null;
        for (Bundle bundle : this.bundles) {
            if (bundle.getState() == Bundle.ACTIVE) {
                try {
                    found = bundle.loadClass(name);
                    break;
                }
                catch (ClassNotFoundException cnfe) {}
            }
        }

        if (found == null) {
            throw new ClassNotFoundException("Class '" + name + "' was not found!");
        }

        return found;
    }

    /**
     * Finds a resource.
     *
     * @param resource The resource to find.
     *
     * @return The found resource or null if none were found.
     */
    @Override
    public URL findResource(String resource) {
        URL found = null;

        for (Bundle bundle : this.bundles) {
            if (bundle.getState() == Bundle.ACTIVE) {
                found = bundle.getResource(resource);
                if (found != null) break;
            }
        }

        return found;
    }

    /**
     * Finds a set of resources.
     *
     * @param resource The resource to find.
     *
     * @return The found resource or null if not found.
     *
     * @throws IOException on IO errors.
     */
    @Override
    public Enumeration<URL> findResources(String resource) throws IOException {
        Enumeration<URL> found = null;

        for (Bundle bundle : this.bundles) {
            if (bundle.getState() == Bundle.ACTIVE) {
                try {
                    found = bundle.getResources(resource);
                    if (found != null && found.hasMoreElements()) break;
                }
                catch (IOException ioe) {
                    if (bundle == this.bundles[this.bundles.length - 1]) {
                        throw ioe;
                    }
                }
            }
        }

        return found;
    }
}