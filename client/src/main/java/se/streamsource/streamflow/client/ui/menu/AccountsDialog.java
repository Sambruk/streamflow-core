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

package se.streamsource.streamflow.client.ui.menu;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.text;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;

import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.StreamFlowResources;
import se.streamsource.streamflow.client.domain.individual.IndividualRepository;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ConfirmationDialog;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.administration.AccountResources;
import se.streamsource.streamflow.client.ui.administration.AccountView;
import se.streamsource.streamflow.client.ui.task.TaskContactModel;
import se.streamsource.streamflow.domain.contact.ContactAddressValue;
import se.streamsource.streamflow.domain.contact.ContactEmailValue;
import se.streamsource.streamflow.domain.contact.ContactPhoneValue;
import se.streamsource.streamflow.domain.contact.ContactValue;
import ca.odell.glazedlists.swing.EventListModel;

/**
 * JAVADOC
 */
public class AccountsDialog
      extends JPanel
{
   AccountsModel model;

   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

   @Service
   IndividualRepository individualRepository;

   @Service
   DialogService dialogs;

   public JList accountList;

   @Uses
   Iterable<CreateAccountDialog> createAccountDialog;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   AccountView accountView;

   public AccountsDialog( @Service ApplicationContext context,
                          @Uses final AccountsModel model )
   {
      super( new BorderLayout() );
//      super.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

      setPreferredSize( new Dimension( 700, 500 ) );

      this.model = model;

      setActionMap( context.getActionMap( this ) );

      accountList = new JList( new EventListModel(model.getAccounts()) );
      accountList.setCellRenderer( new ListItemListCellRenderer() );

      JPanel listPanel = new JPanel(new BorderLayout());
      JScrollPane scroll = new JScrollPane( accountList );
      scroll.setMinimumSize( new Dimension( 200, 300 ) );
      scroll.setPreferredSize( new Dimension( 200, 300 ) );
      listPanel.add(scroll, BorderLayout.CENTER);

      JPanel toolbar = new JPanel();
      toolbar.add( new JButton( getActionMap().get( "add" ) ) );
      toolbar.add( new JButton( getActionMap().get( "remove" ) ) );
      listPanel.add( toolbar, BorderLayout.SOUTH );

      add( listPanel, BorderLayout.WEST );

      accountList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( getActionMap().get( "remove" ) ) );

      accountList.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               // Account
               if (accountView != null)
                  remove( accountView );

               if (accountList.getSelectedIndex() != -1)
               {
                  AccountModel account = model.accountModel( accountList.getSelectedIndex() );
                  accountView = obf.newObjectBuilder( AccountView.class ).use( account ).newInstance();
                  add( accountView, BorderLayout.CENTER );
               }
               revalidate();
            }
         }
      } );
   }

   @Action
   public void execute()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void add() throws ResourceException, UnitOfWorkCompletionException
   {
      CreateAccountDialog dialog = createAccountDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AccountResources.create_account_title ) );

      if (dialog.settings() != null)
      {
         model.newAccount( dialog.settings() );
      }
   }

   @Action
   public void remove() throws UnitOfWorkCompletionException
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamFlowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         System.out.println( "DeleteAccount invoked" );
         model.removeAccount( accountList.getSelectedIndex() );
      }
   }

}