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

package net.shibboleth.idp.attribute.mapper.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.shibboleth.idp.attribute.AttributeValue;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.ScopedStringAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.attribute.XMLObjectAttributeValue;
import net.shibboleth.idp.attribute.mapper.impl.ByteAttributeValueMapper;
import net.shibboleth.idp.attribute.mapper.impl.ScopedStringAttributeValueMapper;
import net.shibboleth.idp.attribute.mapper.impl.StringAttributeValueMapper;
import net.shibboleth.idp.attribute.mapper.impl.XmlObjectAttributeValueMapper;
import net.shibboleth.utilities.java.support.codec.Base64Support;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link AttributeValueMapper}.
 */
public class AttributeValueMapperTest extends MappingTests {

    private List<RequestedAttribute> attributes;

    private void loadAttributes() {
        if (null != attributes) {
            return;
        }
        attributes = loadFile("requestedAttributeValues.xml");
    }

    private List<XMLObject> getValues(String attributeName) {
        loadAttributes();
        for (RequestedAttribute attribute : attributes) {
            if (attributeName.equals(attribute.getName())) {
                return attribute.getAttributeValues();
            }
        }
        return null;
    }

    @Test public void testString() throws ComponentInitializationException {

        final StringAttributeValueMapper mapper = new StringAttributeValueMapper();

        mapper.setLogPrefix("testString");
        mapper.initialize();
        final List<AttributeValue<?>> result = mapper.decodeValues(getValues("stdFormat"));

        Assert.assertEquals(result.size(), 4);
        final Set<String> strings = new HashSet(4);

        for (AttributeValue val : result) {
            strings.add(((StringAttributeValue) val).getValue());
        }
        Assert.assertTrue(strings.contains("String"));
        Assert.assertTrue(strings.contains("1"));
        Assert.assertTrue(strings.contains("U2NvdCBDYW50b3IgaXMgdGhlIGdvZCBvZiBTQU1M"));
        Assert.assertTrue(strings.contains("scope@scoped.example.org"));
    }

    @Test public void testScopedString() throws ComponentInitializationException {

        ScopedStringAttributeValueMapper mapper = new ScopedStringAttributeValueMapper();

        mapper.setLogPrefix("testScopedString");
        mapper.initialize();
        List<AttributeValue<?>> result = mapper.decodeValues(getValues("stdFormat"));
        Assert.assertEquals(result.size(), 1);
        ScopedStringAttributeValue value = (ScopedStringAttributeValue) result.get(0);
        Assert.assertEquals(value.getValue(), "scope");
        Assert.assertEquals(value.getScope(), "scoped.example.org");

        mapper = new ScopedStringAttributeValueMapper();
        mapper.setDelimiter("0b3IgaX");
        mapper.setLogPrefix("testScopedString2");
        mapper.initialize();
        result = mapper.decodeValues(getValues("stdFormat"));
        Assert.assertEquals(result.size(), 1);
        value = (ScopedStringAttributeValue) result.get(0);
        Assert.assertEquals(value.getValue(), "U2NvdCBDYW5");
        Assert.assertEquals(value.getScope(), "MgdGhlIGdvZCBvZiBTQU1M");

        result = mapper.decodeValues(getValues("urn:oid:0.9.2342.19200300.100.1.3"));
        Assert.assertTrue(result.isEmpty());
    }

    @Test public void testByte() throws ComponentInitializationException {

        ByteAttributeValueMapper mapper = new ByteAttributeValueMapper();
        
        mapper.setLogPrefix("testByte");
        mapper.initialize();
        final List<AttributeValue<?>> result = mapper.decodeValues(getValues("stdFormat"));
        Assert.assertEquals(result.size(), 4);
        final Set<String> strings = new HashSet(4);
        
        for (AttributeValue val: result) {
            final String s = Base64Support.encode(((ByteAttributeValue)val).getValue(), Base64Support.CHUNKED);
            strings.add(s);
        }
        Assert.assertTrue(strings.contains("U2NvdCBDYW50b3IgaXMgdGhlIGdvZCBvZiBTQU1M"));        
    }
    
    @Test public void testXMLObject() throws ComponentInitializationException {

        XmlObjectAttributeValueMapper mapper = new XmlObjectAttributeValueMapper();
        
        mapper.setLogPrefix("testXMLObject");
        mapper.initialize();
        final List<AttributeValue<?>> result = mapper.decodeValues(getValues("stdFormat"));
        Assert.assertEquals(result.size(), 1);
        XMLObject object = ((XMLObjectAttributeValue) result.get(0)).getValue();
        
        Assert.assertTrue(object instanceof UIInfo);
    }
}
