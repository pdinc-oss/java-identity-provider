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

package net.shibboleth.idp.saml.attribute.encoding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import net.shibboleth.idp.attribute.Attribute;
import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.AttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.OpenSAMLInitBaseTestCase;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

/**
 * test for {@link AbstractSaml1AttributeEncoder
 */
public class AbstractSaml1AttributeEncoderTest extends OpenSAMLInitBaseTestCase {

    private XSStringBuilder theBuilder;

    private QName theQName = new QName("LocalQNAME");

    private final String MY_NAME = "myName";

    private final String MY_NAMESPACE = "myNameSpace";

    private final String ATTRIBUTE_ID = "attrID";

    private final String ATTRIBUTE_VALUE_1 = "attrValOne";

    private final String ATTRIBUTE_VALUE_2 = "attrValueTwo";
    
    @BeforeClass public void initTest() throws ComponentInitializationException {
        theBuilder = new XSStringBuilder();
    }

    @Test public void testAbstractSaml1AttributeEncoder() throws ComponentInitializationException, AttributeEncodingException {
        AbstractSaml1AttributeEncoder encoder = new AbstractSamlAttributeEncoderTest.mockEncoder(theBuilder, theQName);
        encoder.setName(MY_NAME);
        encoder.setNamespace(MY_NAMESPACE);
        encoder.initialize();
        // Use literal here to catch things being edited by mistake
        Assert.assertEquals(encoder.getProtocol(), "urn:oasis:names:tc:SAML:1.1:protocol");

                
        Attribute attr = new Attribute(ATTRIBUTE_ID);
        attr.setValues(Lists.newArrayList((AttributeValue) new StringAttributeValue(ATTRIBUTE_VALUE_1),
                new StringAttributeValue(ATTRIBUTE_VALUE_2)));
        
        XMLObject obj = encoder.encode(attr);
        
        Assert.assertTrue(obj instanceof org.opensaml.saml.saml1.core.Attribute);
        
        List<XMLObject> result = ((org.opensaml.saml.saml1.core.Attribute) obj).getAttributeValues();
        
        Assert.assertEquals(result.size(), 2);
        Set<String> resultSet = new HashSet<String>(2); 
        for (XMLObject o: result) {
            Assert.assertTrue(o instanceof XSString);
            resultSet.add(((XSString) o).getValue());
        }
        Assert.assertTrue(resultSet.contains(ATTRIBUTE_VALUE_1));
        Assert.assertTrue(resultSet.contains(ATTRIBUTE_VALUE_2));
    }
}
