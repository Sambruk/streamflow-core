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

package se.streamsource.streamflow.client.ui.workspace.cases.actions;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.MacOsUIWrapper;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenVisible;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.SelectLinkDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.resource.caze.CaseOutputConfigValue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.withUsecases;

/**
 * JAVADOC
 */
public class CaseActionsView extends JPanel
   implements TransactionListener, ListEventListener<String>
{
   @Uses
   protected ObjectBuilder<SelectLinkDialog> projectSelectionDialog;

   @Uses
   private ObjectBuilder<ConfirmationDialog> confirmationDialog;

   @Service
   DialogService dialogs;

   @Service
   StreamflowApplication controller;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   private CaseActionsModel model;

   private JPanel actionsPanel = new JPanel();

   public CaseActionsView( @Service ApplicationContext context, @Uses CommandQueryClient client, @Structure ObjectBuilderFactory obf )
   {
      model = obf.newObjectBuilder( CaseActionsModel.class ).use(client).newInstance();

      model.getActionList().addListEventListener( this );

      setLayout( new BorderLayout() );
      setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
      actionsPanel.setLayout( new GridLayout( 0, 1, 5, 5 ) );
      add( actionsPanel, BorderLayout.NORTH );
      setActionMap( context.getActionMap( this ) );
      MacOsUIWrapper.convertAccelerators( context.getActionMap(
            CaseActionsView.class, this ) );

      new RefreshWhenVisible(this, model);
   }

   public void refresh()
   {
      actionsPanel.removeAll();

      ActionMap am = getActionMap();

      for (String action : model.getActionList())
      {
         javax.swing.Action action1 = am.get( action );
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

      revalidate();
      repaint();
   }

   // Case actions

   @Action
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

   @Action
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

   @Action
   public Task close()
   {
      // TODO very odd hack - how to solve state binder update issue during use of accelerator keys.
      Component focusOwner = WindowUtils.findWindow( this ).getFocusOwner();
      focusOwner.transferFocus();

      EventList<TitledLinkValue> resolutions = model.getPossibleResolutions();
      if (resolutions.isEmpty())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.close();
            }
         };
      } else
      {
         final SelectLinkDialog dialog = obf.newObjectBuilder( SelectLinkDialog.class )
               .use( resolutions ).newInstance();
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
   }

   @Action
   public Task delete()
   {
      ConfirmationDialog dialog = confirmationDialog.newInstance();
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

   @Action
   public Task sendto()
   {
      final SelectLinkDialog dialog = projectSelectionDialog.use(
            model.getPossibleProjects() ).newInstance();
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

   @Action
   public Task onhold()
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

   @Action
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

   @Action
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

   @Action
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

   @Action
   public Task print()
   {
      //TODO create a dialog to give the user the oportunity to choose the contents of CaseOutputConfigValue
      final ValueBuilder<CaseOutputConfigValue> config = vbf.newValueBuilder( CaseOutputConfigValue.class );
      config.prototype().contacts().set( true );
      config.prototype().conversations().set( true );
      config.prototype().effectiveFields().set( true );
      config.prototype().attachments().set( true );

      return new PrintCaseTask( config.newInstance() );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withUsecases("sendto", "open", "assign", "close", "delete", "onhold", "reopen", "resume", "unassign"), transactions ))
      {
         model.refresh();
      }
   }

   public void listChanged( ListEvent<String> stringListEvent )
   {
      refresh();
   }

    private class PrintCaseTask extends Task<File, Void>
   {
      private CaseOutputConfigValue config;

      public PrintCaseTask( CaseOutputConfigValue config)
      {
         super( Application.getInstance() );
         this.config = config;

         setUserCanCancel( false );
      }

      @Override
      protected File doInBackground() throws Exception
      {
         setMessage( getResourceMap().getString( "description" ) );

         File file = model.print( config );

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
