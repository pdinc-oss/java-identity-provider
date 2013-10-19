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

package net.shibboleth.idp.attribute.resolver.impl.dc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.BaseDataConnector;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/** A data connector that just returns a static collection of attributes. */
@ThreadSafe
public class StaticDataConnector extends BaseDataConnector {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(StaticDataConnector.class);

    /** Static collection of values returned by this connector. */
    private Map<String, IdPAttribute> attributes;

    /**
     * Get the static values returned by this connector.
     * 
     * @return static values returned by this connector
     */
    @Nullable @NonnullAfterInit public Map<String, IdPAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Set static values returned by this connector.
     * 
     * @param newValues static values returned by this connector
     */
    public synchronized void setValues(@Nullable @NullableElements Collection<IdPAttribute> newValues) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        if (null == newValues) {
            attributes = null;
            return;
        } 
        
        final Map<String, IdPAttribute> map = new HashMap<String, IdPAttribute>(newValues.size());
        for (IdPAttribute attr:newValues) {
            if (null == attr) {
                continue;
            }
            map.put(attr.getId(), attr);
        }
        
        attributes = ImmutableMap.copyOf(map);
    }

    /** {@inheritDoc} */
    @Nonnull protected Map<String, IdPAttribute> doDataConnectorResolve(
            final AttributeResolutionContext resolutionContext) throws ResolutionException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        log.trace("{} Resolved attributes: {}", getLogPrefix(), attributes);
        return attributes;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == attributes) {
            throw new ComponentInitializationException(getLogPrefix()
                    + " No values set up.");
        }
    }
}