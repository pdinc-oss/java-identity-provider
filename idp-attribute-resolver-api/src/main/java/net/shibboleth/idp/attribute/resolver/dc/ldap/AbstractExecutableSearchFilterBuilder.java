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

package net.shibboleth.idp.attribute.resolver.dc.ldap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.ldaptive.ConnectionFactory;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapException;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.attribute.resolver.dc.ExecutableSearchBuilder;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;

/**
 * Abstract executable search filter builder for implementations that can provide a search filter.
 */
public abstract class AbstractExecutableSearchFilterBuilder extends AbstractInitializableComponent 
    implements ExecutableSearchBuilder<ExecutableSearchFilter> {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(AbstractExecutableSearchFilterBuilder.class);

    /**
     * Creates an executable search filter that performs a search with the search filter.
     * 
     * @param searchFilter to execute a search with
     * @return executable search filter
     */
    // Checkstyle: AnonInnerLength OFF
    protected ExecutableSearchFilter build(@Nonnull final FilterTemplate searchFilter) {
        return new ExecutableSearchFilter() {

            /** {@inheritDoc} */
            @Nullable public String getResultCacheKey() {
                return searchFilter.format();
            }

            /** {@inheritDoc} */
            @Nonnull public SearchResponse execute(@Nonnull final SearchOperation operation,
                    @Nonnull final ConnectionFactory factory) throws LdapException {
                final SearchOperation op = SearchOperation.copy(operation);
                op.setConnectionFactory(factory);
                final SearchResponse response = op.execute(searchFilter);
                log.trace("Search returned response {}", response);
                if (response.getResultCode() != ResultCode.SUCCESS) {
                    // It's possible for the LDAP to return partial results and report either a size limit or
                    // time limit result code. Throw if we don't receive all results.
                    throw new LdapException("Search operation did not return success: " + response.getResultCode());
                }
                return response;
            }

            /** {@inheritDoc} */
            @Nonnull public FilterTemplate getSearchFilter() {
                return searchFilter;
            }

            /** {@inheritDoc} */
            public String toString() {
                return searchFilter.toString();
            }
        };
    }
    // Checkstyle: AnonInnerLength ON
}