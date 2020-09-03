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

package net.shibboleth.idp.cli;

import javax.annotation.Nonnull;

import net.shibboleth.ext.spring.cli.AbstractCommandLine;
import net.shibboleth.idp.spring.IdPPropertiesApplicationContextInitializer;

/**
 * An extension to {@link AbstractCommandLine} that auto-adds our context initializer for idp.home
 * and property support.
 *
 * @param <T> argument object type
 * 
 * @since 4.1.0
 */
public abstract class AbstractIdPHomeAwareCommandLine<T extends AbstractIdPHomeAwareCommandLineArguments>
        extends AbstractCommandLine<T> {
    
    /**
     * Constructor.
     */
    protected AbstractIdPHomeAwareCommandLine() {
    }

    /** {@inheritDoc} */
    @Override
    protected int doRun(@Nonnull final T args) {
        if (args.getIdPHome() != null) {
            System.setProperty("idp.home", args.getIdPHome());
        }
        setContextInitializer(new IdPPropertiesApplicationContextInitializer());
        return super.doRun(args);
    }
    
}