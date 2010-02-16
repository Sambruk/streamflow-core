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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganization;
import se.streamsource.streamflow.web.domain.structure.organization.OwningOrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;

import static org.qi4j.api.query.QueryExpressions.*;

@Mixins(OrganizationQueries.Mixin.class)
public interface OrganizationQueries
{
   public QueryBuilder<UserEntity> findUsersByUsername( String query );

   public QueryBuilder<GroupEntity> findGroupsByName( String query );

   public Query<ProjectEntity> findProjects( String query );

   class Mixin
         implements OrganizationQueries
   {

      @Structure
      ModuleSPI module;

      @This
      Organization org;

      public QueryBuilder<UserEntity> findUsersByUsername( String userName )
      {
         QueryBuilder<UserEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( UserEntity.class );
         queryBuilder = queryBuilder.where(
               and(
                     eq( templateFor( UserAuthentication.Data.class ).disabled(), false ),
                     contains( templateFor( UserEntity.class ).organizations(), org ) )
         );

         if (userName.length() > 0)
         {
            queryBuilder = queryBuilder.where(
                  matches( templateFor( UserEntity.class ).userName(), "^" + userName )
            );
         }

         return queryBuilder;
      }

      public QueryBuilder<GroupEntity> findGroupsByName( String query )
      {
         // TODO Ensure that the group is a member of this organization somehow
         QueryBuilder<GroupEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( GroupEntity.class );
         queryBuilder = queryBuilder.where(
               eq( templateFor( Removable.Data.class ).removed(), false ) );

         if (query.length() > 0)
         {
            queryBuilder = queryBuilder.where(
                  matches( templateFor( Describable.Data.class ).description(), "^" + query )
            );
         }

         return queryBuilder;
      }

      public Query<ProjectEntity> findProjects( String query )
      {
         QueryBuilder<ProjectEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( ProjectEntity.class );
         queryBuilder = queryBuilder.where(
               and(
                  eq( templateFor( Removable.Data.class ).removed(), false ),
                  eq( templateFor( OwningOrganization.class,
                                   templateFor( OwningOrganizationalUnit.Data.class).organizationalUnit().get()).organization(), org)));

         if (query.length() > 0)
         {
            queryBuilder = queryBuilder.where(
                  matches( templateFor( Describable.Data.class ).description(), "^" + query )
            );
         }

         Query<ProjectEntity> projects = queryBuilder.newQuery( module.unitOfWorkFactory().currentUnitOfWork() );
         projects.orderBy( orderBy( templateFor( Describable.Data.class ).description() ) );
         return projects;
      }
   }
}
