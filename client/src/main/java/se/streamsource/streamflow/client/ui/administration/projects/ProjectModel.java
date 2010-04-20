/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.ui.administration.projects;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.tasktypes.SelectedTaskTypesModel;
import se.streamsource.streamflow.client.ui.administration.tasktypes.TaskTypesModel;
import se.streamsource.streamflow.client.ui.administration.tasktypes.forms.FormsModel;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

/**
 * JAVADOC
 */
public class ProjectModel
      implements EventListener
{
   private
   @Uses
   ProjectMembersModel membersModel;

   private
   @Uses
   FormsModel formsModel;

   private
   @Uses
   TaskTypesModel taskTypesModel;

   private
   @Uses
   LabelsModel labelsModel;

   private
   @Uses
   SelectedLabelsModel selectedLabelsModel;

   private
   @Uses
   SelectedTaskTypesModel selectedTaskTypesModel;

   public ProjectMembersModel getMembersModel()
   {
      return membersModel;
   }

   public FormsModel getFormsModel()
   {
      return formsModel;
   }

   public TaskTypesModel getTaskTypesModel()
   {
      return taskTypesModel;
   }

   public LabelsModel getLabelsModel()
   {
      return labelsModel;
   }

   public SelectedLabelsModel getSelectedLabelsModel()
   {
      return selectedLabelsModel;
   }

   public SelectedTaskTypesModel getSelectedTaskTypes()
   {
      return selectedTaskTypesModel;
   }

   public void notifyEvent( DomainEvent event )
   {
      membersModel.notifyEvent( event );
      formsModel.notifyEvent( event );
      taskTypesModel.notifyEvent( event );
      selectedLabelsModel.notifyEvent( event );
      selectedTaskTypesModel.notifyEvent( event );
   }

}
