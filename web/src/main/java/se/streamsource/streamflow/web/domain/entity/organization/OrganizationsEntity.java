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

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.sideeffect.SideEffectOf;
import org.qi4j.api.sideeffect.SideEffects;

import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;

/**
 * JAVADOC
 */
@SideEffects(OrganizationsEntity.CreateAdminRoleSideEffect.class)
@Mixins(OrganizationsEntity.OrganizationsAuthorizationMixin.class)
public interface OrganizationsEntity
      extends Organizations,
      OrganizationsQueries,
      Organizations.Data,
      Authorization,
      DomainEntity
{
   public static final String ORGANIZATIONS_ID = "organizations";

   class OrganizationsAuthorizationMixin
         implements Authorization
   {
      public boolean hasPermission( String userId, String permission )
      {
         return userId.equals( UserEntity.ADMINISTRATOR_USERNAME );
      }
   }

   abstract class CreateAdminRoleSideEffect
      extends SideEffectOf<Organizations>
      implements Organizations
   {
      public Organization createOrganization( String name )
      {
         Organization org = result.createOrganization(name);
         org.createRole("Administrator");
         return null;
      }
   }
}