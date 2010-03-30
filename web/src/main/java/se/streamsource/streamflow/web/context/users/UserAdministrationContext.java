/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.users;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.organization.AdministrationType;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.TreeNodeValue;
import se.streamsource.streamflow.infrastructure.application.TreeValue;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnits;
import se.streamsource.streamflow.web.domain.structure.organization.RolePolicy;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;

import java.util.List;

/**
 * JAVADOC
 */
@Mixins(UserAdministrationContext.Mixin.class)
public interface UserAdministrationContext
      extends Context
{
   TreeValue organizations();

   abstract class Mixin
         extends ContextMixin
         implements UserAdministrationContext
   {
      @Structure
      ValueBuilderFactory vbf;

      public TreeValue organizations()
      {
         ValueBuilder<TreeValue> listBuilder = vbf.newValueBuilder( TreeValue.class );
         OrganizationParticipations organizationParticipations = context.role(OrganizationParticipations.class);
         Participant participant = (Participant) organizationParticipations;
         List<TreeNodeValue> list = listBuilder.prototype().roots().get();
         OrganizationParticipations.Data state = (OrganizationParticipations.Data) organizationParticipations;
         addOrganizationalUnits( state.organizations(), list, participant );
         return listBuilder.newInstance();
      }

      private void addOrganizationalUnits( Iterable<? extends OrganizationalUnits> organizations, List<TreeNodeValue> list, Participant participant )
      {
         for (OrganizationalUnits organization : organizations)
         {
            OrganizationalUnits.Data ou = (OrganizationalUnits.Data) organization;
            ValueBuilder<TreeNodeValue> valueBuilder = vbf.newValueBuilder( TreeNodeValue.class );
            TreeNodeValue itemValue = valueBuilder.prototype();
            itemValue.description().set( ((Describable) organization).getDescription() );
            itemValue.entity().set( EntityReference.getEntityReference( organization ) );
            itemValue.nodeType().set( organization instanceof OrganizationEntity ? AdministrationType.organization.name() : AdministrationType.organizationalunit.name() );
            List<TreeNodeValue> subOrgs = itemValue.children().get();

            RolePolicy rolePolicy = (RolePolicy) ou;

            if (rolePolicy.hasRoles( participant ) || participant.toString().equals( UserEntity.ADMINISTRATOR_USERNAME ))
            {
               addOrganizationalUnits( ((OrganizationalUnits.Data) organization).organizationalUnits(), subOrgs, participant );
               list.add( valueBuilder.newInstance() );

            } else
               addOrganizationalUnits( ((OrganizationalUnits.Data) organization).organizationalUnits(), list, participant );
         }
      }

   }
}
