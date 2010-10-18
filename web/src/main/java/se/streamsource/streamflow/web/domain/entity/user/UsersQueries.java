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

package se.streamsource.streamflow.web.domain.entity.user;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import java.util.List;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(UsersQueries.Mixin.class)
public interface UsersQueries
{
   public UserEntityListDTO users();
   UserEntity getUserByName( String name );

   class Mixin
         implements UsersQueries
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Organizations.Data state;

      public UserEntityListDTO users()
      {
         Query<UserEntity> usersQuery = qbf.newQueryBuilder( UserEntity.class ).
               newQuery( uowf.currentUnitOfWork() );

         usersQuery.orderBy( orderBy( templateFor( UserAuthentication.Data.class ).userName() ) );


         ValueBuilder<UserEntityListDTO> listBuilder = vbf.newValueBuilder( UserEntityListDTO.class );
         List<UserEntityDTO> userlist = listBuilder.prototype().users().get();

         ValueBuilder<UserEntityDTO> builder = vbf.newValueBuilder( UserEntityDTO.class );

         for (UserEntity user : usersQuery)
         {
            builder.prototype().entity().set( EntityReference.getEntityReference( user ) );
            builder.prototype().username().set( user.userName().get() );
            builder.prototype().disabled().set( user.disabled().get() );

            userlist.add( builder.newInstance() );
         }

         return listBuilder.newInstance();

      }

      public UserEntity getUserByName( String name )
      {
         return uowf.currentUnitOfWork().get( UserEntity.class, name );
      }
   }
}