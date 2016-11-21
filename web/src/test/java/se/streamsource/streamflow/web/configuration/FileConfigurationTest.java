/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.configuration;

import java.io.File;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

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
      module.layer().application().setName("StreamFlowServer");
      module.services(FileConfiguration.class);
      module.objects(getClass());
   }

   @Test
   public void testMac()
   {
      objectBuilderFactory.newObjectBuilder(FileConfigurationTest.class).injectTo(this);

      System.setProperty("os.name", "Mac OS X");

      File user = config.user();
      Assert.assertThat("OS is correct", config.os(), CoreMatchers.equalTo(FileConfiguration.OS.mac));
      Assert.assertThat("configuration is correct", config.configurationDirectory(), CoreMatchers.equalTo(new File(user, "/Library/Preferences/StreamFlowServer-test")));
   }

   @Test
   public void testLinux()
   {
      objectBuilderFactory.newObjectBuilder(FileConfigurationTest.class).injectTo(this);
      System.setProperty("os.name", "Linux");

      File user = config.user();
      Assert.assertThat("OS is correct", config.os(), CoreMatchers.equalTo(FileConfiguration.OS.unix));
      Assert.assertThat("configuration is correct", config.configurationDirectory(), CoreMatchers.equalTo(new File(user, "/.StreamFlowServer-test/etc")));
   }
}
