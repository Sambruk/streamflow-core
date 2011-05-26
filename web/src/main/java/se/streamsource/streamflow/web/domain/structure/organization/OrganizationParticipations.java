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

package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.common.Optional;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.group.Group;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * List of organizations a participant is a member of.
 */
@Concerns(OrganizationParticipations.LeaveConcern.class)
@Mixins(OrganizationParticipations.Mixin.class)
public interface OrganizationParticipations
{
   void join( Organization org );

   void leave( Organization ou );

   interface Data
   {
      ManyAssociation<Organization> organizations();

      void joinedOrganization( @Optional DomainEvent event, Organization org );

      void leftOrganization( @Optional DomainEvent event, Organization org );
   }

   abstract class Mixin
         implements OrganizationParticipations, Data
   {
      @This
      Data state;

      public void join( Organization ou )
      {
         if (!state.organizations().contains( ou ))
         {
            joinedOrganization( null, ou );
         }
      }

      public void leave( Organization ou )
      {
         if (state.organizations().contains( ou ))
         {
            leftOrganization( null, ou );
         }
      }

      public void joinedOrganization( @Optional DomainEvent event, Organization org )
      {
         state.organizations().add( org );
      }

      public void leftOrganization( @Optional DomainEvent event, Organization org )
      {
         state.organizations().remove( org );
      }
   }

   abstract class LeaveConcern
         extends ConcernOf<OrganizationParticipations>
         implements OrganizationParticipations
   {
      @This
      Member member;

      @This
      Participant participant;

      @Structure
      UnitOfWorkFactory uowf;

      public void leave( Organization ou )
      {
         for (OrganizationalUnit organizationalUnit : ((OrganizationalUnits.Data) ou).organizationalUnits())
         {
            userLeaves( organizationalUnit );
         }

         next.leave( ou );
      }

      private void userLeaves( OrganizationalUnit org )
      {
         // Remove permissions
         org.revokeRoles( participant );

         // Leave projects
         for (Project project : ((Projects.Data)org).projects())
         {
            project.removeMember( member );
         }

         // Leave groups
         for (Group group : ((Groups.Data)org).groups())
         {
            group.removeParticipant( participant );
         }

         // Recurse through rest of organization
         for (OrganizationalUnit orgUnit : ((OrganizationalUnits.Data)org).organizationalUnits())
         {
            userLeaves( orgUnit );
         }
      }
   }
}
