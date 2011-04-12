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

package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.infrastructure.application.*;
import se.streamsource.streamflow.web.domain.entity.user.*;
import se.streamsource.streamflow.web.domain.structure.group.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;

import java.util.*;

/**
 * JAVADOC
 */
public class AdministrationContext
      implements IndexContext<LinkTree>
{
   @Structure
   ValueBuilderFactory vbf;

   public LinkTree index()
   {
      // TODO This needs to consider roles for server+org

      ValueBuilder<LinkTree> treeValueBuilder = vbf.newValueBuilder( LinkTree.class );

      // Add server admin link as root
      ValueBuilder<LinkValue> linkBuilder = vbf.newValueBuilder( LinkValue.class );
      linkBuilder.prototype().text().set( "Server" );
      linkBuilder.prototype().id().set( "server" );
      linkBuilder.prototype().rel().set( "server" );
      linkBuilder.prototype().href().set( "server/" );
      treeValueBuilder.prototype().link().set( linkBuilder.newInstance() );

      // Add organizations
      Participant participant = RoleMap.role( Participant.class );
      OrganizationParticipations.Data participations = RoleMap.role( OrganizationParticipations.Data.class );
      List<LinkTree> list = treeValueBuilder.prototype().children().get();

      for (Organization organization : participations.organizations())
      {
         ValueBuilder<LinkTree> orgLinkBuilder = vbf.newValueBuilder( LinkTree.class );

         linkBuilder = vbf.newValueBuilder( LinkValue.class );
         linkBuilder.prototype().text().set( organization.getDescription() );
         linkBuilder.prototype().id().set( organization.toString() );
         linkBuilder.prototype().rel().set( "organization" );
         linkBuilder.prototype().href().set( "organizations/" + organization.toString() + "/" );
         orgLinkBuilder.prototype().link().set( linkBuilder.newInstance() );

         OrganizationalUnits.Data units = (OrganizationalUnits.Data) organization;
         for (OrganizationalUnit organizationalUnit : units.organizationalUnits())
         {
            addOrganizationalUnit( organizationalUnit, orgLinkBuilder.prototype().children().get(), participant );
         }

         list.add( orgLinkBuilder.newInstance() );
      }

      return treeValueBuilder.newInstance();
   }

   private void addOrganizationalUnit( OrganizationalUnit ou, List<LinkTree> list, Participant participant )
   {
      RolePolicy rolePolicy = ou;

      if (rolePolicy.hasRoles( participant ) || participant.toString().equals( UserEntity.ADMINISTRATOR_USERNAME ))
      {
         ValueBuilder<LinkTree> orgLinkBuilder = vbf.newValueBuilder( LinkTree.class );

         ValueBuilder<LinkValue> linkBuilder = vbf.newValueBuilder( LinkValue.class );
         linkBuilder.prototype().text().set( ou.getDescription() );
         linkBuilder.prototype().id().set( ou.toString() );
         linkBuilder.prototype().rel().set( "organizationalunit" );
         linkBuilder.prototype().href().set( "organizationalunits/" + ou.toString() + "/" );
         orgLinkBuilder.prototype().link().set( linkBuilder.newInstance() );

         OrganizationalUnits.Data units = (OrganizationalUnits.Data) ou;
         for (OrganizationalUnit organizationalUnit : units.organizationalUnits())
         {
            addOrganizationalUnit( organizationalUnit, orgLinkBuilder.prototype().children().get(), participant );
         }
         list.add( orgLinkBuilder.newInstance() );
      } else
      {
         OrganizationalUnits.Data units = (OrganizationalUnits.Data) ou;
         for (OrganizationalUnit organizationalUnit : units.organizationalUnits())
         {
            addOrganizationalUnit( organizationalUnit, list, participant );
         }
      }
   }
}
