package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration
import se.natusoft.docutations.NotNull
import se.natusoft.docutations.Nullable
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiProperty
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.activator.annotation.OSGiServiceProvider
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.api.messaging.APSMessageSubscriber
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.exceptions.APSValidationException
import se.natusoft.osgi.aps.model.APSHandler
import se.natusoft.osgi.aps.model.APSResult
import se.natusoft.osgi.aps.model.ID
import se.natusoft.osgi.aps.activator.APSActivatorInteraction
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.tracker.APSServiceTracker
import se.natusoft.osgi.aps.tools.annotation.activator.*

@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
@OSGiServiceProvider(
        // Possible criteria for client lookups. ex: "(aps-messaging-protocol=vertx-eventbus)" In most cases clients
        // won't care.
        properties = [
                @OSGiProperty( name = APS.Service.Provider, value =
                        "aps-vertx-event-bus-messaging-provider:subscriber" ),
                @OSGiProperty( name = APS.Service.Category, value = APS.Value.Service.Category.Network ),
                @OSGiProperty( name = APS.Service.Function, value = APS.Value.Service.Function.Messaging ),
                @OSGiProperty( name = APS.Messaging.Protocol.Name, value = "vertx-eventbus" ),
                @OSGiProperty( name = APS.Messaging.Persistent, value = APS.FALSE ),
                @OSGiProperty( name = APS.Messaging.Clustered, value = APS.TRUE )
        ]
)
class MessageSubscriberProvider extends AddressResolver implements APSMessageSubscriber<Message> {

    //
    // Private members
    //

    /**
     * We manage the registration of our service our self through the APSActivatorInteraction API. There will
     * be one ServiceRegistration put in this list since we only have one instance.
     *
     * When services are injected by APSActivator, as service rather than an APSServiceTracker<Service>, then
     * a java.lang.reflect.Proxy:ied version is injected. This proxy uses the APSServiceTrackers active service,
     * allocates it, calls the method, and then releases the service again, and then returns the result. However
     * if the tracker has no found service then the proxy call is put into a List, and when the tracker has
     * received a service then all entries in this list are executed against the service, and the list is then
     * emptied. This allows for calls to be made to a wrapped/proxied service instance even if the real service
     * is not yet available. This however only works for reactive APIs that has no return values, for obvious
     * reasons.
     *
     * The EventBus does not fulfil that requirement. Thereby we remove our self by unregistering us as an
     * OSGi service when we loose the EventBus. Since the API for this service completely to 100% fulfils
     * this requirement, clients of this can still do proxied (and cached) calls while we are away, and they
     * will be executed when we are available again. **Do note** however that the nonBlocking=true attribute
     * must be set on the @OSGiService annotation when used like this.
     *
     * All this of course assumes that a service goes away only because it is being restarted / upgraded, and
     * will rather quickly be available again.
     **/
    private List<ServiceRegistration> svcRegs = [ ]

    /** The logger for this class. */
    @Managed( name = "subscriber", loggingFor = "aps-vertx-eventbus-messaging:subscriber" )
    private APSLogger logger

    /** Our bundles context. */
    @Managed
    private BundleContext context

    /**
     * This tracks the EventBus. init() will setup an onActiveServiceAvailable callback handler which
     * will provide the eventBus instance.
     **/
    @OSGiService( additionalSearchCriteria = "(vertx-object=EventBus)", timeout = "30 sec" )
    private APSServiceTracker<EventBus> eventBusTracker
    private EventBus eventBus

    /** Used to delay service registration. */
    @Managed( name = "subscriberAI" )
    private APSActivatorInteraction activatorInteraction

    /** Active subscribers are stored here. */
    private Map<ID, MessageConsumer> subscribers = [ : ]

    //
    // Startup / shutdown
    //

    /**
     * This is run by APSActivator when all @Managed & @OSGiService annotated fields have been injected.
     **/
    @Initializer
    void init() {
        // Yes, what these handlers do could be done directly below in onActiveServiceAvailable {...} instead
        // of changing state. This is however more future safe.
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.READY ) {
            this.activatorInteraction.registerService( MessageSubscriberProvider.class, this.context, this.svcRegs )
        }
        this.activatorInteraction.setStateHandler( APSActivatorInteraction.State.TEMP_UNAVAILABLE ) {
            this.svcRegs.first().unregister()
            this.svcRegs.clear()
        }

        this.eventBusTracker.onActiveServiceAvailable { EventBus service, ServiceReference serviceReference ->
            this.eventBus = service

            this.activatorInteraction.state = APSActivatorInteraction.State.READY
        }
        this.eventBusTracker.onActiveServiceLeaving { ServiceReference service, Class serviceAPI ->
            this.eventBus = null

            this.activatorInteraction.state = APSActivatorInteraction.State.TEMP_UNAVAILABLE
        }
    }

    //
    // Methods
    //

    /**
     * Adds a subscriber.
     *
     * @param destination The destination to subscribe to.
     *                    This is up to the implementation, but it is strongly recommended that
     *                    this is a name that will be looked up in some configuration for the real
     *                    destination, by the service rather than have the client pass a value from
     *                    its configuration.
     * @param subscriptionId A unique id for this subscription. Use the same to unsubscribe.
     * @param result The result of the call callback. Will always return success. Can be null.
     * @param handler The subscription handler.
     */
    @Override
    void subscribe( @NotNull String destination, @NotNull ID subscriptionId, @Nullable APSHandler<APSResult> result,
                    @NotNull APSHandler<APSMessage<Message>> handler ) {
        //        this.logger.error( "@@@@@@@@ THREAD: ${Thread.currentThread()}" )

        String address = resolveAddress( destination )

        MessageConsumer consumer = this.eventBus.consumer( address ) { Message<String> msg ->

            // The JsonObject that Vertx provides for the event bus can be created from a Map,
            // but when such is received and .getMap() is called on it, it only produces a Map
            // for that top level instance! If you get a key that returns another object it will
            // be a JsonObject and not a Map. So .getMap() has to be called on each sub object.
            // That does not make the Map a true JSON-ish representation. You don't get back
            // what was used to create it in the first place. Passing this Map to StructMap
            // (aps-core-lib) of course fails. Therefore I decided to use the APS JSON handling
            // instead and just convert the Map to JSON in a String when sending, and then
            // parsing the String as JSON and converting to a Map<String, Object> structure
            // when receiving. This results in an identical object to what was sent. This is
            // now handled by TypeConv which actually supports several formats.

            Object message = TypeConv.vertxToAps( msg.body() )

            handler.handle( new APSMessageProvider( message: message, vertxMsg: msg ) )

        }

        this.subscribers[ subscriptionId ] = consumer

        if ( result != null ) {
            result.handle( APSResult.success( null ) )
        }
    }

    /**
     * Cancel a subscription.
     *
     * @param subscriptionId The same id as passed to subscribe.
     * @param result The result of the call. Can be null.
     */
    @Override
    void unsubscribe( @NotNull ID subscriptionId, @Nullable APSHandler<APSResult> result ) {
        //        this.logger.error( "@@@@@@@@ THREAD: ${Thread.currentThread()}" )

        MessageConsumer consumer = this.subscribers[ subscriptionId ]

        if ( consumer != null ) {
            consumer.unregister()

            if ( result != null ) {
                result.handle( APSResult.success( null ) )
            }
        }
        else {
            Exception e = new APSValidationException( "No subscription with id '${subscriptionId}' exists!" )
            if ( result != null ) {
                result.handle( APSResult.failure( e ) )
            }
            else {
                throw e
            }
        }
    }
}
