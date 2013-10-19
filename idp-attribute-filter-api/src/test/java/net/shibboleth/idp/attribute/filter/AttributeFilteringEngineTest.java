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

package net.shibboleth.idp.attribute.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.AttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.filter.AttributeFilterContext;
import net.shibboleth.idp.attribute.filter.AttributeFilterPolicy;
import net.shibboleth.idp.attribute.filter.AttributeFilter;
import net.shibboleth.idp.attribute.filter.AttributeRule;
import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.ComponentValidationException;
import net.shibboleth.utilities.java.support.component.DestroyedComponentException;
import net.shibboleth.utilities.java.support.component.UninitializedComponentException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/** Unit test for {@link AttributeFilter}. */
public class AttributeFilteringEngineTest {

    /** Test that post-construction state is what is expected. */
    @Test public void testPostConstructionState() throws Exception {
        AttributeFilter engine = new AttributeFilter("engine", null);
        Assert.assertNotNull(engine.getFilterPolicies());
        Assert.assertTrue(engine.getFilterPolicies().isEmpty());
        Assert.assertEquals(engine.getId(), "engine");

        try {
            new AttributeFilter("  ", null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected
        }

        try {
            new AttributeFilter("", null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected
        }

        try {
            new AttributeFilter(null, null);
            Assert.fail();
        } catch (ConstraintViolationException e) {
            // expected
        }
    }

    /** Test setting and retrieving filter policies. */
    @Test public void testFilterPolicies() throws Exception {
        AttributeFilterPolicy policy1 = new AttributeFilterPolicy("policy1", PolicyRequirementRule.MATCHES_NONE, null);
        AttributeFilterPolicy policy2 = new AttributeFilterPolicy("policy2", PolicyRequirementRule.MATCHES_NONE, null);
        AttributeFilterPolicy policy3 = new AttributeFilterPolicy("policy3", PolicyRequirementRule.MATCHES_NONE, null);

        AttributeFilter engine =
                new AttributeFilter("engine", Lists.<AttributeFilterPolicy> newArrayList(policy1, policy1,
                        policy2));
        engine.initialize();

        Assert.assertTrue(engine.isInitialized());
        Assert.assertEquals(engine.getFilterPolicies().size(), 3);
        Assert.assertTrue(engine.getFilterPolicies().contains(policy1));
        Assert.assertTrue(policy1.isInitialized());
        Assert.assertTrue(engine.getFilterPolicies().contains(policy2));
        Assert.assertTrue(policy2.isInitialized());
        Assert.assertFalse(engine.getFilterPolicies().contains(policy3));
        Assert.assertFalse(policy3.isInitialized());

        engine = new AttributeFilter("engine", Lists.<AttributeFilterPolicy> newArrayList(policy1, policy2));
        engine.initialize();

        Assert.assertEquals(engine.getFilterPolicies().size(), 2);
        List<AttributeFilterPolicy> contents = engine.getFilterPolicies();
        Assert.assertEquals(contents.get(0).getId(), "policy1");
        Assert.assertEquals(contents.get(1).getId(), "policy2");

        try {
            engine.getFilterPolicies().add(policy1);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // expected this
        }
    }

    /** Test filtering attributes. */
    @Test public void testFilterAttributes() throws Exception {
        MockMatcher attribute1Matcher = new MockMatcher();
        attribute1Matcher.setMatchingAttribute("attribute1");
        attribute1Matcher.setMatchingValues(null);

        AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(attribute1Matcher);
        attribute1Policy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Lists.newArrayList(attribute1Policy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Lists.<AttributeValue> newArrayList(new StringAttributeValue("one"),
                new StringAttributeValue("two")));
        filterContext.getPrefilteredAttributes().put(attribute1.getId(), attribute1);

        IdPAttribute attribute2 = new IdPAttribute("attribute2");
        attribute2.setValues(Lists.<AttributeValue> newArrayList(new StringAttributeValue("a"),
                new StringAttributeValue("b")));
        filterContext.getPrefilteredAttributes().put(attribute2.getId(), attribute2);

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        engine.initialize();

        engine.filterAttributes(filterContext);
        Map<String, IdPAttribute> resultAttrs = filterContext.getFilteredAttributes();
        Assert.assertEquals(resultAttrs.size(), 1);
        Set<AttributeValue> result = resultAttrs.get("attribute1").getValues();
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(new StringAttributeValue("one")));
        Assert.assertTrue(result.contains(new StringAttributeValue("two")));
    }

    @Test public void testAllMatcher() throws Exception {

        AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(Matcher.MATCHES_ALL);
        attribute1Policy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Lists.newArrayList(attribute1Policy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Lists.<AttributeValue> newArrayList(new StringAttributeValue("one"),
                new StringAttributeValue("two")));
        filterContext.getPrefilteredAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        engine.initialize();

        engine.filterAttributes(filterContext);
        Set<AttributeValue> result = filterContext.getFilteredAttributes().get("attribute1").getValues();
        Assert.assertEquals(result.size(), 2);
        Assert.assertTrue(result.contains(new StringAttributeValue("one")));
        Assert.assertTrue(result.contains(new StringAttributeValue("two")));
    }
    
    @Test public void testAllMatcherFails() throws Exception {

        AttributeRule attribute2Policy = new AttributeRule();
        attribute2Policy.setId("attribute2Policy");
        attribute2Policy.setAttributeId("attribute1");
        MockMatcher matcher = new MockMatcher();
        matcher.setFailValidate(true);
        attribute2Policy.setMatcher(matcher);
        attribute2Policy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Lists.newArrayList(attribute2Policy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Lists.<AttributeValue> newArrayList(new StringAttributeValue("one"),
                new StringAttributeValue("two")));
        filterContext.getPrefilteredAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        engine.initialize();

        engine.filterAttributes(filterContext);
        Assert.assertTrue(filterContext.getFilteredAttributes().isEmpty());
    }


    @Test public void testNoneMatcher() throws Exception {

        AttributeRule attribute1Policy = new AttributeRule();
        attribute1Policy.setId("attribute1Policy");
        attribute1Policy.setAttributeId("attribute1");
        attribute1Policy.setMatcher(Matcher.MATCHES_NONE);
        attribute1Policy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL,
                        Lists.newArrayList(attribute1Policy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Lists.<AttributeValue> newArrayList(new StringAttributeValue("one"),
                new StringAttributeValue("two")));
        filterContext.getPrefilteredAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        engine.initialize();

        engine.filterAttributes(filterContext);
        Assert.assertTrue(filterContext.getFilteredAttributes().isEmpty());
    }

    @Test public void testDenyFilterAttributes() throws Exception {
        MockMatcher deny = new MockMatcher();
        deny.setMatchingAttribute("attribute1");
        deny.setMatchingValues(Arrays.asList(new StringAttributeValue("one")));

        AttributeRule denyPolicy = new AttributeRule();
        denyPolicy.setId("denyPolicy");
        denyPolicy.setAttributeId("attribute1");
        denyPolicy.setMatcher(deny);
        denyPolicy.setIsDenyRule(true);

        AttributeRule allowPolicy = new AttributeRule();
        allowPolicy.setId("allowPolicy");
        allowPolicy.setAttributeId("attribute1");
        allowPolicy.setMatcher(Matcher.MATCHES_ALL);
        allowPolicy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL, Lists.newArrayList(denyPolicy,
                        allowPolicy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Lists.<AttributeValue> newArrayList(new StringAttributeValue("one"),
                new StringAttributeValue("two")));
        filterContext.getPrefilteredAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        engine.initialize();

        engine.filterAttributes(filterContext);
        Map<String, IdPAttribute> resultAttrs = filterContext.getFilteredAttributes();
        Assert.assertEquals(resultAttrs.size(), 1);
        Set<AttributeValue> result = resultAttrs.get("attribute1").getValues();
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(new StringAttributeValue("two")));
    }

    @Test public void testNoPolicy() throws Exception {
        AttributeRule allowPolicy = new AttributeRule();
        allowPolicy.setId("allowPolicy");
        allowPolicy.setAttributeId("attribute1");
        allowPolicy.setMatcher(Matcher.MATCHES_ALL);
        allowPolicy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_NONE, Lists.newArrayList(allowPolicy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Lists.<AttributeValue> newArrayList(new StringAttributeValue("one"),
                new StringAttributeValue("two")));
        filterContext.getPrefilteredAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        engine.initialize();

        engine.filterAttributes(filterContext);
        Assert.assertTrue(filterContext.getFilteredAttributes().isEmpty());
    }

    @Test public void testDenyAllFilterAttributes() throws Exception {
        AttributeRule denyPolicy = new AttributeRule();
        denyPolicy.setId("denyPolicy");
        denyPolicy.setAttributeId("attribute1");
        denyPolicy.setMatcher(Matcher.MATCHES_ALL);
        denyPolicy.setIsDenyRule(true);

        AttributeRule allowPolicy = new AttributeRule();
        allowPolicy.setId("allowPolicy");
        allowPolicy.setAttributeId("attribute1");
        allowPolicy.setMatcher(Matcher.MATCHES_ALL);
        allowPolicy.setIsDenyRule(false);

        AttributeFilterPolicy policy =
                new AttributeFilterPolicy("attribute1Policy", PolicyRequirementRule.MATCHES_ALL, Lists.newArrayList(denyPolicy,
                        allowPolicy));

        AttributeFilterContext filterContext = new AttributeFilterContext();

        IdPAttribute attribute1 = new IdPAttribute("attribute1");
        attribute1.setValues(Lists.<AttributeValue> newArrayList(new StringAttributeValue("one"),
                new StringAttributeValue("two")));
        filterContext.getPrefilteredAttributes().put(attribute1.getId(), attribute1);

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        engine.initialize();

        engine.filterAttributes(filterContext);
        Map<String, IdPAttribute> resultAttrs = filterContext.getFilteredAttributes();
        Assert.assertTrue(resultAttrs.isEmpty());
    }

    @Test public void testInitDestroy() throws ComponentInitializationException {
        MockMatcher matcher = new MockMatcher();
        AttributeRule filterPolicy = new AttributeRule();
        filterPolicy.setId("filterPolicy");
        filterPolicy.setAttributeId("attribute1");
        filterPolicy.setMatcher(matcher);
        filterPolicy.setIsDenyRule(false);

        MockPolicyRequirementRule policyRule = new MockPolicyRequirementRule();
        AttributeFilterPolicy policy = new AttributeFilterPolicy("policy", policyRule, Arrays.asList(filterPolicy));

        Assert.assertFalse(policyRule.isInitialized());
        Assert.assertFalse(policyRule.isDestroyed());
        Assert.assertFalse(matcher.isInitialized());
        Assert.assertFalse(matcher.isDestroyed());

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        engine.initialize();

        Assert.assertTrue(policyRule.isInitialized());
        Assert.assertFalse(policyRule.isDestroyed());
        Assert.assertTrue(matcher.isInitialized());
        Assert.assertFalse(matcher.isDestroyed());

        engine.destroy();
        Assert.assertTrue(policyRule.isInitialized());
        Assert.assertTrue(policyRule.isDestroyed());
        Assert.assertTrue(matcher.isInitialized());
        Assert.assertTrue(matcher.isDestroyed());

        try {
            engine.initialize();
            Assert.fail();
        } catch (DestroyedComponentException e) {
            // OK
        }
    }

    @Test public void testValidate() throws ComponentInitializationException, ComponentValidationException {
        MockMatcher matcher = new MockMatcher();
        AttributeRule filterPolicy = new AttributeRule();
        filterPolicy.setId("filterPolicy");
        filterPolicy.setAttributeId("attribute1");
        filterPolicy.setMatcher(matcher);
        filterPolicy.setIsDenyRule(false);

        MockPolicyRequirementRule policyRule = new MockPolicyRequirementRule();
        AttributeFilterPolicy policy = new AttributeFilterPolicy("Id", policyRule, Arrays.asList(filterPolicy));

        AttributeFilter engine = new AttributeFilter("engine", Lists.newArrayList(policy));
        Assert.assertFalse(policyRule.getValidated());
        Assert.assertFalse(matcher.getValidated());

        try {
            engine.validate();
            Assert.fail();
        } catch (UninitializedComponentException e) {
            // OK
        }
        Assert.assertFalse(policyRule.getValidated());
        Assert.assertFalse(matcher.getValidated());

        engine.initialize();
        engine.validate();
        Assert.assertTrue(policyRule.getValidated());
        Assert.assertTrue(matcher.getValidated());

        policyRule.setFailValidate(true);
        try {
            engine.validate();
            Assert.fail();
        } catch (ComponentValidationException e) {
            // OK
        }

    }
}