/*
z * Copyright 2009 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.shibboleth.idp.consent.entities;

import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class RelyingParty {
	final private long id;

    final private String entityId;
    
	private String displayName;
    
    private String displayDescription;

    public RelyingParty(final long id, final String entityId) {
    	this.id = id;
    	this.entityId = entityId;
    }
    
    /**
     * @return Returns the entityId.
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * @return Returns the id.
     */
    public long getId() {
        return id;
    }

    /**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return Returns the displayDescription.
	 */
	public String getDisplayDescription() {
		return displayDescription;
	}

	/**
	 * @param displayDescription The displayDescription to set.
	 */
	public void setDisplayDescription(String displayDescription) {
		this.displayDescription = displayDescription;
	}
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "RelyingParty [id=" + id + ", entityId=" + entityId + "]";
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RelyingParty other = (RelyingParty) obj;
        if (entityId == null) {
            if (other.entityId != null)
                return false;
        } else if (!entityId.equals(other.entityId))
            return false;
        if (id != other.id)
            return false;
        return true;
    }
}
