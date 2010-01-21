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

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXList;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.StreamFlowApplication;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.resource.task.SubmittedFormListDTO;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * JAVADOC
 */
public class TaskSubmittedFormsView
      extends JPanel
{
   private TaskSubmittedFormsModel model;
   private JXList submittedForms;

   @Service
   DialogService dialogs;

   @Structure
   ObjectBuilderFactory obf;

   private
   @Service
   StreamFlowApplication main;


   private SimpleDateFormat formatter = new SimpleDateFormat( i18n.text( WorkspaceResources.date_format ) );
   public EventListModel eventListModel;

   public TaskSubmittedFormsView( @Service ApplicationContext context )
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
            String dateString = formatter.format( listDTO.submissionDate().get() );
            String listItem = dateString + ":" + listDTO.form().get() + " (" + listDTO.submitter().get() + ")";
            JLabel component =  (JLabel) super.getListCellRendererComponent( jList, listDTO.form().get(), i, b, b1 );
            component.setToolTipText( listItem );
            return component;
         }
      } );
      JScrollPane submittedFormsScollPane = new JScrollPane();
      submittedFormsScollPane.setViewportView( submittedForms );

      add( submittedFormsScollPane, BorderLayout.CENTER );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      add( toolbar, BorderLayout.SOUTH );
   }

   @org.jdesktop.application.Action
   public void add() throws IOException, ResourceException
   {
      FormsListModel formsListModel = obf.newObjectBuilder(FormsListModel.class)
            .use( model.getClient() ).newInstance();
      FormSubmitWizardController wizardController = obf.newObjectBuilder(FormSubmitWizardController.class).
            use( formsListModel ).newInstance();
      Wizard wizard = wizardController.createWizard();
      Point onScreen = main.getMainFrame().getLocationOnScreen();
      WizardDisplayer.showWizard(wizard, new Rectangle(onScreen, new Dimension( 800, 600 )));
   }

   public void setModel( TaskSubmittedFormsModel model )
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