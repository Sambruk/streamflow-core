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

package se.streamsource.streamflow.client.ui.account;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventListModel;
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
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.util.LinkListCellRenderer;
import se.streamsource.streamflow.client.util.SelectionActionEnabler;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.text;

/**
 * JAVADOC
 */
public class AccountsDialog
      extends JPanel
      implements ListEventListener
{
   AccountsModel model;

   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   ValueBuilderFactory vbf;

   @Structure
   ObjectBuilderFactory obf;

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
      accountList.setCellRenderer( new LinkListCellRenderer() );

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

      final CardLayout cardLayout = new CardLayout();
      final JPanel viewPanel = new JPanel( cardLayout );
      viewPanel.add( new JPanel(), "EMPTY" );

      add( viewPanel, BorderLayout.CENTER );

      accountList.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( getActionMap().get( "remove" ) ) );

      accountList.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               if (accountList.getSelectedIndex() != -1)
               {
                  AccountModel account = model.accountModel( (LinkValue) accountList.getSelectedValue() );
                  accountView = obf.newObjectBuilder( AccountView.class ).use( account ).newInstance();
                  viewPanel.add( accountView, "VIEW" );
                  cardLayout.show( viewPanel, "VIEW" );
               } else
               {
                  cardLayout.show( viewPanel, "EMPTY" );
               }
               viewPanel.revalidate();
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
         listChanged(null);
      }
   }

   @Action
   public void remove() throws UnitOfWorkCompletionException
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( ((LinkValue)accountList.getSelectedValue()).text().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         model.removeAccount( (LinkValue) accountList.getSelectedValue() );
         listChanged(null);
      }
   }

   public void listChanged( ListEvent listEvent )
   {
      int prevSelected = accountList.getSelectedIndex();
      accountList.setModel( new EventListModel<LinkValue>( model.accounts ) );
      accountList.repaint();
      accountList.setSelectedIndex( prevSelected );

   }

   public void setSelectedAccount(LinkValue selectedValue)
   {
      accountList.setSelectedValue(selectedValue, true);
   }
}