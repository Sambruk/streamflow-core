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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;


/**
 * JAVADOC
 */
public class SubmittedFormsAdminView
      extends JPanel
{
   @Structure
   ObjectBuilderFactory obf;

   private CaseSubmittedFormsView formsView;
   public RefreshWhenVisible refresher;
   private CaseSubmittedFormsModel model;

   public SubmittedFormsAdminView( @Uses final CaseSubmittedFormsView submittedFormsView,
                                       @Uses final CaseSubmittedFormView submittedFormView )
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
               if (idx != -1 && idx < submittedForms.getModel().getSize())
               {
                  CaseSubmittedFormModel submittedFormModel = model.getSubmittedFormModel( idx );
                  submittedFormView.setModel( submittedFormModel );
               } else
               {
                  submittedFormView.setModel( null );
               }
            }
         }
      } );
   }

   public void setModel( CaseSubmittedFormsModel model )
   {
      this.model = model;
      formsView.setModel( model );
   }

   @Override
   public void setVisible( boolean b )
   {
      super.setVisible( b );
      formsView.setVisible( b );
   }
}