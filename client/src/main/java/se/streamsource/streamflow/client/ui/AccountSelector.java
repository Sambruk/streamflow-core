/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventListModel;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.infrastructure.ui.ListItemListCellRenderer;
import se.streamsource.streamflow.client.ui.administration.AccountModel;
import se.streamsource.streamflow.client.ui.menu.AccountsModel;

import javax.swing.JList;

/**
 * Selection of active account
 */
public class AccountSelector
      extends JList
      implements ListEventListener
{
   private AccountsModel dataModel;

   public AccountSelector( @Uses final AccountsModel dataModel )
   {
      super( new EventListModel(dataModel.getAccounts()) );
      this.dataModel = dataModel;
      setCellRenderer( new ListItemListCellRenderer() );

      dataModel.getAccounts().addListEventListener( this );
   }

   public AccountModel getSelectedAccount()
   {
      return getSelectedIndex() == -1 ? null : dataModel.accountModel( getSelectedIndex() );
   }

   public void listChanged( ListEvent listEvent )
   {
      if (isSelectionEmpty() && dataModel.getAccounts().size() == 1)
      {
         setSelectedIndex( 0 );
      }
   }
}
