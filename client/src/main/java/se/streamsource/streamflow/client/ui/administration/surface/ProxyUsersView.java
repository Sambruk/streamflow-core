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

package se.streamsource.streamflow.client.ui.administration.surface;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CheckBoxProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.CreateProxyUserDialog;
import se.streamsource.streamflow.client.ui.OptionsAction;
import se.streamsource.streamflow.client.ui.ResetPasswordDialog;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

public class ProxyUsersView
      extends JPanel
{
   private ProxyUsersModel model;

   @Uses
   Iterable<CreateProxyUserDialog> userDialogs;

   @Uses
   Iterable<ResetPasswordDialog> resetPwdDialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Service
   DialogService dialogs;

   JXTable proxyUsersTable;

   public ProxyUsersView( @Service ApplicationContext context, @Uses ProxyUsersModel model )
   {
      ApplicationActionMap am = context.getActionMap( this );
      setActionMap( am );

      this.model = model;
      proxyUsersTable = new JXTable( model );
      proxyUsersTable.getColumn( 0 ).setCellRenderer( new DefaultTableRenderer( new CheckBoxProvider() ) );
      proxyUsersTable.getColumn( 0 ).setMaxWidth( 30 );
      proxyUsersTable.getColumn( 0 ).setResizable( false );
      proxyUsersTable.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "resetPassword" ) ) );

      JScrollPane scroll = new JScrollPane();
      scroll.setViewportView( proxyUsersTable );

      super.setLayout(new BorderLayout());
      super.add( scroll, BorderLayout.CENTER );

      JPopupMenu options = new JPopupMenu();
      options.add( am.get( "resetPassword" ) );
      options.add( am.get( "remove" ) );

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( am.get( "add" ) ) );
      toolbar.add( new JButton( new OptionsAction( options ) ) );
      super.add( toolbar, BorderLayout.SOUTH );

      proxyUsersTable.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( am.get( "resetPassword" ), am.get( "remove") ) );
   }


   @org.jdesktop.application.Action
   public void add()
   {
      CreateProxyUserDialog dialog = userDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.create_user_title ) );

      if ( dialog.userCommand() != null )
      {
         model.createProxyUser( dialog.userCommand() );
      }
   }

   @org.jdesktop.application.Action
   public void resetPassword()
   {
      ResetPasswordDialog dialog = resetPwdDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.reset_password_title ) + ": " + model.getValueAt( proxyUsersTable.getSelectedRow(), 1 ) );

      if (dialog.password() != null)
      {
         model.resetPassword( proxyUsersTable.getSelectedRow(), dialog.password() );
      }
   }

      @Action
   public void remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.remove_proxyuser_title ) );

      if (dialog.isConfirmed())
      {
         model.remove( proxyUsersTable.getSelectedRow() );
      }
   }

}