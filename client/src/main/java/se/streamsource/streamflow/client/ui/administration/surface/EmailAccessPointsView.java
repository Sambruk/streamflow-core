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

import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.StreamflowResources;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.client.util.CommandTask;
import se.streamsource.streamflow.client.util.ListDetailView;
import se.streamsource.streamflow.client.util.RefreshWhenShowing;
import se.streamsource.streamflow.client.util.dialog.ConfirmationDialog;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.dialog.NameDialog;
import se.streamsource.streamflow.client.util.i18n;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;
import se.streamsource.streamflow.util.Strings;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.text;


public class EmailAccessPointsView
        extends ListDetailView
        implements TransactionListener
{
   EmailAccessPointsModel model;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   public EmailAccessPointsView(@Service ApplicationContext context, @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder(EmailAccessPointsModel.class).use(client).newInstance();

      ActionMap am = context.getActionMap(this);
      setActionMap(am);

      initMaster(new EventListModel<LinkValue>(model.getList()), am.get("add"), new javax.swing.Action[]{am.get("remove")}, new DetailFactory()
      {
         public Component createDetail(LinkValue detailLink)
         {
            EmailAccessPointView view = obf.newObjectBuilder(EmailAccessPointView.class).use(model.createEmailAccessPointModel(detailLink)).newInstance();
            return view;
         }
      });

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add()
   {
      final NameDialog dialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog(this, dialog, text(AdministrationResources.add_email_accesspoint));

      if (!Strings.empty(dialog.name()))
      {
         return new CommandTask()
         {
            @Override
            public void command()
                    throws Exception
            {
               model.create(dialog.name());
            }
         };
      } else
         return null;
   }

   @Action
   public Task remove()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      ConfirmationDialog dialog = confirmationDialog.iterator().next();
      dialog.setRemovalMessage(selected.text().get());
      dialogs.showOkCancelHelpDialog(this, dialog, i18n.text(StreamflowResources.confirmation));
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
                    throws Exception
            {
               model.remove(selected);
            }
         };
      } else
         return null;
   }

   public void notifyTransactions(Iterable<TransactionDomainEvents> transactions)
   {
      model.notifyTransactions(transactions);
      super.notifyTransactions(transactions);
   }
}