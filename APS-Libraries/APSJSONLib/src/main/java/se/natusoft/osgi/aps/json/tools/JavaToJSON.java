/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Library
 *     
 *     Code Version
 *         0.9.1
 *     
 *     Description
 *         Provides a JSON parser and creator. Please note that this bundle has no dependencies to any
 *         other APS bundle! It can be used as is without APS in any Java application and OSGi container.
 *         The reason for this is that I do use it elsewhere and don't want to keep 2 different copies of
 *         the code. OSGi wise this is a library. All packages are exported and no activator nor services
 *         are provided.
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
 *         2012-01-16: Created!
 *         
 */
package se.natusoft.osgi.aps.json.tools;

import se.natusoft.osgi.aps.json.*;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Takes a JavaBean and produces a JSONObject.
 */
public class JavaToJSON {

    /**
     * Converts a JavaBean object into a JSONObject.
     *
     * @param javaBean The JavaBean object to convert.
     *
     * @return A JSONObject containing all values from the JavaBean.
     *
     * @throws JSONConvertionException on converting failure.
     */
    public static JSONObject convertObject(Object javaBean) throws JSONConvertionException {
        return convertObject(new JSONObject(), javaBean);
    }

    /**
     * Converts a JavaBean object into a JSONObject.
     *
     * @param jsonObject The jsonObject to convert the bean into or null for a new JSONObject.
     * @param javaBean The JavaBean object to convert.
     *
     * @return A JSONObject containing all values from the JavaBean.
     *
     * @throws JSONConvertionException on converting failure.
     */
    public static JSONObject convertObject(JSONObject jsonObject, Object javaBean) throws JSONConvertionException {
        JSONObject obj = jsonObject;
        if (obj == null) {
            obj = new JSONObject();
        }

        if (javaBean instanceof Dictionary) {
            Enumeration dictEnum = ((Dictionary)javaBean).keys();
            while (dictEnum.hasMoreElements()) {
                Object key = dictEnum.nextElement();
                String value = ((Dictionary)javaBean).get(key).toString();
                obj.addProperty(key.toString(), new JSONString(value));
            }
        }
        else if (javaBean instanceof Map) {
            for (Object key : ((Map)javaBean).keySet()) {
                Object value = ((Map)javaBean).get(key);
                obj.addProperty(key.toString(), convertValue(value));
            }
        }
        else {
            for (Method method : javaBean.getClass().getMethods()) {
                if (
                        !method.getName().equals("getClass") &&
                        (
                                method.getName().startsWith("is") ||
                                (
                                        method.getName().startsWith("get") &&
                                        method.getName().length() > 3
                                )
                        )
                   ) {
                    Object value = null;
                    String prop = null;

                    if (method.getName().startsWith("get")) {
                        prop = method.getName().substring(3);
                    }
                    else if (method.getName().startsWith("is")) {
                        prop = method.getName().substring(2);
                    }
                    prop = prop.substring(0,1).toLowerCase() + prop.substring(1);

                    try {
                        value = method.invoke(javaBean);
                    }
                    catch (Exception e) {
                        value = e.getMessage();
                    }

                    obj.addProperty(prop, convertValue(value));
                }
            }
        }

        return obj;
    }

    /**
     * Converts a value from a java value to a JSONValue.
     *
     * @param value The java value to convert. It can be one of String, Number, Boolean, null, JavaBean, or an array of those.
     *              If you pass in something else you will get an empty JSONObject back!
     *
     * @return The converted JSONValue.
     */
    public static JSONValue convertValue(Object value) {
        JSONValue json = null;

        if (value == null) {
            json = new JSONNull();
        }
        else if (
                Number.class.isAssignableFrom(value.getClass()) ||
                byte.class.isAssignableFrom(value.getClass()) ||
                double.class.isAssignableFrom(value.getClass()) ||
                float.class.isAssignableFrom(value.getClass()) ||
                int.class.isAssignableFrom(value.getClass()) ||
                long.class.isAssignableFrom(value.getClass()) ||
                short.class.isAssignableFrom(value.getClass())
                ) {
            json = new JSONNumber((Number)value);
        }
        else if (value instanceof String) {
            json = new JSONString((String)value);
        }
        else if (boolean.class.isAssignableFrom(value.getClass()) || Boolean.class.isAssignableFrom(value.getClass())) {
            json = new JSONBoolean((Boolean)value);
        }
        else if (value instanceof Date) {
            json = new JSONNumber(((Date)value).getTime());
        }
        else if (value.getClass().isArray()) {
            JSONArray array = new JSONArray();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object aValue = Array.get(value, i);
                array.addValue(convertValue(aValue));
            }
            json = array;
        }
        else if (value instanceof List) {
            JSONArray array = new JSONArray();
            for (Object cValue : (List)value) {
                array.addValue(convertValue(cValue));
            }
            json = array;
        }
        else if (value.getClass().isEnum()) {
            json = new JSONString(((Enum)value).name());
        }
        else { // Treat as object
            json = convertObject(value);
        }

        return json;
    }
}
