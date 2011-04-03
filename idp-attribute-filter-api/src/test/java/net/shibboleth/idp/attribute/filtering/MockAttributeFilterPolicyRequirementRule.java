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

package net.shibboleth.idp.attribute.filtering;

/** Simple, mock implementation of {@link AttributeFilterPolicyRequirementRule}. */
public class MockAttributeFilterPolicyRequirementRule implements AttributeFilterPolicyRequirementRule {

    /** Whether this rule is satisfied or not. */
    private boolean satisfied;

    /** {@inheritDoc} */
    public boolean isSatisfied(AttributeFilterContext filterContext) throws AttributeFilteringException {
        return satisfied;
    }

    /**
     * Sets whether this rule is satisfied or not.
     * 
     * @param isSatisfied true if this rule is satisfied, false otherwise
     */
    public void setSatisfied(boolean isSatisfied) {
        satisfied = isSatisfied;
    }
}