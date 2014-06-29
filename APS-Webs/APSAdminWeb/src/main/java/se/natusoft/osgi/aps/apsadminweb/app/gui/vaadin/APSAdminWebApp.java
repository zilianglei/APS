/* 
 * 
 * PROJECT
 *     Name
 *         APS Administration Web
 *     
 *     Code Version
 *         0.11.0
 *     
 *     Description
 *         This is a web application providing and administration web frame.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2011-08-27: Created!
 *         
 */
package se.natusoft.osgi.aps.apsadminweb.app.gui.vaadin;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;
import se.natusoft.osgi.aps.tools.annotation.activator.Managed;
import se.natusoft.osgi.aps.tools.web.APSAdminWebLoginHandler;
import se.natusoft.osgi.aps.tools.web.WebClientContext;
import se.natusoft.osgi.aps.tools.web.vaadin.APSVaadinOSGiApplication;
import se.natusoft.osgi.aps.tools.web.vaadin.components.SidesAndCenterLayout;

import javax.servlet.annotation.WebServlet;

/**
 * This is the main administration app for APS. It uses APSAdminWebService to get registered admin web apps and
 * makes a tab for each found. In other words, this is just a front for different admin web apps. All an admin
 * web app needs to do to be available here is to register itself with APSAdminWebService (which includes the
 * url to the app). Each tabs content is a Vaadin Embedded component running the registered app.
 */
@Title("Application Platform Services Administration App")
@Theme("aps")
@PreserveOnRefresh
public class APSAdminWebApp extends APSVaadinOSGiApplication implements ClickListener {

    @WebServlet(value = "/apsadminweb/*",
            asyncSupported = true)
    @VaadinServletConfiguration(
            productionMode = false,
            ui = APSAdminWebApp.class)
    public static class Servlet extends VaadinServlet {}

    //
    // Private Members
    //

    /** The application tabs. */
    // The TabPanel uses @OSGiservice annotation to inject an APSServiceTracker wrapped service instance.
    // The injection is handled by APSActivator which is managed by the APSVaadinOSGiApplication base class.
    // For the injection of the service into TabPanel to work the TabPanel needs to be instantiated and
    // managed by the APSActivator. Doing new TabPanel here instead will cause a NullPointerException since
    // then the service used by the TabPanel will never get injected.
    @SuppressWarnings("UnusedDeclaration")
    @Managed
    private TabPanel tabPanel;

    /** The main window layout. */
    @SuppressWarnings("FieldCanBeLocal")
    private VerticalLayout mainLayout = null;

    /** A login handler. */
    private APSAdminWebLoginHandler loginHandler = null;


    //
    // Vaadin GUI init
    //

    /**
     * Initializes services used by the application.
     *
     * @param clientContext The client context for accessing services.
     */
    @Override
    public void initServices(WebClientContext clientContext) {

    }

    /**
     * Called when session is about to die to cleanup anything setup in initServices().
     *
     * @param clientContext The context for the current client.
     */
    @Override
    public void cleanupServices(WebClientContext clientContext) {
        if (this.loginHandler != null) {
            this.loginHandler.shutdown();
        }
    }

    /**
     * Creates the application GUI.
     */
    @Override
    public void initGUI() {

        this.mainLayout = new VerticalLayout();
        this.mainLayout.setSizeFull();

        SidesAndCenterLayout layout = new SidesAndCenterLayout();
        layout.setTop(new LogoPanel(this));
        this.tabPanel.refreshTabs();
        layout.setCenter(this.tabPanel);

        layout.doLayout();
        this.mainLayout.addComponent(layout);

        setContent(this.mainLayout);

    }

    /**
     * Handle logins.
     *
     * @param clientContext The clients context.
     */
    protected void handleLogin(WebClientContext clientContext) {
        useDefaultLoginHandler();
    }


    //
    // Event Handler Methods
    //
    
    /**
     * Called when refresh button is clicked.
     * 
     * @param event The click event.
     */
    @Override
    public void buttonClick(ClickEvent event) {
        getUserNotifier().info("", "Refreshing ...");
        this.tabPanel.refreshTabs();
    }

}
