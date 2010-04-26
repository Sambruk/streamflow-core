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

package se.streamsource.streamflow.client.ui.administration.projects;

import org.qi4j.api.injection.scope.Uses;
import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesAdminView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsAdminView;
import se.streamsource.streamflow.client.ui.administration.label.LabelsView;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesView;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsView;

import javax.swing.*;

/**
 * JAVADOC
 */
public class ProjectView
      extends JTabbedPane
{
   public ProjectView( @Uses ProjectMembersView membersView,
                       @Uses FormsAdminView formsView,
                       @Uses CaseTypesAdminView caseTypesAdminView,
                       @Uses LabelsView labelsView,
                       @Uses SelectedLabelsView selectedLabelsView,
                       @Uses SelectedCaseTypesView selectedCaseTypesView )
   {
      super();

      addTab( text(AdministrationResources.members_tab), membersView );
      addTab( text(AdministrationResources.forms_tab), formsView );
      addTab( text(AdministrationResources.casetypes_tab ), caseTypesAdminView );
      addTab( text(AdministrationResources.labels_tab), labelsView );
      addTab( text(AdministrationResources.selected_labels_tab), selectedLabelsView );
      addTab( text(AdministrationResources.selected_casetypes_tab ), selectedCaseTypesView );
   }
}