/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.web.resource.organizations;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;
import se.streamsource.streamflow.web.domain.group.GroupEntity;
import se.streamsource.streamflow.web.domain.group.Participant;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.user.UserEntity;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(OrganizationalUnitResource.OrganizationalUnitMixin.class)
public interface OrganizationalUnitResource
{
   ListValue findUsers( StringDTO query );

   ListValue findGroups( StringDTO query );

   ListValue findProjects( StringDTO query );

   class OrganizationalUnitMixin
         implements OrganizationalUnitResource
   {
      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      Module module;

      public ListValue findUsers( StringDTO query )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         ListValueBuilder listBuilder = new ListValueBuilder( vbf );

         if (query.string().get().length() > 0)
         {
            QueryBuilder<UserEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( UserEntity.class );
            queryBuilder.where( matches(
                  templateFor( UserEntity.class ).userName(), "^" + query.string().get() ) );
            Query<UserEntity> users = queryBuilder.newQuery( uow );

            for (Participant participant : users)
            {
               builder.prototype().entity().set( EntityReference.getEntityReference( participant ) );
               listBuilder.addListItem( participant.getDescription(), builder.newInstance().entity().get() );
            }
         }
         return listBuilder.newList();
      }


      public ListValue findGroups( StringDTO query )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         ListValueBuilder listBuilder = new ListValueBuilder( vbf );

         if (query.string().get().length() > 0)
         {
            QueryBuilder<GroupEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( GroupEntity.class );
            Query<GroupEntity> groups = queryBuilder.where(
                  and(
                        eq( templateFor( GroupEntity.class ).removed(), false ),
                        matches( templateFor( GroupEntity.class ).description(), "^" + query.string().get() ) ) ).
                  newQuery( uow );

            for (Participant participant : groups)
            {
               builder.prototype().entity().set( EntityReference.getEntityReference( participant ) );
               listBuilder.addListItem( participant.getDescription(), builder.newInstance().entity().get() );
            }
         }
         return listBuilder.newList();
      }

      public ListValue findProjects( StringDTO query )
      {
         UnitOfWork uow = uowf.currentUnitOfWork();

         ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
         ListValueBuilder listBuilder = new ListValueBuilder( vbf );

         if (query.string().get().length() > 0)
         {
            QueryBuilder<ProjectEntity> queryBuilder = module.queryBuilderFactory().newQueryBuilder( ProjectEntity.class );
            Query<ProjectEntity> projects = queryBuilder.where( and(
                  eq( templateFor( ProjectEntity.class ).removed(), false ),
                  matches( templateFor( ProjectEntity.class ).description(), "^" + query.string().get() ) ) ).
                  newQuery( uow );

            for (Project project : projects)
            {
               builder.prototype().entity().set( EntityReference.getEntityReference( project ) );
               listBuilder.addListItem( project.getDescription(), builder.newInstance().entity().get() );
            }
         }
         return listBuilder.newList();
      }
   }
}
