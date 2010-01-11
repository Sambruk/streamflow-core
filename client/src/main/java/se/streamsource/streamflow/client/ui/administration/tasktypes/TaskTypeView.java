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

package se.streamsource.streamflow.client.ui.administration.tasktypes;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.tasktypes.forms.FormsAdminView;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsView;

import javax.swing.JTabbedPane;

/**
 * JAVADOC
 */
public class TaskTypeView
      extends JTabbedPane
{
   private TaskTypeModel taskTypeModel;

   public TaskTypeView( @Uses SelectedLabelsView selectedLabelsView ,
                        @Uses FormsAdminView formsView,
                        @Uses TaskTypeModel taskTypeModel)
   {
      this.taskTypeModel = taskTypeModel;
      addTab( i18n.text( AdministrationResources.labels_tab ), selectedLabelsView );
      addTab( i18n.text( AdministrationResources.forms_tab ), formsView );
   }
}