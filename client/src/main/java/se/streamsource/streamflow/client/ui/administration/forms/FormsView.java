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

package se.streamsource.streamflow.client.ui.administration.forms;

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
import se.streamsource.streamflow.infrastructure.event.domain.source.*;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.*;
import se.streamsource.streamflow.util.*;

import javax.swing.*;
import java.awt.*;

import static org.qi4j.api.util.Iterables.*;
import static se.streamsource.streamflow.client.util.i18n.*;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.*;

/**
 * JAVADOC
 */
public class FormsView
      extends ListDetailView
      implements TransactionListener
{
   private FormsModel model;

   @Uses
   Iterable<ConfirmationDialog> confirmationDialog;

   @Service
   DialogService dialogs;

   @Uses
   Iterable<NameDialog> nameDialogs;

   @Uses
   ObjectBuilder<SelectLinkDialog> possibleMoveToDialogs;

   public FormsView( @Service ApplicationContext context, @Uses final CommandQueryClient client, @Structure final ObjectBuilderFactory obf)
   {
      this.model = obf.newObjectBuilder( FormsModel.class ).use( client ).newInstance();

      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      initMaster( new EventListModel<LinkValue>( model.getList()), am.get("add"), new javax.swing.Action[]{am.get("move"), am.get("showUsages"), am.get( "remove" )}, new DetailFactory()
      {
         public Component createDetail( LinkValue detailLink )
         {
            CommandQueryClient formClient = client.getClient( detailLink );

            return obf.newObjectBuilder( FormView.class ).use( formClient).newInstance();
         }
      });

      new RefreshWhenShowing(this, model);
   }

   @Action
   public Task add()
   {
      NameDialog formDialog = nameDialogs.iterator().next();

      dialogs.showOkCancelHelpDialog( this, formDialog, i18n.text( AdministrationResources.create_new_form ) );

      final String name = formDialog.name();
      if (!Strings.empty( name ) )
      {
         return new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.create( name );
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
         if (selected != null)
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
         }
      }

      return null;
   }

   @Action
   public Task move()
   {
      final LinkValue selected = (LinkValue) list.getSelectedValue();
      final SelectLinkDialog dialog = possibleMoveToDialogs.use(model.getPossibleMoveTo(selected)).newInstance();
      dialog.setPreferredSize( new Dimension(200,300) );

      dialogs.showOkCancelHelpDialog( this, dialog, text( AdministrationResources.choose_move_to ) );

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

      DomainEvent event = first( filter( withNames("createdForm"), events(transactions ) ));
      if (event != null)
      {
         String id = EventParameters.getParameter( event, 1 );
         for (LinkValue link : model.getUnsortedList())
         {
            if (link.href().get().endsWith( id+"/" ))
            {
               list.setSelectedValue( link, true );
               break;
            }
         }
      }

      super.notifyTransactions( transactions );
   }
}