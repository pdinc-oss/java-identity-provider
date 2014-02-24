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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.collection.CollectionSupport;
import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializeableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

/**
 * Base class for all {@link ResolverPlugin}s.
 * 
 * @param <ResolvedType> object type this plug-in resolves to
 */
@ThreadSafe
public abstract class AbstractResolverPlugin<ResolvedType> extends AbstractIdentifiableInitializeableComponent
        implements ResolverPlugin<ResolvedType>, DisposableBean {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(AbstractResolverPlugin.class);

    /** Whether an {@link AttributeResolutionContext} that occurred resolving attributes will be re-thrown. */
    private boolean propagateResolutionExceptions = true;

    /** Criterion that must be met for this plugin to be active for the given request. */
    @Nonnull private Predicate<AttributeResolutionContext> activationCriteria = Predicates.alwaysTrue();

    /** IDs of the {@link ResolutionPlugIn}s this plug-in depends on. */
    @Nonnull @NonnullElements private Set<ResolverPluginDependency> dependencies = Collections.emptySet();

    /**
     * Get whether an {@link AttributeResolutionContext} that occurred resolving attributes will be re-thrown. Doing so
     * will cause the entire attribute resolution request to fail.
     * 
     * @return true if {@link ResolutionException}s are propagated, false if not
     */
    @Override public boolean isPropagateResolutionExceptions() {
        return propagateResolutionExceptions;
    }

    /**
     * Set whether an {@link AttributeResolutionContext} that occurred resolving attributes will be re-thrown. Doing so
     * will cause the entire attribute resolution request to fail.
     * 
     * @param propagate true if {@link ResolutionException}s are propagated, false if not
     */
    public void setPropagateResolutionExceptions(final boolean propagate) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        propagateResolutionExceptions = propagate;
    }

    /**
     * Gets the criteria that must be met for this plugin to be active for a given request.
     * 
     * @return criteria that must be met for this plugin to be active for a given request, never null
     */
    @Override @Nonnull public Predicate<AttributeResolutionContext> getActivationCriteria() {
        return activationCriteria;
    }

    /**
     * Sets the criteria that must be met for this plugin to be active for a given request.
     * 
     * @param criteria criteria that must be met for this plugin to be active for a given request
     */
    public void setActivationCriteria(@Nonnull final Predicate<AttributeResolutionContext> criteria) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);
        activationCriteria = Constraint.isNotNull(criteria, "Activiation criteria cannot be null");
    }

    /**
     * Gets the unmodifiable list of dependencies for this plugin.
     * 
     * @return unmodifiable list of dependencies for this plugin, never null
     */
    @Override @Nonnull @NonnullElements @Unmodifiable public Set<ResolverPluginDependency> getDependencies() {
        return dependencies;
    }

    /**
     * Sets the list of dependencies for this plugin.
     * 
     * @param pluginDependencies unmodifiable list of dependencies for this plugin
     */
    public void setDependencies(@Nullable @NullableElements final Set<ResolverPluginDependency> pluginDependencies) {
        ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        HashSet<ResolverPluginDependency> checkedDeps = new HashSet<>();
        CollectionSupport.addIf(checkedDeps, pluginDependencies, Predicates.notNull());
        dependencies = Collections.unmodifiableSet(checkedDeps);
    }

    /**
     * Performs the attribute resolution for this plugin.
     * 
     * <p>
     * This method first checks to see if this plugin has been initialized and has not be destroyed. Then it checks if
     * the plugins activation criterion has been met. Finally it delegates to
     * {@link #doResolve(AttributeResolutionContext)}. If an exception is thrown and
     * {@link #isPropagateResolutionExceptions()} is false the exception is logged but not re-thrown, otherwise it is
     * re-thrown.
     * </p>
     * 
     * @param resolutionContext current attribute resolution context
     * 
     * @return the attributes made available by the resolution, or null if no attributes were resolved
     * 
     * @throws ResolutionException thrown if there was a problem resolving the attributes
     */
    @Override @Nullable public final ResolvedType resolve(@Nonnull final AttributeResolutionContext resolutionContext)
            throws ResolutionException {
        ComponentSupport.ifNotInitializedThrowUninitializedComponentException(this);
        ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

        Constraint.isNotNull(resolutionContext, "AttributeResolutionContext cannot be null");

        if (!activationCriteria.apply(resolutionContext)) {
            log.debug("Resolver plugin '{}': activation criteria not met, nothing to do", getId());
            return null;
        }

        final AttributeResolverWorkContext workContext =
                resolutionContext.getSubcontext(AttributeResolverWorkContext.class, false);
        Constraint.isNotNull(workContext, "AttributeResolverWorkContext cannot be null");

        try {
            return doResolve(resolutionContext, workContext);
        } catch (ResolutionException e) {
            //
            // NOTE - if you change this logic you MUST make changes in any derived classes that
            // depend on our handling of propagateResolutionExceptions.
            //
            if (propagateResolutionExceptions) {
                throw e;
            } else {
                log.debug("Resolver {} produced the following" + " error but was configured not to propogate it.",
                        new Object[] {getId(), e,});
                return null;
            }
        }
    }

    /** {@inheritDoc} */
    @Override protected void doDestroy() {
        activationCriteria = Predicates.alwaysFalse();
        dependencies = Collections.emptySet();

        super.doDestroy();
    }

    /** {@inheritDoc} */
    @Override protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();

        // rebuild the hash set - we may have modified the dependencies in the
        // child class initialization.
        HashSet<ResolverPluginDependency> checkedDeps = new HashSet<>(dependencies);
        dependencies = ImmutableSet.copyOf(checkedDeps);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return Objects.hashCode(getId());
    }

    /** {@inheritDoc} */
    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AbstractResolverPlugin)) {
            return false;
        }

        AbstractResolverPlugin<ResolvedType> other = (AbstractResolverPlugin<ResolvedType>) obj;
        return Objects.equal(getId(), other.getId());
    }

    /**
     * Perform the actual resolution. The resolved attribute(s) should not be recorded in the work context.
     * 
     * @param resolutionContext current resolution context
     * @param workContext child context where intermediate results are tracked
     * 
     * @return the resolved attributes or null if no attributes were resolved
     * @throws ResolutionException thrown if there is a problem resolving the attributes
     * 
     * @see AbstractResolverPlugin#resolve(AttributeResolutionContext)
     */
    @Nullable protected abstract ResolvedType doResolve(@Nonnull final AttributeResolutionContext resolutionContext,
            @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException;

}