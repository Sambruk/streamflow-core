/**
 *
 * Copyright 2009-2010 Streamsource AB
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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import java.util.ArrayList;
import java.util.List;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(OrganizationParticipationsQueries.Mixin.class)
public interface OrganizationParticipationsQueries
{
   public QueryBuilder<User> users();

   public Query<User> possibleUsers();

   class Mixin
         implements OrganizationParticipationsQueries
   {
      @Structure
      QueryBuilderFactory qbf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      OrganizationEntity organization;

      public QueryBuilder<User> users()
      {
         QueryBuilder<User> usersQuery = qbf.newQueryBuilder( User.class );
         return usersQuery.where( contains( templateFor( OrganizationParticipations.Data.class).organizations(), organization ));
      }

      public Query<User> possibleUsers()
      {
         QueryBuilder<User> builder = qbf.newQueryBuilder( User.class );
         Query<User> query = builder.newQuery( uowf.currentUnitOfWork() ).orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );
         List<User> possibleUsers = new ArrayList<User>( );
         for (User user : query)
         {
            if (!((OrganizationParticipations.Data)user).organizations().contains( organization ))
            {
               possibleUsers.add( user );
            }
         }

         return qbf.newQueryBuilder( User.class ).newQuery( possibleUsers );
      }
   }
}
