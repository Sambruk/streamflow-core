/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import org.jdesktop.swingx.util.WindowUtils;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.ErrorResources;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class PossibleFormsView extends JPanel
      implements ActionListener, Refreshable, TransactionListener
{
   @Structure
   Module module;

   private
   @Service
   StreamflowApplication main;

   private PossibleFormsModel modelForms;
   private Wizard wizard;

   public PossibleFormsView(@Uses PossibleFormsModel possibleFormsModel)
   {
      this.modelForms = possibleFormsModel;
      setLayout( new GridLayout( 0, 1 ) );
      setBorder( BorderFactory.createEmptyBorder( 2, 0, 2, 2 ) );
      setFocusable( false );

      new RefreshWhenShowing(this, this);
   }

   public void refresh()
   {
      modelForms.refresh();

      removeAll();

      EventList<LinkValue> formList = modelForms.getList();

      for (LinkValue itemValue : formList)
      {
         PossibleFormView formView = module.objectBuilderFactory().newObjectBuilder(PossibleFormView.class).use(itemValue).newInstance();
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

         final FormDraftModel formDraftModel = modelForms.getFormDraftModel(form.form().id().get());
         FormDraftDTO formDraftDTO = (FormDraftDTO) ((FormDraftModel) formDraftModel).getFormDraftDTO().buildWith().prototype();

         final WizardPage[] wizardPages = new WizardPage[ formDraftDTO.pages().get().size() ];
         for (int i = 0; i < formDraftDTO.pages().get().size(); i++)
         {
            PageSubmissionDTO page = formDraftDTO.pages().get().get( i );
            if ( page.fields().get() != null && page.fields().get().size() >0 )
            {
               wizardPages[i] = module.objectBuilderFactory().newObjectBuilder(FormSubmissionWizardPageView.class).
                     use( formDraftModel, page ).newInstance();
            }
         }
         try
         {
            wizard = WizardPage.createWizard( formDraftDTO.description().get(), wizardPages, new WizardPage.WizardResultProducer()
            {
               public Object finish( Map map ) throws WizardException
               {
                  // Force focus move before submit
                  Component focusOwner = WindowUtils.findWindow( wizardPages[ wizardPages.length - 1 ]  ).getFocusOwner();
                  if (focusOwner != null)
                  {
                     focusOwner.transferFocus();

                     new CommandTask()
                     {
                        @Override
                        protected void command() throws Exception
                        {
                           formDraftModel.submit();
                        }
                     }.execute();
                  }
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
                        formDraftModel.delete();
                     }
                  }.execute();
                  return true;
               }
            } );
         } catch ( NullPointerException npe )
         {
            throw new IllegalArgumentException( ErrorResources.form_page_without_fields.name() );
         }
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
