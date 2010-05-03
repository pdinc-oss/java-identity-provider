/*
 * Copyright 2009 University Corporation for Advanced Internet Development, Inc.
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class Attribute {

    final private String id;

    final private Collection<String> values;

    final private int valueHash;
    
    /** Localized human intelligible attribute name. */
    private Map<Locale, String> displayNames;
    
    /** Localized human readable description of attribute. */
    private Map<Locale, String> displayDescriptions;

    
    public Attribute(final String id, final int valueHash) {
    	this.id = id;
    	this.valueHash = valueHash;
    	values = null;
    }
    
    public Attribute(final String id, final Collection<String> values) {
    	this.id = id;
    	this.values = values;
    	this.valueHash = values.hashCode();    	
    }
    
    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @return Returns the hash of the values.
     */
    public int getValueHash() {
        return valueHash;
    }

    /**
     * @return Returns the values.
     */
    public Collection<String> getValues() {
        return values;
    }

    public String getName(final Locale locale) {
        return this.displayNames.get(locale);
    }
    
    public String getDescription(final Locale locale) {
        return this.displayDescriptions.get(locale);
    }
    


    /** {@inheritDoc} */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + valueHash;
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
		Attribute other = (Attribute) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (valueHash != other.valueHash)
			return false;
		return true;
	}

	/** {@inheritDoc} */
    @Override
    public String toString() {
        return "Attribute [id=" + id + ", valueHash=" + valueHash + "]";
    }

}
