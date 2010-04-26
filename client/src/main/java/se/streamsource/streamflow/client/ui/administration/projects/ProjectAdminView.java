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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.client.ui.administration.OrganizationalUnitAdministrationModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.CaseTypesModel;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;
import se.streamsource.streamflow.client.ui.administration.label.SelectedLabelsModel;
import se.streamsource.streamflow.client.ui.administration.casetypes.SelectedCaseTypesModel;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.ui.administration.casetypes.forms.FormsModel;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * JAVADOC
 */
public class ProjectAdminView
      extends JSplitPane
{
   @Structure
   ObjectBuilderFactory obf;

   ProjectsModel projectsModel;

   @Uses
   OrganizationalUnitAdministrationModel organizationModel;

   public ProjectAdminView( @Uses final ProjectsView projectsView, final @Uses ProjectsModel projectsModel, @Uses final AdministrationView adminView )
   {
      super();
      this.projectsModel = projectsModel;

      setLeftComponent( projectsView );
      setRightComponent( new JPanel() );

      setDividerLocation( 200 );

      final JList list = projectsView.getProjectList();
      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = list.getSelectedIndex();
               if (idx < list.getModel().getSize() && idx >= 0)
               {
                  LinkValue projectValue = (LinkValue) list.getModel().getElementAt( idx );
                  ProjectModel projectModel = projectsModel.getProjectModel( projectValue.id().get() );
                  ProjectMembersModel membersModel = projectModel.getMembersModel();
                  FormsModel formsModel = projectModel.getFormsModel();
                  CaseTypesModel caseTypesModel = projectModel.getCaseTypesModel();
                  LabelsModel labelsModel = projectModel.getLabelsModel();
                  SelectedLabelsModel selectedLabelsModel = projectModel.getSelectedLabelsModel();
                  SelectedCaseTypesModel selectedCaseTypes = projectModel.getSelectedCaseTypes();
                  ProjectView view = obf.newObjectBuilder( ProjectView.class ).use(
                        membersModel,
                        labelsModel,
                        selectedLabelsModel,
                        formsModel,
                        caseTypesModel,
                        selectedCaseTypes,
                        organizationModel,
                        adminView ).newInstance();
                  setRightComponent( view );
               } else
               {
                  setRightComponent( new JPanel() );
               }
            }
         }
      } );


      projectsView.addAncestorListener( new RefreshWhenVisible( projectsModel, this ) );
   }

}