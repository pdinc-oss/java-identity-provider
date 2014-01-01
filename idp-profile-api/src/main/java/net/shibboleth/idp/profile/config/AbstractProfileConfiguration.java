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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import com.google.common.base.Objects;

/** Base class for {@link ProfileConfiguration} implementations. */
public abstract class AbstractProfileConfiguration implements ProfileConfiguration {

    /** ID of the profile configured. */
    @Nonnull @NotEmpty private final String id;

    /** Whether this profile is enabled. */
    private boolean enabled;

    /** The security configuration for this profile. */
    @Nullable private SecurityConfiguration securityConfiguration;

    /**
     * Constructor.
     * 
     * @param profileId ID of the the communication profile, never null or empty
     */
    public AbstractProfileConfiguration(@Nonnull @NotEmpty final String profileId) {
        id = Constraint.isNotNull(StringSupport.trimOrNull(profileId), "Profile identifier cannot be null or empty");
        enabled = true;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull @NotEmpty public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this profile is enabled.
     * 
     * @param isEnabled whether this profile is enabled
     */
    public void setEnabled(final boolean isEnabled) {
        enabled = isEnabled;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    /**
     * Sets the security configuration for this profile.
     * 
     * @param configuration security configuration for this profile
     */
    public void setSecurityConfiguration(@Nullable final SecurityConfiguration configuration) {
        securityConfiguration = configuration;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AbstractProfileConfiguration)) {
            return false;
        }

        AbstractProfileConfiguration other = (AbstractProfileConfiguration) obj;
        return Objects.equal(id, other.getId());
    }
    
}