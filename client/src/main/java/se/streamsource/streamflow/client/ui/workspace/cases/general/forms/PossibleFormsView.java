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

package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import ca.odell.glazedlists.*;
import org.jdesktop.swingx.util.*;
import org.netbeans.api.wizard.*;
import org.netbeans.spi.wizard.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

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

      new RefreshWhenShowing(this, this);
   }

   public void refresh()
   {
      modelForms.refresh();

      removeAll();

      EventList<LinkValue> formList = modelForms.getList();

      for (LinkValue itemValue : formList)
      {
         PossibleFormView formView = new PossibleFormView( itemValue );
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

         final WizardPage[] wizardPages = new WizardPage[ formDraftValue.pages().get().size() ];
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
               // Force focus move before submit
               Component focusOwner = WindowUtils.findWindow( wizardPages[ wizardPages.length - 1 ]  ).getFocusOwner();
               focusOwner.transferFocus();

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
