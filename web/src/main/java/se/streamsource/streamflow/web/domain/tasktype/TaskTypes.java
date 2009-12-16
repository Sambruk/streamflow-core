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

package se.streamsource.streamflow.web.domain.tasktype;

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.Aggregated;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.project.Project;
import se.streamsource.streamflow.web.domain.project.ProjectEntity;
import se.streamsource.streamflow.web.domain.project.OwningOrganizationalUnit;

import static org.qi4j.api.query.QueryExpressions.*;

/**
 * JAVADOC
 */
@Mixins(TaskTypes.Mixin.class)
public interface TaskTypes
{
   // Commands

   TaskTypeEntity createTaskType( String name );

   boolean removeTaskType( TaskType taskType );

   // Queries

   ListValue taskTypeList();

   ListValue possibleProjects( @Optional TaskType taskType );

   interface Data
   {
      @Aggregated
      ManyAssociation<TaskType> taskTypes();

      TaskTypeEntity createdTaskType( DomainEvent event, String id );

      void removedTaskType( DomainEvent event, TaskType taskType );

      TaskTypeEntity getTaskTypeByName( String name );
   }

   abstract class Mixin
         implements TaskTypes, Data
   {
      @Service
      IdentityGenerator idGen;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      @Structure
      QueryBuilderFactory qbf;

      public TaskTypeEntity createTaskType( String name )
      {
         TaskTypeEntity taskType = createdTaskType( DomainEvent.CREATE, idGen.generate( TaskTypeEntity.class ) );
         taskType.changeDescription( name );

         return taskType;
      }

      public TaskTypeEntity getTaskTypeByName( String name )
      {
         return (TaskTypeEntity) Describable.Mixin.getDescribable( taskTypes(), name );
      }

      public TaskTypeEntity createdTaskType( DomainEvent event, String id )
      {
         TaskTypeEntity taskType = uowf.currentUnitOfWork().newEntity( TaskTypeEntity.class, id );
         taskTypes().add( taskType );

         return taskType;
      }

      public ListValue taskTypeList()
      {
         return new ListValueBuilder( vbf ).addDescribableItems( taskTypes() ).newList();
      }

      public ListValue possibleProjects( TaskType taskType )
      {
         QueryBuilder<Project> projects = qbf.newQueryBuilder( Project.class );

         ProjectEntity template = templateFor( ProjectEntity.class );

         if (taskType != null)
            projects = projects.where( contains( template.selectedTaskTypes(), taskType ) );

         Query<Project> query = projects.newQuery( uowf.currentUnitOfWork() );

         ListValueBuilder lvb = new ListValueBuilder( vbf );
         for (Project project : query)
         {
            OwningOrganizationalUnit.Data ou = (OwningOrganizationalUnit.Data) project;
            lvb.addDescribable( project, ou.organizationalUnit().get() );
         }

         return lvb.newList();
      }
   }
}