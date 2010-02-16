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

package se.streamsource.streamflow.web.resource.organizations.projects.members;

import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.web.domain.structure.project.Members;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;

/**
 * Mapped to:
 * /organizations/{organization}/projects/{project}/members
 */
public class MembersServerResource
      extends CommandQueryServerResource
{
   public ListValue members()
   {
      String identity = getRequest().getAttributes().get( "project" ).toString();
      UnitOfWork uow = uowf.currentUnitOfWork();
      Members.Data members = uow.get( Members.Data.class, identity );

      return new ListValueBuilder( vbf ).addDescribableItems( members.members() ).newList();
   }

/*   public ListValue findusers( StringDTO query ) throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String orgId = getRequest().getAttributes().get( "organization" ).toString();

      OwningOrganization organization = uowf.currentUnitOfWork().get( OwningOrganization.class, orgId );
      checkPermission( organization );

      ListValue list = ((OrganizationQueries) organization.organization().get()).findUsers( query.string().get() );

      String projectId = getRequest().getAttributes().get( "project" ).toString();
      ProjectEntity project = uow.get( ProjectEntity.class, projectId );

      ListValueBuilder listBuilder = new ListValueBuilder( vbf );

      for (ListItemValue user : list.items().get())
      {
         if (!project.members().contains( uow.get( Member.class, user.entity().get().identity() ) ))
         {
            listBuilder.addListItem( user.description().get(), user.entity().get() );
         }
      }

      return listBuilder.newList();
   }

   public ListValue findgroups( StringDTO query ) throws ResourceException
   {
      UnitOfWork uow = uowf.currentUnitOfWork();

      String orgId = getRequest().getAttributes().get( "organization" ).toString();

      OwningOrganization organization = uowf.currentUnitOfWork().get( OwningOrganization.class, orgId );
      checkPermission( organization );

      ListValue list = ((OrganizationQueries) organization.organization().get()).findGroups( query.string().get() );

      String projectId = getRequest().getAttributes().get( "project" ).toString();
      ProjectEntity group = uow.get( ProjectEntity.class, projectId );

      ListValueBuilder listBuilder = new ListValueBuilder( vbf );

      for (ListItemValue grp : list.items().get())
      {
         if (!group.members().contains( uow.get( Member.class, grp.entity().get().identity() ) ))
         {
            listBuilder.addListItem( grp.description().get(), grp.entity().get() );
         }
      }

      return listBuilder.newList();
   }*/
}