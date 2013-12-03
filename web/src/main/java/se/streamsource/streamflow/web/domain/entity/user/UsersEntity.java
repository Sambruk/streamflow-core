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
package se.streamsource.streamflow.web.domain.entity.user;

import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.interaction.security.Authorization;
import se.streamsource.streamflow.web.domain.structure.user.Users;

/**
 * JAVADOC
 */
@Mixins(UsersEntity.OrganizationsAuthorizationMixin.class)
public interface UsersEntity
      extends Users,
      UsersQueries,
      Authorization,
      Users.Data,
      DomainEntity
{
   public static final String USERS_ID = "users";

   class OrganizationsAuthorizationMixin
         implements Authorization
   {
      public boolean hasPermission( String userId, String permission )
      {
         return userId.equals( UserEntity.ADMINISTRATOR_USERNAME );
      }
   }
}