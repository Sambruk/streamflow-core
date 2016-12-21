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
package se.streamsource.dci.test.interactions.jmx;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;

import javax.management.MBeanServer;

import static org.qi4j.api.util.Iterables.iterable;

/**
 * JAVADOC
 */
public class JmxServerContext
      implements IndexContext<Iterable<String>>
{
   @Structure
   Module module;

   public Iterable<String> index()
   {
      return iterable(RoleMap.role(MBeanServer.class).getDomains());
   }
}
