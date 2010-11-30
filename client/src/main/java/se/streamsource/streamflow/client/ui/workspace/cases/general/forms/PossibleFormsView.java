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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import ca.odell.glazedlists.EventList;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.LinkValueListModel;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.domain.form.FormDraftValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withNames;

public class PossibleFormsView extends JPanel
      implements ActionListener, Refreshable, TransactionListener
{
   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private
   @Service
   StreamflowApplication main;

   private LinkValueListModel modelForms;
   public Wizard wizard;
   private final CommandQueryClient client;

   public PossibleFormsView(@Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf)
   {
      this.client = client;
      setLayout( new GridLayout( 0, 1 ) );
      setBorder( BorderFactory.createEmptyBorder( 2, 0, 2, 2 ) );
      setFocusable( false );

      modelForms = obf.newObjectBuilder( PossibleFormsModel.class ).use( client ).newInstance();

      new RefreshWhenVisible(this, this);
   }

   public void refresh()
   {
      modelForms.refresh();

      removeAll();

      EventList<LinkValue> formList = modelForms.getList();

      for (LinkValue itemValue : formList)
      {
         PossibleFormView formView = new PossibleFormView( itemValue );

         formView.setPreferredSize( new Dimension( 145, 25 ) );
         formView.setMinimumSize( new Dimension( 145, 25 ) );
         formView.setMaximumSize( new Dimension( 145, 25 ) );
         formView.addActionListener( this );
         add( formView, Component.LEFT_ALIGNMENT );
      }

      this.revalidate();
      this.repaint();
   }

   @Override
   public void setEnabled( boolean enabled )
   {
      for (Component component : getComponents())
      {
         component.setEnabled( enabled );
      }
      super.setEnabled( enabled );
   }

   public void actionPerformed( ActionEvent e )
   {
      // Open up the wizard with the correct form for submission.

      if (e.getSource() instanceof PossibleFormView)
      {
         final PossibleFormView form = (PossibleFormView) e.getSource();

         CommandQueryClient possibleFormClient = client.getSubClient( form.form().id().get() );

         possibleFormClient.postCommand( "create" );
         LinkValue formDraftLink = possibleFormClient.query( "formdraft", LinkValue.class );

         // get the form submission value;
         final CommandQueryClient formDraftClient = client.getClient( formDraftLink );
         FormDraftValue formDraftValue = (FormDraftValue) formDraftClient.query( "index", FormDraftValue.class )
               .buildWith().prototype();

         WizardPage[] wizardPages = new WizardPage[ formDraftValue.pages().get().size() ];
         for (int i = 0; i < formDraftValue.pages().get().size(); i++)
         {
            PageSubmissionValue page = formDraftValue.pages().get().get( i );
            if ( page.fields().get() != null && page.fields().get().size() >0 )
            {
               wizardPages[i] = obf.newObjectBuilder( FormSubmissionWizardPageView.class ).
                     use( formDraftClient, page ).newInstance();
            }
         }
         wizard = WizardPage.createWizard( formDraftValue.description().get(), wizardPages, new WizardPage.WizardResultProducer()
         {
            public Object finish( Map map ) throws WizardException
            {
               new CommandTask()
               {
                  @Override
                  protected void command() throws Exception
                  {
                     formDraftClient.putCommand( "submit" );
                  }
               }.execute();
               return null;
            }

            public boolean cancel( Map map )
            {
               new CommandTask()
               {
                  @Override
                  public void command()
                     throws Exception
                  {
                     formDraftClient.delete();
                  }
               }.execute();
               return true;
            }
         } );
         Point onScreen = main.getMainFrame().getLocationOnScreen();
         WizardDisplayer.showWizard( wizard, new Rectangle( onScreen, new Dimension( 800, 600 ) ) );

      }
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withNames("changedOwner", "changedCaseType"), transactions ))
      {
         refresh();
      }
   }
}
