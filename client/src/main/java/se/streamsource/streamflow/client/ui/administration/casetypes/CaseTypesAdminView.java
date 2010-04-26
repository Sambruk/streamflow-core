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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.ui.administration.AdministrationView;
import se.streamsource.streamflow.client.ui.administration.label.LabelsModel;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * JAVADOC
 */
public class CaseTypesAdminView
      extends JSplitPane
{
   @Structure
   ObjectBuilderFactory obf;

   public CaseTypesAdminView( @Uses final CaseTypesView caseTypesView,
                              @Uses final CaseTypesModel caseTypesModel,
                              @Uses final LabelsModel labelsModel,
                              @Uses final AdministrationView administrationView)
   {
      super();

      setLeftComponent( caseTypesView );
      setRightComponent( new JPanel() );

      setDividerLocation( 200 );

      final JList list = caseTypesView.getProjectList();
      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = list.getSelectedIndex();
               if (idx < list.getModel().getSize() && idx >= 0)
               {
                  LinkValue caseTypeLink = (LinkValue) list.getModel().getElementAt( idx );
                  CaseTypeModel caseTypeModel = caseTypesModel.getCaseTypeModel( caseTypeLink.id().get() );
                  CaseTypeView view = obf.newObjectBuilder( CaseTypeView.class ).use(
                        caseTypeModel.getSelectedLabelsModel(), labelsModel, caseTypeModel.getFormsModel(), caseTypeModel.getSelectedFormsModel(), administrationView, caseTypeModel ).newInstance();
                  setRightComponent( view );
               } else
               {
                  setRightComponent( new JPanel() );
               }
            }
         }
      } );


      addAncestorListener( new RefreshWhenVisible( caseTypesModel, this ) );
   }

}