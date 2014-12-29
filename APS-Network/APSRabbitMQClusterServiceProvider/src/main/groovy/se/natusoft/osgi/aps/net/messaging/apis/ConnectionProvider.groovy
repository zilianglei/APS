package se.natusoft.osgi.aps.net.messaging.apis

import com.rabbitmq.client.Connection
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * This is received and should provide an implementation that provides a RabbitMQ Connection.
 */
@CompileStatic
@TypeChecked
public interface ConnectionProvider {

    /**
     * Returns a RabbitMQ connection.
     *
     * @throws IOException on failure to do so.
     */
    Connection getConnection() throws IOException;
}
