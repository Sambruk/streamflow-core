/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.dci.test.interactions.jmx;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.structure.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.link.*;

import javax.management.*;

/**
 * JAVADOC
 */
public class MBeanContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      MBeanAttributeInfo[] attributes = RoleMap.role( MBeanInfo.class ).getAttributes();
      for (MBeanAttributeInfo attribute : attributes)
      {
         try
         {
            Object value = RoleMap.role( MBeanServer.class ).getAttribute( RoleMap.role( ObjectName.class ), attribute.getName() );
            builder.addLink( attribute.getDescription() + (value instanceof String ? "=" + value.toString() : ""), attribute.getName() );
         } catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      return builder.newLinks();
   }
}
