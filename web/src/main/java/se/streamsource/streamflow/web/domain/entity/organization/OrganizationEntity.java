/**
 *
 * Copyright 2009-2013 Jayway Products AB
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

import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypesQueries;
import se.streamsource.streamflow.web.domain.interaction.gtd.IdGenerator;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;
import se.streamsource.streamflow.web.domain.interaction.security.CaseAccessDefaults;
import se.streamsource.streamflow.web.domain.structure.attachment.Attachments;
import se.streamsource.streamflow.web.domain.structure.attachment.CasePdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.external.ShadowCases;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinitions;
import se.streamsource.streamflow.web.domain.structure.form.FieldGroups;
import se.streamsource.streamflow.web.domain.structure.form.Forms;
import se.streamsource.streamflow.web.domain.structure.group.Groups;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.FormOnRemove;
import se.streamsource.streamflow.web.domain.structure.organization.IntegrationPoints;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnitRefactoring;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.Priorities;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.streamflow.web.domain.structure.project.ProjectRoles;
import se.streamsource.streamflow.web.domain.structure.role.Roles;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUsers;

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
      FormOnRemove.Data,
      FormOnRemove.Events,
      DatatypeDefinitions.Data,
      FieldGroups.Data,
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
      Priorities.Data,
      CaseAccessDefaults.Data,
      Groups.Data,
      Groups.Events,
      ShadowCases.Data,
      IntegrationPoints.Data,
           
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
