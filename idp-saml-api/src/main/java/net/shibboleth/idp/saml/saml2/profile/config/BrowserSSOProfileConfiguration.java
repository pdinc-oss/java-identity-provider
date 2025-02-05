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

package net.shibboleth.idp.saml.saml2.profile.config;

import java.security.Principal;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.authn.config.AuthenticationProfileConfiguration;
import net.shibboleth.idp.saml.authn.principal.AuthnContextClassRefPrincipal;
import net.shibboleth.idp.saml.profile.config.logic.ProxyAwareForceAuthnPredicate;
import net.shibboleth.idp.saml.saml2.profile.config.navigate.ProxyAwareAuthnContextComparisonLookupFunction;
import net.shibboleth.idp.saml.saml2.profile.config.navigate.ProxyAwareDefaultAuthenticationMethodsLookupFunction;
import net.shibboleth.utilities.java.support.annotation.constraint.NonNegative;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.NotLive;
import net.shibboleth.utilities.java.support.annotation.constraint.Unmodifiable;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.logic.FunctionSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.SubjectLocality;

import com.google.common.base.Predicates;

/** Configuration support for SAML 2 Browser SSO. */
public class BrowserSSOProfileConfiguration extends AbstractSAML2ArtifactAwareProfileConfiguration
        implements AuthenticationProfileConfiguration {
    
    /** ID for this profile configuration. */
    @Nonnull @NotEmpty public static final String PROFILE_ID = "http://shibboleth.net/ns/profiles/saml2/sso/browser";
    
    /** Default maximum delegation chain length. */
    @Nonnull public static final Long DEFAULT_DELEGATION_CHAIN_LENGTH = 1L;
        
    /** Bit constant for RequestedAuthnContext feature. */
    public static final int FEATURE_AUTHNCONTEXT = 0x1;

    /** Bit constant for Scoping feature. */
    public static final int FEATURE_SCOPING = 0x2;
    
    /** Whether attributes should be resolved in the course of the profile. */
    @Nonnull private Predicate<ProfileRequestContext> resolveAttributesPredicate;

    /** Whether responses to the authentication request should include an attribute statement. */
    @Nonnull private Predicate<ProfileRequestContext> includeAttributeStatementPredicate;

    /** Whether to ignore Scoping elements within AuthnRequest. */
    @Nonnull private Predicate<ProfileRequestContext> ignoreScoping;
    
    /** Whether to mandate forced authentication for the request. */
    @Nonnull private Predicate<ProfileRequestContext> forceAuthnPredicate;

    /** Whether to compare client and assertion addresses on inbound SSO. */
    @Nonnull private Predicate<ProfileRequestContext> checkAddressPredicate;

    /** Whether the response endpoint should be validated if the request is signed. */
    @Nonnull private Predicate<ProfileRequestContext> skipEndpointValidationWhenSignedPredicate;

    /** Whether authentication results should carry the proxied AuthnInstant. */
    @Nonnull private Predicate<ProfileRequestContext> proxiedAuthnInstantPredicate;

    /** Lookup function to supply maximum session lifetime. */
    @Nonnull private Function<ProfileRequestContext,Duration> maximumSPSessionLifetimeLookupStrategy;

    /** Lookup function to supply maximum time since inbound AuthnInstant. */
    @Nonnull private Function<ProfileRequestContext,Duration> maximumTimeSinceAuthnLookupStrategy;

    /** 
     * The predicate used to determine if produced assertions may be delegated.
     */
    @Nonnull private Predicate<ProfileRequestContext> allowDelegationPredicate;
    
    /** Lookup function to supply maximum delegation chain length. */
    @Nonnull private Function<ProfileRequestContext,Long> maximumTokenDelegationChainLengthLookupStrategy;

    /** Lookup function to supply the strategy function for translating SAML 2.0 AuthnContext data. */
    @Nonnull private Function<ProfileRequestContext,Function<AuthnContext,Collection<Principal>>>
        authnContextTranslationStrategyLookupStrategy;

    /** Lookup function to supply the strategy function for translating fully-generic data. */
    @Nonnull private Function<ProfileRequestContext,Function<ProfileRequestContext,Collection<Principal>>>
        authnContextTranslationStrategyExLookupStrategy;

    /** Lookup function for requested AC operator. */
    @Nonnull private Function<ProfileRequestContext,String> authnContextComparisonLookupStrategy;
    
    /** Lookup function to supply default authentication methods. */
    @Nonnull private Function<ProfileRequestContext,Collection<AuthnContextClassRefPrincipal>>
            defaultAuthenticationContextsLookupStrategy;
    
    /** Lookup function to supply authentication flows. */
    @Nonnull private Function<ProfileRequestContext,Set<String>> authenticationFlowsLookupStrategy;
    
    /** Lookup function to supply post authentication flows. */
    @Nonnull private Function<ProfileRequestContext,Collection<String>> postAuthenticationFlowsLookupStrategy;
    
    /** Lookup function to supply NameID formats. */
    @Nonnull private Function<ProfileRequestContext,Collection<String>> nameIDFormatPrecedenceLookupStrategy;
    
    /** Constructor. */
    public BrowserSSOProfileConfiguration() {
        this(PROFILE_ID);
    }

    /**
     * Constructor.
     * 
     * @param profileId unique ID for this profile
     */
    protected BrowserSSOProfileConfiguration(@Nonnull @NotEmpty final String profileId) {
        super(profileId);
        setSignResponses(true);
        setEncryptAssertions(true);
        resolveAttributesPredicate = Predicates.alwaysTrue();
        includeAttributeStatementPredicate = Predicates.alwaysTrue();
        ignoreScoping = Predicates.alwaysFalse();
        forceAuthnPredicate = new ProxyAwareForceAuthnPredicate();
        checkAddressPredicate = Predicates.alwaysTrue();
        skipEndpointValidationWhenSignedPredicate = Predicates.alwaysFalse();
        proxiedAuthnInstantPredicate = Predicates.alwaysTrue();
        maximumSPSessionLifetimeLookupStrategy = FunctionSupport.constant(null);
        maximumTimeSinceAuthnLookupStrategy = FunctionSupport.constant(null);
        maximumTokenDelegationChainLengthLookupStrategy = FunctionSupport.constant(DEFAULT_DELEGATION_CHAIN_LENGTH);
        allowDelegationPredicate = Predicates.alwaysFalse();
        authenticationFlowsLookupStrategy = FunctionSupport.constant(null);
        postAuthenticationFlowsLookupStrategy = FunctionSupport.constant(null);
        authnContextTranslationStrategyLookupStrategy = FunctionSupport.constant(null);
        authnContextTranslationStrategyExLookupStrategy = FunctionSupport.constant(null);
        authnContextComparisonLookupStrategy = new ProxyAwareAuthnContextComparisonLookupFunction();
        defaultAuthenticationContextsLookupStrategy = new ProxyAwareDefaultAuthenticationMethodsLookupFunction();
        nameIDFormatPrecedenceLookupStrategy = FunctionSupport.constant(null);
    }
    
    /**
     * Get whether attributes should be resolved during the profile.
     *
     * <p>Default is true</p>
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return true iff attributes should be resolved
     */
    public boolean isResolveAttributes(@Nullable final ProfileRequestContext profileRequestContext) {
        return resolveAttributesPredicate.test(profileRequestContext);
    }
    
    /**
     * Set whether attributes should be resolved during the profile.
     * 
     * @param flag flag to set
     */
    public void setResolveAttributes(final boolean flag) {
        resolveAttributesPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Set a condition to determine whether attributes should be resolved during the profile.
     * 
     * @param condition condition to set
     */
    public void setResolveAttributesPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        resolveAttributesPredicate = Constraint.isNotNull(condition, "Resolve attributes predicate cannot be null");
    }

    /**
     * Get whether responses to the authentication request should include an attribute statement.
     *
     * <p>Default is true</p>
     * 
     * @param profileRequestContext current profile request context
     *
     * @return whether responses to the authentication request should include an attribute statement
     */
    public boolean isIncludeAttributeStatement(@Nullable final ProfileRequestContext profileRequestContext) {
        return includeAttributeStatementPredicate.test(profileRequestContext);
    }

    /**
     * Set whether responses to the authentication request should include an attribute statement.
     *
     * @param flag flag to set
     */
    public void setIncludeAttributeStatement(final boolean flag) {
        includeAttributeStatementPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Set a condition to determine whether responses to the authentication request should include an
     * attribute statement.
     *
     * @param condition  condition to set
     */
    public void setIncludeAttributeStatementPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        includeAttributeStatementPredicate = Constraint.isNotNull(condition,
                "Include attribute statement predicate cannot be null");
    }
    
    /**
     * Gets whether Scoping elements in requests should be ignored/omitted.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return whether Scoping elements in requests should be ignored/omitted
     * 
     * @since 4.0.0
     */
    public boolean isIgnoreScoping(@Nullable final ProfileRequestContext profileRequestContext) {
        return ignoreScoping.test(profileRequestContext);
    }
    
    /**
     * Sets whether Scoping elements in requests should be ignored/omitted.
     * 
     * <p>Defaults to false.</p>
     * 
     * @param flag flag to set
     * 
     * @since 4.0.0
     */
    public void setIgnoreScoping(final boolean flag) {
        ignoreScoping = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Sets a condition to determine whether Scoping elements in requests should be ignored/omitted.
     * 
     * @param condition condition to set
     * 
     * @since 4.0.0
     */
    public void setIgnoreScopingPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        ignoreScoping = Constraint.isNotNull(condition, "Ignore Scoping condition cannot be null");
    }
    
    /** {@inheritDoc} */
    public boolean isForceAuthn(@Nullable final ProfileRequestContext profileRequestContext) {
        return forceAuthnPredicate.test(profileRequestContext);
    }
    
    /**
     * Set whether a fresh user presence proof should be required for this request.
     * 
     * @param flag flag to set
     */
    public void setForceAuthn(final boolean flag) {
        forceAuthnPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Set a condition to determine whether a fresh user presence proof should be required for this request.
     * 
     * @param condition condition to set
     */
    public void setForceAuthnPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        forceAuthnPredicate = Constraint.isNotNull(condition, "Forced authentication predicate cannot be null");
    }

    /**
     * Get whether the client's address must match the address in an inbound {@link SubjectLocality}
     * element during inbound SSO.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return whether to compare addresses
     * 
     * @since 4.0.0
     */
    public boolean isCheckAddress(@Nullable final ProfileRequestContext profileRequestContext) {
        return checkAddressPredicate.test(profileRequestContext);
    }
    
    /**
     * Set whether the client's address must match the address in an inbound {@link SubjectLocality}
     * element during inbound SSO.
     * 
     * @param flag flag to set
     * 
     * @since 4.0.0
     */
    public void setCheckAddress(final boolean flag) {
        checkAddressPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Set a condition to determine whether the client's address must match the address in an inbound
     * {@link SubjectLocality} element during inbound SSO.
     * 
     * @param condition condition to set
     * 
     * @since 4.0.0
     */
    public void setCheckAddressPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        checkAddressPredicate = Constraint.isNotNull(condition, "Address checking predicate cannot be null");
    }
    
    /**
     * Get condition to determine whether the response endpoint should be validated if the request is signed.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return condition
     * 
     * @since 4.0.0
     */
    public boolean isSkipEndpointValidationWhenSigned(@Nullable final ProfileRequestContext profileRequestContext) {
        return skipEndpointValidationWhenSignedPredicate.test(profileRequestContext);
    }
    
    /**
     * Set whether the response endpoint should be validated if the request is signed.
     * 
     * @param flag flag to set
     * 
     * @since 3.4.0
     */
    public void setSkipEndpointValidationWhenSigned(final boolean flag) {
        skipEndpointValidationWhenSignedPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Set condition to determine whether the response endpoint should be validated if the request is signed.
     * 
     * @param condition condition to set
     * 
     * @since 3.4.0
     */
    public void setSkipEndpointValidationWhenSignedPredicate(
            @Nonnull final Predicate<ProfileRequestContext> condition) {
        skipEndpointValidationWhenSignedPredicate = Constraint.isNotNull(condition, "Condition cannot be null");
    }
    
    /**
     * Gets whether authentication results produced by use of this profile should carry the proxied
     * assertion's AuthnInstant, rather than the current time.
     * 
     * <p>Defaults to true.</p>
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return whether to proxy across the inbound AuthnInstant
     * 
     * @since 4.0.0
     */
    public boolean isProxiedAuthnInstant(@Nullable final ProfileRequestContext profileRequestContext) {
        return proxiedAuthnInstantPredicate.test(profileRequestContext);
    }
    
    /**
     * Sets whether authentication results produced by use of this profile should carry the proxied
     * assertion's AuthnInstant, rather than the current time.
     * 
     * @param flag flag to set
     * 
     * @since 4.0.0
     */
    public void setProxiedAuthnInstant(final boolean flag) {
        proxiedAuthnInstantPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }
    
    /**
     * Sets condition to determine whether authentication results produced by use of this profile should
     * carry the proxied assertion's AuthnInstant, rather than the current time.
     * 
     * @param condition condition to set
     * 
     * @since 4.0.0
     */
    public void setProxiedAuthnInstantPredicate(@Nonnull final Predicate<ProfileRequestContext> condition) {
        proxiedAuthnInstantPredicate = Constraint.isNotNull(condition, "Condition cannot be null");
    }

    /**
     * Get the maximum amount of time the service provider should maintain a session for the user
     * based on the authentication assertion. A null or 0 is interpreted as an unlimited lifetime.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return max lifetime of service provider should maintain a session
     */
    @Nullable public Duration getMaximumSPSessionLifetime(@Nullable final ProfileRequestContext profileRequestContext) {
        final Duration lifetime = maximumSPSessionLifetimeLookupStrategy.apply(profileRequestContext);
        Constraint.isFalse(lifetime != null && lifetime.isNegative(),
                "Maximum SP session lifetime must be greater than or equal to 0");
        return lifetime;
    }

    /**
     * Set the maximum amount of time the service provider should maintain a session for the user
     * based on the authentication assertion. A null or 0 is interpreted as an unlimited lifetime.
     * 
     * @param lifetime max lifetime of service provider should maintain a session
     */
    public void setMaximumSPSessionLifetime(@Nullable final Duration lifetime) {
        Constraint.isFalse(lifetime != null && lifetime.isNegative(),
                "Maximum SP session lifetime must be greater than or equal to 0");
        
        maximumSPSessionLifetimeLookupStrategy = FunctionSupport.constant(lifetime);
    }
    
    /**
     * Set a lookup strategy for the maximum amount of time the service provider should maintain a session for the user.
     * 
     * @param strategy  lookup strategy
     * 
     * @since 3.4.0
     */
    public void setMaximumSPSessionLifetimeLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Duration> strategy) {
        maximumSPSessionLifetimeLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }
    
    /**
     * Get the maximum amount of time allowed to have elapsed since an incoming AuthnInstant.
     * 
     * <p>A null or 0 is interpreted as an unlimited amount.</p>
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return max time since inbound AuthnInstant
     * 
     * @since 4.0.0
     */
    @Nullable public Duration getMaximumTimeSinceAuthn(@Nullable final ProfileRequestContext profileRequestContext) {
        final Duration amount = maximumTimeSinceAuthnLookupStrategy.apply(profileRequestContext);
        Constraint.isFalse(amount != null && amount.isNegative(),
                "Maximum time since authentication must be greater than or equal to 0");
        return amount;
    }

    /**
     * Set the maximum amount of time allowed to have elapsed since an incoming AuthnInstant.
     * 
     * <p>A null or 0 is interpreted as an unlimited amount.</p>
     * 
     * @param amount max time to allow
     * 
     * @since 4.0.0
     */
    public void setMaximumTimeSinceAuthn(@Nullable final Duration amount) {
        Constraint.isFalse(amount != null && amount.isNegative(),
                "Maximum time since authentication must be greater than or equal to 0");
        
        maximumTimeSinceAuthnLookupStrategy = FunctionSupport.constant(amount);
    }
    
    /**
     * Set a lookup strategy for the maximum amount of time allowed to have elapsed since an incoming AuthnInstant.
     * 
     * @param strategy  lookup strategy
     * 
     * @since 4.0.0
     */
    public void setMaximumTimeSinceAuthnLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Duration> strategy) {
        maximumTimeSinceAuthnLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }
    
    /**
     * Get the predicate used to determine if produced assertions may be delegated.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return predicate used to determine if produced assertions may be delegated
     */
    @Nonnull public boolean isAllowDelegation(@Nullable final ProfileRequestContext profileRequestContext) {
        return allowDelegationPredicate.test(profileRequestContext);
    }
    

    /**
     * Set whether produced assertions may be delegated.
     * 
     * @param  flag flag to set
     */
    public void setAllowDelegation(final boolean flag) {
        allowDelegationPredicate = flag ? Predicates.alwaysTrue() : Predicates.alwaysFalse();
    }    

    /**
     * Set the predicate used to determine if produced assertions may be delegated.
     * 
     * @param  predicate used to determine if produced assertions may be delegated
     */
    public void setAllowDelegationPredicate(@Nonnull final Predicate<ProfileRequestContext> predicate) {
        allowDelegationPredicate = Constraint.isNotNull(predicate, "Allow delegation predicate cannot be null");
    }

    /**
     * Get the limits on the total number of delegates that may be derived from the initial SAML token.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return the limit on the total number of delegates that may be derived from the initial SAML token
     */
    @NonNegative public long getMaximumTokenDelegationChainLength(
            @Nullable final ProfileRequestContext profileRequestContext) {
        
        final Long len = maximumTokenDelegationChainLengthLookupStrategy.apply(profileRequestContext);
        Constraint.isNotNull(len, "Delegation chain length cannot be null");
        Constraint.isGreaterThanOrEqual(0, len, "Delegation chain length must be greater than or equal to 0");
        
        return len;
    }

    /**
     * Set the limits on the total number of delegates that may be derived from the initial SAML token.
     * 
     * @param length the limit on the total number of delegates that may be derived from the initial SAML token
     */
    public void setMaximumTokenDelegationChainLength(@NonNegative final long length) {
        Constraint.isGreaterThanOrEqual(0, length, "Delegation chain length must be greater than or equal to 0");
        
        maximumTokenDelegationChainLengthLookupStrategy = FunctionSupport.constant(length);
    }
    
    /**
     * Set a lookup strategy for the limits on the total number of delegates that
     * may be derived from the initial SAML token.
     * 
     * @param strategy  lookup strategy
     * 
     * @since 3.4.0
     */
    public void setMaximumTokenDelegationChainLengthLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Long> strategy) {
        maximumTokenDelegationChainLengthLookupStrategy =
                Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }
    
    /**
     * Get the function to use to translate an inbound proxied SAML 2.0 {@link AuthnContext} into the appropriate
     * set of custom {@link Principal} objects to populate into the subject.
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return translation function
     * 
     * @since 4.0.0
     */
    @Nullable public Function<AuthnContext,Collection<Principal>> getAuthnContextTranslationStrategy(
            @Nullable final ProfileRequestContext profileRequestContext) {
        return authnContextTranslationStrategyLookupStrategy.apply(profileRequestContext);
    }

    /**
     * Set the function to use to translate an inbound proxied SAML 2.0 {@link AuthnContext} into the appropriate
     * set of custom {@link Principal} objects to populate into the subject.
     * 
     * @param strategy translation function
     * 
     * @since 4.0.0
     */
    public void setAuthnContextTranslationStrategy(
            @Nullable final Function<AuthnContext,Collection<Principal>> strategy) {
        authnContextTranslationStrategyLookupStrategy = FunctionSupport.constant(strategy);
    }

    /**
     * Set a lookup strategy for the function to use to translate an inbound proxied SAML 2.0 {@link AuthnContext}
     * into the appropriate set of custom {@link Principal} objects to populate into the subject.
     * 
     * @param strategy lookup strategy
     * 
     * @since 4.0.0
     */
    public void setAuthnContextTranslationStrategyLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Function<AuthnContext,Collection<Principal>>> strategy) {
        authnContextTranslationStrategyLookupStrategy =
                Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /**
     * Get the function to use to translate an inbound proxied response into the appropriate
     * set of custom {@link Principal} objects to populate into the subject.
     * 
     * <p>This differs from the original in that the input is the entire {@link ProfileRequestContext}
     * of the proxied authentication state rather than the SAML {@link AuthnContext} directly.</p>
     * 
     * @param profileRequestContext current profile request context
     * 
     * @return translation function
     * 
     * @since 4.1.0
     */
    @Nullable public Function<ProfileRequestContext,Collection<Principal>> getAuthnContextTranslationStrategyEx(
            @Nullable final ProfileRequestContext profileRequestContext) {
        return authnContextTranslationStrategyExLookupStrategy.apply(profileRequestContext);
    }

    /**
     * Set the function to use to translate an inbound proxied response into the appropriate
     * set of custom {@link Principal} objects to populate into the subject.
     * 
     * <p>This differs from the original in that the input is the entire {@link ProfileRequestContext}
     * of the proxied authentication state rather than the SAML {@link AuthnContext} directly.</p>
     * 
     * @param strategy translation function
     * 
     * @since 4.1.0
     */
    public void setAuthnContextTranslationStrategyEx(
            @Nullable final Function<ProfileRequestContext,Collection<Principal>> strategy) {
        authnContextTranslationStrategyExLookupStrategy = FunctionSupport.constant(strategy);
    }

    /**
     * Set a lookup strategy for the function to use to translate an inbound proxied response
     * into the appropriate set of custom {@link Principal} objects to populate into the subject.
     * 
     * <p>This differs from the original in that the input is the entire {@link ProfileRequestContext}
     * of the proxied authentication state rather than the SAML {@link AuthnContext} directly.</p>
     * 
     * @param strategy lookup strategy
     * 
     * @since 4.1.0
     */
    public void setAuthnContextTranslationStrategyExLookupStrategy(
            @Nonnull
            final Function<ProfileRequestContext,Function<ProfileRequestContext,Collection<Principal>>> strategy) {
        authnContextTranslationStrategyExLookupStrategy =
                Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /**
     * Get the comparison operator to use when issuing SAML requests containing requested context classes.
     * 
     * @param profileRequestContext profile request context
     * 
     * @return comparison value or null
     * 
     * @since 4.0.0
     */
    @Nullable public AuthnContextComparisonTypeEnumeration getAuthnContextComparison(
            @Nullable final ProfileRequestContext profileRequestContext) {
        
        final String comparison = authnContextComparisonLookupStrategy.apply(profileRequestContext);
        if (comparison != null) {
            return AuthnContextComparisonTypeEnumeration.valueOf(comparison.toUpperCase());
        }
        
        return null;
    }
    
    /**
     * Set the comparison operator to use when issuing SAML requests containing requested context classes.
     * 
     * @param comparison comparison value or null
     * 
     * @since 4.0.0
     */
    public void setAuthnContextComparison(@Nullable final AuthnContextComparisonTypeEnumeration comparison) {
        authnContextComparisonLookupStrategy =
                FunctionSupport.constant(comparison != null ? comparison.toString() : null);
    }

    /**
     * Set a lookup strategy for the comparison operator to use when issuing SAML requests containing
     * requested context classes.
     * 
     * @param strategy lookup strategy
     * 
     * @since 4.0.0
     */
    public void setAuthnContextComparisonLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,String> strategy) {
        authnContextComparisonLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<Principal> getDefaultAuthenticationMethods(
            @Nullable final ProfileRequestContext profileRequestContext) {
        final Collection<AuthnContextClassRefPrincipal> methods =
                defaultAuthenticationContextsLookupStrategy.apply(profileRequestContext);
        if (methods != null) {
            return List.copyOf(methods);
        }
        return Collections.emptyList();
    }
        
    /**
     * Set the default authentication contexts to use, expressed as custom principals.
     * 
     * @param contexts default authentication contexts to use
     */
    public void setDefaultAuthenticationMethods(
            @Nullable @NonnullElements final Collection<AuthnContextClassRefPrincipal> contexts) {
        if (contexts != null) {
            defaultAuthenticationContextsLookupStrategy = FunctionSupport.constant(List.copyOf(contexts));
        } else {
            defaultAuthenticationContextsLookupStrategy = FunctionSupport.constant(null);
        }
    }

    /**
     * Set a lookup strategy for the authentication contexts to use, expressed as custom principals.
     *
     * @param strategy  lookup strategy
     * 
     * @since 3.3.0
     */
    public void setDefaultAuthenticationMethodsLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Collection<AuthnContextClassRefPrincipal>> strategy) {
        defaultAuthenticationContextsLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public Set<String> getAuthenticationFlows(
            @Nullable final ProfileRequestContext profileRequestContext) {
        final Set<String> flows = authenticationFlowsLookupStrategy.apply(profileRequestContext);
        if (flows != null) {
            return Set.copyOf(flows);
        }
        return Collections.emptySet();
    }

    /**
     * Set the authentication flows to use.
     * 
     * @param flows   flow identifiers to use
     */
    public void setAuthenticationFlows(@Nullable @NonnullElements final Collection<String> flows) {
        if (flows != null) {
            authenticationFlowsLookupStrategy =
                    FunctionSupport.constant(Set.copyOf(StringSupport.normalizeStringCollection(flows)));
        } else {
            authenticationFlowsLookupStrategy = FunctionSupport.constant(null);
        }
    }

    /**
     * Set a lookup strategy for the authentication flows to use.
     *
     * @param strategy  lookup strategy
     * 
     * @since 3.3.0
     */
    public void setAuthenticationFlowsLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Set<String>> strategy) {
        authenticationFlowsLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /** {@inheritDoc} */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<String> getPostAuthenticationFlows(
            @Nullable final ProfileRequestContext profileRequestContext) {
        final Collection<String> flows = postAuthenticationFlowsLookupStrategy.apply(profileRequestContext);
        if (flows != null) {
            return List.copyOf(flows);
        }
        return Collections.emptyList();
    }

    /**
     * Set the ordered collection of post-authentication interceptor flows to enable.
     * 
     * @param flows   flow identifiers to enable
     */
    public void setPostAuthenticationFlows(@Nullable @NonnullElements final Collection<String> flows) {
        if (flows != null) {
            postAuthenticationFlowsLookupStrategy =
                    FunctionSupport.constant(List.copyOf(StringSupport.normalizeStringCollection(flows)));
        } else {
            postAuthenticationFlowsLookupStrategy = FunctionSupport.constant(null);
        }
    }

    /**
     * Set a lookup strategy for the post-authentication interceptor flows to enable.
     *
     * @param strategy  lookup strategy
     * 
     * @since 3.3.0
     */
    public void setPostAuthenticationFlowsLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Collection<String>> strategy) {
        postAuthenticationFlowsLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

    /**
     * Get the name identifier formats to use.
     * 
     * @param profileRequestContext profile request context
     * 
     * @return formats to use
     */
    @Nonnull @NonnullElements @NotLive @Unmodifiable public List<String> getNameIDFormatPrecedence(
            @Nullable final ProfileRequestContext profileRequestContext) {
        
        final Collection<String> formats = nameIDFormatPrecedenceLookupStrategy.apply(profileRequestContext);
        if (formats != null) {
            return List.copyOf(formats);
        }
        return Collections.emptyList();
    }

    /**
     * Set the name identifier formats to use.
     * 
     * @param formats   name identifier formats to use
     */
    public void setNameIDFormatPrecedence(@Nonnull @NonnullElements final Collection<String> formats) {
        Constraint.isNotNull(formats, "List of formats cannot be null");
        
        nameIDFormatPrecedenceLookupStrategy =
                FunctionSupport.constant(List.copyOf(StringSupport.normalizeStringCollection(formats)));
    }

    /**
     * Set a lookup strategy for the name identifier formats to use.
     *
     * @param strategy  lookup strategy
     * 
     * @since 3.3.0
     */
    public void setNameIDFormatPrecedenceLookupStrategy(
            @Nonnull final Function<ProfileRequestContext,Collection<String>> strategy) {
        nameIDFormatPrecedenceLookupStrategy = Constraint.isNotNull(strategy, "Lookup strategy cannot be null");
    }

}