package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import org.junit.Test
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigList
import se.natusoft.osgi.aps.net.messaging.models.config.TestConfigValue
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.net.vertx.api.VertxConsumer
import se.natusoft.osgi.aps.net.vertx.config.VertxConfig
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.tools.APSActivator
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.Managed
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiProperty
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider
import se.natusoft.osgi.aps.tools.reactive.Consumer

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class APSVertxProviderTest extends OSGIServiceTestTools {

    public static Consumer.Consumed<Vertx> vertx = null
    public static Consumer.Consumed<Router> router = null

    @Test
    void reactiveAPITest() throws Exception {
        // Most of the unfamiliar constructs here are provided by OSGiServiceTestTools and groovy DSL features.

        println "============================================================================"
        println "DO NOTE: All the RED colored output comes from Vertx! It is not something "
        println "that have failed! Vertx have just chosen this color for their log output!"
        println "============================================================================"

        deploy 'aps-vertx-provider' with new APSActivator() with {

            VertxConfig config = new VertxConfig()
            TestConfigList<VertxConfig.VertxConfigValue> entries = new TestConfigList<>()

            VertxConfig.VertxConfigValue entry = new VertxConfig.VertxConfigValue()
            entry.name = new TestConfigValue(value: "workerPoolSize")
            entry.value = new TestConfigValue(value: "40")
            entry.type = new TestConfigValue(value: "Int")

            entries.configs.add(entry)

            config.optionsValues = entries

            config

        } using '/se/natusoft/osgi/aps/net/vertx/APSVertxProvider.class'

        deploy 'vertx-consumer-svc' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/VertxConsumerService.class'

        try {

            hold() whilst { vertx == null } maxTime 25L unit SECONDS go()

            assert vertx != null
            assert vertx.get() != null
            assert router != null
            assert router.get() != null

        }
        finally {
            if (vertx != null) vertx.release()
            shutdown()
            hold() maxTime 500 unit MILLISECONDS go() // Give Vertx time to shut down.
        }
    }

}

@SuppressWarnings("GroovyUnusedDeclaration")
@OSGiServiceProvider( properties = [
        @OSGiProperty( name = "consumed", value = "vertx"),
        @OSGiProperty( name = APSVertxService.HTTP_SERVICE_NAME, value = "test")
] )
@CompileStatic
@TypeChecked
// Important: Service interface must be the first after "implements"!! Otherwise serviceAPIs=[Consumer.class] must be specified
// in @OSGiServiceProvider annotation.
class VertxConsumerService extends VertxConsumer implements Consumer<Vertx> {

    @Managed(loggingFor = "Test:VertxConsumerService")
    APSLogger logger

    // Note that this only registers callbacks! The callbacks themselves will not be called until the
    // service have been published. This will not happen util after all injections are done. Thereby
    // this.logger.info(...) will always work.
    VertxConsumerService() {
        this.onVertxAvailable = { Consumer.Consumed<Vertx> vertx ->
            this.logger.info("Received Vertx instance! [${vertx}]")
            APSVertxProviderTest.vertx = vertx
        }
        this.onVertxUnavilable = {
            this.logger.error("No vertx instance available!")
            throw new Exception("Failure, no vertx service available!")
        }
        this.onVertxRevoked = {
            this.logger.info("Vertx instance revoked!")
        }
        this.onRouterAvailable = { Consumer.Consumed<Router> router ->
            this.logger.info("Received Router instance! [${router}]")
            APSVertxProviderTest.router = router
        }
        this.onError = { String message ->
            this.logger.error(message)
        }
    }
}

