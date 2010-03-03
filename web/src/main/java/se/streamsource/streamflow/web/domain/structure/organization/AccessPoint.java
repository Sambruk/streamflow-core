/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.structure.organization;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.domain.structure.Removable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.structure.form.FieldTemplates;
import se.streamsource.streamflow.web.domain.structure.form.FormTemplates;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.project.ProjectRoles;
import se.streamsource.streamflow.web.domain.structure.role.Roles;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskType;
import se.streamsource.streamflow.web.domain.structure.tasktype.TaskTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(AccessPoint.Mixin.class)
public interface AccessPoint
      extends
      Describable
{
   void addProject( Project project );
   void addTaskType( TaskType taskType );
   void addLabel( Label label );

   interface Data
   {
      Property<Project> project();
      Property<TaskType> taskType();
      Property<List<Label>> labels();

      void addedProject( DomainEvent event, Project project );
      void addedTaskType( DomainEvent event, TaskType taskType );
      void addedLabel( DomainEvent event, Label label );
   }

   abstract class Mixin
      implements AccessPoint, Data
   {
      public void addProject( Project project )
      {
         addedProject( DomainEvent.CREATE, project );
      }

      public void addedProject( DomainEvent event, Project project )
      {
         project().set( project );
      }

      public void addTaskType( TaskType taskType )
      {
         addedTaskType( DomainEvent.CREATE, taskType );
      }

      public void addedTaskType( DomainEvent event, TaskType taskType )
      {
         taskType().set( taskType );
      }

      public void addLabel( Label label )
      {
         if ( labels().get() == null )
         {
            // is this a state change in a non-event method?
            labels().set( new ArrayList<Label>() );
         }
         if ( !labels().get().contains( label ) )
         {
            addedLabel( DomainEvent.CREATE, label );
         }
      }

      public void addedLabel( DomainEvent event, Label label )
      {
         labels().get().add( label );
      }
   }
}