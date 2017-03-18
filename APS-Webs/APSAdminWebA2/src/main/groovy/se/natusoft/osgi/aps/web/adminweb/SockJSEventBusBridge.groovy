package se.natusoft.osgi.aps.web.adminweb

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.sockjs.SockJSHandler
import org.osgi.framework.BundleContext
import se.natusoft.osgi.aps.net.vertx.api.APSVertxService
import se.natusoft.osgi.aps.tools.reactive.Consumer
import se.natusoft.osgi.aps.net.vertx.api.VertxConsumer
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.*

/**
 * Provides a Vertx EventBus bridge.
 *
 * __GroovyUnusedDeclaration__
 *
 * There are complains that this class is not used, this because it is managed by APSActivator and the IDE
 * cannot see the code that creates and manages this instance.
 *
 * __PackageAccessibility__
 *
 * This is an OSGi issue. OSGi imports and exports packages, and to be deployable a jar must contain a
 * valid MANIFEST.MF with OSGi keys for imports, exports, etc. Must 3rd party jars do contain a valid
 * OSGi MANIFEST.MF exporting all packages of the jar sp that they can just be dropped into an OSGi
 * container and have their classpath be made available to all other code running in the container.
 *
 * The Groovy Vertx wrapper code does not contain a valid OSGi MANIFEST.MF. I have solved this by having
 * the aps-vertx-provider bundle include the Groovy Vertx wrapper, and export all packages of that
 * dependency. So as long as the aps-vertx-provider is deployed the Groovy Vertx wrapper code will
 * also be available runtime. IDEA however does not understand this. It does not figure out the
 * exported dependency from aps-vertx-provider either. So it sees code that is not OSGi compatible
 * and used in the code without including the dependency jar in the bundle, and complains about
 * that. But since in reality this code will be available at runtime I just hide these incorrect
 * warnings.
 */
@SuppressWarnings(["GroovyUnusedDeclaration", "PackageAccessibility"])
@CompileStatic
@TypeChecked
@OSGiServiceProvider(properties = [
        @OSGiProperty(name = "consumed", value = "vertx"),
        @OSGiProperty(name = APSVertxService.HTTP_SERVICE_NAME, value = "default")
])
class SockJSEventBusBridge extends VertxConsumer implements Consumer<Vertx>, Constants  {
    //
    // Private Members
    //

    @Managed
    private BundleContext context

    @Managed(loggingFor = "aps-admin-web-a2:sockjs-eventbus-bridge")
    private APSLogger logger

    /** A Vertx instance. */
    private Consumer.Consumed<Vertx> vertx

    /** A Router for an HTTP server. */
    private Consumer.Consumed<Router> router

    //
    // Constructors
    //

    SockJSEventBusBridge() {

        this.onVertxAvailable = { Consumer.Consumed<Vertx> vertx -> this.vertx = vertx }

        this.onRouterAvailable = { Consumer.Consumed<Router> router ->
            this.router = router

            // Currently no more detailed permissions than on target address. Might add limits on message contents
            // later.
            def twowaysPermitted1 = [address: BUS_ADDRESS]

            SockJSHandler sockJSHandler = SockJSHandler.create(this.vertx.get()).bridge([
                    inboundPermitteds : [twowaysPermitted1] as Object,
                    outboundPermitteds: [twowaysPermitted1] as Object
            ])

            // Note that this router is already bound to an HTTP server!
            router.get().route("/eventbus/*").handler(sockJSHandler)

            this.logger.info "Vert.x SockJSHandler for event bus bridging started successfully!"
        }

        this.onVertxRevoked = {
            this.vertx = null
            this.logger.info("Vertx was revoked. This bridge will be down until new Vertx arrives.")
        }
    }

    //
    // Methods
    //

    /**
     * Called after all injections are done.
     */
    @Initializer
    void init() {
        this.logger.connectToLogService(this.context)
    }

    @BundleStop
    void shutdown() {
        if (this.router != null) {
            this.router.get().get("/eventbus/*").remove()
            this.router.release()
        }
        if (this.vertx != null) this.vertx.release()
        this.logger.disconnectFromLogService(this.context)
    }
}
