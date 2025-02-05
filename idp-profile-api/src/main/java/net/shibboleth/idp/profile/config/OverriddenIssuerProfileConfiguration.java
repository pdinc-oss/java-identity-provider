/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.idp.profile.config;

import javax.annotation.Nullable;

import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;

/**
 * {@link ProfileConfiguration} with optional override of issuer setting.
 *
 * @since 4.1.0
 */
public interface OverriddenIssuerProfileConfiguration extends ProfileConfiguration {

    /**
     * Get overridden issuer value.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return issuer or null to use usual default
     */
    @Nullable @NotEmpty String getIssuer(@Nullable final ProfileRequestContext profileRequestContext);
    
}