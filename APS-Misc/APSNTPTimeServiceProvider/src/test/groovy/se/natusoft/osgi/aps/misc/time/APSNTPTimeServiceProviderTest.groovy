package se.natusoft.osgi.aps.misc.time

import org.junit.Test
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.api.misc.time.APSTimeService
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.util.APSLogger

import java.time.Instant
import java.util.concurrent.TimeUnit

class APSNTPTimeServiceProviderTest extends OSGIServiceTestTools {

    private APSLogger logger = new APSLogger()

    @Test
    void testNTPTimeService() throws Exception {

        deployConfigAndVertxPlusDeps(  )

        // The actual code to test

        deploy 'aps-ntp-time-service-provider' with new APSActivator() from 'APS-Misc/APSNTPTimeServiceProvider/target/classes'

        try {
            with_new_bundle 'test-exec-bundle', { BundleContext context ->

                APSServiceTracker<APSTimeService> timeServiceTracker =
                        new APSServiceTracker<>(context, APSTimeService.class, "2 sec")
                timeServiceTracker.start()

                // To make sure deployment is done and that tracker has picked up service.
                hold() maxTime 2 unit TimeUnit.SECONDS

                APSTimeService apsTimeService = timeServiceTracker.allocateService()
                println "Local time: ${Instant.ofEpochMilli(new Date().time)}"
                println "Remote time: ${apsTimeService.time}"

                // There really isn't much we can validate here!
                assert apsTimeService.time != null
                assert apsTimeService.lastTimeUpdate != null

                timeServiceTracker.stop()

            }
        }
        finally {
            hold() maxTime 2 unit TimeUnit.SECONDS go()

            shutdown()
        }


    }
}
