/*
 * Copyright 2011 University Corporation for Advanced Internet Development, Inc.
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

package net.shibboleth.idp.attribute.resolver.impl;

import java.util.Collection;
import java.util.Set;

import net.shibboleth.idp.ComponentValidationException;
import net.shibboleth.idp.attribute.resolver.AttributeResolutionContext;
import net.shibboleth.idp.attribute.resolver.AttributeResolutionException;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.BaseAttributeDefinition;
import net.shibboleth.idp.attribute.resolver.BaseDataConnector;
import net.shibboleth.idp.attribute.resolver.ResolverPluginDependency;

import org.opensaml.util.collections.LazySet;
import org.testng.Assert;
import org.testng.annotations.Test;

/** Test the mapped attribute type. */
public class MappedAttributeTester {

    /** The name. */
    private static final String TEST_ATTRIBUTE_NAME = "mapped";

    /** Test with no mapping provided. */
    @Test
    public void testEmptyMap() {
        Collection<ValueMap> map = new LazySet<ValueMap>();
        BaseAttributeDefinition mapped = new MappedAttributeDefinition(TEST_ATTRIBUTE_NAME, map);
        //
        // Set the dependency on the data connector
        //
        Set<ResolverPluginDependency> dependencySet = new LazySet<ResolverPluginDependency>();
        dependencySet.add(new ResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME));
        mapped.setDependencies(dependencySet);

        //
        // And resolve
        //
        AttributeResolver resolver = new AttributeResolver("foo");
        Set<BaseDataConnector> connectorSet = new LazySet<BaseDataConnector>();
        connectorSet.add(TestSources.populatedStaticConnectior());

        Set<BaseAttributeDefinition> attributeSet = new LazySet<BaseAttributeDefinition>();
        attributeSet.add(mapped);
        resolver.setDataConnectors(connectorSet);
        resolver.setAttributeDefinition(attributeSet);

        AttributeResolutionContext context = new AttributeResolutionContext(null);
        try {
            resolver.resolveAttributes(context);
        } catch (AttributeResolutionException e) {
            Assert.fail("resolution failed", e);
        }

        Assert.assertNull(context.getResolvedAttributes().get(TEST_ATTRIBUTE_NAME));
    }

    /** Test no mapping but with a default. */
    @Test
    public void testNoMapDefauly() {
        Collection<ValueMap> map = new LazySet<ValueMap>();
        final String mapResult = "result";

        MappedAttributeDefinition mapped = new MappedAttributeDefinition(TEST_ATTRIBUTE_NAME, map);
        mapped.setDefaultValue(mapResult);

        //
        // Set the dependency on the data connector
        //
        Set<ResolverPluginDependency> dependencySet = new LazySet<ResolverPluginDependency>();
        dependencySet.add(new ResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME));
        mapped.setDependencies(dependencySet);

        //
        // And resolve
        //
        AttributeResolver resolver = new AttributeResolver("foo");
        Set<BaseDataConnector> connectorSet = new LazySet<BaseDataConnector>();
        connectorSet.add(TestSources.populatedStaticConnectior());

        Set<BaseAttributeDefinition> attributeSet = new LazySet<BaseAttributeDefinition>();
        attributeSet.add(mapped);
        resolver.setDataConnectors(connectorSet);
        resolver.setAttributeDefinition(attributeSet);

        AttributeResolutionContext context = new AttributeResolutionContext(null);
        try {
            resolver.resolveAttributes(context);
        } catch (AttributeResolutionException e) {
            Assert.fail("resolution failed", e);
        }

        Collection f = context.getResolvedAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        Assert.assertEquals(f.size(), 1);
        Assert.assertTrue(f.contains(mapResult), "looking for value TEST_DEFAULT_VALUE");
    }

    /** Test mapping . */
    @Test
    public void testMap() {
        Collection<ValueMap> map = new LazySet<ValueMap>();
        final String mapResult = "result";

        ValueMap valueMap = new ValueMap();
        valueMap.setReturnValue(mapResult);
        valueMap.getSourceValues().add(valueMap.new SourceValue(TestSources.COMMON_ATTRIBUTE_VALUE, false, false));
        map.add(valueMap);

        MappedAttributeDefinition mapped = new MappedAttributeDefinition(TEST_ATTRIBUTE_NAME, map);

        //
        // Set the dependency on the data connector
        //
        Set<ResolverPluginDependency> dependencySet = new LazySet<ResolverPluginDependency>();
        dependencySet.add(new ResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME));
        mapped.setDependencies(dependencySet);

        //
        // And resolve
        //
        AttributeResolver resolver = new AttributeResolver("foo");
        Set<BaseDataConnector> connectorSet = new LazySet<BaseDataConnector>();
        connectorSet.add(TestSources.populatedStaticConnectior());

        Set<BaseAttributeDefinition> attributeSet = new LazySet<BaseAttributeDefinition>();
        attributeSet.add(mapped);
        resolver.setDataConnectors(connectorSet);
        resolver.setAttributeDefinition(attributeSet);

        AttributeResolutionContext context = new AttributeResolutionContext(null);
        try {
            resolver.resolveAttributes(context);
        } catch (AttributeResolutionException e) {
            Assert.fail("resolution failed", e);
        }

        Collection f = context.getResolvedAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        Assert.assertEquals(f.size(), 1);
        Assert.assertTrue(f.contains(mapResult), "looking for value TEST_DEFAULT_VALUE");
    }

    /**
     * Test mapping .
     * 
     * @throws ComponentValidationException
     */
    @Test
    public void testMultiMap() throws ComponentValidationException {
        Collection<ValueMap> map = new LazySet<ValueMap>();
        final String mapResult1 = "result1";
        final String mapResult2 = "result2";

        ValueMap valueMap = new ValueMap();
        valueMap.setReturnValue(mapResult1);
        String trunc = TestSources.COMMON_ATTRIBUTE_VALUE;
        trunc = trunc.substring(0, trunc.length() - 1);
        trunc = trunc.toUpperCase();
        valueMap.getSourceValues().add(valueMap.new SourceValue(trunc, true, true));
        map.add(valueMap);

        valueMap = new ValueMap();
        valueMap.setReturnValue(mapResult2);
        valueMap.getSourceValues().add(
                valueMap.new SourceValue(TestSources.CONNECTOR_ATTRIBUTE_VALUE_REGEXP.toUpperCase(), true, false));
        map.add(valueMap);

        MappedAttributeDefinition mapped = new MappedAttributeDefinition(TEST_ATTRIBUTE_NAME, map);
        mapped.validate();

        Set<ResolverPluginDependency> dependencySet = new LazySet<ResolverPluginDependency>();
        dependencySet.add(new ResolverPluginDependency(TestSources.STATIC_CONNECTOR_NAME,
                TestSources.DEPENDS_ON_ATTRIBUTE_NAME));
        mapped.setDependencies(dependencySet);

        AttributeResolver resolver = new AttributeResolver("foo");
        Set<BaseDataConnector> connectorSet = new LazySet<BaseDataConnector>();
        connectorSet.add(TestSources.populatedStaticConnectior());

        Set<BaseAttributeDefinition> attributeSet = new LazySet<BaseAttributeDefinition>();
        attributeSet.add(mapped);
        resolver.setDataConnectors(connectorSet);
        resolver.setAttributeDefinition(attributeSet);

        AttributeResolutionContext context = new AttributeResolutionContext(null);
        try {
            resolver.resolveAttributes(context);
        } catch (AttributeResolutionException e) {
            Assert.fail("resolution failed", e);
        }

        Collection f = context.getResolvedAttributes().get(TEST_ATTRIBUTE_NAME).getValues();

        Assert.assertTrue(f.contains(mapResult1), "looking for value mapResult1");
        Assert.assertTrue(f.contains(mapResult2), "looking for value mapResult2");
        Assert.assertEquals(f.size(), 2);
    }
}
