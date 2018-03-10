package se.natusoft.osgi.aps.core.config

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.osgi.framework.Bundle
import org.osgi.framework.BundleEvent
import se.natusoft.osgi.aps.json.JSONErrorHandler
import se.natusoft.osgi.aps.tools.APSLogger
import se.natusoft.osgi.aps.tools.annotation.activator.BundleListener
import se.natusoft.osgi.aps.tools.annotation.activator.Managed

/**
 * This listens to bundles and manages configurations.
 *
 * This  is how it works:
 * - Bundles MANIFEST.MF is checked for 'APS-Config-Id:', 'APS-Config-Schema:' and 'APS-Config-Default-Resource:'.
 *
 * - 'APS-Config-Id' is a key used to identify the config.
 *
 * - The 'APS-Config-Default-Resource' is used the first time a config is seen to provide default values.
 *   It is also checked for default values when managed config does not provide a value. This will happen
 *   when a new value have been added to a config at a later time.
 *
 * - An APSConfig implementation will be published as an OSGi service for each configuration, also with
 *   'APS-Config-Id=<id>' in the properties for the published service. This is used by config owner to
 *   lookup the correct APSConfig instance to use.
 */
// Nothing other than APSActivator will be referencing this, and it does it via reflection. Thereby the IDE
// cannot tell that this is actually used.
@SuppressWarnings("GroovyUnusedDeclaration")
@CompileStatic
@TypeChecked
class BundlesConfigHandler {

    //
    // Private Members
    //

    @Managed(loggingFor = "aps-config-provider:bundle-config-handler")
    APSLogger logger

    @Managed
    private ConfigManager configManager

    private jsonErrorHandler = new JSONErrorHandler() {

        @Override
        void warning( String message ) { logger.warn( message ) }

        @Override
        void fail( String message, Throwable cause ) throws RuntimeException { logger.error( message, cause ) }
    }

    //
    // Methods
    //

    /**
     * This receives events from other bundles and determines if there are any new configurations to manage.
     *
     * @param event A received bundle event.
     */
    @BundleListener
    void handleEvent( BundleEvent event ) {
        if ( event.type == BundleEvent.STARTED ) {
            handleNewBundle( event.bundle )
        }
        else if ( event.type == BundleEvent.STOPPED ) {
            handleLeavingBundle( event.bundle )
        }
    }

    /**
     * Manages config for new bundle.
     *
     * @param bundle The new bundle to manage config for.
     */
    private void handleNewBundle( Bundle bundle ) {
        String configId = bundle.getHeaders().get( "APS-Config-Id" )
        if ( configId != null ) {
            this.logger.info( "Found bundle with configuration id: " + configId )

            String schemaResourcePath = bundle.headers.get( "APS-Config-Schema" )

            String defaultResourcePath = bundle.headers.get( "APS-Config-Default-Resource" )

            if ( schemaResourcePath != null ) {
                try {
                    // IDEA thinks that:
                    //   configId is an Object, but it is a String.
                    //   schemaResourcePath is an Object, but it is a String.
                    //   defaultResourcePath is an Object, but it is a String.
                    // Thereby the crazy casts.
                    this.configManager.addManagedConfig( (String)configId, bundle, (String)schemaResourcePath, (String)defaultResourcePath )
                }
                catch ( Exception e ) {
                    this.logger.error( "Failed to load config from: ${schemaResourcePath} / ${defaultResourcePath} for bundle '${bundle.symbolicName}'!", e )
                }
            }
            else {
                this.logger.error( "Bad bundle ('${bundle.symbolicName}')! Configuration with id '${configId}' is available, but no APS-Config-Schema found!" )
            }
        }

    }

    /**
     * Handles a bundle leaving.
     *
     * @param bundle The leaving bundle.
     */
    private void handleLeavingBundle( Bundle bundle ) {
        String configId = bundle.getHeaders().get( "APS-Config-Id" )
        this.configManager.removeManagedConfig( configId )
    }
}
