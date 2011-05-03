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

package se.streamsource.streamflow.client.ui.administration.casetypes;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.object.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.ui.administration.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.client.util.dialog.*;
import se.streamsource.streamflow.infrastructure.event.domain.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static se.streamsource.streamflow.client.util.i18n.*;

/**
 * JAVADOC
 */
public class CaseTypesView
      extends ListDetailView
{
   CaseTypesModel model;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Uses
   ObjectBuilder<SelectLinkDialog> possibleMoveToDialogs;

   public CaseTypesView( @Service ApplicationContext context,
                         @Uses final CommandQueryClient client,
                         @Structure final ObjectBuilderFactory obf )
   {
      this.model = obf.newObjectBuilder( CaseTypesModel.class ).use( client ).newInstance();

      final ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("add"), new javax.swing.Action[]{am.get("move"), am.get( "rename" ), am.get("showUsages"), am.get("knowledgeBase"), am.get( "remove" )}, new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            final CommandQueryClient caseTypeClient = client.getClient( detailLink );

            new ResourceActionEnabler(am.get("knowledgeBase"))
            {
               @Override
               protected CommandQueryClient getClient()
               {
                  return caseTypeClient;
               }
            }.refresh();

            TabbedResourceView view = obf.newObjectBuilder( TabbedResourceView.class ).use( caseTypeClient).newInstance();
            return view;
         }
      });

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add()
   {
      final NameDialog dialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.add_casetype_title ) );

      if (!Strings.empty( dialog.name() ))
      {
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.create( dialog.name() );
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
      dialog.setRemovalMessage( selected.text().get() );

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

   @Action
   public Task rename()
   {
      final NameDialog dialog = nameDialogs.iterator().next();
      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.rename_casetype_title ) );

      if (!Strings.empty( dialog.name() ))
      {
         final LinkValue item = (LinkValue) list.getSelectedValue();
         return new CommandTask()
         {
            @Override
            public void command()
                  throws Exception
            {
               model.changeDescription( item, dialog.name() );
            }
         };
      } else
         return null;
   }

   @Action
   public Task move()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();
      final SelectLinkDialog dialog = possibleMoveToDialogs.use(model.getPossibleMoveTo(selected)).newInstance();
      dialog.setPreferredSize( new Dimension(200,300) );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_move_casetype_to ) );

      if (dialog.getSelectedLink() != null)
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.moveForm(selected, dialog.getSelectedLink());
            }
         };
      } else
         return null;
   }

   @Action
   public void knowledgeBase() throws URISyntaxException, IOException
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();

      LinkValue url = model.getKnowledgeBaseLink(selected);

      Desktop.getDesktop().browse(new URI(url.href().get()));
   }

   @Action
   public void showUsages()
   {
      LinkValue item = (LinkValue) list.getSelectedValue();
      EventList<LinkValue> usageList = model.usages( item );

      JList list = new JList();
      list.setCellRenderer( new LinkListCellRenderer() );
      list.setModel( new EventListModel<LinkValue>( usageList ) );

      dialogs.showOkDialog( this, list );

      usageList.dispose();
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      model.notifyTransactions( transactions );

      super.notifyTransactions( transactions );
   }
}