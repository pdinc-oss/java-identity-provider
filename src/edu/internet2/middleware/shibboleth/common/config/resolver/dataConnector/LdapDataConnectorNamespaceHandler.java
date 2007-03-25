/*
 * Copyright [2007] [University Corporation for Advanced Internet Development, Inc.]
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

package edu.internet2.middleware.shibboleth.common.config.resolver.dataConnector;

import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;

/**
 * A namespace handlering for the LDAP data connector.
 */
public class LdapDataConnectorNamespaceHandler extends BaseSpringNamespaceHandler {

    /** Namespace for the LDAP data connector. */
    public static final String NAMESPACE = "urn:mace:shibboleth:2.0:resolver:dc:ldap";

    /** {@inheritDoc} */
    public void init() {
        registerBeanDefinitionParser(LdapDataConnectorBeanDefinitionParser.TYPE_NAME,
                new LdapDataConnectorBeanDefinitionParser());
    }
}