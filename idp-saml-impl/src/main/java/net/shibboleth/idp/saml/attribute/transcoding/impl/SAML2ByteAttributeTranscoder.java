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

package net.shibboleth.idp.saml.attribute.transcoding.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.idp.attribute.AttributeEncodingException;
import net.shibboleth.idp.attribute.ByteAttributeValue;
import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.transcoding.TranscodingRule;
import net.shibboleth.idp.saml.attribute.transcoding.AbstractSAML2AttributeTranscoder;
import net.shibboleth.idp.saml.attribute.transcoding.SAMLEncoderSupport;
import net.shibboleth.utilities.java.support.codec.Base64Support;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.profile.context.ProfileRequestContext;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link net.shibboleth.idp.attribute.AttributeTranscoder} that supports {@link Attribute} and
 * {@link ByteAttributeValue} objects.
 */
public class SAML2ByteAttributeTranscoder extends AbstractSAML2AttributeTranscoder<ByteAttributeValue> {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(SAML2ByteAttributeTranscoder.class);

    /** {@inheritDoc} */
    @Override protected boolean canEncodeValue(@Nonnull final IdPAttribute attribute,
            @Nonnull final IdPAttributeValue value) {
        return value instanceof ByteAttributeValue;
    }

    /** {@inheritDoc} */
    @Override @Nullable protected XMLObject encodeValue(@Nullable final ProfileRequestContext profileRequestContext,
            @Nonnull final IdPAttribute attribute, @Nonnull final TranscodingRule rule,
            @Nonnull final ByteAttributeValue value) throws AttributeEncodingException {
                
        final Boolean encodeType = rule.getOrDefault(PROP_ENCODE_TYPE, Boolean.class, Boolean.TRUE);

        return SAMLEncoderSupport.encodeByteArrayValue(attribute, AttributeValue.DEFAULT_ELEMENT_NAME, value.getValue(),
                encodeType);
    }

    /** {@inheritDoc} */
    @Override @Nullable protected IdPAttributeValue<?> decodeValue(
            @Nullable final ProfileRequestContext profileRequestContext, @Nonnull final Attribute attribute,
            @Nonnull final TranscodingRule rule, @Nullable final XMLObject value) {
        
        final String s = getStringValue(value);
        if (null == s) {
            return null;
        }
        
        final byte[] decoded = Base64Support.decode(s);
        if (decoded.length == 0) {
            log.warn("Ignoring non-base64-encoded value");
            return null;
        }
        
        return ByteAttributeValue.valueOf(decoded);
    }
    
}