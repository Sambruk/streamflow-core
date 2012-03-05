/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.ContextNotFoundException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.link.LinksBuilder;
import se.streamsource.dci.value.link.LinksValue;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * JAVADOC
 */
public class JmxServerResource
   extends CommandQueryResource
   implements SubResources
{
   public JmxServerResource( )
   {
      super( JmxServerContext.class );
   }

   public LinksValue index()
   {
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      for (String domain : context(JmxServerContext.class).index())
      {
         builder.addLink( domain, domain );
      }

      return builder.newLinks();
   }

   public void resource( String segment ) throws ResourceException
   {
      try
      {
         RoleMap.current().set( new ObjectName(segment+":*") );
         subResource( DomainResource.class );
      } catch (MalformedObjectNameException e)
      {
         throw new ContextNotFoundException();
      }
   }
}
