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

package net.shibboleth.idp.attribute.filter.spring.basic;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import net.shibboleth.idp.attribute.filter.Matcher;
import net.shibboleth.idp.attribute.filter.PolicyRequirementRule;
import net.shibboleth.idp.attribute.filter.matcher.logic.impl.AndMatcher;
import net.shibboleth.idp.attribute.filter.matcher.logic.impl.NotMatcher;
import net.shibboleth.idp.attribute.filter.policyrule.logic.impl.AndPolicyRule;
import net.shibboleth.idp.attribute.filter.policyrule.logic.impl.NotPolicyRule;
import net.shibboleth.idp.attribute.filter.spring.basic.impl.AndMatcherParser;
import net.shibboleth.idp.attribute.filter.spring.testing.BaseAttributeFilterParserTest;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 * test for {@link AndMatcherParser}.
 */
@SuppressWarnings("javadoc")
public class AndTest extends BaseAttributeFilterParserTest {

    @Test public void matcher() throws ComponentInitializationException {
        AndMatcher what = (AndMatcher) getMatcher("and.xml");
        final List<Matcher> children = what.getComposedMatchers();
        
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getClass(), NotMatcher.class);
        assertEquals(children.get(1).getClass(), Matcher.MATCHES_ALL.getClass());
    }

    @Test public void policy() throws ComponentInitializationException {
        AndPolicyRule what = (AndPolicyRule) getPolicyRule("and.xml");
        
        final List<PolicyRequirementRule> children = what.getComposedRules();
        assertEquals(children.size(), 2);
        assertEquals(children.get(0).getClass(), NotPolicyRule.class);
        assertEquals(children.get(1).getClass(), PolicyRequirementRule.MATCHES_ALL.getClass());
    }
    
    @Test public void matcherSingleton() throws ComponentInitializationException {
        AndMatcher what = (AndMatcher) getMatcher("andSingleton.xml");
        
        final List<Matcher> children = what.getComposedMatchers();
        
        assertEquals(children.size(), 1);
        assertEquals(children.get(0).getClass(), Matcher.MATCHES_ALL.getClass());
    }

    @Test public void policySingleton() throws ComponentInitializationException {
        AndPolicyRule what = (AndPolicyRule) getPolicyRule("andSingleton.xml");
        
        final List<PolicyRequirementRule> children = what.getComposedRules();
        assertEquals(children.size(), 1);
        assertEquals(children.get(0).getClass(), NotPolicyRule.class);
    
    }
    
}
