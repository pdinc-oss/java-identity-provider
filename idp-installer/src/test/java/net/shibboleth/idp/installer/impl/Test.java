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

package net.shibboleth.idp.installer.impl;

import java.io.IOException;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.shibboleth.idp.installer.impl.BuildWar;
import net.shibboleth.idp.installer.impl.CopyDistribution;
import net.shibboleth.idp.installer.impl.InstallerProperties;
import net.shibboleth.idp.installer.impl.V4Install;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

/**
 *
 */
public class Test {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(Test.class);
   
    /**
     * @param args
     * @throws IOException 
     * @throws ComponentInitializationException 
     */
    public static void main(String[] args) throws IOException, ComponentInitializationException {

        
        System.setProperty(InstallerProperties.TARGET_DIR,"H:\\Downloads\\v4test");
        System.setProperty(InstallerProperties.SOURCE_DIR,
                "h:\\Perforce\\Juno\\New\\java-identity-provider\\idp-distribution\\target\\shibboleth-identity-provider-4.0.0-SNAPSHOT");
        System.setProperty(InstallerProperties.ANT_BASE_DIR,
                "h:\\Perforce\\Juno\\New\\java-identity-provider\\idp-distribution\\target\\shibboleth-identity-provider-4.0.0-SNAPSHOT\\bin");
        System.setProperty(InstallerProperties.KEY_STORE_PASSWORD, "p1");
        System.setProperty(InstallerProperties.SEALER_PASSWORD, "p1");
        final InstallerProperties ip = new InstallerProperties(false);
        ip.initialize();
        final CurrentInstallState is = new CurrentInstallState(ip);
        is.initialize();
        
        final CopyDistribution dist = new CopyDistribution(ip, is);
        dist.execute();
        
        final V4Install inst = new V4Install(ip, is);
        inst.execute();
        
        final BuildWar bw = new BuildWar(ip, is);
        bw.execute();
        
    }

}