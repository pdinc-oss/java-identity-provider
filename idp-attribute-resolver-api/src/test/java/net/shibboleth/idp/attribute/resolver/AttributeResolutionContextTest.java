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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.shibboleth.idp.attribute.IdPAttribute;

import org.testng.Assert;
import org.testng.annotations.Test;

/** Unit test for {@link AttributeResolutionContext}. */
public class AttributeResolutionContextTest {

    /** Test instantiation and post-instantiation state. */
    @Test public void instantiation() {

        AttributeResolutionContext context = new AttributeResolutionContext();
        Assert.assertNull(context.getParent());
        Assert.assertNotNull(context.getRequestedAttributes());
        Assert.assertTrue(context.getRequestedAttributes().isEmpty());
        Assert.assertNotNull(context.getResolvedAttributeDefinitions());
        Assert.assertTrue(context.getResolvedAttributeDefinitions().isEmpty());
        Assert.assertNotNull(context.getResolvedDataConnectors());
        Assert.assertTrue(context.getResolvedDataConnectors().isEmpty());
    }
    
    /** Test {@link AttributeResolutionContext#setRequestedAttributes(java.util.Set)}. */
    @Test public void setRequesedAttributes() {
        AttributeResolutionContext context = new AttributeResolutionContext();

        context.setRequestedAttributes(null);
        Assert.assertNotNull(context.getRequestedAttributes());
        Assert.assertTrue(context.getRequestedAttributes().isEmpty());

        HashSet<IdPAttribute> attributes = new HashSet<IdPAttribute>();
        context.setRequestedAttributes(attributes);
        Assert.assertNotNull(context.getRequestedAttributes());
        Assert.assertTrue(context.getRequestedAttributes().isEmpty());

        attributes.add(null);
        context.setRequestedAttributes(attributes);
        Assert.assertNotNull(context.getRequestedAttributes());
        Assert.assertTrue(context.getRequestedAttributes().isEmpty());

        attributes.add(new IdPAttribute("foo"));
        attributes.add(null);
        attributes.add(new IdPAttribute("bar"));
        context.setRequestedAttributes(attributes);
        Assert.assertNotNull(context.getRequestedAttributes());
        Assert.assertEquals(context.getRequestedAttributes().size(), 2);

        attributes.clear();
        attributes.add(new IdPAttribute("baz"));
        context.setRequestedAttributes(attributes);
        Assert.assertNotNull(context.getRequestedAttributes());
        Assert.assertEquals(context.getRequestedAttributes().size(), 1);
    }

    /** Test {@link AttributeResolutionContext#setRequestedAttributes(java.util.Set)}. */
    @Test public void setResolvedAttributes() {
        AttributeResolutionContext context = new AttributeResolutionContext();

        context.setResolvedAttributes(null);
        Assert.assertNotNull(context.getResolvedAttributes());
        Assert.assertTrue(context.getResolvedAttributes().isEmpty());

        HashSet<IdPAttribute> attributes = new HashSet<IdPAttribute>();
        context.setResolvedAttributes(attributes);
        Assert.assertNotNull(context.getResolvedAttributes());
        Assert.assertTrue(context.getResolvedAttributes().isEmpty());

        attributes.add(null);
        context.setResolvedAttributes(attributes);
        Assert.assertNotNull(context.getResolvedAttributes());
        Assert.assertTrue(context.getResolvedAttributes().isEmpty());

        attributes.add(new IdPAttribute("foo"));
        attributes.add(null);
        attributes.add(new IdPAttribute("bar"));
        context.setResolvedAttributes(attributes);
        Assert.assertNotNull(context.getResolvedAttributes());
        Assert.assertEquals(context.getResolvedAttributes().size(), 2);

        attributes.clear();
        attributes.add(new IdPAttribute("baz"));
        context.setResolvedAttributes(attributes);
        Assert.assertNotNull(context.getResolvedAttributes());
        Assert.assertEquals(context.getResolvedAttributes().size(), 1);
    }

    /** Test adding and retrieving attribute definitions. */
    @Test public void resolvedAttributeDefinitions() throws Exception {
        AttributeResolutionContext context = new AttributeResolutionContext();

        Assert.assertNotNull(context.getResolvedAttributeDefinitions());
        Assert.assertNull(context.getResolvedAttributeDefinitions().get("foo"));

        IdPAttribute attribute = new IdPAttribute("foo");
        MockAttributeDefinition definition = new MockAttributeDefinition("foo", attribute);
        definition.initialize();

        context.recordAttributeDefinitionResolution(definition, attribute);
        Assert.assertNotNull(context.getResolvedAttributeDefinitions());
        Assert.assertEquals(context.getResolvedAttributeDefinitions().size(), 1);
        Assert.assertNotNull(context.getResolvedAttributeDefinitions().get("foo"));
        Assert.assertTrue(context.getResolvedAttributeDefinitions().get("foo") instanceof ResolvedAttributeDefinition);
        Assert.assertTrue(context.getResolvedAttributeDefinitions().get("foo").getResolvedDefinition() == definition);
        Assert.assertTrue(context.getResolvedAttributeDefinitions().get("foo").resolve(context) == attribute);

        try {
            context.recordAttributeDefinitionResolution(definition, attribute);
            Assert.fail("able to record a second resolution for a single attribute definition");
        } catch (ResolutionException e) {
            // expected this
        }

        definition = new MockAttributeDefinition("bar", (IdPAttribute) null);
        definition.initialize();

        context.recordAttributeDefinitionResolution(definition, null);
        Assert.assertNotNull(context.getResolvedAttributeDefinitions());
        Assert.assertEquals(context.getResolvedAttributeDefinitions().size(), 2);
        Assert.assertNotNull(context.getResolvedAttributeDefinitions().get("foo"));
        Assert.assertTrue(context.getResolvedAttributeDefinitions().get("foo") instanceof ResolvedAttributeDefinition);
        Assert.assertNotNull(context.getResolvedAttributeDefinitions().get("bar"));
        Assert.assertTrue(context.getResolvedAttributeDefinitions().get("bar") instanceof ResolvedAttributeDefinition);
        Assert.assertTrue(context.getResolvedAttributeDefinitions().get("bar").getResolvedDefinition() == definition);
        Assert.assertNull(context.getResolvedAttributeDefinitions().get("bar").resolve(context));
    }

    /** Test adding and retrieving data connectors. */
    @Test public void resolvedDataConnectors() throws Exception {
        AttributeResolutionContext context = new AttributeResolutionContext();

        Assert.assertNotNull(context.getResolvedDataConnectors());
        Assert.assertNull(context.getResolvedDataConnectors().get("foo"));

        IdPAttribute attribute = new IdPAttribute("foo");

        Map<String, IdPAttribute> attributes = new HashMap<String, IdPAttribute>();
        attributes.put(attribute.getId(), attribute);

        MockDataConnector connector = new MockDataConnector("foo", attributes);
        connector.initialize();

        context.recordDataConnectorResolution(connector, attributes);
        Assert.assertNotNull(context.getResolvedDataConnectors());
        Assert.assertEquals(context.getResolvedDataConnectors().size(), 1);
        Assert.assertNotNull(context.getResolvedDataConnectors().get("foo"));
        Assert.assertTrue(context.getResolvedDataConnectors().get("foo") instanceof ResolvedDataConnector);
        Assert.assertTrue(context.getResolvedDataConnectors().get("foo").getResolvedConnector() == connector);
        Assert.assertTrue(context.getResolvedDataConnectors().get("foo").resolve(context) == attributes);

        try {
            context.recordDataConnectorResolution(connector, attributes);
            Assert.fail("able to record a second resolution for a single data connector");
        } catch (ResolutionException e) {
            // expected this
        }

        connector = new MockDataConnector("bar", (Map) null);
        connector.initialize();

        context.recordDataConnectorResolution(connector, null);
        Assert.assertNotNull(context.getResolvedDataConnectors());
        Assert.assertEquals(context.getResolvedDataConnectors().size(), 2);
        Assert.assertNotNull(context.getResolvedDataConnectors().get("foo"));
        Assert.assertTrue(context.getResolvedDataConnectors().get("foo") instanceof ResolvedDataConnector);
        Assert.assertNotNull(context.getResolvedDataConnectors().get("bar"));
        Assert.assertTrue(context.getResolvedDataConnectors().get("bar") instanceof ResolvedDataConnector);
        Assert.assertTrue(context.getResolvedDataConnectors().get("bar").getResolvedConnector() == connector);
        Assert.assertNull(context.getResolvedDataConnectors().get("bar").resolve(context));
        
        try {
            MockStaticDataConnector other = new MockStaticDataConnector();
            other.setId("bar");
            other.setValues(Collections.EMPTY_LIST);
            other.initialize();
            context.recordDataConnectorResolution(other, null);
            Assert.fail("Cannot cross the same bridge twice or add the same resolvedId twice");
        } catch (ResolutionException ex) {
            //OK
        }
    }
}