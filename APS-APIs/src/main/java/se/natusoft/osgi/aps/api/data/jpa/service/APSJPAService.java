/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
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
 *         2012-07-18: Created!
 *         
 */
package se.natusoft.osgi.aps.api.data.jpa.service;

import org.osgi.framework.BundleContext;
import se.natusoft.osgi.aps.exceptions.APSResourceNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Map;

/**
 * This service allows an JPA _EntityManager_ to be gotten for a persistent unit name.
 *
 * So why is this done this way ? Why is not an _EntityManagerFactory_ returned?
 *
 * The answer to that is that the _EntityManagerFactory_ is internal to the service who is
 * responsible for creating it and for closing it at sometime (stopping of bundle). The
 * client only needs an _EntityManager_ for which the client is responsible after its creation.
 *
 * The creation of the _EntityManagerFactory_ is delayed until the call to _initialize(...)_.
 * Creating the EMF along with the persistence provider at persistence bundle discovery would
 * limit database connection properties to the persistence.xml file which is less than optimal
 * to put it mildly. This way a client can make use of the _APSDataSourceDefService_ to get the
 * JDBC properties which it can pass along to this service.
 *
 * The default provider implementation of this service uses OpenJPA which provides its own
 * connection pooling.
 */
public interface APSJPAService {

    /**
     * Initializes and returns a provider from the specified properties.
     *
     * @param bundleContext The context of the client bundle. It is used to locate its persistence provider.
     * @param persistenceUnitName The name of the persistent unit defined in persistence.xml.
     * @param props Custom properties to configure database, etc.
     *
     * @return A configured EntityManager.
     */
    APSJPAEntityManagerProvider initialize(BundleContext bundleContext, String persistenceUnitName, Map<String, String> props) throws APSResourceNotFoundException;

    /**
     * Once you get this it is valid until the _APSJPAService_ is stopped (which will happen if the service is redeployed!).
     */
    interface APSJPAEntityManagerProvider {

        /**
         * Returns true if this instance is valid. If not call APSJPAService.initialize(...) again to get a new instance.
         * It will be invalid if the APSJPAService provider have been restarted.
         */
        boolean isValid();

        /**
         * Creates a new _EntityManager_. You are responsible for closing it!
         *
         * Please note that the _EntityManager_ caches all referenced entities. If you keep and reuse it for a longer
         * time it can use more memory. For example at
         * <http://docs.jboss.org/ejb3/app-server/tutorial/extended_pc/extended.html>
         * it says that "Usually, an _EntityManager_ in JBoss EJB 3.0 lives and dies within a JTA transaction". This
         * indicates how long-lived the _EntityManager_ should preferably be.
         *
         * @return A configured EntityManager.
         */
        EntityManager createEntityManager();

        /**
         * Returns the underlying _EntityManagerFactory_. This will return null if isValid() return false!
         *
         * Be very careful what you do with this! It is managed by this service!
         */
        EntityManagerFactory getEntityManagerFactory();
    }

}
