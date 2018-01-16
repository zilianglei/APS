package se.natusoft.osgi.aps.core.lib

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import se.natusoft.docutations.Immutable

/**
 * This handles a structures map key that can be navigated down and upp.
 *
 * The key format is: key.sub-key.sub-key. Each key that has a sub-key returns a Map and the sub-key
 * references a key in the returned map.
 */
@CompileStatic
@TypeChecked
@Immutable
class StructPath {

    //
    // Private Members
    //

    private String path = ""

    //
    // Constructors
    //

    /**
     * Default constructor. Starts empty.
     */
    StructPath() {}

    /**
     * Creates a new StructuredMapKey instance.
     *
     * @param startPath The starting path.
     */
    StructPath( String startPath ) {

        this.path = startPath
    }

    /**
     * Copy constructor.
     *
     * @param mapPath The MapPath to copy.
     */
    StructPath( StructPath mapPath ) {
        this.path = mapPath.path
    }

    //
    // Methods
    //

    /**
     * Moves down the Map structure by providing a subkey that should return a Map.
     *
     * @param subPath The key of the sub map to enter.
     *
     * @return A new StructuredKey representing the new path.
     */
    StructPath down( String subPath ) {

        new StructPath( this.path.empty ? subPath as String : "${this.path}.${subPath}" as String )
    }

    /**
     * @return a new StructuredKey that represents the parent node.
     */
    StructPath up() {
        int lastDot = this.path.lastIndexOf( '.' )

        if ( lastDot == -1 ) {
            throw new IllegalStateException( "Already at root! Can't go further up." )
        }

        new StructPath( this.path.substring( 0, lastDot ) )
    }

    /**
     * @return The sub path key at the far right.
     */
    String getRight() {

        int lastDot = this.path.lastIndexOf( '.' )

        if ( lastDot == -1 ) {
            return this.path
        }

        return this.path.substring( lastDot + 1 )
    }

    /**
     * @return true if the current right is an array.
     */
    boolean isRightArray() {
        return this.right.startsWith("[")
    }

    /**
     * @return The size of the array if the right entry is an array. Otherwise -1 is returned.
     */
    int rightArraySize() {
        int size = -1
        if (isRightArray()) {
            size = Integer.valueOf(getRight().replace("[", "").replace("]", ""))
        }
        return size
    }

    /**
     * @return true if at root, false otherwise.
     */
    boolean isAtRoot() {
        return this.path.size() == 1
    }

    /**
     * @return A '.' separated full key as a String.
     */
    String toString() {

        return this.path
    }

    /**
     * @return the path as its parts.
     */
    List<String> toParts() {

        List<String> list = new LinkedList()
        list.addAll(this.path.split( "\\." ))

        list
    }

}
