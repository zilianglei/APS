/**
 * ## Types
 *
 * ### Address
 *
 * This is a target group for messages. A message sent to an address will only be received by
 * those listening to that address.
 *
 * ### Routers
 *
 * This class actually does nothing at all! It just provides an API to one or more "routers".
 * Well you can have zero also, but nothing will happen then. This just calls the corresponding
 * subscribe(...), unsubscribe(...), message(...), and publish(...) method of each added router.
 *
 * The intention with this is to have at least 2 different "routers". One that only sends locally
 * among the subscribers within the JS instance, that is no networking, just calling another object
 * directly. One that uses the Vert.x eventbus JS client over the eventbus bridge to the Vert.x server
 * side. Most of the messages sent and received are only between the local components and it would be
 * a waste to message them out on the network and back again. Some of the messages we want to go to the
 * backend. This will also allow different clients to indirectly communicate with each other.
 *
 * Also if at some later time I decided to use something else than Vert.x for example, then I only
 * need to change the router handling Vert.x. It will not affect the components which only uses
 * this.
 */
import APSLogger from "./APSLogger";
import { EVENT_ROUTES, EVENT_ROUTING } from "./Constants";

export default class LocalEventBus {

    /**
     * Creates a new LocalEventBus.
     *
     * @constructor
     */
    constructor() {

        this.logger = new APSLogger( "LocalEventBus" );

        // noinspection JSValidateTypes
        /**
         * @type {array} busRouters. These routers must provide the subscribe, unsubscribe, and message methods
         *               and parameters as this API have. These calls will be forwarded to each added router.
         *               Router is a bad name here since each actually takes full responsibility for any
         *               messaging. But it also makes things clear and very flexible. The components only
         *               uses / knows about this class. Where messages go and where they come from is the
         *               responsibility for other code.
         */
        this.busRouters = [];
    }

    /**
     * JSDoc declaration of messageReceiver as param type.
     *
     * @callback subscriber
     * @param {object} message
     */

    /**
     * This adds a subscriber for an address. The first param can also be an object containing 3 keys for each
     * parameter. In that case the other 2 are ignored.
     *
     * @param {string} address        - The address to subscribe to messages from.
     * @param {object} headers        - Filter subscriptions on headers.
     * @param {subscriber} subscriber - A function taking an address and a message.
     */
    subscribe( address, headers = {}, subscriber = null ) {

        if ( typeof address === "object" ) {
            let params = address;
            address = params["address"];
            headers = params["headers"];
            subscriber = params["subscriber"];
        }

        LocalEventBus.validRoutingHeaders( headers );

        if ( headers == null && subscriber == null ) {
            throw new Error( "If the first argument is not an object containing 3 keys then header and subscriber " +
                "must also be supplied!" );
        }

        for ( let busRouter of this.busRouters ) {
            busRouter.subscribe( address, headers, subscriber );
        }
    }

    /**
     * Unsubscribes to a previously done subscription.
     *
     * @param {string} address        - The address of the subscription.
     * @param {object} headers        - The headers used to subscribe with.
     * @param {subscriber} subscriber - The subscriber to unsubscribe.
     */
    unsubscribe( address, headers = null, subscriber = null ) {

        if ( typeof address === "object" ) {
            let params = address;
            address = params["address"];
            headers = params["headers"];
            subscriber = params["subscriber"];
        }

        LocalEventBus.validRoutingHeaders( headers );

        if ( headers == null && subscriber == null ) {
            throw new Error( "If the first argument is not an object containing 3 keys then header and subscriber " +
                "must also be supplied!" );
        }

        for ( let busRouter of this.busRouters ) {

            busRouter.unsubscribe( address, headers, subscriber );
        }
    }

    /**
     * Sends a message to one subscriber.
     *
     * @param {string} address  - The address to message to.
     * @param {object} headers  - The headers for the message.
     * @param {object} message  - The message to message. Note that this must be a JSON string
     */
    message( address, headers = {}, message = {} ) {

        if ( typeof address === "object" ) {
            let params = address;
            address = params["address"];
            headers = params["headers"];
            message = params["message"];
        }

        LocalEventBus.validRoutingHeaders( headers );

        if ( Object.keys( headers ).length === 0 && Object.keys( message ).length === 0 ) {
            throw new Error( "If the first argument is not an object containing 3 keys then header and message " +
                "must also be supplied!" );
        }

        this.logger.debug( "EventBus: sending(address: {}, headers: {}): {}", [address, headers, message] );

        for ( let busRouter of this.busRouters ) {

            busRouter.message( address, headers, message );
        }
    }

    addBusRouter( busRouter ) {
        this.busRouters.push( busRouter )
    }

    /**
     * Validates that the operation is valid for this router based on header info.
     *
     * @param headers The headers to check for validity.
     * @returns {boolean} true or false.
     *
     * @private
     */
    static validRoutingHeaders( headers ) {

        if (
            headers[EVENT_ROUTING] != null && (
                headers[EVENT_ROUTING].indexOf( EVENT_ROUTES.CLIENT ) >= 0 ||
                headers[EVENT_ROUTING].indexOf( EVENT_ROUTES.BACKEND ) >= 0 ||
                headers[EVENT_ROUTING].indexOf( EVENT_ROUTES.CLIENT ) >= 0
            )
        ) {
            // OK.
        }
        else {
            throw new Error( "Bad routing headers: " + JSON.stringify( headers ) + " The following header must be available: " + EVENT_ROUTING +
                ": " + EVENT_ROUTES.CLIENT + " |& " + EVENT_ROUTES.BACKEND + " |& " +
                EVENT_ROUTES.CLUSTER + "!" );
        }
    }


}

