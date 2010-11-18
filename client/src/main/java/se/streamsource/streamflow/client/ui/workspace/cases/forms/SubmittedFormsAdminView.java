/*
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

package se.streamsource.streamflow.client.ui.workspace.cases.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormView;
import se.streamsource.streamflow.client.ui.workspace.cases.forms.CaseSubmittedFormsView;
import se.streamsource.streamflow.resource.roles.IntegerDTO;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * JAVADOC
 */
public class SubmittedFormsAdminView
      extends JSplitPane
{
   public SubmittedFormsAdminView( @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf, @Structure final ValueBuilderFactory vbf)
   {
      CaseSubmittedFormsView submittedFormsView = obf.newObjectBuilder( CaseSubmittedFormsView.class ).use( client ).newInstance();
      setLeftComponent( submittedFormsView );
      setRightComponent( new JPanel() );

      final JList submittedForms = submittedFormsView.getSubmittedFormsList();
      submittedForms.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = submittedForms.getSelectedIndex();
               if (idx != -1 && idx < submittedForms.getModel().getSize())
               {

                  CaseSubmittedFormView submittedFormView = obf.newObjectBuilder( CaseSubmittedFormView.class ).use( client, idx ).newInstance();
                  setRightComponent( submittedFormView );
               } else
               {
                  setRightComponent( new JPanel() );
               }
            }
         }
      } );
   }
}