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

package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.table.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.*;


public class EmailAccessPointsView
      extends JPanel
   implements TransactionListener
{
   EmailAccessPointsModel model;

   @Uses
   ObjectBuilder<SelectLinkDialog> caseTypesDialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   JTable table;

   public EmailAccessPointsView(@Service ApplicationContext context, @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf)
   {
      super(new BorderLayout());

      this.model = obf.newObjectBuilder( EmailAccessPointsModel.class ).use( client ).newInstance();

      table = new JTable(new EventTableModel<RowValue>(model.getRows(), new TableFormat<RowValue>()
      {
         public int getColumnCount()
         {
            return 2;
         }

         public String getColumnName(int i)
         {
            return new String[]{i18n.text(AdministrationResources.email), i18n.text(AdministrationResources.accesspoint)}[i];
         }

         public Object getColumnValue(RowValue rowValue, int i)
         {
            return rowValue.c().get().get(i).f();
         }
      }));

      table.setGridColor(Color.lightGray);
      table.setShowHorizontalLines(true);

      add(new JScrollPane(table), BorderLayout.CENTER);

      ActionMap am = context.getActionMap( this );
      setActionMap(am);

      JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
      toolbar.add(new JButton(am.get("add")));
      toolbar.add(new JButton(am.get("remove")));
      add(toolbar, BorderLayout.SOUTH);

      table.getSelectionModel().addListSelectionListener(new SelectionActionEnabler(am.get("remove")));

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add()
   {
      final SelectLinkDialog dialog = caseTypesDialogs.use( model.possibleAccessPoints() ).newInstance();
      dialog.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      dialogs.showOkCancelHelpDialog(this, dialog, text(AdministrationResources.choose_accesspoint_title));

      if (dialog.getSelectedLink() != null)
      {
         final NameDialog emailDialog = nameDialogs.iterator().next();

         dialogs.showOkCancelHelpDialog( this, emailDialog, text( AdministrationResources.add_accesspoint_title ) );

         if (!Strings.empty( emailDialog.name() ))
         {
            return new CommandTask()
            {
               @Override
               public void command()
                     throws Exception
               {
                  model.create(emailDialog.name(), dialog.getSelectedLink());
               }
            };
         }
      }

      return null;
   }

   @Action
   public Task remove()
   {
      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage( table.getValueAt(table.getSelectedRow(), 0 ).toString());
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.remove(table.getSelectedRow());
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions(transactions);
   }
}
