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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.LinkTree;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;

/**
 * JAVADOC
 */
public class AdministrationContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      // TODO This needs to consider roles for server+org

      LinksBuilder linksBuilder = new LinksBuilder(module.valueBuilderFactory());

      // Add server admin link as root
      ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
      linkBuilder.prototype().text().set( "Server" );
      linkBuilder.prototype().id().set( "server" );
      linkBuilder.prototype().rel().set( "server" );
      linkBuilder.prototype().href().set("server/");
      linksBuilder.addLink(linkBuilder.newInstance());

      // Add organizations
      Participant participant = RoleMap.role( Participant.class );
      OrganizationParticipations.Data participations = RoleMap.role(OrganizationParticipations.Data.class);

      for (Organization organization : participations.organizations())
      {
         if (((Removable.Data)organization).removed().get())
            continue; // Skip this one

         ValueBuilder<LinkTree> orgLinkBuilder = module.valueBuilderFactory().newValueBuilder(LinkTree.class);

         linkBuilder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
         linkBuilder.prototype().text().set( organization.getDescription() );
         linkBuilder.prototype().id().set( organization.toString() );
         linkBuilder.prototype().rel().set( "organization" );
         linkBuilder.prototype().href().set( "organizations/" + organization.toString() + "/" );
         linkBuilder.prototype().classes().set("server");

         linksBuilder.addLink(linkBuilder.newInstance());

         orgLinkBuilder.prototype().link().set( linkBuilder.newInstance() );

         OrganizationalUnits.Data units = (OrganizationalUnits.Data) organization;
         for (OrganizationalUnit organizationalUnit : units.organizationalUnits())
         {
            addOrganizationalUnit( organizationalUnit, organization, linksBuilder, participant );
         }
      }

      return linksBuilder.newLinks();
   }

   private void addOrganizationalUnit(OrganizationalUnit ou, Object parent, LinksBuilder linksBuilder, Participant participant)
   {
      if (ou.hasRoles( participant ) || participant.toString().equals( UserEntity.ADMINISTRATOR_USERNAME ))
      {
         ValueBuilder<LinkTree> orgLinkBuilder = module.valueBuilderFactory().newValueBuilder(LinkTree.class);

         ValueBuilder<LinkValue> linkBuilder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
         linkBuilder.prototype().text().set( ou.getDescription() );
         linkBuilder.prototype().id().set( ou.toString() );
         linkBuilder.prototype().rel().set( "organizationalunit" );
         linkBuilder.prototype().classes().set(parent.toString());
         linkBuilder.prototype().href().set( "organizationalunits/" + ou.toString() + "/" );
         orgLinkBuilder.prototype().link().set( linkBuilder.newInstance() );

         linksBuilder.addLink(linkBuilder.newInstance());

         OrganizationalUnits.Data units = (OrganizationalUnits.Data) ou;
         for (OrganizationalUnit organizationalUnit : units.organizationalUnits())
         {
            addOrganizationalUnit( organizationalUnit, ou, linksBuilder, participant );
         }
      } else
      {
         OrganizationalUnits.Data units = (OrganizationalUnits.Data) ou;
         for (OrganizationalUnit organizationalUnit : units.organizationalUnits())
         {
            addOrganizationalUnit( organizationalUnit, ou, linksBuilder, participant );
         }
      }
   }
}
