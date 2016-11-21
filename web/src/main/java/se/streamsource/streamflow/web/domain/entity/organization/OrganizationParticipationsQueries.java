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

import static org.qi4j.api.query.QueryExpressions.contains;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;

import java.util.ArrayList;
import java.util.List;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;

import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

@Mixins(OrganizationParticipationsQueries.Mixin.class)
public interface OrganizationParticipationsQueries
{
   public QueryBuilder<User> users();

   public Query<User> possibleUsers();

   class Mixin
         implements OrganizationParticipationsQueries
   {
      @Structure
      Module module;

      @This
      OrganizationEntity organization;

      public QueryBuilder<User> users()
      {
         QueryBuilder<User> usersQuery = module.queryBuilderFactory().newQueryBuilder(User.class);
         return usersQuery.where( contains( templateFor( OrganizationParticipations.Data.class).organizations(), organization ));
      }

      public Query<User> possibleUsers()
      {
         QueryBuilder<User> builder = module.queryBuilderFactory().newQueryBuilder(User.class);
         Query<User> query = builder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() ).orderBy(orderBy(templateFor(UserAuthentication.Data.class).userName()));
         List<User> possibleUsers = new ArrayList<User>( );
         for (User user : query)
         {
            if (!((OrganizationParticipations.Data)user).organizations().contains( organization ))
            {
               possibleUsers.add( user );
            }
         }

         return module.queryBuilderFactory().newQueryBuilder(User.class).newQuery( possibleUsers );
      }
   }
}
