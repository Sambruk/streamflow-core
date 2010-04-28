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

package se.streamsource.streamflow.client.ui.administration.casetypes;

import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsAdminView;
import se.streamsource.streamflow.client.ui.administration.form.SelectedFormsView;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.ResolutionsView;
import se.streamsource.streamflow.client.ui.administration.resolutions.SelectedResolutionsView;

import javax.swing.JTabbedPane;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

/**
 * JAVADOC
 */
public class CaseTypeView
      extends JTabbedPane
{
   public CaseTypeView( @Uses SelectedLabelsView selectedLabelsView ,
                        @Uses ResolutionsView resolutionsView,
                        @Uses SelectedResolutionsView selectedResolutionsView,
                        @Uses FormsAdminView formsView,
                        @Uses SelectedFormsView selectedFormsView)
   {
      addTab( text( AdministrationResources.selected_labels_tab ), selectedLabelsView );
      addTab( text( AdministrationResources.resolutions_tab ), resolutionsView );
      addTab( text( AdministrationResources.selected_resolutions_tab ), selectedResolutionsView );
      addTab( text( AdministrationResources.forms_tab ), formsView );
      addTab( text( AdministrationResources.selected_forms_tab ), selectedFormsView );
   }
}