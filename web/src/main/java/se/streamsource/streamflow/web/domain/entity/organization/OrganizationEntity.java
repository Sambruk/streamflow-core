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

import org.qi4j.api.entity.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import se.streamsource.streamflow.domain.structure.*;
import se.streamsource.streamflow.web.domain.entity.*;
import se.streamsource.streamflow.web.domain.entity.casetype.*;
import se.streamsource.streamflow.web.domain.interaction.gtd.*;
import se.streamsource.streamflow.web.domain.interaction.security.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;
import se.streamsource.streamflow.web.domain.structure.casetype.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.label.*;
import se.streamsource.streamflow.web.domain.structure.organization.*;
import se.streamsource.streamflow.web.domain.structure.project.*;
import se.streamsource.streamflow.web.domain.structure.role.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

/**
 * A root organization.
 */
@Mixins({OrganizationEntity.LifecycleMixin.class, RolePolicyAuthorization.class})
public interface OrganizationEntity
      extends DomainEntity,

      // Interactions
      Authorization,
      IdGenerator,
      IdGenerator.Data,

      // Structure
      Organization,

      // Data
      Describable.Data,
      Forms.Data,
      Labels.Data,
      OrganizationalUnits.Data,
      OwningOrganization,
      ProjectRoles.Data,
      Removable.Data,
      RolePolicy.Data,
      Roles.Data,
      SelectedLabels.Data,
      CaseTypes.Data,
      AccessPoints.Data,
      EmailAccessPoints.Data,
      ProxyUsers.Data,
      Attachments.Data,
      DefaultPdfTemplate.Data,
      FormPdfTemplate.Data,
      CasePdfTemplate.Data,
           
      //Queries
      OrganizationParticipationsQueries,
      OrganizationQueries,
      OrganizationalUnitsQueries,
      CaseTypesQueries
{
   abstract class LifecycleMixin
         extends OrganizationalUnitRefactoring.Mixin
         implements Lifecycle, OwningOrganization
   {
      @This
      Organization org;

      public void create() throws LifecycleException
      {
         organization().set( org );
      }

      public void remove() throws LifecycleException
      {
      }
   }
}
