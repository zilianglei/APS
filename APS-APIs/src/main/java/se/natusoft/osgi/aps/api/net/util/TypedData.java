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
 *         2016-02-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.util;

/**
 * Defines data with content type.
 */
public interface TypedData {

    /** Use this for unknown content type. */
    String UNKNOWN_CONTENT_TYPE = "UNKNOWN";

    /**
     * Sets the data content.
     *
     * @param content The value content to set.
     */
    void setContent(byte[] content);

    /**
     * Returns the data content.
     */
    byte[] getContent();

    /**
     * Sets the type of the content.
     *
     * @param contentType The type to set. Preferably use mime types like "application/json", etc.
     */
    void setContentType(String contentType);

    /**
     * Returns the type of the content.
     */
    String getContentType();

    /**
     * Utility implementation.
     */
    class Provider implements TypedData {
        //
        // Private Members
        //

        private byte[] content;
        private String contentType;

        //
        // Constructors
        //

        /**
         * Creates a new Provider.
         */
        public Provider() {}

        /**
         * Creates a new Provider.
         *
         * @param contentType The data type.
         */
        public Provider(String contentType) {
            this.contentType = contentType;
        }

        /**
         * Creates a new Provider.
         *
         * @param content The data content.
         * @param contentType The data type.
         */
        public Provider(byte[] content, String contentType) {
            this.content = content;
            this.contentType = contentType;
        }

        //
        // Methods
        //

        /**
         * Sets the data content.
         *
         * @param content The data content to set.
         */
        @Override
        public void setContent(byte[] content) {
            this.content = content;
        }

        /**
         * Returns the data content.
         */
        @Override
        public byte[] getContent() {
            return this.content;
        }

        /**
         * Sets the type of the data content.
         *
         * @param contentType The type to set. Preferably use mime types.
         */
        @Override
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        /**
         * Returns the type of the data content.
         */
        @Override
        public String getContentType() {
            return this.contentType;
        }
    }
}
