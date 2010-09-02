/*
 * Copyright 2009 University Corporation for Advanced Internet Development, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.shibboleth.idp.service;

/**
 * A simple interface that represents coarse grained services, or components, within the software.
 */
public interface Service {

    /** Indicates the service object has been created but not yet started. */
    public static final String STATE_NEW = "new";

    /** Indicates the service is in the process of being started but is not yet ready for use. */
    public static final String STATE_STARTING = "starting";

    /** Indicates that the service is ready for use. */
    public static final String STATE_RUNNING = "running";

    /** Indicates that the service is stopping and is not available for use. */
    public static final String STATE_STOPPING = "stopping";

    /** Indicates that the service is stopped and is not available for use. */
    public static final String STATE_STOPPED = "stopped";

    /**
     * Gets the ID of this service.
     * 
     * @return ID of this service
     */
    public String getId();

    /**
     * Gets a human-readable display name for this service.
     * 
     * @return human-readable display name for this service
     */
    public String getDisplayName();

    /**
     * Gets the current state of the service.
     * 
     * @return current state of the service
     */
    public String getCurrentState();

    /**
     * Starts this service. Calling this on an started service should return immediately without affecting the service.
     * 
     * @throws ServiceException thrown if there is a problem initializing the service
     */
    public void start() throws ServiceException;

    /**
     * Stops a service, freeing any resources it may currently be using. Whether a service can be restarted after being
     * stopped is implementation dependent.
     * 
     * @throws ServiceException thrown if there is a problem destroying the service
     */
    public void stop() throws ServiceException;

    /**
     * Validates that a service is operational and function properly (with the limits that such things can be checked).
     * 
     * @throws ServiceException thrown if there is a problem with the service
     */
    public void validate() throws ServiceException;
}