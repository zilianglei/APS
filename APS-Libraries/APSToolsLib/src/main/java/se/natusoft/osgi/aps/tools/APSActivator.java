/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides a library of utilities, among them APSServiceTracker used by all other APS bundles.
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
 *         2013-08-02: Created!
 *         
 */
package se.natusoft.osgi.aps.tools;

import org.osgi.framework.*;
import se.natusoft.osgi.aps.tools.annotation.*;
import se.natusoft.osgi.aps.tools.exceptions.APSActivatorException;
import se.natusoft.osgi.aps.tools.tracker.OnServiceAvailable;
import se.natusoft.osgi.aps.tools.tracker.OnTimeout;
import se.natusoft.osgi.aps.tools.tuples.Tuple2;
import se.natusoft.osgi.aps.tools.tuples.Tuple4;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This class can be specified as bundle activator in which case you use the following annotations:
 *
 * * **@APSOSGiServiceProvider** -
 *   This should be specified on a class that implements a service interface and should be registered as
 *   an OSGi service. _Please note_ that the first declared implemented interface is used as service interface!
 *   If any fields of this service class is annotated with @APSOSGiService(required=true) then the registration
 *   of the service will be delayed until the required service becomes available.
 *
 * * **@APSOSGiService** -
 *   This should be specified on a field having a type of a service interface to have a service of that type
 *   injected, and continuously tracked. Any call to the service will throw an APSNoServiceAvailableException
 *   (runtime) if no service has become available before the specified timeout. It is also possible to have
 *   APSServiceTracker as field type in which case the underlying configured tracker will be injected instead.
 *
 * * **@APSInject** -
 *   This will have an instance injected. There will be a unique instance for each name specified with the
 *   default name of "default" being used in none is specified. There are 2 field types handled specially:
 *   BundleContext and APSLogger. A BundleContext field will get the bundles context injected. For an APSLogger
 *   instance the 'loggingFor' annotation property can be specified.
 *
 * * **@APSBundleStart** -
 *   This should be used on a method and will be called on bundle start. The method should take no arguments.
 *   If you need a BundleContext just declare a field of BundleContext type and it will be injected. The use
 *   of this annotation is only needed for things not supported by this activator. Please note that a method
 *   annotated with this annotation can be static!
 *
 * * **@APSBundleStop** -
 *   This should be used on a method and will be called on bundle stop. The method should take no arguments.
 *   This should probably be used if @APSBundleStart is used. Please note that a method annotated with this
 *   annotation can be static!
 *
 * All injected service instances for @APSOSGiService will be APSServiceTracker wrapped
 * service instances that will automatically handle services leaving and coming. They will throw
 * APSNoServiceAvailableException on timeout!
 *
 * Most methods are protected making it easy to subclass this class and expand on its functionality.
 */
public class APSActivator implements BundleActivator, OnServiceAvailable, OnTimeout {

    //
    // Private Members
    //

    private APSLogger activatorLogger;

    private List<ServiceRegistration> services;
    private Map<String, APSServiceTracker> trackers;
    private Map<String, Object> namedInstances;
    private List<Tuple2<Method, Object>> shutdownMethods;
    private List<Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>>> requiredServices;
    private Map<Class, Object> managedInstances;

    private BundleContext context;

    //
    // Methods
    //

    /**
     * Called when this bundle is started so the Framework can perform the
     * bundle-specific activities necessary to start this bundle. This method
     * can be used to register services or to allocate any resources that this
     * bundle needs.
     *
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being started.
     * @throws Exception If this method throws an exception, this
     *                   bundle is marked as stopped and the Framework will remove this
     *                   bundle's listeners, unregister all services registered by this
     *                   bundle, and release all services used by this bundle.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        this.services = new LinkedList<>();
        this.trackers = new HashMap<>();
        this.namedInstances = new HashMap<>();
        this.shutdownMethods = new LinkedList<>();
        this.managedInstances = new HashMap<>();
        this.requiredServices = new LinkedList<>();

        this.activatorLogger = new APSLogger(System.out);
        this.activatorLogger.setLoggingFor("APSActivator");
        this.activatorLogger.start(context);

        Bundle bundle = context.getBundle();

        List<String> classEntryPaths = new LinkedList<>();
        getClassEntries(bundle, classEntryPaths, "/");

        for (String entryPath : classEntryPaths) {
            try {
                Class entryClass =
                        bundle.loadClass(
                                entryPath.substring(0, entryPath.length() - 6).replace(File.separatorChar, '.')
                        );
                handleFieldInjections(entryClass, context);
                handleServiceRegistrations(entryClass, context);
                handleMethods(entryClass, context);
            }
            catch (ClassNotFoundException cnfe) {
                this.activatorLogger.error("Failed to get class for bundle class entry!", cnfe);
            }
        }
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the `BundleActivator.start()`
     * method started. There should be no active threads that were started by
     * this bundle when this bundle returns. A stopped bundle must not call any
     * Framework objects.
     *
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context The execution context of the bundle being stopped.
     * @throws Exception If this method throws an exception, the
     *                   bundle is still marked as stopped, and the Framework will remove
     *                   the bundle's listeners, unregister all services registered by the
     *                   bundle, and release all services used by the bundle.
     */
    @Override
    public void stop(BundleContext context) throws Exception {

        Exception failure = null;

        for (Tuple2<Method, Object> shutdownMethod : this.shutdownMethods) {
            try {
                shutdownMethod.t1.invoke(shutdownMethod.t2, null);

                this.activatorLogger.info("Called bundle shutdown method '" + shutdownMethod.t2.getClass() +
                        "." + shutdownMethod.t1.getName() + "() for bundle: " +
                        context.getBundle().getSymbolicName() + "!");
            }
            catch (Exception e) {
                this.activatorLogger.error("Bundle stop problem!", e);
                failure = e;
            }
        }

        for (ServiceRegistration serviceRegistration : this.services) {
            try {
                serviceRegistration.unregister();
            }
            catch (Exception e) {
                this.activatorLogger.error("Bundle stop problem!", e);
                failure = e;
            }
        }

        for (String trackerKey : this.trackers.keySet()) {
            APSServiceTracker tracker = this.trackers.get(trackerKey);
            try {
                tracker.stop(context);
            }
            catch (Exception e) {
                this.activatorLogger.error("Bundle stop problem!", e);
                failure = e;
            }
        }

        for (String namedInstanceKey : this.namedInstances.keySet()) {
            Object namedInstance = this.namedInstances.get(namedInstanceKey);
            if (namedInstance instanceof APSLogger) {
                try {
                    ((APSLogger)namedInstance).stop(context);
                }
                catch (Exception e) {
                    this.activatorLogger.error("Bundle stop problem!", e);
                    failure = e;
                }
            }
        }

        if (failure != null) {
            throw new APSActivatorException("Bundle stop not entirely successful!", failure);
        }
    }

    // ---- Service Registration ---- //

    /**
     * Converts from annotation properties to a java.util.Properties instance.
     *
     * @param osgiProperties The annotation properties to convert.
     */
    protected Properties osgiPropertiesToProperties(OSGiProperty[] osgiProperties) {
        Properties props = new Properties();
        for (OSGiProperty prop : osgiProperties) {
            props.setProperty(prop.name(), prop.value());
        }
        return props;
    }

    /**
     * Handles publishing of bundle services. If a published service has any dependencies to
     * other services that are marked as required then the publishing is delayed until all required
     * services are available. In this case the service will be unpublished if any of the required
     * services times out.
     *
     * @param managedClass The managed service class to instantiate and register as a service.
     * @param context The bundles context.
     * @throws Exception
     */
    protected void handleServiceRegistrations(Class managedClass, BundleContext context) throws Exception {
        if (this.requiredServices.isEmpty()) {
            registerServices(managedClass, context, this.services);
        }
        else {
            for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                requiredService.t1.onActiveServiceAvailable(this);
                requiredService.t1.setOnTimeout(this);
            }
        }
    }

    /**
     * This is used to start delayed service publishing. Each tracker of a required service will be calling
     * this method on first service becoming available. When all required services are available the delayed
     * service will be published.
     *
     * @param service The received service.
     * @param serviceReference The reference to the received service.
     *
     * @throws Exception
     */
    @Override
    public void onServiceAvailable(Object service, ServiceReference serviceReference) throws Exception {
        List<Class> uniqueClasses = new LinkedList<>();
        for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
            if (!uniqueClasses.contains(requiredService.t2)) {
                uniqueClasses.add(requiredService.t2);
            }
        }

        for (Class managedClass : uniqueClasses) {
            boolean allRequiredAvailable = true;
            for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                if (requiredService.t2.equals(managedClass) && !requiredService.t1.hasTrackedService()) {
                    allRequiredAvailable = false;
                    break;
                }
            }

            if (allRequiredAvailable) {
                for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                    if (requiredService.t2.equals(managedClass) && requiredService.t3 == false) {
                        registerServices(requiredService.t2, this.context, requiredService.t4);
                        this.services.addAll(requiredService.t4);
                        requiredService.t3 = true;
                    }
                }
            }
        }
    }

    /**
     * This gets called for required services when the tracker have timed out waiting for a service to become
     * available and is about to throw an APSNoServiceAvailableException. This will unpublish all published
     * services that have a requirement on the timed out service. The service will be republished later when
     * it becomes available again by onServiceAvailable() above.
     *
     * @throws RuntimeException
     */
    @Override
    public void onTimeout() throws RuntimeException {
        List<Class> uniqueClasses = new LinkedList<>();
        for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
            if (!uniqueClasses.contains(requiredService.t2)) {
                uniqueClasses.add(requiredService.t2);
            }
        }

        for (Class managedClass : uniqueClasses) {
            boolean allRequiredAvailable = true;
            for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                if (requiredService.t2.equals(managedClass) && !requiredService.t1.hasTrackedService()) {
                    allRequiredAvailable = false;
                    break;
                }
            }

            if (!allRequiredAvailable) {
                for (Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService : this.requiredServices) {
                    if (requiredService.t2.equals(managedClass)) {
                        for (ServiceRegistration serviceRegistration : requiredService.t4) {
                            try {
                                serviceRegistration.unregister();
                                requiredService.t3 = false;
                                this.services.remove(serviceRegistration);
                            }
                            catch (Exception e) {
                                this.activatorLogger.error("Bundle stop problem!", e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Registers/publishes services annotated with @APSOSGiServiceProvider.
     *
     * @param managedClass The managed class to instantiate and register as an OSGi service.
     * @param context The bundle context.
     * @param serviceRegs The list to save all service registrations to for later unregistration.
     * @throws Exception
     */
    protected void registerServices(Class managedClass, BundleContext context, List<ServiceRegistration> serviceRegs) throws Exception {
        APSOSGiServiceProvider serviceProvider = (APSOSGiServiceProvider)managedClass.getAnnotation(APSOSGiServiceProvider.class);
        if (serviceProvider != null) {
            List<Properties> instanceProps = null;

            if (serviceProvider.properties().length > 0) {
                instanceProps = new LinkedList<>();
                instanceProps.add(osgiPropertiesToProperties(serviceProvider.properties()));
            }
            else if (serviceProvider.instances().length > 0) {
                instanceProps = new LinkedList<>();
                for (APSOSGiServiceInstance serviceInst : serviceProvider.instances()) {
                    instanceProps.add(osgiPropertiesToProperties(serviceInst.properties()));
                }
            }
            else if (!serviceProvider.instanceFactoryClass().equals(InstanceFactory.class)) {
                InstanceFactory instanceFactory = serviceProvider.instanceFactoryClass().newInstance();
                instanceProps = instanceFactory.getPropertiesPerInstance();
            }
            else {
                instanceProps = new LinkedList<>();
                instanceProps.add(new Properties());
            }

            for (Properties serviceProps : instanceProps) {
                serviceProps.put(Constants.SERVICE_PID, managedClass.getName());
                Class[] interfaces = managedClass.getInterfaces();
                if (interfaces != null && interfaces.length >= 1) {
                    ServiceRegistration serviceReg =
                            context.registerService(
                                    interfaces[0].getName(),
                                    getManagedInstance(managedClass),
                                    serviceProps
                            );

                    serviceRegs.add(serviceReg);
                    this.activatorLogger.info("Registered '" + managedClass.getName() + "' as a service provider of '" +
                            interfaces[0].getName() + "' for bundle: " + context.getBundle().getSymbolicName() + "!");
                }
                else {
                    throw new IllegalArgumentException("The @APSOSGiServiceProvider annotated service of class '" +
                        managedClass.getName() + "' does not implement a service interface!");
                }
            }
        }
    }

    // ---- Field Injections ---- //

    /**
     * This handles all field injections by delegating to handlers of specific types of field injections.
     *
     * @param managedClass The managed class to inject into.
     * @param context The bundles context.
     */
    protected void handleFieldInjections(Class managedClass, BundleContext context) {
        for (Field field : managedClass.getDeclaredFields()) {
            handleServiceInjections(field, managedClass, context);
            handleInstanceInjections(field, managedClass, context);
        }
    }

    /**
     * Tracks and injects APSServiceTracker directly or as wrapped service instance using the tracker to
     * call the service depending on the field type.
     *
     * @param field The field to inject.
     * @param managedClass Used to lookup or create an instance of this class to inject into.
     * @param context The bundle context.
     */
    protected void handleServiceInjections(Field field, Class managedClass, BundleContext context) {
        APSOSGiService service = field.getAnnotation(APSOSGiService.class);
        if (service != null) {
            String trackerKey = field.getType().getName() + service.additionalSearchCriteria();
            APSServiceTracker tracker = this.trackers.get(trackerKey);

            if (tracker == null) {
                tracker = new APSServiceTracker<>(context, field.getType(), service.additionalSearchCriteria(),
                        service.timeout());
                this.trackers.put(trackerKey, tracker);
            }
            tracker.start();
            Object managedInstance = getManagedInstance(managedClass);
            if (field.getType().equals(APSServiceTracker.class)) {
                injectObject(managedInstance, tracker, field);
            }
            else {
                injectObject(managedInstance, tracker.getWrappedService(), field);
            }

            if (service.required()) {
                Tuple4<APSServiceTracker, Class, Boolean, List<ServiceRegistration>> requiredService =
                        new Tuple4<>(tracker, managedClass, false, (List<ServiceRegistration>)new LinkedList<ServiceRegistration>());
                this.requiredServices.add(requiredService);
            }

            this.activatorLogger.info("Injected tracked service '" + field.getType().getName() +
                    (service.additionalSearchCriteria().length() > 0 ? " " + service.additionalSearchCriteria() : "") +
                    "' " + "into '" + managedClass.getName() + "." + field.getName() + "' for bundle: " +
                    context.getBundle().getSymbolicName() + "!");
        }
    }

    /**
     * Handles injections of APSLogger, BundleContext, or other class types with default constructor.
     *
     * @param field The field to inject into.
     * @param managedClass Used to lookup or create an instance of this class to inject into.
     * @param context The bundle context.
     */
    protected void handleInstanceInjections(Field field, Class managedClass, BundleContext context) {
        APSInject inject = field.getAnnotation(APSInject.class);
        if (inject != null) {
            String namedInstanceKey = inject.name() + field.getType().getName();
            Object namedInstance = this.namedInstances.get(namedInstanceKey);
            if (namedInstance == null) {
                if (field.getType().equals(APSLogger.class)) {
                    namedInstance = new APSLogger(System.out);
                    if (inject.loggingFor().length() > 0) {
                        ((APSLogger)namedInstance).setLoggingFor(inject.loggingFor());
                    }
                    ((APSLogger)namedInstance).start(context);
                }
                else if (field.getType().equals(BundleContext.class)) {
                    namedInstance = context;
                }
                else {
                    try {
                        namedInstance = field.getType().newInstance();
                    }
                    catch (InstantiationException | IllegalAccessException e) {
                        throw new APSActivatorException("Failed to instantiate: " + managedClass.getName() +
                                "." + field.getName() + "!", e);
                    }
                }
                this.namedInstances.put(namedInstanceKey, namedInstance);
            }
            Object managedInstance = getManagedInstance(managedClass);
            injectObject(managedInstance, namedInstance, field);

            this.activatorLogger.info("Injected '" + namedInstance.getClass().getName() +
                    "' instance for name '" + inject.name() + "' " +
                    "into '" + managedClass.getName() + "." + field.getName() + "' for bundle: " +
                    context.getBundle().getSymbolicName() + "!");
        }
    }

    // ---- Methods ---- //

    /**
     * Handles annotated methods.
     *
     * @param managedClass Used to lookup or create an instance of this class containing the method to call.
     * @param context The bundle context.
     */
    protected void handleMethods(Class managedClass, BundleContext context) {
        for (Method method : managedClass.getDeclaredMethods()) {
            handleStartupMethods(method, managedClass, context);
            handleShutdownMethods(method, managedClass, context);
        }
    }

    /**
     * Handles methods annotated with @APSBundleStartup.
     *
     * @param method The annotated method to call.
     * @param managedClass Used to lookup or create an instance of this class containing the method to call.
     * @param context The bundle context.
     */
    protected void handleStartupMethods(Method method, Class managedClass, BundleContext context) {
        APSBundleStart bundleStart = method.getAnnotation(APSBundleStart.class);
        if (bundleStart != null) {
            if (method.getParameterTypes().length > 0) {
                throw new APSActivatorException("An @APSBundleStart method must take no parameters! [" +
                    managedClass.getName() + "." + method.getName() + "(?)]");
            }
            Object managedInstance = null;
            if (!Modifier.isStatic(method.getModifiers())) {
                managedInstance = getManagedInstance(managedClass);
            }
            try {
                method.invoke(managedInstance, null);

                this.activatorLogger.info("Called bundle start method '" + managedClass.getName() +
                        "." + method.getName() + "()' for bundle: " + context.getBundle().getSymbolicName() + "!");
            } catch (IllegalAccessException e) {
                throw new APSActivatorException("Failed to call start method! [" +
                           managedClass.getName() + "." + method.getName() + "()]", e);
            }
            catch (InvocationTargetException ite) {
                throw new APSActivatorException("Called start method failed!", ite.getCause());
            }
        }
    }

    /**
     * Handles methods annotated with @APSBundleStop.
     *
     * @param method The annotated method to call.
     * @param managedClass Used to lookup or create an instance of this class containing the method to call.
     * @param context The bundle context.
     */
    protected void handleShutdownMethods(Method method, Class managedClass, BundleContext context) {
        APSBundleStop bundleStop = method.getAnnotation(APSBundleStop.class);
        if (bundleStop != null) {
            Tuple2<Method, Object> shutdownMethod = new Tuple2<>(method, null);
            if (!Modifier.isStatic(method.getModifiers())) {
                shutdownMethod.t2 = getManagedInstance(managedClass);
            }

            this.shutdownMethods.add(shutdownMethod);
        }
    }

    // ---- Support ---- //

    /**
     * Returns a managed instance of a class.
     *
     * @param managedClass The managed class to get instance for.
     */
    protected Object getManagedInstance(Class managedClass) {
        Object managedInstance = this.managedInstances.get(managedClass);
        if (managedInstance == null) {
            try {
                managedInstance = managedClass.newInstance();
                this.managedInstances.put(managedClass, managedInstance);
                this.activatorLogger.info("Instantiated '" + managedClass.getName() + "': "+ managedInstance);
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new APSActivatorException("Failed to instantiate activator managed class!", e);
            }
        }

        return managedInstance;
    }

    /**
     * Support method to do injections.
     *
     * @param injectTo The instance to inject into.
     * @param toInject The instance to inject.
     * @param field The field to inject to.
     */
    protected void injectObject(Object injectTo, Object toInject, Field field) {
        try {
            field.setAccessible(true);
            field.set(injectTo, toInject);
        }
        catch (IllegalAccessException iae) {
            throw new APSActivatorException("Failed to inject managed field [" + field + "] into [" +
                    injectTo.getClass() + "]", iae);
        }
    }

    /**
     * Populates the entries list with recursively found class entries.
     *
     * @param bundle The bundle to get class entries for.
     * @param entries The list to add the class entries to.
     * @param startPath The start path to look for entries.
     */
    protected void getClassEntries(Bundle bundle, List<String> entries, String startPath) {
        Enumeration<String> entryPathEnumeration = bundle.getEntryPaths(startPath);
        while (entryPathEnumeration.hasMoreElements()) {
            String entryPath = entryPathEnumeration.nextElement();
            if (entryPath.endsWith("/")) {
                getClassEntries(bundle, entries, entryPath);
            }
            else if (entryPath.endsWith(".class")) {
                entries.add(entryPath);
            }
        }
    }

    //
    // Inner Classes
    //

    /**
     * Implementations of this provide properties for each instance of a service to publish.
     */
    public interface InstanceFactory {
        /**
         * Returns a set of Properties for each instance.
         */
        List<Properties> getPropertiesPerInstance();
    }
}
