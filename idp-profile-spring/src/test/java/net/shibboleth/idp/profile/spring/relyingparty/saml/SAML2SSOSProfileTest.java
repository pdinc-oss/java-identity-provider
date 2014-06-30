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

package net.shibboleth.idp.profile.spring.relyingparty.saml;

import net.shibboleth.idp.saml.idwsf.profile.config.SSOSProfileConfiguration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SAML2SSOSProfileTest extends BaseSAMLProfileTest {

    @Test public void defaults() {

        SSOSProfileConfiguration profile = getBean(SSOSProfileConfiguration.class, "saml/ssos.xml", "beans.xml");

        Assert.assertEquals(profile.getMaximumTokenDelegationChainLength(), 0);
        Assert.assertFalse(profile.getDelegationPredicate().apply(null));
    }

    @Test public void values() {
        SSOSProfileConfiguration profile =
                getBean(SSOSProfileConfiguration.class, "saml/ssosValues.xml", "beans.xml");

        Assert.assertEquals(profile.getMaximumTokenDelegationChainLength(), 99);
        Assert.assertTrue(profile.getDelegationPredicate().apply(null));
    }
}