/* 
 * 
 * PROJECT
 *     Name
 *         APS Tools Library
 *     
 *     Code Version
 *         0.11.0
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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2014-03-08: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.tuples;

/**
 * A tuple with two values.
 */
public class Tuple2<T1, T2> extends Tuple<T1> {

    public T2 t2;

    public Tuple2() {}

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }
}
