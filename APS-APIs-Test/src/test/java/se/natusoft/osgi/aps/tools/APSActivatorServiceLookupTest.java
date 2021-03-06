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
 *     tommy ()
 *         Changes:
 *         2015-01-10: Created!
 *
 */
package se.natusoft.osgi.aps.tools;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import se.natusoft.osgi.aps.activator.APSActivator;
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools;
import se.natusoft.osgi.aps.test.tools.TestBundle;
import se.natusoft.osgi.aps.tools.services.SearchCriteriaProviderTestService;
import se.natusoft.osgi.aps.tools.services.TestService;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("PackageAccessibility")
public class APSActivatorServiceLookupTest {

    @Test
    public void searchCriteriaProviderTest() throws Exception {
        OSGIServiceTestTools testTools = new OSGIServiceTestTools();
        TestBundle testBundle = testTools.createBundle("test-bundle");
        testBundle.addEntryPaths(
                "/se/natusoft/osgi/aps/tools/services/ServicesSetupProviderTestService.class",
                "/se/natusoft/osgi/aps/tools/services/SearchCriteriaProviderTestService.class"
        );

        APSActivator activator = new APSActivator();
        activator.start(testBundle.getBundleContext());

        try {
            // There should now be 3 instances of ServicesSetupProviderTestService, and one of SearchCriteriaProviderTestService.
            // All provides TestService, which is why we need to loop through all services until we find the service with the
            // implementation class we want. That service should have one of the the other class instances injected into it
            // as TestService and will forward getServiceInstanceInfo() to it. Since we filtered on "second" in the search
            // criteria we expect the string "second" to be returned.
            for (ServiceReference serviceReference : testBundle.getRegisteredServices()) {
                TestService ts = (TestService)testBundle.getBundleContext().getService(serviceReference);
                if (ts instanceof SearchCriteriaProviderTestService) {
                    Assert.assertEquals("Expected to get 'second' back from getServiceInstanceInfo()!", ts.getServiceInstanceInfo(), "second");
                }
                testBundle.getBundleContext().ungetService(serviceReference);
            }
        }
        finally {
            activator.stop(testBundle.getBundleContext());
        }

    }
}
