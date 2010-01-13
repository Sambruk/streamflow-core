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

package se.streamsource.streamflow.web.domain.entity.organization;

import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.label.PossibleLabelsQueries;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.organization.Projects;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

/**
 * JAVADOC
 */
public interface OrganizationalUnitEntity
      extends DomainEntity,

      // Interactions
      Describable,
      Describable.Data,
      PermissionQueries,

      // Structure
      OrganizationalUnit,
      OrganizationalUnitRefactoring.Data,
      OrganizationalUnits.Data,
      Groups.Data,
      Projects.Data,
      Removable.Data,
      RolePolicy.Data,
      OwningOrganization,
      SelectedLabels.Data,

      // Queries
      OrganizationalUnitsQueries,
      PossibleLabelsQueries
{
}
