package se.natusoft.osgi.aps.tools.web;

import org.osgi.framework.BundleContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a login handler to use by any admin web registering with the APSAdminWeb to validate
 * that there is a valid login available.
 */
public class APSAdminWebLoginHandler extends APSLoginHandler implements APSLoginHandler.HandlerInfo {
    //
    // Private Members
    //

    /** The current APSSession id. */
    private String sessionId = null;

    //
    // Constructors
    //

    /**
     * Creates a new APSAdminWebLoginHandler.
     *
     * @param context The bundle context.
     */
    public APSAdminWebLoginHandler(BundleContext context) {
        super(context, null);
        setHandlerInfo(this);
    }

    //
    // Methods
    //

    /**
     * Sets the session id from a cookie in the specified request.
     *
     * @param request The request to get the session id cookie from.
     */
    public void setSessionIdFromRequestCookie(HttpServletRequest request) {
        String sessId = CookieTool.getCookie(request.getCookies(), "aps-adminweb-session-id");
        if (sessId != null) {
            this.sessionId = sessId;
        }
    }

    /**
     * Saves the current session id on the specified response.
     *
     * @param response The response to save the session id cookie on.
     */
    public void saveSessionIdOnResponse(HttpServletResponse response) {
        if (this.sessionId != null) {

            CookieTool.setCookie(response, "aps-adminweb-session-id", this.sessionId, 3600 * 24, "/");
        }
    }

    /**
     * @return An id to an APSSessionService session.
     */
    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    /**
     * Sets a new session id.
     *
     * @param sessionId The session id to set.
     */
    @Override
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return The name of the session data containing the logged in user if any.
     */
    @Override
    public String getUserSessionName() {
        return "aps-admin-user";
    }

    /**
     * @return The required role of the user for it to be considered logged in.
     */
    @Override
    public String getRequiredRole() {
        return "apsadmin";
    }
}