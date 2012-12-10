/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.resource.organizations;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.AdministratorContext;
import se.streamsource.streamflow.web.context.administration.AdministratorsContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;

/**
 * JAVADOC
 */
public class AdministratorsResource
   extends CommandQueryResource
   implements SubResources
{
   public AdministratorsResource( )
   {
      super( AdministratorsContext.class );
   }

   public LinksValue possibleusers()
   {
      LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "addadministrator" );

      for (Describable user : context(AdministratorsContext.class).possibleusers())
      {
         String group = "" + Character.toUpperCase( user.getDescription().charAt( 0 ) );
         linksBuilder.addDescribable( user, group );
      }

      return linksBuilder.newLinks();
   }

   public LinksValue possiblegroups()
   {
      final LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() ).command( "addadministrator" );

      for (Group group : context(AdministratorsContext.class).possiblegroups())
      {
         String groupText = "" + Character.toUpperCase( group.getDescription().charAt( 0 ) );
         linksBuilder.addDescribable( group, groupText );
      }

      return linksBuilder.newLinks();
   }

   public void resource( String segment ) throws ResourceException
   {
      Participant participant = setRole( Participant.class, segment );
      if(!RoleMap.role( RolePolicy.class ).hasRoles( participant ))
      {
         throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, segment+" is not an administrator");
      }

      subResourceContexts( AdministratorContext.class );
   }
}
