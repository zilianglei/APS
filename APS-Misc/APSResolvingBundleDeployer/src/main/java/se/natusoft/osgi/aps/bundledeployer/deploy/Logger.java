package se.natusoft.osgi.aps.bundledeployer.deploy;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * A simple logger.
 */
public class Logger {
    //
    // Private Members
    //

    /** The context of our bundle. */
    private BundleContext context = null;

    /** A reference to the log service. */
    private ServiceReference logServiceRef = null;

    //
    // Constructors
    //

    /**
     * Creates a new Logger.
     *
     * @param bundleContext The context of our bundle.
     */
    public Logger(BundleContext bundleContext) {
        this.context = bundleContext;
    }

    //
    // Methods
    //

    /**
     * @return A LogService instance if such can be found.
     */
    private LogService getLogService() {
        if (this.logServiceRef == null) {
            this.logServiceRef = this.context.getServiceReference(LogService.class.getName());
        }
        if (this.logServiceRef == null) {
            return null;
        }

        return (LogService)this.context.getService(this.logServiceRef);
    }

    /**
     * Releases a successfully gotten LogService.
     */
    private void releaseLogService() {
        if (this.logServiceRef != null) {
            this.context.ungetService(this.logServiceRef);
        }
    }

    /**
     * Logs info message.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogService.LOG_INFO, message);
            releaseLogService();
        }
        else {
            System.out.println("INFO: " + message);
        }
    }

    /**
     * Logs info message.
     *
     * @param message The message to log.
     * @param t A throwable that is the cause of the log.
     */
    public void info(String message, Throwable t) {
        LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogService.LOG_INFO, message, t);
            releaseLogService();
        }
        else {
            System.out.println("INFO: " + message);
            t.printStackTrace();
        }
    }

    /**
     * Logs error message.
     *
     * @param message The message to log.
     */
    public void error(String message) {
        LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogService.LOG_ERROR, message);
            releaseLogService();
        }
        else {
            System.out.println("ERROR: " + message);
        }
    }

    /**
     * Logs error message.
     *
     * @param message The message to log.
     * @param t The throwable that is the cause of the log.
     */
    public void error(String message, Throwable t) {
        LogService logService = getLogService();
        if (logService != null) {
            logService.log(LogService.LOG_ERROR, message, t);
            releaseLogService();
        }
        else {
            System.out.println("ERROR: " + message);
            t.printStackTrace();
        }
    }
}