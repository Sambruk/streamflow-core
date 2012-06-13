/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.NewUserDTO;
import se.streamsource.streamflow.api.administration.UserEntityDTO;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationParticipationsQueries;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.entity.user.UsersQueries;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.Users;

import java.util.List;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class OrganizationUsersContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      UsersQueries users = RoleMap.role( UsersQueries.class );

      ValueBuilder<LinksValue> listBuilder = module.valueBuilderFactory().newValueBuilder(LinksValue.class);
      List<LinkValue> userlist = listBuilder.prototype().links().get();

      ValueBuilder<UserEntityDTO> builder = module.valueBuilderFactory().newValueBuilder(UserEntityDTO.class);

      for (UserEntity user : users.users())
      {
         builder.prototype().href().set( user.toString() + "/" );
         builder.prototype().id().set( user.toString() );
         builder.prototype().text().set( user.userName().get() );
         builder.prototype().disabled().set( user.disabled().get() );

         userlist.add( builder.newInstance() );
      }

      return listBuilder.newInstance();
   }


/*   public LinksValue possibleusers()
   {
      OrganizationParticipationsQueries participants = role( OrganizationParticipationsQueries.class );

      Query<User> query = participants.possibleUsers();

      return new LinksBuilder( module.valueBuilderFactory() ).addDescribables( query ).newLinks();
   }
*/
   public void importusers()
   {
      // Marker method for now. Refactor OrganizationUsersResource.importusers to call this instead
   }

   public void createuser( NewUserDTO DTO)
   {
      Users users = RoleMap.role( Users.class );
      User user = users.createUser( DTO.username().get(), DTO.password().get() );

      Organization org = role( Organization.class );
      user.join( org );
   }
}
