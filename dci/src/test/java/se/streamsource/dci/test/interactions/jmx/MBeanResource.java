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

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * JAVADOC
 */
public class MBeanResource
      extends CommandQueryResource
      implements SubResources
{
   public MBeanResource()
   {
      super( MBeanContext.class );
   }

   public LinksValue index()
   {
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      for (MBeanAttributeInfo attribute : context(MBeanContext.class).index())
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

   public void resource( String segment ) throws ResourceException
   {
      for (MBeanAttributeInfo mBeanAttributeInfo : RoleMap.role( MBeanInfo.class ).getAttributes())
      {
         if (mBeanAttributeInfo.getName().equals( segment ))
            RoleMap.current().set( mBeanAttributeInfo );
      }

      subResourceContexts( MBeanAttributeContext.class );
   }
}
