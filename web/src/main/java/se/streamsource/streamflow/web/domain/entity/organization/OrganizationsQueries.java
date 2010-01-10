/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.entity.organization;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.query.QueryExpressions;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.domain.ListValueBuilder;
import se.streamsource.streamflow.resource.user.UserEntityDTO;
import se.streamsource.streamflow.resource.user.UserEntityListDTO;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.organizations.Organizations;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.domain.structure.Describable;

import java.util.List;

@Mixins(OrganizationsQueries.Mixin.class)
public interface OrganizationsQueries
{
   public ListValue organizations();

   public UserEntityListDTO users();

   OrganizationEntity getOrganizationByName( String name );

   Query<OrganizationEntity> getAllOrganizations();

   UserEntity getUserByName( String name );

   class Mixin
         implements OrganizationsQueries
   {
      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      @Structure
      UnitOfWorkFactory uowf;

      @This
      Organizations.Data state;

      public ListValue organizations()
      {
         return new ListValueBuilder( vbf ).addDescribableItems( getAllOrganizations() ).newList();
      }

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

      public OrganizationEntity getOrganizationByName( String name )
      {
         Describable.Data template = QueryExpressions.templateFor( Describable.Data.class );
         return qbf.newQueryBuilder( OrganizationEntity.class ).
               where( QueryExpressions.eq( template.description(), name ) ).
               newQuery( uowf.currentUnitOfWork() ).find();
      }

      public Query<OrganizationEntity> getAllOrganizations()
      {
         return qbf.newQueryBuilder( OrganizationEntity.class ).
               newQuery( uowf.currentUnitOfWork() );
      }

      public UserEntity getUserByName( String name )
      {
         return uowf.currentUnitOfWork().get( UserEntity.class, name );
      }
   }
}
