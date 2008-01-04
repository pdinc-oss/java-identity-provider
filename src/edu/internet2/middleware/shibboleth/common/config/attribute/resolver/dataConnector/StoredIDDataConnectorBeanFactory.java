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

package edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector;

import javax.sql.DataSource;

import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.StoredIDDataConnector;

/** Spring factory bean for {@link StoredIDDataConnector}s. */
public class StoredIDDataConnectorBeanFactory extends BaseDataConnectorFactoryBean {

    /** Datasource used to communicate with database. */
    private DataSource datasource;

    /** ID of the attribute generated by the connector. */
    private String generatedAttribute;

    /** ID of the attribute whose first value is used when generating the computed ID. */
    private String sourceAttribute;

    /** Salt used when computing the ID. */
    private byte[] salt;

    /** {@inheritDoc} */
    public Class getObjectType() {
        return StoredIDDataConnector.class;
    }

    /**
     * Gets the datasource used to communicate with database.
     * 
     * @return datasource used to communicate with database
     */
    public DataSource getDatasource() {
        return datasource;
    }

    /**
     * Sets the datasource used to communicate with database.
     * 
     * @param source datasource used to communicate with database
     */
    public void setDatasource(DataSource source) {
        datasource = source;
    }

    /**
     * Gets the ID of the attribute generated by the connector.
     * 
     * @return ID of the attribute generated by the connector
     */
    public String getGeneratedAttribute() {
        return generatedAttribute;
    }

    /**
     * Sets the ID of the attribute generated by the connector.
     * 
     * @param id ID of the attribute generated by the connector
     */
    public void setGeneratedAttribute(String id) {
        generatedAttribute = id;
    }

    /**
     * Gets the ID of the attribute whose first value is used when generating the computed ID.
     * 
     * @return ID of the attribute whose first value is used when generating the computed ID
     */
    public String getSourceAttribute() {
        return sourceAttribute;
    }

    /**
     * Sets the ID of the attribute whose first value is used when generating the computed ID.
     * 
     * @param id ID of the attribute whose first value is used when generating the computed ID
     */
    public void setSourceAttribute(String id) {
        this.sourceAttribute = id;
    }

    /**
     * Gets the salt used when computing the ID.
     * 
     * @return salt used when computing the ID
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * Sets the salt used when computing the ID.
     * 
     * @param salt salt used when computing the ID
     */
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
        StoredIDDataConnector connector = new StoredIDDataConnector(getDatasource(), getGeneratedAttribute(),
                getSourceAttribute(), getSalt());
        populateDataConnector(connector);
        return connector;
    }
}