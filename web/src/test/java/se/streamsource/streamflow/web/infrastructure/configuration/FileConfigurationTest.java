/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.infrastructure.configuration;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

import java.io.File;

/**
 * Test of FileConfiguration service
 */
public class FileConfigurationTest
        extends AbstractQi4jTest
{
    @Service
    FileConfiguration config;

    public void assemble(ModuleAssembly module) throws AssemblyException
    {
        module.addServices(FileConfiguration.class);
        module.addObjects(getClass());
    }

    @Test
    public void testMac()
    {
        objectBuilderFactory.newObjectBuilder(FileConfigurationTest.class).injectTo(this);

        System.setProperty("os.name", "Mac OS X");

        String user = System.getProperty("user.home");
        Assert.assertThat("OS is correct", config.os(), CoreMatchers.equalTo(FileConfiguration.OS.mac));
        Assert.assertThat("configuration is correct", config.configurationDirectory(), CoreMatchers.equalTo(new File(user + "/Library/Preferences/StreamFlowServer")));
    }

    @Test
    public void testLinux()
    {
        objectBuilderFactory.newObjectBuilder(FileConfigurationTest.class).injectTo(this);
        System.setProperty("os.name", "Linux");

        String user = System.getProperty("user.home");
        Assert.assertThat("OS is correct", config.os(), CoreMatchers.equalTo(FileConfiguration.OS.unix));
        Assert.assertThat("configuration is correct", config.configurationDirectory(), CoreMatchers.equalTo(new File(user+"/.StreamFlowServer/etc")));
    }
}
