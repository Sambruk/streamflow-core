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

package se.streamsource.streamflow.client.ui.workspace.cases;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.PageSubmissionDTO;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormDraftModel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageView;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class CaseActionsView extends JPanel
      implements TransactionListener, Observer
{

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   @Service
   StreamflowApplication main;

   private CaseModel model;

   private JPanel actionsPanel = new JPanel();

   private enum CaseActionButtonTemplate
   {
      open,
      sendto,
      assign,
      unassign,
      onhold,
      resume,
      createsubcase,
      close,
      resolve,
      formonclose,
      reopen,
      delete,
      exportpdf;


   }

   public CaseActionsView( @Service ApplicationContext context, @Uses CaseModel model )
   {
      this.model = model;

      model.addObserver( this );

      setLayout( new BorderLayout() );
      setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
      actionsPanel.setLayout( new GridLayout( 0, 1, 5, 5 ) );
      add( actionsPanel, BorderLayout.NORTH );
      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            CaseActionsView.class, this ) );
   }

   // Case actions
   @Action
   public Task createsubcase()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.createSubCase();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task open()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.open();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task assign()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.assignToMe();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task close()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();

      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.close();
         }
      };
   }


   @Action(block = Task.BlockingScope.COMPONENT)
   public Task resolve()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();

      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder( SelectLinkDialog.class )
            .use( model.getPossibleResolutions() ).newInstance();
      dialogs.showOkCancelHelpDialog(
            WindowUtils.findWindow( this ),
            dialog,
            i18n.text( AdministrationResources.resolve ) );

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.resolve( dialog.getSelectedLink() );
            }
         };
      } else
         return null;

   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task formonclose()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      if (focusOwner != null)
         focusOwner.transferFocus();
      //TODO find, create and submit form wizard for form on close form before calling formonclose
      
      if( formOnCloseWizard() )
      {
         return new CommandTask()
         {
            @Override
            protected void command()
                  throws Exception
            {
               model.formOnClose();
            }
         };
      } else
         return null;
   }


   @Action(block = Task.BlockingScope.COMPONENT)
   public Task delete()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setRemovalMessage( i18n.text( WorkspaceResources.caze ) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.delete();
            }
         };
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task sendto()
   {
      final SelectLinkDialog dialog = module.objectBuilderFactory().newObjectBuilder(SelectLinkDialog.class).use(
            model.getPossibleSendTo() ).newInstance();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( WorkspaceResources.choose_owner_title ) );

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.sendTo( dialog.getSelectedLink() );
            }
         };
      } else
         return null;
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task onhold( ActionEvent event )
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.onHold();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task reopen()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.reopen();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task resume()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.resume();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task unassign()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.unassign();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task exportpdf()
   {
      //TODO create a dialog to give the user the oportunity to choose the contents of CaseOutputConfigDTO
      final ValueBuilder<CaseOutputConfigDTO> config = module.valueBuilderFactory().newValueBuilder( CaseOutputConfigDTO.class );
      config.prototype().history().set( true );
      config.prototype().contacts().set( true );
      config.prototype().conversations().set( true );
      config.prototype().submittedForms().set( true );
      config.prototype().attachments().set( true );

      return new PrintCaseTask( config.newInstance() );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withUsecases( "sendto", "open", "assign", "close", "delete", "onhold", "reopen", "resume", "unassign", "resolved", "formonclose"), transactions ))
      {
         model.refresh();
      }
   }

   public void update( Observable o, Object arg )
   {
      // Update list of action buttons
      actionsPanel.removeAll();

      ActionMap am = getActionMap();

      for (CaseActionButtonTemplate buttonOrder : CaseActionButtonTemplate.values())
      {
         for (LinkValue commandLink : Iterables.flatten( model.getCommands(), model.getQueries() ))
         {
            if (buttonOrder.toString().equals( commandLink.rel().get() ))
            {
               javax.swing.Action action1 = am.get( commandLink.rel().get() );
               if (action1 != null)
               {
                  JButton button = new JButton( action1 );
                  button.registerKeyboardAction( action1, (KeyStroke) action1
                        .getValue( javax.swing.Action.ACCELERATOR_KEY ),
                        JComponent.WHEN_IN_FOCUSED_WINDOW );
                  button.setHorizontalAlignment( SwingConstants.LEFT );
                  actionsPanel.add( button );
//				NotificationGlassPane.registerButton(button);
               }
            }
         }
      }

      revalidate();
      repaint();
   }

   private boolean formOnCloseWizard()
   {
      CommandQueryClient formOnCloseClient = model.getClient().getClient( "submitformonclose/" );

      formOnCloseClient.postCommand( "create" );
      LinkValue formDraftLink = formOnCloseClient.query( "formdraft", LinkValue.class );

      // get the form submission value;
      final CommandQueryClient formDraftClient = model.getClient().getClient( formDraftLink );

      final FormDraftModel formDraftModel = module.objectBuilderFactory().newObjectBuilder(FormDraftModel.class).use(formDraftClient).newInstance();

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

     Map initialProperties = new HashMap( );

     Wizard wizard = WizardPage.createWizard( formDraftDTO.description().get(), wizardPages, new WizardPage.WizardResultProducer()
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

               map.put( "success", true );
            }
            return map;
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
            map.put( "success", false );
            return true;
         }
      } );
      Point onScreen = main.getMainFrame().getLocationOnScreen();
      Map result = (Map)WizardDisplayer.showWizard( wizard, new Rectangle( onScreen, new Dimension( 800, 600 ) ), null, initialProperties );

      return result.get( "success" ) == null ? false : (Boolean)result.get( "success" );
   }

   private class PrintCaseTask extends Task<File, Void>
   {
      private CaseOutputConfigDTO config;

      public PrintCaseTask( CaseOutputConfigDTO config )
      {
         super( Application.getInstance() );
         this.config = config;

         setUserCanCancel( false );
      }

      @Override
      protected File doInBackground() throws Exception
      {
         setMessage( getResourceMap().getString( "description" ) );

         File file = model.export(config);

         return file;
      }

      @Override
      protected void succeeded( File file )
      {
         // Open file
         Desktop desktop = Desktop.getDesktop();
         try
         {
            desktop.edit( file );
         } catch (IOException e)
         {
            try
            {
               desktop.open( file );
            } catch (IOException e1)
            {
               dialogs.showMessageDialog( CaseActionsView.this, i18n.text( WorkspaceResources.could_not_print ), "" );
            }
         }
      }
   }
}
