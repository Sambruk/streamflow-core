/**
 *
 * Copyright 2009-2011 Streamsource AB
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
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.workspace.cases.form.SubmittedFormListDTO;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * JAVADOC
 */
public class SubmittedFormsAdminView
      extends JSplitPane
{
   public SubmittedFormsAdminView( @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf, @Structure final ValueBuilderFactory vbf)
   {
      final CaseSubmittedFormsView submittedFormsView = obf.newObjectBuilder( CaseSubmittedFormsView.class ).use( client ).newInstance();
      setLeftComponent( submittedFormsView );
      setRightComponent( new JPanel() );

      final JTree submittedForms = submittedFormsView.getSubmittedFormsTree();
      submittedForms.addTreeSelectionListener( new TreeSelectionListener()
      {
         public void valueChanged( TreeSelectionEvent e )
         {
             
            SubmittedFormListDTO form = (SubmittedFormListDTO) ((DefaultMutableTreeNode)e.getPath().getLastPathComponent()).getUserObject();
            int idx = submittedFormsView.getModel().getSubmittedForms().indexOf( form );
            if (idx != -1)
            {

               CaseSubmittedFormView submittedFormView = obf.newObjectBuilder( CaseSubmittedFormView.class ).
                     use( client, idx ).newInstance();
               setRightComponent( submittedFormView );
            } else
            {
               setRightComponent( new JPanel() );
            }
         }
      } );
   }
}