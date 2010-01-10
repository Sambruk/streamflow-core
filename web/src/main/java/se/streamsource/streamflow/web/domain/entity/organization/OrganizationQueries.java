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
import org.qi4j.api.query.QueryBuilder;
import static org.qi4j.api.query.QueryExpressions.*;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.structure.ModuleSPI;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.domain.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.entity.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.group.Participant;
import se.streamsource.streamflow.web.domain.structure.organization.Organization;
import se.streamsource.streamflow.web.domain.structure.project.Project;

@Mixins(OrganizationQueries.Mixin.class)
public interface OrganizationQueries
{
   public ListValue findUsers( String query );

   public ListValue findGroups( String query );

   public ListValue findProjects( String query );

   class Mixin
         implements OrganizationQueries
   {

      @Structure
      ModuleSPI module;

      @This
      Organization org;

      public ListValue findUsers( String query )
      {
         ValueBuilder<EntityReferenceDTO> builder = module.valueBuilderFactory().newValueBuilder( EntityReferenceDTO.class );
         ListValueBuilder listBuilder = new ListValueBuilder( module.valueBuilderFactory() );

         if (query.length() > 0)
         {
            QueryBuilder<UserEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( UserEntity.class );
            Query<UserEntity> users = queryBuilder.where(
                  and(
                        matches( templateFor( UserEntity.class ).userName(), "^" + query ),
                        contains( templateFor( UserEntity.class ).organizations(), org )
                  )
            )
                  .newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

            try
            {
               for (Participant participant : users)
               {
                  builder.prototype().entity().set( EntityReference.getEntityReference( participant ) );
                  listBuilder.addListItem( participant.getDescription(), builder.newInstance().entity().get() );
               }
            } catch (Exception e)
            {
               //e.printStackTrace();
            }
         }
         return listBuilder.newList();
      }

      public ListValue findGroups( String query )
      {
         ValueBuilder<EntityReferenceDTO> builder = module.valueBuilderFactory().newValueBuilder( EntityReferenceDTO.class );
         ListValueBuilder listBuilder = new ListValueBuilder( module.valueBuilderFactory() );

         if (query.length() > 0)
         {
            QueryBuilder<GroupEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( GroupEntity.class );
            Query<GroupEntity> groups = queryBuilder.where(
                  and(
                        eq( templateFor( GroupEntity.class ).removed(), false ),
                        matches( templateFor( GroupEntity.class ).description(), "^" + query ) ) ).
                  newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

            try
            {
               for (Participant participant : groups)
               {
                  builder.prototype().entity().set( EntityReference.getEntityReference( participant ) );
                  listBuilder.addListItem( participant.getDescription(), builder.newInstance().entity().get() );
               }
            } catch (Exception e)
            {
               //e.printStackTrace();
            }
         }
         return listBuilder.newList();
      }

      public ListValue findProjects( String query )
      {
         ValueBuilder<EntityReferenceDTO> builder = module.valueBuilderFactory().newValueBuilder( EntityReferenceDTO.class );
         ListValueBuilder listBuilder = new ListValueBuilder( module.valueBuilderFactory() );

         if (query.length() > 0)
         {
            QueryBuilder<ProjectEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( ProjectEntity.class );
            Query<ProjectEntity> projects = queryBuilder.where( and(
                  eq( templateFor( ProjectEntity.class ).removed(), false ),
                  matches( templateFor( ProjectEntity.class ).description(), "^" + query ) ) ).
                  newQuery( module.unitOfWorkFactory().currentUnitOfWork() );

            try
            {
               for (Project project : projects)
               {
                  builder.prototype().entity().set( EntityReference.getEntityReference( project ) );
                  listBuilder.addListItem( project.getDescription(), builder.newInstance().entity().get() );
               }
            } catch (Exception e)
            {
               //e.printStackTrace();
            }
         }
         return listBuilder.newList();
      }
   }
}
