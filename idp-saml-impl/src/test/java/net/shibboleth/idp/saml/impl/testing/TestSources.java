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

package net.shibboleth.idp.saml.impl.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.resolver.AbstractAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.AbstractDataConnector;
import net.shibboleth.idp.attribute.resolver.AttributeDefinition;
import net.shibboleth.idp.attribute.resolver.DataConnector;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.ResolverAttributeDefinitionDependency;
import net.shibboleth.idp.attribute.resolver.ResolverDataConnectorDependency;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolverWorkContext;
import net.shibboleth.idp.saml.attribute.resolver.impl.SAML2NameIDAttributeDefinition;
import net.shibboleth.idp.testing.DatabaseTestingSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullAfterInit;
import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NullableElements;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentSupport;

/** Basic data sources for testing the attribute generators. */
public final class TestSources {
    /** The name we use in this test for the static connector. */
    public static final String STATIC_CONNECTOR_NAME = "staticCon";


    /** The name of the attribute we use as source. */
    public static final String DEPENDS_ON_ATTRIBUTE_NAME_ATTR = "at1";

    public static final String DEPENDS_ON_ATTRIBUTE_NAME_CONNECTOR = "ac1";

    /** The name of another attribute we use as source. */
    public static final String DEPENDS_ON_SECOND_ATTRIBUTE_NAME = "at2";

    /** Another attributes values. */
    public static final String[] SECOND_ATTRIBUTE_VALUE_STRINGS = {"at2-Val1", "at2-Val2"};

    public static final StringAttributeValue[] SECOND_ATTRIBUTE_VALUE_RESULTS = {
            new StringAttributeValue(SECOND_ATTRIBUTE_VALUE_STRINGS[0]),
            new StringAttributeValue(SECOND_ATTRIBUTE_VALUE_STRINGS[0]),};

    /** A value from both providers. */
    public static final String COMMON_ATTRIBUTE_VALUE_STRING = "at1-Data";

    public static final StringAttributeValue COMMON_ATTRIBUTE_VALUE_RESULT = new StringAttributeValue(
            COMMON_ATTRIBUTE_VALUE_STRING);

    /** A value from the connector. */
    public static final String CONNECTOR_ATTRIBUTE_VALUE_STRING = "at1-Connector";

    public static final StringAttributeValue CONNECTOR_ATTRIBUTE_VALUE_RESULT = new StringAttributeValue(
            CONNECTOR_ATTRIBUTE_VALUE_STRING);

    /** A value from the attribute. */
    public static final String ATTRIBUTE_ATTRIBUTE_VALUE_STRING = "at1-Attribute";

    public static final StringAttributeValue ATTRIBUTE_ATTRIBUTE_VALUE_RESULT = new StringAttributeValue(
            ATTRIBUTE_ATTRIBUTE_VALUE_STRING);

    /** Regexp. for CONNECTOR_ATTRIBUTE_VALUE (for map and regexp testing). */

    public static final String CONNECTOR_ATTRIBUTE_VALUE_REGEXP = "at1-(.+)or";

    public static final Pattern CONNECTOR_ATTRIBUTE_VALUE_REGEXP_PATTERN = Pattern
            .compile(CONNECTOR_ATTRIBUTE_VALUE_REGEXP);

    public static final StringAttributeValue CONNECTOR_ATTRIBUTE_VALUE_REGEXP_RESULT = new StringAttributeValue(
            "Connect");

    /** Principal name for Principal method tests */
    public static final String TEST_PRINCIPAL = "PrincipalName";

    /** Relying party name for Principal method tests */
    public static final String TEST_RELYING_PARTY = "RP1";

    /** Authentication method for Principal method tests */
    public static final String TEST_AUTHN_METHOD = "AuthNmEthod";

    public static final String IDP_ENTITY_ID = DatabaseTestingSupport.IDP_ENTITY_ID;

    public static final String PRINCIPAL_ID = DatabaseTestingSupport.PRINCIPAL_ID;

    public static final String SP_ENTITY_ID = DatabaseTestingSupport.SP_ENTITY_ID;

    /** Constructor. */
    private TestSources() {
    }

    /**
     * Create a static connector with provided attributes and values.
     * 
     * @param attributes the objects to populate it with
     * @return The connector
     * @throws ComponentInitializationException if we cannot initialized (unlikely)
     */
    public static DataConnector populatedStaticConnector(@Nonnull @NonnullElements final List<IdPAttribute> attributes)
            throws ComponentInitializationException {

        final StaticDataConnector connector = new StaticDataConnector();
        connector.setId(STATIC_CONNECTOR_NAME);
        connector.setValues(attributes);
        connector.initialize();

        return connector;
    }

    /**
     * Create a static connector with known attributes and values.
     * 
     * @return The connector
     * @throws ComponentInitializationException if we cannot initialized (unlikely)
     */
    public static DataConnector populatedStaticConnector() throws ComponentInitializationException {
        List<IdPAttribute> attributeSet = new ArrayList<>(2);

        IdPAttribute attr = new IdPAttribute(DEPENDS_ON_ATTRIBUTE_NAME_CONNECTOR);
        attr.setValues(List.of(new StringAttributeValue(COMMON_ATTRIBUTE_VALUE_STRING),
                new StringAttributeValue(CONNECTOR_ATTRIBUTE_VALUE_STRING)));
        attributeSet.add(attr);

        attr = new IdPAttribute(DEPENDS_ON_SECOND_ATTRIBUTE_NAME);
        attr.setValues(List.of(new StringAttributeValue(SECOND_ATTRIBUTE_VALUE_STRINGS[0]),
                new StringAttributeValue(SECOND_ATTRIBUTE_VALUE_STRINGS[1])));
        attributeSet.add(attr);
        
        return populatedStaticConnector(attributeSet);
    }

    /**
     * Create a static attribute with known values.
     * 
     * @return the attribute definition
     * @throws ComponentInitializationException if we cannot initialized (unlikely)
     */
    public static AttributeDefinition populatedStaticAttribute() throws ComponentInitializationException {
        return populatedStaticAttribute(DEPENDS_ON_ATTRIBUTE_NAME_ATTR, 2);
    }
    
    public static AttributeDefinition populatedStaticAttribute(String attributeName,
            int attributeValuesCount) throws ComponentInitializationException {
        
        final List<IdPAttributeValue> valuesList = new ArrayList<>();

        if (attributeValuesCount > 0) {
            valuesList.add(new StringAttributeValue(COMMON_ATTRIBUTE_VALUE_STRING));
        }
        if (attributeValuesCount > 1) {
            valuesList.add(new StringAttributeValue(ATTRIBUTE_ATTRIBUTE_VALUE_STRING));
        }
        for (int i = 2; i < attributeValuesCount; i++) {
            valuesList.add(new StringAttributeValue(ATTRIBUTE_ATTRIBUTE_VALUE_STRING + i));
        }
        final IdPAttribute attr = new IdPAttribute(attributeName);
        attr.setValues(valuesList);

        return populatedStaticAttribute(attr);
    }

    /**
     * Create a static attribute with known attribute.
     * 
     * @param attribute the input attribute
     * @return the attribute definition
     * @throws ComponentInitializationException if we cannot initialized (unlikely)
     */
    public static AttributeDefinition populatedStaticAttribute(@Nonnull final IdPAttribute attribute)
            throws ComponentInitializationException {
        
        final StaticAttributeDefinition definition = new StaticAttributeDefinition();
        definition.setId(attribute.getId());
        definition.setValue(attribute);
        definition.initialize();
        return definition;
    }

    public static AttributeDefinition nonStringAttributeDefiniton(String name) throws ComponentInitializationException {
        final SAML2NameIDAttributeDefinition defn = new SAML2NameIDAttributeDefinition();
        defn.setId(name);

        // Set the dependency on the data connector
        ResolverAttributeDefinitionDependency depend = new ResolverAttributeDefinitionDependency(TestSources.DEPENDS_ON_ATTRIBUTE_NAME_ATTR);
        defn.setAttributeDependencies(Collections.singleton(depend));
        defn.initialize();
        return defn;
    }

    public static AttributeResolutionContext createResolutionContext(String principal, String issuerID,
            String recipientId) {
        ProfileRequestContext parent = new ProfileRequestContext();
        AttributeResolutionContext retVal = parent.getSubcontext(AttributeResolutionContext.class, true);

        retVal.setAttributeIssuerID(issuerID);
        retVal.setAttributeRecipientID(recipientId);
        retVal.setPrincipal(principal);

        retVal.getSubcontext(AttributeResolverWorkContext.class, true);
        return retVal;
    }

    public static ResolverAttributeDefinitionDependency makeAttributeDefinitionDependency(String attributeId) {
        ResolverAttributeDefinitionDependency retVal = new ResolverAttributeDefinitionDependency(attributeId);
        return retVal;
    }
    
    public static ResolverDataConnectorDependency makeDataConnectorDependency(@Nonnull String connectorId, @Nullable String attributeId) {
        ResolverDataConnectorDependency retVal = new ResolverDataConnectorDependency(connectorId);
        if (null == attributeId) {
            retVal.setAllAttributes(true);
        } else {
            retVal.setAttributeNames(Collections.singleton(attributeId));
        }
        return retVal; 
    }
    
    private static class StaticAttributeDefinition extends AbstractAttributeDefinition {

        /** Static value returned by this definition. */
        @NonnullAfterInit private IdPAttribute value;

        /**
         * Set the attribute value we are returning.
         * 
         * @param newAttribute what to set.
         */
        public void setValue(@Nullable final IdPAttribute newAttribute) {
            ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
            ComponentSupport.ifDestroyedThrowDestroyedComponentException(this);

            value = newAttribute;
        }

        /**
         * Return the static attribute we are returning.
         * 
         * @return the attribute.
         */
        @NonnullAfterInit public IdPAttribute getValue() {
            return value;
        }

        /** {@inheritDoc} */
        @Override @Nonnull protected IdPAttribute doAttributeDefinitionResolve(
                final AttributeResolutionContext resolutionContext,
                @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
            return value;
        }

        /** {@inheritDoc} */
        @Override protected void doInitialize() throws ComponentInitializationException {
            super.doInitialize();

            if (null == value) {
                throw new ComponentInitializationException(getLogPrefix() + " no attribute value set");
            }
        }
    }

    private static class StaticDataConnector extends AbstractDataConnector {

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
        public void setValues(@Nullable @NullableElements Collection<IdPAttribute> newValues) {

            if (null == newValues) {
                attributes = null;
                return;
            }

            final Map<String,IdPAttribute> map = new HashMap<>(newValues.size());
            for (final IdPAttribute attr : newValues) {
                if (null == attr) {
                    continue;
                }
                map.put(attr.getId(), attr);
            }

            attributes = Map.copyOf(map);
        }

        /** {@inheritDoc} */
        @Override @Nonnull protected Map<String, IdPAttribute> doDataConnectorResolve(
                @Nonnull final AttributeResolutionContext resolutionContext,
                @Nonnull final AttributeResolverWorkContext workContext) throws ResolutionException {
            return attributes;
        }

        /** {@inheritDoc} */
        @Override protected void doInitialize() throws ComponentInitializationException {
            super.doInitialize();

            if (null == attributes) {
                throw new ComponentInitializationException(getLogPrefix() + " No values set up.");
            }
        }

    }
}
