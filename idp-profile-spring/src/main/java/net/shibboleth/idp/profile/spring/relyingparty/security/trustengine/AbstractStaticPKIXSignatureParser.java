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

package net.shibboleth.idp.profile.spring.relyingparty.security.trustengine;

import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;

import net.shibboleth.ext.spring.util.SpringSupport;
import net.shibboleth.idp.profile.spring.relyingparty.security.SecurityNamespaceHandler;
import net.shibboleth.utilities.java.support.xml.ElementSupport;

import org.opensaml.security.x509.impl.CertPathPKIXTrustEvaluator;
import org.opensaml.security.x509.impl.StaticPKIXValidationInformationResolver;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Base Parser for trust engines of type StaticPKIXKeySignature and StaticPKIXX509Credential.
 */
public abstract class AbstractStaticPKIXSignatureParser extends AbstractTrustEngineParser {

    /** Validation Information. */
    public static final QName VALIDATION_INFO = new QName(SecurityNamespaceHandler.NAMESPACE, "ValidationInfo");

    /** Trusted Names Information. */
    public static final QName TRUSTED_NAMES = new QName(SecurityNamespaceHandler.NAMESPACE, "TrustedName");

    /**
     * Get the definition for the {@link org.opensaml.security.x509.PKIXValidationInformationResolver}. This is
     * constructed from the Trusted names and the Validation Info.
     * 
     * @param element what to parse
     * @param parserContext the context to parse inside
     * @return the definition
     */
    protected BeanDefinition getPKIXValidationInformationResolver(Element element,
            @Nonnull final ParserContext parserContext) {

        final List<Element> validationInfoElements = ElementSupport.getChildElements(element, VALIDATION_INFO);
        final List<Element> trustedNameElements = ElementSupport.getChildElements(element, TRUSTED_NAMES);

        BeanDefinitionBuilder builder =
                BeanDefinitionBuilder.genericBeanDefinition(StaticPKIXValidationInformationResolver.class);
        builder.addConstructorArgValue(SpringSupport.parseCustomElements(validationInfoElements, parserContext));
        builder.addConstructorArgValue(SpringSupport.getElementTextContentAsManagedList(trustedNameElements));

        return builder.getBeanDefinition();
    }

    /**
     * Get the definition for the {@link org.opensaml.security.x509.PKIXTrustEvaluator}. This is purely a
     * {@link CertPathPKIXTrustEvaluator} constructed with whatever ValidationOptions we provide.
     * 
     * @param element what to parse
     * @param parserContext the context to parse inside
     * @return the definition
     */
    protected BeanDefinition getPKIXTrustEvaluator(Element element, @Nonnull final ParserContext parserContext) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CertPathPKIXTrustEvaluator.class);

        final List<Element> validationOptionsElements =
                ElementSupport.getChildElements(element, PKIXValidationOptionsParser.ELEMENT_NAME);

        if (null != validationOptionsElements && !validationOptionsElements.isEmpty()) {
            builder.addConstructorArgValue(SpringSupport.parseCustomElements(validationOptionsElements, parserContext));
        }
        return builder.getBeanDefinition();
    }

}