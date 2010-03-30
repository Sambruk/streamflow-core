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

package se.streamsource.streamflow.client.ui.administration.tasktypes;

import org.qi4j.api.injection.scope.Uses;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.form.SelectedFormsView;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.tasktypes.forms.FormsAdminView;

import javax.swing.*;

/**
 * JAVADOC
 */
public class TaskTypeView
      extends JTabbedPane
{
   private TaskTypeModel taskTypeModel;

   public TaskTypeView( @Uses SelectedLabelsView selectedLabelsView ,
                        @Uses FormsAdminView formsView,
                        @Uses SelectedFormsView selectedFormsView,
                        @Uses TaskTypeModel taskTypeModel)
   {
      this.taskTypeModel = taskTypeModel;
      addTab( text( AdministrationResources.selected_labels_tab ), selectedLabelsView );
      addTab( text( AdministrationResources.forms_tab ), formsView );
      addTab( text( AdministrationResources.selected_forms_tab ), selectedFormsView );
   }
}