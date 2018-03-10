package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.osgi.aps.model.APSHandler
import se.natusoft.osgi.aps.model.APSObject
import se.natusoft.osgi.aps.exceptions.APSValidationException

/**
 * ## Structured Map
 *
 * This refers to a Map containing both single value values and Map and List values. Basically it refers to
 * a JSON structure. Data read from JSON sources will be put into this. So why do we handle JSON as a Map ?
 * Well, because JSON basically is a Map, and because this is Groovy and Groovy supports JSON structures
 * using maps. The difference is that Groovy has '[' where JSON has '{' for objects.
 *
 * I started out calling it a JSONMap, but finally decided agains that.
 *
 * A key is a String contining branches separated by '.' characters for each sub structure, and I've
 * decided to call these paths.
 *
 * Note that since this delegates to the wrapped Map the class is also a Map when compiled!
 *
 * Here is an example from the test that shows how to lookup and how to use the paths:
 *
 *         assert structMap.lookup( "header.type" ).toString() == "service"
 *         assert structMap.lookup( "header.address" ).toString() == "aps.admin.web"
 *         assert structMap.lookup( "header.classifier" ).toString() == "public"
 *         assert structMap.lookup( "body.action" ).toString() == "get-webs"
 *         assert structMap.lookup( "reply.webs.[0].name") == "ConfigAdmin"
 *         assert structMap.lookup( "reply.webs.[1].name") == "RemoteServicesAdmin"
 *         assert structMap.lookup( "reply.webs.[1].url") == "https://localhost:8080/aps/RemoteSvcAdmin"
 *         assert structMap.lookup( "reply.webs.[*]" ) == 2
 *
 * Note that the values are API-wise of type Object! This is because it can be anything, like a String, Map,
 * List, Number (if you stick to JSON formats) or any other type of value you put in there.
 *
 * Also note the indexes in the paths in the example. It is not "webs[0]" but "webs.[0]"! The index is a
 * reference name in itself. The paths returned by getStructPaths() have a number between the '[' and the ']' for
 * List entries. An index of [*] and nothing more after that will return the number of entries in that list
 * as an int.
 */
@SuppressWarnings("SpellCheckingInspection")
@CompileStatic
@TypeChecked
class StructMap implements Map<String, Object> {

    //
    // Properties
    //

    @Delegate
    Map<String, Object> map = [ : ]

    //
    // Constructors
    //

    /**
     * Default constructor.
     */
    @SuppressWarnings("GroovyUnusedDeclaration")
    StructMap() {}

    /**
     * Creates a new Mapo instance from a Map.
     *
     * @param map The map to work with.
     */
    StructMap( Map<String, Object> map ) {
        this.map.putAll( map )
    }

    //
    // Methods
    //

    /**
     * Calls the provided handler for each value key in the map.
     *
     * @param keyHandler The handler to call with value keys.
     */
    void withStructPath( APSHandler<String> keyHandler ) {

        findPaths( this.map, new StructPath(), keyHandler )
    }

    /**
     * Does the actual resolving of all value keys.
     *
     * @param map The map to look in.
     * @param path The current path in the map.
     * @param pathHandler The handler to call with paths.
     */
    private void findPaths( Object searchable, StructPath path, APSHandler<String> pathHandler ) {

        if ( searchable instanceof Map ) {

            Map<String, Object> map = searchable as Map<String, Object>
            map.each { String key, Object value ->

                StructPath subKey = path.down( key )

                if ( value instanceof Map || value instanceof List ) {

                    findPaths( value, subKey, pathHandler )
                }
                else {

                    pathHandler.handle( subKey.toString() )
                }
            }
        }
        else if ( searchable instanceof List ) {

            StructPath subKey = path.down( "[${( searchable as List ).size()}]" )

            if ( !( searchable as List ).isEmpty() ) {

                Object value = ( searchable as List ).first()

                if ( value instanceof Map || value instanceof List ) {

                    findPaths( value, subKey, pathHandler )
                }
                else {

                    pathHandler.handle( subKey.toString() )
                }
            }
        }
    }

    /**
     * Looks up the value of a specified structPath.
     *
     * @param structPath The structPath to lookup.
     * @oaramn result Called with value found at path.
     */
    void lookup( String structPath, APSHandler<Object> result ) {

        result.handle( lookup( structPath ) )
    }

    /**
     * Looks up the value of a specified stuctured path.
     *
     * A path ending in [*] will return the number of entries in the array.
     *
     * @param structPath The structured path to lookup.
     *
     * @return The value found at the path.
     */
    Object lookup( String structPath ) {
        Object current = this.map

        for ( String part : structPath.split( "\\." ) ) {

            if ( Map.class.isAssignableFrom( current.class ) ) {

                current = ( current as Map ).get( part )
            }
            else if ( List.class.isAssignableFrom( current.class ) ) {

                if ( !part.startsWith( "[" ) ) {
                    throw new APSValidationException( "Expected a list index here, got: '${part}'" )
                }

                String ixStr = part.replace( "[", "" ).replace( "]", "" )

                // Index '*' means number of entries in the list. In this case anything below this is
                // ignored, so we return with the size immediately.
                if ( ixStr == "*" ) {
                    return ( current as List ).size()
                }
                else {
                    int index = Integer.valueOf( ixStr )
                    current = ( current as List ).get( index )
                }
            }
        }

        current
    }

    /**
     * Calls lookup but wraps the result in an APSObject.
     *
     * @param structPath The path to lookup.
     *
     * @return An APSObject wrapped value.
     */
    APSObject lookupObject( String structPath ) {
        return new APSObject( lookup( structPath ) )
    }

    /**
     * provides a value.
     *
     * @param structPath The value path.
     * @param value The value.
     */
    void provide( String structPath, Object value ) {
        if ( value instanceof APSObject ) {
            value = value.content()
        }

        // Yeah, I know. This is not a wonder of clarity!!

        Object current = this.map

        String[] parts = structPath.split( "\\." )

        if ( parts.length > 1 ) {
            //Object oldCurrent = null

            String last = parts[ parts.length - 1 ]

            for ( int i = 0; i < parts.length; i++ ) {
                String part = parts[ i ]

                Object oldCurrent = current

                if ( i == ( parts.length - 1 ) ) {

                    if ( Map.class.isAssignableFrom( current.class ) ) {

                        ( current as Map<String, Object> ).put( last, value )
                    }
                    else { // Must be list

                        int index = Integer.valueOf( part.replace( "[", "" ).replace( "]", "" ) )
                        while ( index > ( ( current as List<Object> ).size() - 1 ) ) {
                            ( current as List<Object> ).add( [ : ] )
                        }
                        ( current as List<Object> ).set( index, value )
                    }
                }
                else if ( Map.class.isAssignableFrom( current.class ) ) {

                    current = ( current as Map ).get( part )
                }
                else if ( List.class.isAssignableFrom( current.class ) ) {

                    if ( !part.startsWith( "[" ) ) {
                        throw new APSValidationException( "Expected a list index here, got: '${part}'" )
                    }

                    int index = Integer.valueOf( part.replace( "[", "" ).replace( "]", "" ) )
                    while ( ( current as List ).size() < ( index + 1 ) ) {
                        ( current as List ).add( [ : ] )
                    }
                    current = ( current as List ).get( index )
                }

                // We need to create a new object.
                if ( current == null ) {
                    Object newValue = ""

                    if ( i < ( parts.length - 1 ) ) {
                        // IDEA seem to have gotten a permanent memory of a no longer existing situation here!
                        //noinspection GroovyIfStatementWithIdenticalBranches
                        if ( parts[ i + 1 ].startsWith( "[" ) ) {
                            newValue = [ ]
                        }
                        else {
                            newValue = [ : ]
                        }
                    }

                    // The first should always be a Map!
                    if ( oldCurrent == null || Map.isAssignableFrom( oldCurrent.class ) ) {
                        ( oldCurrent as Map ).put( part, newValue )
                    }
                    else {
                        ( oldCurrent as List<Object> ).add( newValue )
                    }

                    current = newValue
                }

            }
        }
        else {
            this.map.put( structPath, value )
        }
    }
}
