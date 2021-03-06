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
package se.streamsource.streamflow.web.domain.entity.organization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.project.Projects;
import se.streamsource.streamflow.web.domain.structure.role.Role;

/**
 * JAVADOC
 */
@Concerns(OrganizationalUnitEntity.RolePolicyConcern.class)
@Mixins(RolePolicyAuthorization.class)
public interface OrganizationalUnitEntity
      extends DomainEntity,

      // Interactions
      Authorization,
        Describable,
      Describable.Data,

      // Structure
      OrganizationalUnit,
      OrganizationalUnitRefactoring.Data,
      Ownable.Data,
      OrganizationalUnits.Data,
      Forms.Data,
      Groups.Data,
      Groups.Events,
      Projects.Data,
      Removable.Data,
      RolePolicy.Data,
      OwningOrganization,
      Labels.Data,
      SelectedLabels.Data,
      CaseTypes.Data,

      // Queries
      OrganizationalUnitsQueries
{
   abstract class RolePolicyConcern
      extends ConcernOf<RolePolicy>
      implements RolePolicy
   {
      @This
      Ownable.Data owner;

      public boolean participantHasPermission( String participant, String permission )
      {
         boolean result = next.participantHasPermission( participant, permission );

         return result ? result : ((RolePolicy)owner.owner().get()).participantHasPermission( participant, permission );
      }

      public boolean participantHasRole( Participant participant, Role role )
      {
         boolean result = next.participantHasRole( participant, role );

         return result ? result : ((RolePolicy)owner.owner().get()).participantHasRole( participant, role );
      }

      public boolean hasRoles( Participant participant )
      {
         boolean result = next.hasRoles( participant );

         return result ? result : ((RolePolicy)owner.owner().get()).hasRoles( participant );
      }

      public List<Participant> participantsWithRole( Role role )
      {
         Set<Participant> participantsHashSet = new HashSet<Participant>(  );

         participantsHashSet.addAll( next.participantsWithRole( role ) );

         if( owner != null && !owner.owner().get().equals( this ))
         {
            participantsHashSet.addAll( ((RolePolicy) owner.owner().get()).participantsWithRole( role ) );
         }
         List<Participant> result = new ArrayList<Participant>(  );
         result.addAll( participantsHashSet );
         return result;
      }
   }
}
