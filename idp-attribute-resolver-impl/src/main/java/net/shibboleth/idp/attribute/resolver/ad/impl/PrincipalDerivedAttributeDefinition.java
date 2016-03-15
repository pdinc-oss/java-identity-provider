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

package net.shibboleth.idp.attribute.resolver.ad.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.authn.context.SubjectContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.navigate.ChildContextLookup;
import org.opensaml.messaging.context.navigate.ParentContextLookup;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * An attribute definition which returns an attribute attributes derived from the {@link java.security.Principal}s
 * associated with the request.
 */
public class PrincipalDerivedAttributeDefinition extends AbstractAttributeDefinition {

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(PrincipalDerivedAttributeDefinition.class);
    
    /** Strategy used to locate the {@link ProfileRequestContext} to use. */
    @Nonnull private Function<AttributeResolutionContext, ProfileRequestContext> prcLookupStrategy;

    /** Strategy used to locate the {@link SubjectContext} to use. */
    @Nonnull private Function<ProfileRequestContext, SubjectContext> scLookupStrategy;

    /**
     * Engine used to generate the values associated with the {@link Principal}
     * 
     * The engine returns null or an empty list if the {@link Principal} isn't relevant.
     */
    @Nonnull private Function<Principal, List<IdPAttributeValue<?>>> attributeValueEngine;

    /** Constructor. */
    public PrincipalDerivedAttributeDefinition() {
        prcLookupStrategy = new ParentContextLookup<>();
        scLookupStrategy = new ChildContextLookup<ProfileRequestContext, SubjectContext>(SubjectContext.class);
    }
    
    /**
     * Set the strategy used to locate the {@link ProfileRequestContext} associated with a given
     * {@link AttributeResolutionContext}.
     * 
     * @param strategy strategy used to locate the {@link ProfileRequestContext} associated with a given
     *            {@link AttributeResolutionContext}
     */
    public void setProfileRequestContextLookupStrategy(
            @Nonnull final Function<AttributeResolutionContext, ProfileRequestContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        prcLookupStrategy = Constraint.isNotNull(strategy, "ProfileRequestContext lookup strategy cannot be null");
    }

    /**
     * Set the strategy used to locate the {@link SubjectContext} associated with a given
     * {@link AttributeResolutionContext}.
     * 
     * @param strategy strategy used to locate the {@link SubjectContext} associated with a given
     *            {@link AttributeResolutionContext}
     */
    public void
            setSubjectContextLookupStrategy(@Nonnull final Function<ProfileRequestContext, SubjectContext> strategy) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);

        scLookupStrategy = Constraint.isNotNull(strategy, "SubjectContext lookup strategy cannot be null");
    }

    /**
     * Sets the attribute value engine.
     * 
     * @param engine what to set.
     */
    public void setAttributeValueEngine(@Nonnull final Function<Principal, List<IdPAttributeValue<?>>> engine) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        attributeValueEngine = Constraint.isNotNull(engine, "Attribute Engine cannot be null");
    }

    @Override @Nullable protected IdPAttribute doAttributeDefinitionResolve(
            @Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {

        final ProfileRequestContext prc = prcLookupStrategy.apply(resolutionContext);
        final SubjectContext cs = scLookupStrategy.apply(prc);
        final List<IdPAttributeValue<?>> results = new ArrayList<>(1);

        for (final Subject subject : cs.getSubjects()) {
            for (final Principal principal : subject.getPrincipals()) {
                final List<IdPAttributeValue<?>> values = attributeValueEngine.apply(principal);
                if ((null != values) && !values.isEmpty()) {
                    results.addAll(values);
                }
            }
        }
        if (results.isEmpty()) {
            log.info("{} generated no values, attrribute no resolved.", getLogPrefix());
            return null;
        }
        log.debug("{} Generated {} values.", getLogPrefix(), results.size());
        log.trace("{} Values:", getLogPrefix(), results);
        final IdPAttribute attribute = new IdPAttribute(getId());
        attribute.setValues(results);
        return attribute;
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        Constraint.isNotNull(scLookupStrategy, "SubjectContext lookup strategy cannot be null");
        Constraint.isNotNull(prcLookupStrategy, "ProfileRequestContext lookup strategy cannot be null");
        Constraint.isNotNull(attributeValueEngine, "Attribute Engine cannot be null");

        super.doInitialize();
    }

}