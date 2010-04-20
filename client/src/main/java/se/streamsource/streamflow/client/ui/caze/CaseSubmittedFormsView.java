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

package se.streamsource.streamflow.client.ui.caze;

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.resource.caze.SubmittedFormListDTO;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;

/**
 * JAVADOC
 */
public class CaseSubmittedFormsView
      extends JPanel
{
   private CaseSubmittedFormsModel model;
   private JXList submittedForms;

   @Service
   DialogService dialogs;

   @Structure
   ObjectBuilderFactory obf;

   private
   @Service
   StreamFlowApplication main;


   public EventListModel eventListModel;

   public CaseSubmittedFormsView( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      ActionMap am = context.getActionMap( this );
      setActionMap( am );
      setMinimumSize( new Dimension( 150, 0 ) );

      submittedForms = new JXList();
      submittedForms.setPreferredSize( new Dimension( 150, 1000 ) );
      submittedForms.setCellRenderer( new DefaultListCellRenderer()
      {
         @Override
         public Component getListCellRendererComponent( JList jList, Object o, int i, boolean b, boolean b1 )
         {
            SubmittedFormListDTO listDTO = (SubmittedFormListDTO) o;
            String dateString = DateFormat.getDateInstance( DateFormat.MEDIUM ).format( listDTO.submissionDate().get() );
            String listItem = dateString + ":" + listDTO.form().get() + " (" + listDTO.submitter().get() + ")";
            JLabel component =  (JLabel) super.getListCellRendererComponent( jList, listDTO.form().get(), i, b, b1 );
            component.setToolTipText( listItem );
            return component;
         }
      } );
      JScrollPane submittedFormsScollPane = new JScrollPane();
      submittedFormsScollPane.setViewportView( submittedForms );

      add( submittedFormsScollPane, BorderLayout.CENTER );
   }

   public void setModel( CaseSubmittedFormsModel model )
   {
      this.model = model;
      if (eventListModel != null)
         eventListModel.dispose();
      eventListModel = new EventListModel( model.getSubmittedForms());
      submittedForms.setModel( eventListModel );
   }

   public JList getSubmittedFormsList()
   {
      return submittedForms;
   }

   @Override
   public void setVisible( boolean b )
   {
      super.setVisible( b );
      if (b)
      {
         if (model != null)
         {
            model.refresh();
         }
      }
   }
}