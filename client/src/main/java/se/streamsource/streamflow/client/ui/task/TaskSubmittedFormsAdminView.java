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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.resource.CommandQueryClient;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;


/**
 * JAVADOC
 */
public class TaskSubmittedFormsAdminView
      extends JPanel
{
   @Structure
   ObjectBuilderFactory obf;

   @Uses
   CommandQueryClient client;

   private TaskSubmittedFormsView formsView;
   public RefreshWhenVisible refresher;

   public TaskSubmittedFormsAdminView( @Uses final TaskSubmittedFormsView submittedFormsView,
                                       @Uses final TaskSubmittedFormView submittedFormView )
   {
      super( new BorderLayout() );
      add( submittedFormsView, BorderLayout.WEST );
      add( submittedFormView, BorderLayout.CENTER );

      this.formsView = submittedFormsView;
      final JList submittedForms = submittedFormsView.getSubmittedFormsList();
      submittedForms.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               int idx = submittedForms.getSelectedIndex();
               if (idx != -1)
               {
                  TaskSubmittedFormModel submittedFormModel = obf.newObjectBuilder(
                        TaskSubmittedFormModel.class ).use( client.getSubClient( ""+idx ) ).newInstance();
                  submittedFormView.setModel( submittedFormModel );
               } else
               {
                  submittedFormView.setModel( null );
               }
            }
         }
      } );
   }

   public void setModel( TaskSubmittedFormsModel model )
   {
      formsView.setModel( model );
   }

   @Override
   public void setVisible( boolean b )
   {
      super.setVisible( b );
      formsView.setVisible( b );
   }
}