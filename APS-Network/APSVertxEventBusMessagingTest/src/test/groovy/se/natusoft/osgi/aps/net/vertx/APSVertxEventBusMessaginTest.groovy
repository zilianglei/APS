package se.natusoft.osgi.aps.net.vertx

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.junit.Test
import se.natusoft.osgi.aps.api.messaging.APSMessagePublisher
import se.natusoft.osgi.aps.api.messaging.APSMessageSubscriber
import se.natusoft.osgi.aps.api.messaging.APSMessage
import se.natusoft.osgi.aps.constants.APS
import se.natusoft.osgi.aps.core.lib.StructMap
import se.natusoft.osgi.aps.types.APSResult
import se.natusoft.osgi.aps.types.ID
import se.natusoft.osgi.aps.test.tools.OSGIServiceTestTools
import se.natusoft.osgi.aps.activator.APSActivator
import se.natusoft.osgi.aps.util.APSLogger
import se.natusoft.osgi.aps.activator.annotation.Initializer
import se.natusoft.osgi.aps.activator.annotation.Managed
import se.natusoft.osgi.aps.activator.annotation.OSGiService
import se.natusoft.osgi.aps.types.APSUUID
import static java.util.concurrent.TimeUnit.SECONDS

@CompileStatic
@TypeChecked
class APSVertXEventBusMessagingTest extends OSGIServiceTestTools {

    static final String UNIQUE_MESSAGE = UUID.randomUUID().toString()

    static boolean messageReceived = false

    @Test
    void runTest() throws Exception {

        deployConfigAndVertxPlusDeps(
                // Override default deployment from ~/.m2/... of our self and use the just built code instead.
                busMessagingDeployer( null ) {
                    deploy 'aps-vertx-event-bus-messaging-provider' with new APSActivator() from 'se.natusoft.osgi.aps','aps-vertx-event-bus-messaging-provider','1.0.0'
                }
        )

        deploy 'msg-receiver' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/MsgReceiver.class'

        deploy 'msg-sender' with new APSActivator() using '/se/natusoft/osgi/aps/net/vertx/MsgSender.class'

        // Wait for and then validate result. Even if in this case a cluster of one member is created, it takes
        // some time.

        try {
            hold() whilst { !messageReceived } maxTime 10L unit SECONDS go()

            assert messageReceived
        }
        finally {
            shutdown()
            Thread.sleep( 500 ) // Give Vertx time to shut down.
        }
    }
}

@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
class MsgReceiver {

    @OSGiService( timeout = "15 sec", nonBlocking = true )
    private APSMessageSubscriber<Map<String, Object>> msgSubscriber

    @Managed( loggingFor = "msg-receiver" )
    private APSLogger logger

    @Initializer
    void init() {
        this.logger.info( "<<<<<< MsgReceiver >>>>>" )

        ID subscriptionId = new APSUUID()

        this.msgSubscriber.subscribe( "testaddr", subscriptionId, APS.MSG_NO_RESULT ) {
            APSMessage<Map<String, Object>> messageValue ->

                this.logger.info( ">>>>>> Received message!" )

                StructMap message = new StructMap( messageValue.content() )
                message.lookupObject( "meta.test-message" ).onTrue() { boolean tm ->

                    if ( message[ 'id' ] == APSVertXEventBusMessagingTest.UNIQUE_MESSAGE ) {

                        this.logger.info( "Got '${ message[ "id" ] }'!" )
                        APSVertXEventBusMessagingTest.messageReceived = true
                    }
                }

                this.msgSubscriber.unsubscribe( subscriptionId ) { APSResult res ->
                    if ( !res.success() ) {
                        this.logger.error( "Failed to unsubscribe!" )
                    }
                }
        }
        this.logger.info( "Subscribed to 'testaddr'" )
    }
}


@SuppressWarnings( "GroovyUnusedDeclaration" )
@CompileStatic
@TypeChecked
class MsgSender {

    // This manages since on nonBlocking = true, the call to msgService is cached by the proxy until
    // the service is available, and then executed.
    @OSGiService( timeout = "15 sec", nonBlocking = true )
    private APSMessagePublisher<Map<String, Object>> msgPublisher

    @Managed( loggingFor = "msg-sender" )
    private APSLogger logger

    @Initializer
    void init() {
        this.logger.info( "<<<<<< MsgSender >>>>>" )

        // Note that we must do publish, not send here! send sends to one listening target member. If there
        // are more it does a round robin, but it only goes to one in each case. Publish however always goes
        // to everybody. Since the clustered version of vertx is used, this means that any cluster on the same
        // subnet will receive the message, including other builds running at the same time. This is why we are
        // sending an UUID and publish to everyone. The receiver will only react on the correct UUID and ignore
        // the rest.

        this.msgPublisher.publish( "testaddr",
                ["meta": ["test-message": true],
                 "id"  : APSVertXEventBusMessagingTest.UNIQUE_MESSAGE] as Map<String, Object> ) { APSResult res ->
            if ( !res.success() ) {
                throw res.failure()
            }
            else {
                logger.info( "Published '${ APSVertXEventBusMessagingTest.UNIQUE_MESSAGE }'!" )
            }
        }

    }


}

