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
package se.streamsource.dci.test.interactions;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.test.interactions.file.FileResource;
import se.streamsource.dci.test.interactions.jmx.JmxServerResource;

import java.io.File;
import java.lang.management.ManagementFactory;

/**
 * JAVADOC
 */
public class RootResource
   extends CommandQueryResource
{
   public RootResource()
   {
   }

   @SubResource
   public void files( )
   {
      RoleMap.current().set( new File("").getAbsoluteFile() );
      subResource( FileResource.class );
   }

   @SubResource
   public void jmx()
   {
      RoleMap.current().set( ManagementFactory.getPlatformMBeanServer() );
      subResource( JmxServerResource.class );
   }
}
