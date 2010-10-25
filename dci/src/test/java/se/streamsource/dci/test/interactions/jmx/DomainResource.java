/*
 * Copyright 2009-2010 Streamsource AB
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

import org.qi4j.api.injection.scope.Uses;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.SubResources;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * JAVADOC
 */
public class DomainResource
      extends CommandQueryResource
      implements SubResources
{
   public DomainResource( )
   {
      super( DomainContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      try
      {
         ObjectName mbeanName = new ObjectName( RoleMap.role( ObjectName.class ).getDomain() + ":" + segment );

         RoleMap.current().set( mbeanName );
         RoleMap.current().set( RoleMap.role( MBeanServer.class ).getMBeanInfo( mbeanName ) );

         subResource( MBeanResource.class );
      } catch (Exception e)
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e);
      }
   }
}
