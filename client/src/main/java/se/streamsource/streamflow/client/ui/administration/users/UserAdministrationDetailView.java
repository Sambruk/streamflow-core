/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.users;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowApplication;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.StreamflowButton;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

public class UserAdministrationDetailView
      extends JPanel
      implements Observer, TransactionListener
{

   @Service
   DialogService dialogs;

   @Structure
   Module module;

   @Service
   StreamflowApplication main;

   private UserAdministrationDetailModel model;

   private JPanel actionsPanel = new JPanel();
   private ApplicationContext context;

   private enum UserAdministrationButtonTemplate
   {
      setdisabled,
      setenabled,
      join,
      leave,
      resetpassword
   }

   public UserAdministrationDetailView( @Service ApplicationContext context, @Uses UserAdministrationDetailModel model )
   {
      this.model = model;
      this.context = context;

      model.addObserver( this );

      setLayout( new BorderLayout() );
      setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
      actionsPanel.setLayout( new GridLayout( 0, 1, 5, 5 ) );
      add( actionsPanel, BorderLayout.NORTH );
      setActionMap( context.getActionMap( this ) );
      new RefreshWhenShowing( this, model );
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task setdisabled()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.setdisabled();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task setenabled()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.setenabled();
         }
      };
   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task join()
   {
      return new CommandTask()
      {
         @Override
         public void command()
               throws Exception
         {
            model.join();
         }
      };
   }


   @Action(block = Task.BlockingScope.COMPONENT)
   public Task leave()
   {
      ConfirmationDialog dialog = module.objectBuilderFactory().newObject(ConfirmationDialog.class);
      dialog.setCustomMessage( i18n.text( WorkspaceResources.unrestrict_case ) );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.leave();
            }
         };
      } else
         return null;

   }

   @Action(block = Task.BlockingScope.COMPONENT)
   public Task resetpassword()
   {
      final ResetPasswordDialog dialog = module.objectBuilderFactory().newObject( ResetPasswordDialog.class );
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.reset_password_title ) );

      if (dialog.password() != null)
      {
         return new CommandTask()
         {
            @Override
            protected void command()
                  throws Exception
            {
               model.resetPassword( dialog.password() );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (matches( withUsecases( "join", "leave", "changedisabled" ), transactions ))
      {
         model.refresh();
      }
   }

   public void update( Observable o, Object arg )
   {
      // Update list of action buttons
      actionsPanel.removeAll();

      ActionMap am = getActionMap();

      for (UserAdministrationButtonTemplate buttonOrder : UserAdministrationButtonTemplate.values())
      {
         for (LinkValue commandLink : Iterables.flatten( model.getCommands(), model.getQueries() ))
         {
            if (buttonOrder.toString().equals( commandLink.rel().get() ))
            {
               javax.swing.Action action1 = am.get( commandLink.rel().get() );
               if (action1 != null)
               {
                  StreamflowButton button = new StreamflowButton( action1 );
                  button.registerKeyboardAction( action1, (KeyStroke) action1
                        .getValue( javax.swing.Action.ACCELERATOR_KEY ),
                        JComponent.WHEN_IN_FOCUSED_WINDOW );
                  button.setHorizontalAlignment( SwingConstants.LEFT );
                  actionsPanel.add( button );
                  action1.putValue( "sourceButton", button );
               }
            }
         }
      }

      revalidate();
      repaint();
   }
}
