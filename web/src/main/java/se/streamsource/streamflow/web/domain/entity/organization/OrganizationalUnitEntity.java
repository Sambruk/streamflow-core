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

import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.web.domain.entity.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.group.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;

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
