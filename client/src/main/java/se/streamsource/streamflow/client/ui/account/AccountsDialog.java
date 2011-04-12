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

import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.jdesktop.swingx.util.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import org.qi4j.api.unitofwork.*;
import org.qi4j.api.value.*;
import org.restlet.resource.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.domain.individual.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.application.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.*;

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
                  AccountModel account = model.accountModel( accountList.getSelectedIndex() );
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
      dialog.setRemovalMessage( ((ListItemValue)accountList.getSelectedValue()).description().get() );
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         model.removeAccount( accountList.getSelectedIndex() );
         listChanged(null);
      }
   }

   public void listChanged( ListEvent listEvent )
   {
      int prevSelected = accountList.getSelectedIndex();
      accountList.setModel( new EventListModel<ListItemValue>( model.accounts ) );
      accountList.repaint();
      accountList.setSelectedIndex( prevSelected );

   }

   public void setSelectedAccount(ListItemValue selectedValue)
   {
      accountList.setSelectedValue(selectedValue, true);
   }
}