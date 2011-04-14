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

package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.project.Projects;

/**
 * JAVADOC
 */
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
}
