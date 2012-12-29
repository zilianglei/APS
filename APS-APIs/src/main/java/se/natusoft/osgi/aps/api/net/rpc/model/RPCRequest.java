/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.9.0
 *     
 *     Description
 *         Provides the APIs for the application platform services.
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-01-30: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.rpc.model;

import se.natusoft.osgi.aps.api.net.rpc.errors.RPCError;

import java.util.Set;

/**
 */
public interface RPCRequest {
    /**
     * Returns true if this request is valid. If this returns false all information except getError() is invalid, and
     * getError() should return a valid RPCError object.
     */
    boolean isValid();

    /**
     * Returns an RPCError object if isValid() == false, null otherwise.
     */
    RPCError getError();

    /**
     * Returns a fully qualified name of service to call. This will be null for protocols where service name is
     * not provided this way. So this cannot be taken for given!
     */
    String getServiceQName();
    
    /**
     * @return The method to call
     */
    String getMethod();

    /**
     * Returns true if there is a call id available in the request.
     * <p/>
     * A call id is something that is received with a request and passed back with the
     * response to the request. Some RPC implementations will require this and some wont.
     */
    boolean hasCallId();

    /**
     * Returns the method call call Id.
     * <p/>
     * A call id is something that is received with a request and passed back with the
     * response to the request. Some RPC implementations will require this and some wont.
     */
    Object getCallId();

    /**
     * Adds a parameter. This is mutually exclusive with addParameter(name, parameter)!
     *
     * @param parameter The parameter to add.
     */
    void addParameter(Object parameter);

    /**
     * Adds a named parameter. This is mutually exclusive with addParameter(parameter)!
     *
     * @param name The name of the parameter.
     * @param parameter The parameter to add.
     */
    void addNamedParameter(String name, Object parameter);

    /**
     * @return The number of parameters available.
     */
    int getNumberOfParameters();

    /**
     * Returns the parameter at the specified index.
     *
     * @param index The index of the parameter to get.
     * @param paramClass The expected class of the parameter.              
     *
     * @return The parameter object.
     */
    <T> T getParameter(int index, Class<T> paramClass);

    /**
     * @return true if there are named parameters available. If false the plain parameter list should be used.
     */
    boolean hasNamedParameters();

    /**
     * @return The available parameter names.
     */
    Set<String> getParameterNames();

    /**
     * @param name The name of the parameter to get.
     * @param paramClass The expected class of the parameter.
     *
     * @return A named parameter.
     */
    <T> T getNamedParameter(String name, Class<T> paramClass);
}