/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         1.0.0
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
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *         
 */
package se.natusoft.osgi.aps.util;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides a very simple pooling of instances.
 */
public class APSSimplePool<Pooled> {

    //
    // Private Members
    //

    private Set<Pooled> freePool = new LinkedHashSet<>();

    private Set<Pooled> busyPool = new LinkedHashSet<>();

    private boolean closed = false;

    //
    // Constructors
    //

    /**
     * Creates a new APSSimplePool.
     */
    public APSSimplePool() {}

    /**
     * Creates a new APSSimplePool.
     *
     * @param initial The initial pooled items.
     */
    public APSSimplePool(Set<Pooled> initial) {

        this.freePool.addAll(initial);
    }

    //
    // Methods
    //

    /**
     * Adds an entry to the pool.
     *
     * @param pooled The entry to add.
     */
    public synchronized void add(Pooled pooled) {

        this.freePool.add(pooled);
    }

    /**
     * Removes an entry from the pool.
     *
     * @param pooled The entry to remove.
     */
    public synchronized void remove(Pooled pooled) {

        this.freePool.remove(pooled);
    }

    /**
     * Allocates a pooled object.
     *
     * @throws PoolException if the pool is empty.
     */
    public synchronized Pooled allocate() throws PoolException {

        if (this.closed) throw new PoolException("Pool is closed!");
        if (this.freePool.isEmpty()) throw new PoolException("Pool is empty!");

        Pooled pooled = this.freePool.iterator().next();
        this.freePool.remove(pooled);
        this.busyPool.add(pooled);
        return pooled;
    }

    /**
     * Releases a pooled object back into the pool.
     *
     * @param pooled The pooled object to release.
     */
    public synchronized void release(Pooled pooled) {

        if (this.closed) throw new PoolException("Pool is closed!");
        this.busyPool.remove(pooled);
        this.freePool.add(pooled);
    }

    /**
     * @return A set of all entries both free and busy. This to clean up the pool and free resources. Preferably do close() before this.
     */
    public synchronized Set<Pooled> getAllPooledEntries() {

        Set<Pooled> all = new LinkedHashSet<Pooled>();

        all.addAll( this.freePool );
        all.addAll( this.busyPool );

        return all;
    }

    /**
     * Closes the pool. No more allocate or release calls can be made after this.
     */
    public synchronized void close() {

        this.closed = true;
    }

    /**
     * @return Number of available entries.
     */
    public synchronized int getAvailableSize() {

        return this.freePool.size();
    }

    /**
     * @return Number of currently busy entries.
     */
    public synchronized int getBusySize() {

        return this.busyPool.size();
    }

    /**
     * @return true if the pool is empty.
     */
    public synchronized boolean isEmpty() {

        return getPoolSize() == 0;
    }

    /**
     * @return The size of the pool.
     */
    public synchronized int getPoolSize() {

        return this.freePool.size() + this.busyPool.size();
    }

    //
    // Inner Classes
    //

    /**
     * On any failures.
     */
    public static class PoolException extends RuntimeException {

        public PoolException(String message, Throwable cause) {

            super(message, cause);
        }

        public PoolException(String message) {

            super(message);
        }
    }
}
