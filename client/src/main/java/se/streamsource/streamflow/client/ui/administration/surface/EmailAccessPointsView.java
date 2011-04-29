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

import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import java.awt.*;

import static se.streamsource.streamflow.client.util.i18n.*;


public class EmailAccessPointsView
      extends ListDetailView
{
   EmailAccessPointsModel model;

   @Uses
   ObjectBuilder<InputDialog> inputDialogs;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   public EmailAccessPointsView( @Service ApplicationContext context, @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf )
   {
      this.model = obf.newObjectBuilder( EmailAccessPointsModel.class ).use( client ).newInstance();

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("add"), new javax.swing.Action[]{am.get( "remove" )}, new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            CommandQueryClient caseTypeClient = client.getClient( detailLink );
            return obf.newObjectBuilder( EmailAccessPointView.class ).use( caseTypeClient).newInstance();
         }
      });

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add()
   {
      final InputDialog dialog = inputDialogs.use(i18n.text(AdministrationResources.email)).newInstance();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_email_accesspoint ) );

      if (!Strings.empty( dialog.value() ))
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.createEmailAccessPoint(dialog.value());
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
      dialogs.showOkCancelHelpDialog( this, dialog, i18n.text( StreamflowResources.confirmation ) );
      if (dialog.isConfirmed())
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.remove( selected );
            }
         };
      } else
         return null;
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );

      super.notifyTransactions(transactions);
   }
}
