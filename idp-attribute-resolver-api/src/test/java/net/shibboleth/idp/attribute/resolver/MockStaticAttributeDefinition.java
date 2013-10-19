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

package net.shibboleth.idp.attribute.resolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.resolver.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.BaseAttributeDefinition;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An attribute definition that simply returns a static value.   Used for testing only.  This is 
 * a cut and paste job from StaticAttributeDefinition in idp-attribute-resolver-impl */
@ThreadSafe
public class MockStaticAttributeDefinition extends BaseAttributeDefinition {
    
    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(MockStaticAttributeDefinition.class);

    /** Static value returned by this definition. */
    private IdPAttribute value;

    /**
     * Set the attribute value we are returning.
     * 
     * @param newAttribute what to set.
     */
    public synchronized void setValue(@Nullable IdPAttribute newAttribute) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        
        if (null != newAttribute) {
            value = newAttribute;
        }
    }

    /**
     * Return the static attribute we are returning.
     * 
     * @return the attribute.
     */
    @Nonnull public IdPAttribute getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Nonnull protected IdPAttribute doAttributeDefinitionResolve(
            final AttributeResolutionContext resolutionContext) throws ResolutionException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        log.debug("Attribute definition '{}': Resolving static attribute {}", getId(), value);
        return value;
    }

    /** {@inheritDoc} */
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        if (null == value) {
            throw new ComponentInitializationException("Static Attribute definition " + getId()
                    + " does not have an attribute set up.");
        }
    }
}