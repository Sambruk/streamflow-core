/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.resource.organizations.policy;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.role.Role;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

import java.util.List;

/**
 * Mapped to:
 * /organizations/{policy}/administrators
 * /organizations/{organization}/organizationalunits/{policy}/administrators
 */
public class AdministratorsServerResource
      extends CommandQueryServerResource
{
/*
   public ListValue administrators()
   {
      UnitOfWork unitOfWork = uowf.currentUnitOfWork();

      String identity = getRequest().getAttributes().get( "policy" ).toString();
      RolePolicy.Data role = unitOfWork.get( RolePolicy.Data.class, identity );

      OwningOrganization org = ((OwningOrganization)role);
      OrganizationEntity organization = (OrganizationEntity) org.organization().get();
      Role adminRole = organization.getAdministratorRole();

      List<EntityReference> admins = role.participantsWithRole( adminRole );
      ListValueBuilder builder = new ListValueBuilder( vbf );
      for (EntityReference admin : admins)
      {
         Participant participant = unitOfWork.get( Participant.class, admin.identity() );
         builder.addListItem( participant.getDescription(), admin );
      }
      return builder.newList();
   }

*/
   public void addadministrator( StringDTO participantId ) throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String identity = getRequest().getAttributes().get( "policy" ).toString();
      RolePolicy role = uow.get( RolePolicy.class, identity );

      Participant participant = uow.get( Participant.class, participantId.string().get() );

      OwningOrganization org = ((OwningOrganization)role);
      OrganizationEntity organization = (OrganizationEntity) org.organization().get();
      Role adminRole = organization.getAdministratorRole();

      checkPermission( role);
      role.grantRole( participant, adminRole );
   }

/*
   public ListValue findusers( StringDTO query ) throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String orgId = getRequest().getAttributes().get( "policy" ).toString();

      OwningOrganization organization = uowf.currentUnitOfWork().get( OwningOrganization.class, orgId );
      checkPermission( organization );

      ListValue list = ((OrganizationQueries) organization.organization().get()).findUsers( query.string().get() );

      ListValue administrators = administrators();

      ListValueBuilder listBuilder = new ListValueBuilder( vbf );

      for (ListItemValue user : list.items().get())
      {
         if (!administrators.items().get().contains( user ))
         {
            listBuilder.addListItem( user.description().get(), user.entity().get() );
         }
      }

      return listBuilder.newList();
   }

   public ListValue findgroups( StringDTO query ) throws ResourceException
   {
      String orgId = getRequest().getAttributes().get( "policy" ).toString();

      OwningOrganization organization = uowf.currentUnitOfWork().get( OwningOrganization.class, orgId );
      checkPermission( organization );

      ListValue list = ((OrganizationQueries) organization.organization().get()).findGroups( query.string().get() );

      ListValue administrators = administrators();

      ListValueBuilder listBuilder = new ListValueBuilder( vbf );

      for (ListItemValue grp : list.items().get())
      {
         if (!administrators.items().get().contains( grp ))
         {
            listBuilder.addListItem( grp.description().get(), grp.entity().get() );
         }
      }

      return listBuilder.newList();
   }
*/
}